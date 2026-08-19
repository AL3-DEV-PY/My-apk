package com.example

import com.example.data.i18n.AppLanguage
import com.example.data.i18n.Translations
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun translations_containPracticeStrings() {
        val en = Translations.get(AppLanguage.ENGLISH)
        val ar = Translations.get(AppLanguage.ARABIC)

        assertEquals("Practice", en.practiceTab)
        assertEquals("التدريب", ar.practiceTab)
        assertEquals("Smart Review", en.smartReview)
        assertEquals("المراجعة الذكية", ar.smartReview)
    }

    @Test
    fun appLanguages_haveCorrectLayoutDirection() {
        assertEquals(androidx.compose.ui.unit.LayoutDirection.Rtl, AppLanguage.ARABIC.layoutDirection)
        assertEquals(androidx.compose.ui.unit.LayoutDirection.Ltr, AppLanguage.ENGLISH.layoutDirection)
    }

    @Test
    fun translations_containLeaderboardAndProfileStrings() {
        val en = Translations.get(AppLanguage.ENGLISH)
        val ar = Translations.get(AppLanguage.ARABIC)

        assertEquals("Leaderboard", en.leaderboardTab)
        assertEquals("لوحة الصدارة", ar.leaderboardTab)
        assertEquals("Global", en.globalRank)
        assertEquals("عالمي", ar.globalRank)
        assertEquals("Learning Statistics", en.statisticsHeader)
        assertEquals("إحصائيات التعلم", ar.statisticsHeader)
    }
}
