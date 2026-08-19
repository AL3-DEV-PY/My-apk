package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Profile(
    @Json(name = "id") val id: String,
    @Json(name = "username") val username: String? = null,
    @Json(name = "display_name") val displayName: String? = "Learner",
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "xp") val xp: Int = 0,
    @Json(name = "coins") val coins: Int = 0,
    @Json(name = "streak") val streak: Int = 0,
    @Json(name = "daily_goal") val dailyGoal: Int = 15,
    @Json(name = "native_language_id") val nativeLanguageId: Long? = null,
    @Json(name = "learning_language_id") val learningLanguageId: Long? = null,
    @Json(name = "current_level") val currentLevel: String? = "A1",
    @Json(name = "target_level") val targetLevel: String? = "B1",
    @Json(name = "age_group") val ageGroup: String? = null,
    @Json(name = "gender") val gender: String? = null,
    @Json(name = "learning_reasons") val learningReasons: List<String> = emptyList(),
    @Json(name = "onboarding_completed") val onboardingCompleted: Boolean = false,
    @Json(name = "onboarding_step") val onboardingStep: Int = 1,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class LanguageItem(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "native_name") val nativeName: String? = null,
    @Json(name = "code") val code: String,
    @Json(name = "flag_emoji") val flagEmoji: String = "🌐",
    @Json(name = "icon_url") val iconUrl: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "learners_count") val learnersCount: Int = 0,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "sort_order") val sortOrder: Int = 1
)

@JsonClass(generateAdapter = true)
data class Course(
    @Json(name = "id") val id: Long,
    @Json(name = "language_id") val languageId: Long,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String = "",
    @Json(name = "level") val level: String = "A1 Beginner",
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "total_lessons") val totalLessons: Int = 0,
    @Json(name = "order_index") val orderIndex: Int = 1,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "sort_order") val sortOrder: Int = 1,
    val units: List<UnitItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class UnitItem(
    @Json(name = "id") val id: Long,
    @Json(name = "course_id") val courseId: Long,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String = "",
    @Json(name = "order_index") val orderIndex: Int = 1,
    @Json(name = "sort_order") val sortOrder: Int = 1,
    val lessons: List<Lesson> = emptyList()
)

enum class LessonStatus {
    COMPLETED,
    CURRENT,
    LOCKED
}

@JsonClass(generateAdapter = true)
data class Lesson(
    @Json(name = "id") val id: Long,
    @Json(name = "unit_id") val unitId: Long,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String = "",
    @Json(name = "xp_reward") val xpReward: Int = 15,
    @Json(name = "duration_mins") val durationMins: Int = 5,
    @Json(name = "order_index") val orderIndex: Int = 1,
    @Json(name = "is_free") val isFree: Boolean = true,
    @Json(name = "is_active") val isActive: Boolean = true,
    val status: LessonStatus = LessonStatus.LOCKED,
    val exercisesCount: Int = 0
)

enum class ExerciseType {
    MULTIPLE_CHOICE,
    VOCABULARY,
    LISTENING,
    TRANSLATION,
    PRONUNCIATION,
    FILL_IN_BLANK
}

@JsonClass(generateAdapter = true)
data class Exercise(
    @Json(name = "id") val id: Long,
    @Json(name = "lesson_id") val lessonId: Long,
    @Json(name = "type") val type: String = "MULTIPLE_CHOICE",
    @Json(name = "question") val question: String,
    @Json(name = "options") val options: List<String> = emptyList(),
    @Json(name = "correct_answer") val correctAnswer: String,
    @Json(name = "explanation") val explanation: String? = null,
    @Json(name = "audio_url") val audioUrl: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "sort_order") val sortOrder: Int = 1
)

@JsonClass(generateAdapter = true)
data class VocabularyItem(
    @Json(name = "id") val id: Long,
    @Json(name = "word") val word: String,
    @Json(name = "translation") val translation: String,
    @Json(name = "phonetic") val phonetic: String? = null,
    @Json(name = "part_of_speech") val partOfSpeech: String = "Noun",
    @Json(name = "example_sentence") val exampleSentence: String? = null,
    @Json(name = "language_code") val languageCode: String = "en",
    @Json(name = "audio_url") val audioUrl: String? = null,
    val masteryLevel: Int = 1, // 1 to 5
    val isBookmarked: Boolean = false
)

@JsonClass(generateAdapter = true)
data class DailyChallenge(
    @Json(name = "id") val id: Long,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String = "",
    @Json(name = "reward_xp") val rewardXp: Int = 25,
    @Json(name = "reward_coins") val rewardCoins: Int = 10,
    @Json(name = "target") val target: Int = 1,
    @Json(name = "is_active") val isActive: Boolean = true,
    val currentProgress: Int = 0,
    val isCompleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class AchievementItem(
    @Json(name = "id") val id: Long,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String = "",
    @Json(name = "icon") val iconName: String = "star",
    @Json(name = "category") val category: String = "General",
    @Json(name = "max_progress") val maxProgress: Int = 1,
    val isUnlocked: Boolean = false,
    val unlockedAt: String? = null,
    val progress: Int = 0
)

@JsonClass(generateAdapter = true)
data class UserProgress(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "user_id") val userId: String,
    @Json(name = "lesson_id") val lessonId: Long,
    @Json(name = "completed") val completed: Boolean = false,
    @Json(name = "progress") val progress: Int = 0,
    @Json(name = "xp_earned") val xpEarned: Int = 0,
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class LeaderboardEntry(
    @Json(name = "id") val id: String,
    @Json(name = "username") val username: String? = null,
    @Json(name = "display_name") val displayName: String = "Learner",
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "xp") val xp: Int = 0,
    @Json(name = "rank") val rank: Int = 1,
    @Json(name = "is_current_user") val isCurrentUser: Boolean = false
)

@JsonClass(generateAdapter = true)
data class LessonCompletionResult(
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "rewarded") val rewarded: Boolean = true,
    @Json(name = "xp_earned") val xpEarned: Int = 0,
    @Json(name = "coins_earned") val coinsEarned: Int = 0,
    @Json(name = "profile") val profile: Profile? = null
)
