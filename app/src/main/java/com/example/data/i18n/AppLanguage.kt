package com.example.data.i18n

import androidx.compose.ui.unit.LayoutDirection

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flag: String,
    val isRtl: Boolean
) {
    ARABIC("ar", "Arabic", "العربية", "🇸🇦", true),
    ENGLISH("en", "English", "English", "🇺🇸", false),
    FRENCH("fr", "French", "Français", "🇫🇷", false),
    SPANISH("es", "Spanish", "Español", "🇪🇸", false),
    GERMAN("de", "German", "Deutsch", "🇩🇪", false),
    TURKISH("tr", "Turkish", "Türkçe", "🇹🇷", false),
    ITALIAN("it", "Italian", "Italiano", "🇮🇹", false),
    JAPANESE("ja", "Japanese", "日本語", "🇯🇵", false),
    KOREAN("ko", "Korean", "한국어", "🇰🇷", false);

    val layoutDirection: LayoutDirection
        get() = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    companion object {
        val supportedLanguages: List<AppLanguage> = listOf(
            ARABIC,
            ENGLISH,
            FRENCH,
            SPANISH,
            GERMAN,
            TURKISH
        )

        fun fromCode(code: String): AppLanguage {
            return values().find { it.code.equals(code, ignoreCase = true) } ?: ARABIC
        }
    }
}
