package com.example.ui.screens.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.i18n.L10nStrings
import com.example.data.model.DailyChallenge
import com.example.data.model.LanguageItem
import com.example.data.model.Profile
import com.example.data.model.VocabularyItem
import com.example.data.repository.Resource
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun PracticeScreen(
    l10n: L10nStrings,
    profile: Profile?,
    selectedTargetLanguage: LanguageItem,
    vocabularyResource: Resource<List<VocabularyItem>>,
    challengesResource: Resource<List<DailyChallenge>>,
    onToggleBookmark: (VocabularyItem) -> Unit,
    onStartFlashcards: (items: List<VocabularyItem>?, isSmartReview: Boolean) -> Unit,
    onClaimChallengeReward: (DailyChallenge) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterBookmarkedOnly by remember { mutableStateOf(false) }
    val audioPlayer = com.example.ui.audio.rememberAudioPlayerManager()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LinguaXBackground)
            .padding(horizontal = 16.dp)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            PracticeHeader(
                l10n = l10n,
                profile = profile,
                selectedTargetLanguage = selectedTargetLanguage
            )
        }

        // 2x2 Practice Modes Grid
        item {
            PracticeModesGrid(
                l10n = l10n,
                vocabularyResource = vocabularyResource,
                onStartVocabularyFlashcards = { items ->
                    onStartFlashcards(items, false)
                }
            )
        }

        // Smart Review Prominent Section
        item {
            SmartReviewSection(
                l10n = l10n,
                vocabularyResource = vocabularyResource,
                onStartSmartReview = { items ->
                    onStartFlashcards(items, true)
                }
            )
        }

        // Daily Challenges Integration Section
        item {
            DailyChallengesPracticeSection(
                l10n = l10n,
                challengesResource = challengesResource,
                onClaimReward = onClaimChallengeReward
            )
        }

        // Vocabulary Library & Explorer Header & Filters
        item {
            VocabularyExplorerHeader(
                l10n = l10n,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                filterBookmarkedOnly = filterBookmarkedOnly,
                onToggleFilter = { filterBookmarkedOnly = it },
                vocabularyResource = vocabularyResource,
                onStartFlashcards = { items -> onStartFlashcards(items, false) }
            )
        }

        // Vocabulary Cards List
        item {
            VocabularyListContent(
                vocabularyResource = vocabularyResource,
                searchQuery = searchQuery,
                filterBookmarkedOnly = filterBookmarkedOnly,
                l10n = l10n,
                selectedTargetLanguage = selectedTargetLanguage,
                audioPlayer = audioPlayer,
                onToggleBookmark = onToggleBookmark
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PracticeHeader(
    l10n: L10nStrings,
    profile: Profile?,
    selectedTargetLanguage: LanguageItem
) {
    LinguaX3DCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF131D30),
        borderBrush = LinguaXBorderGradient,
        elevation = 6.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = LinguaXPrimaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = selectedTargetLanguage.flagEmoji, fontSize = 16.sp)
                        Text(
                            text = selectedTargetLanguage.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LinguaXAccentLight
                        )
                    }
                }

                if (profile != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF261D10),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXWarning.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = LinguaXWarning,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${profile.streak}d",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LinguaXWarning
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF102620),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXSuccess.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = LinguaXSuccess,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${profile.xp} XP",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LinguaXSuccessLight
                                )
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = l10n.practiceTab,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    ),
                    color = LinguaXTextPrimary
                )
                Text(
                    text = "Consolidate vocabulary, grammar structures & active recall",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LinguaXTextSecondary
                )
            }
        }
    }
}

@Composable
private fun PracticeModesGrid(
    l10n: L10nStrings,
    vocabularyResource: Resource<List<VocabularyItem>>,
    onStartVocabularyFlashcards: (List<VocabularyItem>) -> Unit
) {
    val vocabList = (vocabularyResource as? Resource.Success)?.data ?: emptyList()
    val vocabCount = vocabList.size
    val bookmarkedCount = vocabList.count { it.isBookmarked }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = l10n.practiceModes,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = LinguaXTextPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mode 1: Vocabulary Flashcards
            PracticeModeTile(
                title = l10n.flashcards,
                subtitle = "$vocabCount words • $bookmarkedCount review",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                gradient = LinguaXPrimaryGradient,
                badge = "Ready",
                badgeColor = LinguaXAccent,
                onClick = { onStartVocabularyFlashcards(vocabList) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("practice_mode_vocabulary")
            )

            // Mode 2: Grammar
            PracticeModeTile(
                title = l10n.grammarPractice,
                subtitle = "Rules & sentence structure",
                icon = Icons.Default.AutoAwesome,
                gradient = Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4338CA))),
                badge = "Active",
                badgeColor = Color(0xFFA5B4FC),
                onClick = { onStartVocabularyFlashcards(vocabList) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("practice_mode_grammar")
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mode 3: Listening Lab (Coming Soon)
            PracticeModeTile(
                title = l10n.listeningPractice,
                subtitle = "Audio streaming in prep",
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                gradient = Brush.linearGradient(listOf(Color(0xFF2C3B55), Color(0xFF1E2838))),
                badge = l10n.comingSoon,
                badgeColor = LinguaXTextTertiary,
                isAvailable = false,
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .testTag("practice_mode_listening")
            )

            // Mode 4: Speaking Coach (Coming Soon)
            PracticeModeTile(
                title = l10n.speakingPractice,
                subtitle = "Voice recognition in prep",
                icon = Icons.Default.Mic,
                gradient = Brush.linearGradient(listOf(Color(0xFF2C3B55), Color(0xFF1E2838))),
                badge = l10n.comingSoon,
                badgeColor = LinguaXTextTertiary,
                isAvailable = false,
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .testTag("practice_mode_speaking")
            )
        }
    }
}

