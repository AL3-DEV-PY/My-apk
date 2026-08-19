package com.example.data.supabase

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Profile
import com.example.data.repository.UserSession
import org.json.JSONArray
import org.json.JSONObject

/**
 * Robust Local Session Storage using SharedPreferences.
 * Stores and restores authenticated user sessions across app restarts,
 * activity recreations, and background/foreground cycles without race conditions.
 */
class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "linguax_session_store"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_PROFILE_JSON = "profile_json"
        private const val KEY_COMPLETED_LESSONS = "completed_lesson_ids"
        private const val KEY_APP_LANGUAGE = "app_language"
        private const val KEY_ONBOARDING_DRAFT = "onboarding_draft"
    }

    fun saveAppLanguage(languageCode: String) {
        prefs.edit().putString(KEY_APP_LANGUAGE, languageCode).apply()
    }

    fun loadAppLanguageCode(): String? {
        return prefs.getString(KEY_APP_LANGUAGE, null)
    }

    fun saveCompletedLessonId(lessonId: Long) {
        val current = getCompletedLessonIdStrings().toMutableSet()
        current.add(lessonId.toString())
        prefs.edit().putStringSet(KEY_COMPLETED_LESSONS, current).apply()
    }

    private fun getCompletedLessonIdStrings(): Set<String> {
        return prefs.getStringSet(KEY_COMPLETED_LESSONS, emptySet()) ?: emptySet()
    }

    fun getCompletedLessonIds(): Set<Long> {
        return getCompletedLessonIdStrings().mapNotNull { it.toLongOrNull() }.toSet()
    }

    fun saveOnboardingDraft(draftJson: String) {
        prefs.edit().putString(KEY_ONBOARDING_DRAFT, draftJson).apply()
    }

    fun loadOnboardingDraft(): String? {
        return prefs.getString(KEY_ONBOARDING_DRAFT, null)
    }

    fun clearOnboardingDraft() {
        prefs.edit().remove(KEY_ONBOARDING_DRAFT).apply()
    }

    fun saveSession(session: UserSession) {
        val reasonsArray = JSONArray()
        session.profile.learningReasons.forEach { reasonsArray.put(it) }

        val profileJson = JSONObject().apply {
            put("id", session.profile.id)
            put("username", session.profile.username ?: "")
            put("display_name", session.profile.displayName ?: "Learner")
            put("avatar_url", session.profile.avatarUrl ?: "")
            put("xp", session.profile.xp)
            put("coins", session.profile.coins)
            put("streak", session.profile.streak)
            put("daily_goal", session.profile.dailyGoal)
            session.profile.nativeLanguageId?.let { put("native_language_id", it) }
            session.profile.learningLanguageId?.let { put("learning_language_id", it) }
            put("current_level", session.profile.currentLevel ?: "A1")
            put("target_level", session.profile.targetLevel ?: "B1")
            put("age_group", session.profile.ageGroup ?: "")
            put("gender", session.profile.gender ?: "")
            put("learning_reasons", reasonsArray)
            put("onboarding_completed", session.profile.onboardingCompleted)
            put("onboarding_step", session.profile.onboardingStep)
        }.toString()

        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_ACCESS_TOKEN, session.accessToken ?: "")
            .putString(KEY_PROFILE_JSON, profileJson)
            .apply()
    }

    fun loadSession(): UserSession? {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return null

        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null).takeIf { !it.isNullOrBlank() }
        val profileJsonStr = prefs.getString(KEY_PROFILE_JSON, null)

        val profile = if (!profileJsonStr.isNullOrBlank()) {
            try {
                val json = JSONObject(profileJsonStr)
                val reasonsList = mutableListOf<String>()
                val jsonReasons = json.optJSONArray("learning_reasons")
                if (jsonReasons != null) {
                    for (i in 0 until jsonReasons.length()) {
                        reasonsList.add(jsonReasons.getString(i))
                    }
                }

                Profile(
                    id = json.optString("id", userId),
                    username = json.optString("username").takeIf { it.isNotBlank() },
                    displayName = json.optString("display_name", email.substringBefore("@")),
                    avatarUrl = json.optString("avatar_url").takeIf { it.isNotBlank() },
                    xp = json.optInt("xp", 0),
                    coins = json.optInt("coins", 0),
                    streak = json.optInt("streak", 0),
                    dailyGoal = json.optInt("daily_goal", 15),
                    nativeLanguageId = if (json.has("native_language_id")) json.optLong("native_language_id") else null,
                    learningLanguageId = if (json.has("learning_language_id")) json.optLong("learning_language_id") else null,
                    currentLevel = json.optString("current_level", "A1"),
                    targetLevel = json.optString("target_level", "B1"),
                    ageGroup = json.optString("age_group").takeIf { it.isNotBlank() },
                    gender = json.optString("gender").takeIf { it.isNotBlank() },
                    learningReasons = reasonsList,
                    onboardingCompleted = json.optBoolean("onboarding_completed", false),
                    onboardingStep = json.optInt("onboarding_step", 1)
                )
            } catch (_: Exception) {
                Profile(
                    id = userId,
                    username = email.substringBefore("@"),
                    displayName = email.substringBefore("@"),
                    xp = 0,
                    coins = 0,
                    streak = 0,
                    dailyGoal = 15,
                    onboardingCompleted = false
                )
            }
        } else {
            Profile(
                id = userId,
                username = email.substringBefore("@"),
                displayName = email.substringBefore("@"),
                xp = 0,
                coins = 0,
                streak = 0,
                dailyGoal = 15,
                onboardingCompleted = false
            )
        }

        return UserSession(
            userId = userId,
            email = email,
            accessToken = accessToken,
            profile = profile
        )
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
