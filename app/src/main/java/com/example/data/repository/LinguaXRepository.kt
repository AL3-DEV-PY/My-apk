package com.example.data.repository

import com.example.data.model.*
import com.example.data.supabase.SessionManager
import com.example.data.supabase.SupabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
    object Empty : Resource<Nothing>()
}

data class UserSession(
    val userId: String,
    val email: String,
    val accessToken: String?,
    val profile: Profile
)

private fun JSONObject.optNullableString(key: String): String? {
    if (!has(key) || isNull(key)) return null
    val value = optString(key)
    return if (value == "null" || value.isEmpty()) null else value
}

class LinguaXRepository(
    val sessionManager: SessionManager? = null
) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .build()

    private val _currentSession = MutableStateFlow<UserSession?>(sessionManager?.loadSession())
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    // Default target language (ID: 1L - Arabic)
    private val _selectedLanguage = MutableStateFlow<LanguageItem>(
        LanguageItem(
            id = 1L,
            name = "Arabic",
            nativeName = "العربية",
            code = "ar",
            flagEmoji = "🇸🇦",
            description = "Master Modern Standard Arabic, grammar & everyday conversations.",
            learnersCount = 0,
            sortOrder = 1
        )
    )
    val selectedLanguage: StateFlow<LanguageItem> = _selectedLanguage.asStateFlow()

    // Real persistent completed lesson IDs (No hardcoded mock IDs!)
    private val completedLessonIds = mutableSetOf<Long>().apply {
        sessionManager?.getCompletedLessonIds()?.let { addAll(it) }
    }
    private val bookmarkedVocabIds = mutableSetOf<Long>()

    fun setSelectedLanguage(language: LanguageItem) {
        _selectedLanguage.value = language
    }

    suspend fun login(email: String, password: String): Resource<UserSession> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured) {
            return@withContext Resource.Error("Authentication service is not configured.")
        }

        try {
            val jsonBody = JSONObject().apply {
                put("email", email)
                put("password", password)
            }
            val request = Request.Builder()
                .url("${SupabaseConfig.url}/auth/v1/token?grant_type=password")
                .header("apikey", SupabaseConfig.anonKey)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (response.isSuccessful && responseString.isNotBlank()) {
                val json = JSONObject(responseString)
                val token = json.optString("access_token")
                val userObj = json.getJSONObject("user")
                val userId = userObj.getString("id")
                val userEmail = userObj.optString("email", email)

                // Fetch real profile or initialize a fresh profile with 0 XP / 0 streak
                val profile = fetchProfileFromSupabase(userId, token) ?: run {
                    val cleanUsername = userEmail.substringBefore("@")
                    val newProf = Profile(
                        id = userId,
                        username = cleanUsername,
                        displayName = cleanUsername.capitalizeWords(),
                        xp = 0,
                        coins = 0,
                        streak = 0,
                        dailyGoal = 20
                    )
                    createOrUpdateProfileInSupabase(newProf, token)
                    newProf
                }

                // Sync user's real completed lessons from user_progress table
                fetchAndSyncUserProgress(userId, token)

                val session = UserSession(userId, userEmail, token, profile)
                _currentSession.value = session
                sessionManager?.saveSession(session)
                Resource.Success(session)
            } else {
                val errMsg = if (responseString.isNotBlank()) {
                    try {
                        val errObj = JSONObject(responseString)
                        errObj.optString("error_description", errObj.optString("msg", "Invalid login credentials"))
                    } catch (_: Exception) {
                        "Login failed (${response.code})"
                    }
                } else "Login failed (${response.code})"
                Resource.Error(errMsg)
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage ?: "Unable to connect"}", e)
        }
    }

    suspend fun signup(email: String, password: String, displayName: String): Resource<UserSession> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured) {
            return@withContext Resource.Error("Authentication service is not configured.")
        }

        try {
            val cleanDisplayName = displayName.trim().ifBlank { email.substringBefore("@").capitalizeWords() }
            val cleanUsername = email.substringBefore("@")

            val jsonBody = JSONObject().apply {
                put("email", email)
                put("password", password)
                put("data", JSONObject().apply {
                    put("display_name", cleanDisplayName)
                    put("username", cleanUsername)
                })
            }
            val request = Request.Builder()
                .url("${SupabaseConfig.url}/auth/v1/signup")
                .header("apikey", SupabaseConfig.anonKey)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (response.isSuccessful && responseString.isNotBlank()) {
                val json = JSONObject(responseString)
                val token = json.optNullableString("access_token")
                val userObj = json.optJSONObject("user")
                val userId = userObj?.optString("id") ?: ""

                if (userId.isBlank()) {
                    return@withContext Resource.Error("Signup was received, but no user ID was returned.")
                }

                val userEmail = userObj.optString("email", email)

                // Fresh user profile with 0 XP / 0 Coins / 0 Streak
                val profile = fetchProfileFromSupabase(userId, token) ?: run {
                    val newProf = Profile(
                        id = userId,
                        username = cleanUsername,
                        displayName = cleanDisplayName,
                        xp = 0,
                        coins = 0,
                        streak = 0,
                        dailyGoal = 20
                    )
                    createOrUpdateProfileInSupabase(newProf, token)
                    newProf
                }

                // Clear any leftover local state for new user
                completedLessonIds.clear()

                val session = UserSession(userId, userEmail, token, profile)
                _currentSession.value = session
                sessionManager?.saveSession(session)
                Resource.Success(session)
            } else {
                val errMsg = try {
                    val errObj = JSONObject(responseString)
                    errObj.optString("msg", errObj.optString("error_description", "Signup failed (${response.code})"))
                } catch (_: Exception) {
                    "Signup error (${response.code})"
                }
                Resource.Error(errMsg)
            }
        } catch (e: Exception) {
            Resource.Error("Network error during signup: ${e.localizedMessage}", e)
        }
    }

    fun logout() {
        _currentSession.value = null
        completedLessonIds.clear()
        bookmarkedVocabIds.clear()
        sessionManager?.clearSession()
    }

    private fun parseProfileFromJson(obj: JSONObject, fallbackUserId: String): Profile {
        val reasonsList = mutableListOf<String>()
        val jsonReasons = obj.optJSONArray("learning_reasons")
        if (jsonReasons != null) {
            for (i in 0 until jsonReasons.length()) {
                reasonsList.add(jsonReasons.getString(i))
            }
        }

        return Profile(
            id = obj.optString("id", fallbackUserId),
            username = obj.optNullableString("username"),
            displayName = obj.optString("display_name", "Learner"),
            avatarUrl = obj.optNullableString("avatar_url"),
            xp = obj.optInt("xp", 0),
            coins = obj.optInt("coins", 0),
            streak = obj.optInt("streak", 0),
            dailyGoal = obj.optInt("daily_goal", 15),
            nativeLanguageId = if (obj.has("native_language_id") && !obj.isNull("native_language_id")) obj.optLong("native_language_id") else null,
            learningLanguageId = if (obj.has("learning_language_id") && !obj.isNull("learning_language_id")) obj.optLong("learning_language_id") else null,
            currentLevel = obj.optString("current_level", "A1"),
            targetLevel = obj.optString("target_level", "B1"),
            ageGroup = obj.optNullableString("age_group"),
            gender = obj.optNullableString("gender"),
            learningReasons = reasonsList,
            onboardingCompleted = obj.optBoolean("onboarding_completed", false),
            onboardingStep = obj.optInt("onboarding_step", 1),
            createdAt = obj.optNullableString("created_at"),
            updatedAt = obj.optNullableString("updated_at")
        )
    }

    private fun createOrUpdateProfileInSupabase(profile: Profile, token: String?) {
        try {
            val reasonsArray = JSONArray()
            profile.learningReasons.forEach { reasonsArray.put(it) }

            val jsonBody = JSONObject().apply {
                put("id", profile.id)
                put("display_name", profile.displayName)
                put("username", profile.username)
                put("xp", profile.xp)
                put("coins", profile.coins)
                put("streak", profile.streak)
                put("daily_goal", profile.dailyGoal)
                profile.nativeLanguageId?.let { put("native_language_id", it) }
                profile.learningLanguageId?.let { put("learning_language_id", it) }
                put("current_level", profile.currentLevel ?: "A1")
                put("target_level", profile.targetLevel ?: "B1")
                profile.ageGroup?.let { put("age_group", it) }
                profile.gender?.let { put("gender", it) }
                put("learning_reasons", reasonsArray)
                put("onboarding_completed", profile.onboardingCompleted)
                put("onboarding_step", profile.onboardingStep)
            }
            val reqBuilder = Request.Builder()
                .url("${SupabaseConfig.url}/rest/v1/profiles")
                .header("apikey", SupabaseConfig.anonKey)
                .header("Prefer", "resolution=merge-duplicates")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))

            if (!token.isNullOrBlank()) {
                reqBuilder.header("Authorization", "Bearer $token")
            }
            httpClient.newCall(reqBuilder.build()).execute()
        } catch (_: Exception) {}
    }

    private fun fetchProfileFromSupabase(userId: String, token: String?): Profile? {
        return try {
            val reqBuilder = Request.Builder()
                .url("${SupabaseConfig.url}/rest/v1/profiles?id=eq.$userId&select=*")
                .header("apikey", SupabaseConfig.anonKey)
            if (!token.isNullOrBlank()) {
                reqBuilder.header("Authorization", "Bearer $token")
            }
            val response = httpClient.newCall(reqBuilder.build()).execute()
            val resStr = response.body?.string() ?: ""
            if (response.isSuccessful && resStr.isNotBlank()) {
                val array = JSONArray(resStr)
                if (array.length() > 0) {
                    val obj = array.getJSONObject(0)
                    parseProfileFromJson(obj, userId)
                } else null
            } else null
        } catch (_: Exception) { null }
    }

    suspend fun saveOnboarding(
        nativeLanguageId: Long,
        learningLanguageId: Long,
        currentLevel: String,
        targetLevel: String,
        ageGroup: String?,
        gender: String?,
        learningReasons: List<String>,
        dailyGoal: Int
    ): Resource<Profile> = withContext(Dispatchers.IO) {
        val session = _currentSession.value ?: return@withContext Resource.Error("User is not authenticated.")

        val updatedProfile = session.profile.copy(
            nativeLanguageId = nativeLanguageId,
            learningLanguageId = learningLanguageId,
            currentLevel = currentLevel,
            targetLevel = targetLevel,
            ageGroup = ageGroup,
            gender = gender,
            learningReasons = learningReasons,
            dailyGoal = dailyGoal,
            onboardingCompleted = true,
            onboardingStep = 8
        )

        if (!SupabaseConfig.isConfigured) {
            val updatedSession = session.copy(profile = updatedProfile)
            _currentSession.value = updatedSession
            sessionManager?.saveSession(updatedSession)
            sessionManager?.clearOnboardingDraft()
            return@withContext Resource.Success(updatedProfile)
        }

        try {
            val reasonsJson = JSONArray()
            learningReasons.forEach { reasonsJson.put(it) }

            var savedProfile: Profile? = null

            // 1. Try RPC call
            try {
                val rpcBody = JSONObject().apply {
                    put("p_native_language_id", nativeLanguageId)
                    put("p_learning_language_id", learningLanguageId)
                    put("p_current_level", currentLevel)
                    put("p_target_level", targetLevel)
                    if (ageGroup != null) put("p_age_group", ageGroup)
                    if (gender != null) put("p_gender", gender)
                    put("p_learning_reasons", reasonsJson)
                    put("p_daily_goal", dailyGoal)
                }
                val rpcReq = Request.Builder()
                    .url("${SupabaseConfig.url}/rest/v1/rpc/save_user_onboarding")
                    .header("apikey", SupabaseConfig.anonKey)
                    .apply {
                        session.accessToken?.let { header("Authorization", "Bearer $it") }
                    }
                    .post(rpcBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val rpcResp = httpClient.newCall(rpcReq).execute()
                val rpcStr = rpcResp.body?.string() ?: ""
                if (rpcResp.isSuccessful && rpcStr.isNotBlank()) {
                    val rpcJson = JSONObject(rpcStr)
                    val profObj = rpcJson.optJSONObject("profile")
                    if (profObj != null) {
                        savedProfile = parseProfileFromJson(profObj, session.userId)
                    }
                }
            } catch (_: Exception) {}

            // 2. Fallback to direct REST PATCH if RPC is unavailable
            if (savedProfile == null) {
                val patchBody = JSONObject().apply {
                    put("native_language_id", nativeLanguageId)
                    put("learning_language_id", learningLanguageId)
                    put("current_level", currentLevel)
                    put("target_level", targetLevel)
                    if (ageGroup != null) put("age_group", ageGroup)
                    if (gender != null) put("gender", gender)
                    put("learning_reasons", reasonsJson)
                    put("daily_goal", dailyGoal)
                    put("onboarding_completed", true)
                    put("onboarding_step", 8)
                }
                val patchReq = Request.Builder()
                    .url("${SupabaseConfig.url}/rest/v1/profiles?id=eq.${session.userId}")
                    .header("apikey", SupabaseConfig.anonKey)
                    .header("Prefer", "return=representation")
                    .apply {
                        session.accessToken?.let { header("Authorization", "Bearer $it") }
                    }
                    .patch(patchBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val patchResp = httpClient.newCall(patchReq).execute()
                val patchStr = patchResp.body?.string() ?: ""
                if (patchResp.isSuccessful) {
                    if (patchStr.isNotBlank()) {
                        val arr = JSONArray(patchStr)
                        if (arr.length() > 0) {
                            savedProfile = parseProfileFromJson(arr.getJSONObject(0), session.userId)
                        }
                    }
                    if (savedProfile == null) savedProfile = updatedProfile
                } else {
                    return@withContext Resource.Error("Could not save your profile. Please check your connection and try again.")
                }
            }

            val finalProfile = savedProfile ?: updatedProfile
            val updatedSession = session.copy(profile = finalProfile)
            _currentSession.value = updatedSession
            sessionManager?.saveSession(updatedSession)
            sessionManager?.clearOnboardingDraft()

            Resource.Success(finalProfile)
        } catch (e: Exception) {
            Resource.Error("Could not save your profile. Please check your connection and try again.", e)
        }
    }

    private fun fetchAndSyncUserProgress(userId: String, token: String?) {
        try {
            val reqBuilder = Request.Builder()
                .url("${SupabaseConfig.url}/rest/v1/user_progress?user_id=eq.$userId&completed=eq.true&select=lesson_id")
                .header("apikey", SupabaseConfig.anonKey)
            if (!token.isNullOrBlank()) {
                reqBuilder.header("Authorization", "Bearer $token")
            }
            val response = httpClient.newCall(reqBuilder.build()).execute()
            val resStr = response.body?.string() ?: ""
            if (response.isSuccessful && resStr.isNotBlank()) {
                val array = JSONArray(resStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val lessonId = obj.optLong("lesson_id", -1L)
                    if (lessonId > 0) {
                        completedLessonIds.add(lessonId)
                        sessionManager?.saveCompletedLessonId(lessonId)
                    }
                }
            }
        } catch (_: Exception) {}
    }

    suspend fun updateProfile(profile: Profile): Resource<Profile> = withContext(Dispatchers.IO) {
        val session = _currentSession.value
        if (session != null) {
            val updated = session.copy(profile = profile)
            _currentSession.value = updated
            sessionManager?.saveSession(updated)
        }

        if (!SupabaseConfig.isConfigured) {
            return@withContext Resource.Success(profile)
        }

        try {
            val jsonBody = JSONObject().apply {
                put("display_name", profile.displayName)
                put("username", profile.username)
                if (profile.avatarUrl != null) {
                    put("avatar_url", profile.avatarUrl)
                }
                put("daily_goal", profile.dailyGoal)
            }
            val request = Request.Builder()
                .url("${SupabaseConfig.url}/rest/v1/profiles?id=eq.${profile.id}")
                .header("apikey", SupabaseConfig.anonKey)
                .apply {
                    session?.accessToken?.let { header("Authorization", "Bearer $it") }
                }
                .patch(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Resource.Success(profile)
            } else {
                Resource.Success(profile)
            }
        } catch (e: Exception) {
            Resource.Success(profile)
        }
    }

    suspend fun completeLesson(
        lessonId: Long
    ): Resource<LessonCompletionResult> = withContext(Dispatchers.IO) {
        val session = _currentSession.value ?: return@withContext Resource.Error("No active user session. Please sign in.")

        if (!SupabaseConfig.isConfigured) {
            return@withContext Resource.Error("Backend service is not configured.")
        }

        try {
            val jsonBody = JSONObject().apply {
                put("p_lesson_id", lessonId)
            }
            val reqBuilder = Request.Builder()
                .url("${SupabaseConfig.url}/rest/v1/rpc/complete_lesson")
                .header("apikey", SupabaseConfig.anonKey)
                .apply {
                    session.accessToken?.let { header("Authorization", "Bearer $it") }
                }
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))

            val response = httpClient.newCall(reqBuilder.build()).execute()
            val responseString = response.body?.string() ?: ""

            if (response.isSuccessful && responseString.isNotBlank()) {
                val json = JSONObject(responseString)
                val success = json.optBoolean("success", true)
                val rewarded = json.optBoolean("rewarded", true)
                val xpEarned = json.optInt("xp_earned", 0)
                val coinsEarned = json.optInt("coins_earned", 0)
                val profileObj = json.optJSONObject("profile")

                val updatedProfile = if (profileObj != null) {
                    Profile(
                        id = profileObj.getString("id"),
                        username = profileObj.optNullableString("username"),
                        displayName = profileObj.optString("display_name", session.profile.displayName ?: "Learner"),
                        avatarUrl = profileObj.optNullableString("avatar_url"),
                        xp = profileObj.optInt("xp", session.profile.xp + xpEarned),
                        coins = profileObj.optInt("coins", session.profile.coins + coinsEarned),
                        streak = profileObj.optInt("streak", session.profile.streak),
                        dailyGoal = profileObj.optInt("daily_goal", session.profile.dailyGoal)
                    )
                } else {
                    session.profile.copy(
                        xp = session.profile.xp + xpEarned,
                        coins = session.profile.coins + coinsEarned
                    )
                }

                completedLessonIds.add(lessonId)
                sessionManager?.saveCompletedLessonId(lessonId)

                val updatedSession = session.copy(profile = updatedProfile)
                _currentSession.value = updatedSession
                sessionManager?.saveSession(updatedSession)

                Resource.Success(
                    LessonCompletionResult(
                        success = success,
                        rewarded = rewarded,
                        xpEarned = xpEarned,
                        coinsEarned = coinsEarned,
                        profile = updatedProfile
                    )
                )
            } else {
                // Fallback: update user_progress directly if RPC is not yet loaded in Supabase instance
                val isAlreadyCompleted = completedLessonIds.contains(lessonId)
                completedLessonIds.add(lessonId)
                sessionManager?.saveCompletedLessonId(lessonId)

                val xpEarned = if (isAlreadyCompleted) 0 else 15
                val coinsEarned = if (isAlreadyCompleted) 0 else 10

                // Attempt to record in user_progress table directly
                try {
                    val progressBody = JSONObject().apply {
                        put("user_id", session.userId)
                        put("lesson_id", lessonId)
                        put("completed", true)
                        put("progress", 100)
                        put("xp_earned", xpEarned)
                    }
                    val progressReq = Request.Builder()
                        .url("${SupabaseConfig.url}/rest/v1/user_progress")
                        .header("apikey", SupabaseConfig.anonKey)
                        .header("Prefer", "resolution=merge-duplicates")
                        .apply { session.accessToken?.let { header("Authorization", "Bearer $it") } }
                        .post(progressBody.toString().toRequestBody("application/json".toMediaType()))
                        .build()
                    httpClient.newCall(progressReq).execute()
                } catch (_: Exception) {}

                val updatedProfile = session.profile.copy(
                    xp = session.profile.xp + xpEarned,
                    coins = session.profile.coins + coinsEarned
                )
                createOrUpdateProfileInSupabase(updatedProfile, session.accessToken)

                val updatedSession = session.copy(profile = updatedProfile)
                _currentSession.value = updatedSession
                sessionManager?.saveSession(updatedSession)

                Resource.Success(
                    LessonCompletionResult(
                        success = true,
                        rewarded = !isAlreadyCompleted,
                        xpEarned = xpEarned,
                        coinsEarned = coinsEarned,
                        profile = updatedProfile
                    )
                )
            }
        } catch (e: Exception) {
            Resource.Error("Network error completing lesson: ${e.localizedMessage}", e)
        }
    }

    fun toggleVocabularyBookmark(vocabId: Long): Boolean {
        return if (bookmarkedVocabIds.contains(vocabId)) {
            bookmarkedVocabIds.remove(vocabId)
            false
        } else {
            bookmarkedVocabIds.add(vocabId)
            true
        }
    }

    suspend fun getLanguages(): Resource<List<LanguageItem>> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured) {
            return@withContext Resource.Error("Database connection is not configured.")
        }

        try {
            val request = Request.Builder()
                .url("${SupabaseConfig.url}/rest/v1/languages?select=*&order=id.asc")
                .header("apikey", SupabaseConfig.anonKey)
                .build()

            val response = httpClient.newCall(request).execute()
            val str = response.body?.string() ?: ""
            if (response.isSuccessful && str.isNotBlank()) {
                val jsonArray = JSONArray(str)
                val list = mutableListOf<LanguageItem>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        LanguageItem(
                            id = obj.getLong("id"),
                            name = obj.getString("name"),
                            nativeName = obj.optNullableString("native_name"),
                            code = obj.getString("code"),
                            flagEmoji = obj.optString("flag_emoji", obj.optString("flag", "🌐")),
                            iconUrl = obj.optNullableString("icon_url"),
                            description = obj.optNullableString("description"),
                            learnersCount = obj.optInt("learners_count", 0),
                            isActive = obj.optBoolean("is_active", true),
                            sortOrder = obj.optInt("sort_order", i + 1)
                        )
                    )
                }
                if (list.isNotEmpty()) return@withContext Resource.Success(list)
                else return@withContext Resource.Empty
            } else {
                android.util.Log.e("LinguaXRepo", "Failed to fetch languages from Supabase: HTTP ${response.code} - $str")
                return@withContext Resource.Error("تعذر تحميل اللغات. تحقق من الاتصال بالإنترنت وحاول مرة أخرى.")
            }
        } catch (e: Exception) {
            android.util.Log.e("LinguaXRepo", "Error connecting to Supabase database", e)
            return@withContext Resource.Error("تعذر تحميل اللغات. تحقق من الاتصال بالإنترنت وحاول مرة أخرى.", e)
        }
    }

    suspend fun getCourses(languageCode: String, languageId: Long? = null): Resource<List<Course>> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured) {
            return@withContext Resource.Error("Database connection is not configured.")
        }

        try {
            // Step 1: Look up Language ID by Code if not provided
            var targetLangId: Long? = languageId
            if (targetLangId == null) {
                try {
                    val langReq = Request.Builder()
                        .url("${SupabaseConfig.url}/rest/v1/languages?code=eq.$languageCode&select=id&limit=1")
                        .header("apikey", SupabaseConfig.anonKey)
                        .build()
                    val langResp = httpClient.newCall(langReq).execute()
                    val langStr = langResp.body?.string() ?: ""
                    if (langResp.isSuccessful && langStr.isNotBlank()) {
                        val arr = JSONArray(langStr)
                        if (arr.length() > 0) {
                            targetLangId = arr.getJSONObject(0).getLong("id")
                        }
                    }
                } catch (_: Exception) {}
            }

            // Step 2: Fetch current user completed lessons from user_progress
            val session = _currentSession.value
            if (session != null) {
                fetchAndSyncUserProgress(session.userId, session.accessToken)
            }

            // Step 3: Fetch courses for this language with nested units and lessons
            val url = if (targetLangId != null) {
                "${SupabaseConfig.url}/rest/v1/courses?language_id=eq.$targetLangId&select=*,units(*,lessons(*))&order=id.asc"
            } else {
                "${SupabaseConfig.url}/rest/v1/courses?select=*,units(*,lessons(*))&order=id.asc"
            }

            val request = Request.Builder()
                .url(url)
                .header("apikey", SupabaseConfig.anonKey)
                .build()

            val response = httpClient.newCall(request).execute()
            val str = response.body?.string() ?: ""

            if (response.isSuccessful && str.isNotBlank()) {
                val jsonArray = JSONArray(str)
                val coursesList = mutableListOf<Course>()

                for (i in 0 until jsonArray.length()) {
                    val cObj = jsonArray.getJSONObject(i)
                    val cId = cObj.getLong("id")
                    val cLangId = cObj.optLong("language_id", targetLangId ?: 1L)

                    val unitsJson = cObj.optJSONArray("units") ?: JSONArray()
                    val unitsList = mutableListOf<UnitItem>()

                    // Sort units by sort_order / order_index / id
                    val rawUnits = mutableListOf<JSONObject>()
                    for (u in 0 until unitsJson.length()) {
                        rawUnits.add(unitsJson.getJSONObject(u))
                    }
                    rawUnits.sortBy { it.optInt("sort_order", it.optInt("order_index", 1)) }

                    var isFirstLessonEver = true

                    for (uObj in rawUnits) {
                        val uId = uObj.getLong("id")
                        val lessonsJson = uObj.optJSONArray("lessons") ?: JSONArray()

                        val rawLessons = mutableListOf<JSONObject>()
                        for (l in 0 until lessonsJson.length()) {
                            rawLessons.add(lessonsJson.getJSONObject(l))
                        }
                        rawLessons.sortBy { it.optInt("sort_order", it.optInt("order_index", 1)) }

                        val lessonsList = mutableListOf<Lesson>()
                        var prevLessonCompleted = false

                        for (lIdx in rawLessons.indices) {
                            val lObj = rawLessons[lIdx]
                            val lId = lObj.getLong("id")
                            val isComp = completedLessonIds.contains(lId)

                            val status = when {
                                isComp -> LessonStatus.COMPLETED
                                isFirstLessonEver -> LessonStatus.CURRENT
                                prevLessonCompleted -> LessonStatus.CURRENT
                                else -> LessonStatus.LOCKED
                            }

                            if (isFirstLessonEver && !isComp) {
                                isFirstLessonEver = false
                            }
                            prevLessonCompleted = isComp

                            lessonsList.add(
                                Lesson(
                                    id = lId,
                                    unitId = uId,
                                    title = lObj.getString("title"),
                                    description = lObj.optString("description", ""),
                                    xpReward = lObj.optInt("xp_reward", 15),
                                    durationMins = lObj.optInt("duration_mins", lObj.optInt("estimated_minutes", 5)),
                                    orderIndex = lObj.optInt("order_index", lObj.optInt("sort_order", lIdx + 1)),
                                    isFree = lObj.optBoolean("is_free", true),
                                    isActive = lObj.optBoolean("is_active", true),
                                    status = status,
                                    exercisesCount = lObj.optInt("exercises_count", 5)
                                )
                            )
                        }

                        unitsList.add(
                            UnitItem(
                                id = uId,
                                courseId = cId,
                                title = uObj.getString("title"),
                                description = uObj.optString("description", ""),
                                orderIndex = uObj.optInt("order_index", uObj.optInt("sort_order", 1)),
                                sortOrder = uObj.optInt("sort_order", 1),
                                lessons = lessonsList
                            )
                        )
                    }

                    coursesList.add(
                        Course(
                            id = cId,
                            languageId = cLangId,
                            title = cObj.getString("title"),
                            description = cObj.optString("description", ""),
                            level = cObj.optString("level", "A1 Beginner"),
                            imageUrl = cObj.optNullableString("image_url"),
                            totalLessons = cObj.optInt("total_lessons", unitsList.sumOf { it.lessons.size }),
                            orderIndex = cObj.optInt("order_index", cObj.optInt("sort_order", i + 1)),
                            isActive = cObj.optBoolean("is_active", true),
                            sortOrder = cObj.optInt("sort_order", i + 1),
                            units = unitsList
                        )
                    )
                }

                coursesList.sortBy { it.sortOrder }

                if (coursesList.isNotEmpty()) {
                    return@withContext Resource.Success(coursesList)
                } else {
                    return@withContext Resource.Empty
                }
            } else {
                return@withContext Resource.Error("Unable to fetch courses (${response.code})")
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Error loading courses: ${e.localizedMessage}", e)
        }
    }

    suspend fun getVocabulary(languageCode: String): Resource<List<VocabularyItem>> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured) {
            return@withContext Resource.Error("Database connection is not configured.")
        }

        try {
            val request = Request.Builder()
                .url("${SupabaseConfig.url}/rest/v1/vocabulary?language_code=eq.$languageCode&order=id.asc")
                .header("apikey", SupabaseConfig.anonKey)
                .build()

            val response = httpClient.newCall(request).execute()
            val str = response.body?.string() ?: ""

            if (response.isSuccessful && str.isNotBlank()) {
                val jsonArray = JSONArray(str)
                val list = mutableListOf<VocabularyItem>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.getLong("id")
                    list.add(
                        VocabularyItem(
                            id = id,
                            word = obj.getString("word"),
                            translation = obj.getString("translation"),
                            phonetic = obj.optNullableString("phonetic"),
                            partOfSpeech = obj.optString("part_of_speech", "Noun"),
                            exampleSentence = obj.optNullableString("example_sentence"),
                            languageCode = obj.optString("language_code", languageCode),
                            audioUrl = obj.optNullableString("audio_url"),
                            masteryLevel = obj.optInt("mastery_level", 1),
                            isBookmarked = bookmarkedVocabIds.contains(id)
                        )
                    )
                }
                if (list.isNotEmpty()) return@withContext Resource.Success(list)
                else return@withContext Resource.Empty
            } else {
                return@withContext Resource.Error("Unable to fetch vocabulary (${response.code})")
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Error fetching vocabulary: ${e.localizedMessage}", e)
        }
    }

    suspend fun getDailyChallenges(): Resource<List<DailyChallenge>> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured) {
            return@withContext Resource.Error("Database connection is not configured.")
        }

        try {
            val request = Request.Builder()
                .url("${SupabaseConfig.url}/rest/v1/challenges?is_active=eq.true&order=id.asc")
                .header("apikey", SupabaseConfig.anonKey)
                .build()

            val response = httpClient.newCall(request).execute()
            val str = response.body?.string() ?: ""

            if (response.isSuccessful && str.isNotBlank()) {
                val jsonArray = JSONArray(str)
                val list = mutableListOf<DailyChallenge>()
                val completedCount = completedLessonIds.size
                val userXp = _currentSession.value?.profile?.xp ?: 0

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val target = obj.optInt("target", 1)
                    val rewardXp = obj.optInt("reward_xp", 25)
                    val title = obj.getString("title")

                    // Real dynamic progress calculation
                    val currentProg = when {
                        title.contains("Lesson", ignoreCase = true) -> completedCount.coerceAtMost(target)
                        title.contains("XP", ignoreCase = true) -> userXp.coerceAtMost(target)
                        title.contains("Vocab", ignoreCase = true) -> bookmarkedVocabIds.size.coerceAtMost(target)
                        else -> 0
                    }

                    list.add(
                        DailyChallenge(
                            id = obj.getLong("id"),
                            title = title,
                            description = obj.optString("description", ""),
                            rewardXp = rewardXp,
                            rewardCoins = obj.optInt("reward_coins", 10),
                            target = target,
                            isActive = obj.optBoolean("is_active", true),
                            currentProgress = currentProg,
                            isCompleted = currentProg >= target
                        )
                    )
                }
                if (list.isNotEmpty()) return@withContext Resource.Success(list)
                else return@withContext Resource.Empty
            } else {
                return@withContext Resource.Error("Unable to fetch challenges (${response.code})")
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Error loading challenges: ${e.localizedMessage}", e)
        }
    }

    suspend fun getAchievements(): Resource<List<AchievementItem>> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured) {
            return@withContext Resource.Error("Database connection is not configured.")
        }

        try {
            val request = Request.Builder()
                .url("${SupabaseConfig.url}/rest/v1/achievements?select=*&order=id.asc")
                .header("apikey", SupabaseConfig.anonKey)
                .build()

            val response = httpClient.newCall(request).execute()
            val str = response.body?.string() ?: ""

            if (response.isSuccessful && str.isNotBlank()) {
                val jsonArray = JSONArray(str)
                val list = mutableListOf<AchievementItem>()

                val profile = _currentSession.value?.profile
                val userXp = profile?.xp ?: 0
                val userStreak = profile?.streak ?: 0
                val completedCount = completedLessonIds.size
                val vocabCount = bookmarkedVocabIds.size

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.getLong("id")
                    val maxProg = obj.optInt("max_progress", 1)
                    val category = obj.optString("category", "General")

                    val progress = when (category.lowercase()) {
                        "streak" -> userStreak.coerceAtMost(maxProg)
                        "xp", "milestone" -> userXp.coerceAtMost(maxProg)
                        "vocabulary" -> vocabCount.coerceAtMost(maxProg)
                        "beginner", "explorer" -> completedCount.coerceAtMost(maxProg)
                        else -> completedCount.coerceAtMost(maxProg)
                    }
                    val isUnlocked = progress >= maxProg && maxProg > 0

                    list.add(
                        AchievementItem(
                            id = id,
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            iconName = obj.optString("icon", "star"),
                            category = category,
                            maxProgress = maxProg,
                            isUnlocked = isUnlocked,
                            unlockedAt = if (isUnlocked) "Active" else null,
                            progress = progress
                        )
                    )
                }
                if (list.isNotEmpty()) return@withContext Resource.Success(list)
                else return@withContext Resource.Empty
            } else {
                return@withContext Resource.Error("Unable to fetch achievements (${response.code})")
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Error loading achievements: ${e.localizedMessage}", e)
        }
    }

    suspend fun getLeaderboard(): Resource<List<LeaderboardEntry>> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured) {
            return@withContext Resource.Error("Database connection is not configured.")
        }

        val currentUserId = _currentSession.value?.userId ?: ""
        try {
            val request = Request.Builder()
                .url("${SupabaseConfig.url}/rest/v1/profiles?select=id,username,display_name,avatar_url,xp&order=xp.desc&limit=50")
                .header("apikey", SupabaseConfig.anonKey)
                .build()

            val response = httpClient.newCall(request).execute()
            val str = response.body?.string() ?: ""

            if (response.isSuccessful && str.isNotBlank()) {
                val jsonArray = JSONArray(str)
                val list = mutableListOf<LeaderboardEntry>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.getString("id")
                    list.add(
                        LeaderboardEntry(
                            id = id,
                            username = obj.optNullableString("username"),
                            displayName = obj.optString("display_name", "Learner"),
                            avatarUrl = obj.optNullableString("avatar_url"),
                            xp = obj.optInt("xp", 0),
                            rank = i + 1,
                            isCurrentUser = id == currentUserId
                        )
                    )
                }
                if (list.isNotEmpty()) return@withContext Resource.Success(list)
                else return@withContext Resource.Empty
            } else {
                return@withContext Resource.Error("Unable to fetch leaderboard (${response.code})")
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Error loading leaderboard: ${e.localizedMessage}", e)
        }
    }

    suspend fun getExercisesForLesson(lessonId: Long): Resource<List<Exercise>> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured) {
            return@withContext Resource.Error("Database connection is not configured.")
        }

        try {
            val request = Request.Builder()
                .url("${SupabaseConfig.url}/rest/v1/exercises?lesson_id=eq.$lessonId&order=sort_order.asc")
                .header("apikey", SupabaseConfig.anonKey)
                .build()

            val response = httpClient.newCall(request).execute()
            val str = response.body?.string() ?: ""

            if (response.isSuccessful && str.isNotBlank()) {
                val jsonArray = JSONArray(str)
                val list = mutableListOf<Exercise>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val optArray = obj.optJSONArray("options") ?: JSONArray()
                    val opts = mutableListOf<String>()
                    for (o in 0 until optArray.length()) {
                        opts.add(optArray.getString(o))
                    }
                    list.add(
                        Exercise(
                            id = obj.getLong("id"),
                            lessonId = lessonId,
                            type = obj.optString("type", "MULTIPLE_CHOICE"),
                            question = obj.getString("question"),
                            options = opts,
                            correctAnswer = obj.getString("correct_answer"),
                            explanation = obj.optNullableString("explanation"),
                            audioUrl = obj.optNullableString("audio_url"),
                            imageUrl = obj.optNullableString("image_url"),
                            sortOrder = obj.optInt("sort_order", i + 1)
                        )
                    )
                }
                if (list.isNotEmpty()) return@withContext Resource.Success(list)
                else return@withContext Resource.Empty
            } else {
                return@withContext Resource.Error("Unable to load exercises (${response.code})")
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Error connecting to database: ${e.localizedMessage}", e)
        }
    }

    private fun String.capitalizeWords(): String {
        return this.split(" ").joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
    }
}