@Composable
private fun PracticeModeTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: Brush,
    badge: String,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAvailable: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = isAvailable,
        shape = RoundedCornerShape(16.dp),
        color = if (isAvailable) Color(0xFF131D2F) else Color(0xFF101724),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAvailable) Color(0xFF202F47) else Color(0xFF192333)
        ),
        modifier = modifier.height(130.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(gradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                    color = if (isAvailable) LinguaXTextPrimary else LinguaXTextSecondary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = LinguaXTextTertiary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SmartReviewSection(
    l10n: L10nStrings,
    vocabularyResource: Resource<List<VocabularyItem>>,
    onStartSmartReview: (List<VocabularyItem>) -> Unit
) {
    val vocabList = (vocabularyResource as? Resource.Success)?.data ?: emptyList()
    val bookmarkedWords = vocabList.filter { it.isBookmarked }
    val hasReviewWords = bookmarkedWords.isNotEmpty()

    LinguaX3DCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("smart_review_card"),
        backgroundColor = Color(0xFF142036),
        borderBrush = if (hasReviewWords) LinguaXAccentGradient else LinguaXBorderGradient,
        elevation = 6.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(LinguaXAccentGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = l10n.smartReview,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = LinguaXTextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (hasReviewWords) LinguaXWarning.copy(alpha = 0.2f) else LinguaXSurfaceElevated
                ) {
                    Text(
                        text = if (hasReviewWords) "${bookmarkedWords.size} words queued" else "All caught up",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (hasReviewWords) LinguaXWarning else LinguaXTextTertiary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = if (hasReviewWords) {
                    "Smart Review utilizes spaced repetition cues on your bookmarked vocabulary to solidify retention."
                } else {
                    "No words currently marked for review. Bookmark challenging words during your study to populate Smart Review."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = LinguaXTextSecondary
            )

            Button(
                onClick = {
                    if (hasReviewWords) {
                        onStartSmartReview(bookmarkedWords)
                    } else if (vocabList.isNotEmpty()) {
                        onStartSmartReview(vocabList.take(5))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("start_smart_review_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasReviewWords) LinguaXAccent else LinguaXPrimary,
                    contentColor = if (hasReviewWords) Color.Black else Color.White
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (hasReviewWords) "${l10n.smartReview} (${bookmarkedWords.size})" else l10n.smartReview,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyChallengesPracticeSection(
    l10n: L10nStrings,
    challengesResource: Resource<List<DailyChallenge>>,
    onClaimReward: (DailyChallenge) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = LinguaXWarning,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = l10n.activeChallenges,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = LinguaXTextPrimary
                )
            }
        }

        ResourceContainer(
            resource = challengesResource,
            loadingText = l10n.loading,
            emptyText = l10n.noDataAvailable
        ) { challenges ->
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                challenges.forEach { challenge ->
                    val progress = (challenge.currentProgress.toFloat() / challenge.target.toFloat()).coerceIn(0f, 1f)
                    val isDone = challenge.isCompleted || progress >= 1f

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF131D2F),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2B40)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = challenge.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LinguaXTextPrimary
                                )
                                Text(
                                    text = challenge.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LinguaXTextSecondary
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    LinguaXProgressBar(
                                        progress = progress,
                                        fillBrush = if (isDone) LinguaXGreenGradient else LinguaXAccentGradient,
                                        height = 4.dp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${challenge.currentProgress}/${challenge.target}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LinguaXTextTertiary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDone) LinguaXSuccess.copy(alpha = 0.2f) else LinguaXPrimaryContainer,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = if (isDone) LinguaXSuccess else LinguaXAccentLight,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "+${challenge.rewardXp} XP",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isDone) LinguaXSuccessLight else LinguaXAccentLight
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VocabularyExplorerHeader(
    l10n: L10nStrings,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filterBookmarkedOnly: Boolean,
    onToggleFilter: (Boolean) -> Unit,
    vocabularyResource: Resource<List<VocabularyItem>>,
    onStartFlashcards: (List<VocabularyItem>) -> Unit
) {
    val vocabList = (vocabularyResource as? Resource.Success)?.data ?: emptyList()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = l10n.vocabularyTab,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = LinguaXTextPrimary
            )

            if (vocabList.isNotEmpty()) {
                Surface(
                    onClick = { onStartFlashcards(vocabList) },
                    shape = RoundedCornerShape(10.dp),
                    color = LinguaXPrimaryContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXPrimary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = LinguaXAccentLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = l10n.flashcards,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LinguaXAccentLight
                        )
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = l10n.searchVocabulary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LinguaXTextTertiary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = LinguaXAccent
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = LinguaXTextSecondary
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LinguaXSurfaceElevated,
                unfocusedContainerColor = LinguaXSurface,
                focusedBorderColor = LinguaXAccent,
                unfocusedBorderColor = LinguaXBorder,
                focusedTextColor = LinguaXTextPrimary,
                unfocusedTextColor = LinguaXTextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vocabulary_search_field")
        )

        // Filter chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !filterBookmarkedOnly,
                onClick = { onToggleFilter(false) },
                label = { Text(l10n.filterAll) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LinguaXPrimary,
                    selectedLabelColor = Color.White,
                    containerColor = LinguaXSurfaceElevated,
                    labelColor = LinguaXTextSecondary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            FilterChip(
                selected = filterBookmarkedOnly,
                onClick = { onToggleFilter(true) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                label = { Text(l10n.filterBookmarked) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LinguaXWarning,
                    selectedLabelColor = Color.Black,
                    containerColor = LinguaXSurfaceElevated,
                    labelColor = LinguaXTextSecondary
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

@Composable
private fun VocabularyListContent(
    vocabularyResource: Resource<List<VocabularyItem>>,
    searchQuery: String,
    filterBookmarkedOnly: Boolean,
    l10n: L10nStrings,
    selectedTargetLanguage: LanguageItem,
    audioPlayer: com.example.ui.audio.AudioPlayerManager,
    onToggleBookmark: (VocabularyItem) -> Unit
) {
    ResourceContainer(
        resource = vocabularyResource,
        loadingText = l10n.loading,
        emptyText = l10n.noDataAvailable
    ) { vocabList ->
        val filtered = vocabList.filter { item ->
            val matchesQuery = item.word.contains(searchQuery, ignoreCase = true) ||
                    item.translation.contains(searchQuery, ignoreCase = true)
            val matchesBookmark = if (filterBookmarkedOnly) item.isBookmarked else true
            matchesQuery && matchesBookmark
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = l10n.noDataAvailable,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LinguaXTextTertiary
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filtered.forEach { vocab ->
                    VocabularyCardItem(
                        item = vocab,
                        languageCode = selectedTargetLanguage.code,
                        audioPlayer = audioPlayer,
                        onToggleBookmark = { onToggleBookmark(vocab) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VocabularyCardItem(
    item: VocabularyItem,
    languageCode: String,
    audioPlayer: com.example.ui.audio.AudioPlayerManager,
    onToggleBookmark: () -> Unit
) {
    val isGlobalAudioPlaying by audioPlayer.isPlaying.collectAsStateWithLifecycle()
    var isThisItemPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(isGlobalAudioPlaying) {
        if (!isGlobalAudioPlaying) {
            isThisItemPlaying = false
        }
    }

    LinguaX3DCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vocab_card_${item.id}"),
        backgroundColor = Color(0xFF131C2E),
        borderBrush = LinguaXBorderGradient,
        elevation = 3.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.word,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = LinguaXTextPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = LinguaXSecondaryContainer
                    ) {
                        Text(
                            text = item.partOfSpeech,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = LinguaXSecondaryLight,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            isThisItemPlaying = true
                            audioPlayer.play(
                                text = item.word,
                                languageCode = languageCode,
                                audioUrl = item.audioUrl
                            )
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isThisItemPlaying) LinguaXAccent else Color(0xFF1C2840))
                            .testTag("pronounce_practice_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Pronounce",
                            tint = if (isThisItemPlaying) Color.Black else LinguaXAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (item.isBookmarked) LinguaXWarning.copy(alpha = 0.2f) else Color(0xFF1C2840))
                            .testTag("bookmark_practice_${item.id}")
                    ) {
                        Icon(
                            imageVector = if (item.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (item.isBookmarked) LinguaXWarning else LinguaXTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (!item.phonetic.isNullOrBlank()) {
                Text(
                    text = item.phonetic,
                    style = MaterialTheme.typography.bodySmall,
                    color = LinguaXAccentLight
                )
            }

            Text(
                text = item.translation,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = LinguaXTextSecondary
            )

            if (!item.exampleSentence.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F1826),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2B42)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"${item.exampleSentence}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = LinguaXTextTertiary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
