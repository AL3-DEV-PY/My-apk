package com.example.ui.screens.vocabulary

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.i18n.L10nStrings
import com.example.data.model.LanguageItem
import com.example.data.model.VocabularyItem
import com.example.data.repository.Resource
import com.example.ui.audio.AudioPlayerManager
import com.example.ui.audio.rememberAudioPlayerManager
import com.example.ui.components.LinguaX3DCard
import com.example.ui.components.LinguaXHeader
import com.example.ui.components.ResourceContainer
import com.example.ui.theme.*

@Composable
fun VocabularyScreen(
    l10n: L10nStrings,
    selectedTargetLanguage: LanguageItem,
    vocabularyResource: Resource<List<VocabularyItem>>,
    onToggleBookmark: (VocabularyItem) -> Unit,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterBookmarkedOnly by remember { mutableStateOf(false) }
    val audioPlayer = rememberAudioPlayerManager()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinguaXBackground)
            .padding(16.dp)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LinguaXHeader(
            title = l10n.vocabularyTab,
            subtitle = "${selectedTargetLanguage.flagEmoji} ${selectedTargetLanguage.name} • Flashcards & Pronunciation"
        )

        // 3D Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
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
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = LinguaXTextSecondary
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
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

        // Filter Chips Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = !filterBookmarkedOnly,
                onClick = { filterBookmarkedOnly = false },
                label = { Text(l10n.filterAll) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LinguaXPrimary,
                    selectedLabelColor = Color.White,
                    containerColor = LinguaXSurfaceElevated,
                    labelColor = LinguaXTextSecondary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            FilterChip(
                selected = filterBookmarkedOnly,
                onClick = { filterBookmarkedOnly = true },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                label = { Text(l10n.filterBookmarked) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LinguaXWarning,
                    selectedLabelColor = Color.Black,
                    containerColor = LinguaXSurfaceElevated,
                    labelColor = LinguaXTextSecondary
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Vocabulary Flashcards List
        ResourceContainer(
            resource = vocabularyResource,
            loadingText = l10n.loading,
            emptyText = l10n.noDataAvailable,
            onRetry = onRetry
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
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered, key = { it.id }) { vocab ->
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
}

@Composable
fun VocabularyCardItem(
    item: VocabularyItem,
    languageCode: String,
    audioPlayer: AudioPlayerManager,
    onToggleBookmark: () -> Unit
) {
    val isGlobalAudioPlaying by audioPlayer.isPlaying.collectAsStateWithLifecycle()
    var isThisItemPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(isGlobalAudioPlaying) {
        if (!isGlobalAudioPlaying) {
            isThisItemPlaying = false
        }
    }

    val pulseScale by animateFloatAsState(
        targetValue = if (isThisItemPlaying) 1.15f else 1f,
        animationSpec = tween(300),
        label = "vocab_pulse"
    )

    LinguaX3DCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vocab_item_${item.id}"),
        backgroundColor = Color(0xFF131C2E),
        borderBrush = LinguaXBorderGradient,
        elevation = 4.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row with Word, Part of Speech pill & Bookmark Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = item.word,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        ),
                        color = LinguaXTextPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LinguaXSecondaryContainer
                    ) {
                        Text(
                            text = item.partOfSpeech,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = LinguaXSecondaryLight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Audio pronunciation button
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
                            .size(36.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(if (isThisItemPlaying) LinguaXAccent else Color(0xFF1C2840))
                            .testTag("pronounce_vocab_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Pronounce",
                            tint = if (isThisItemPlaying) Color.Black else LinguaXAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Bookmark toggle
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (item.isBookmarked) LinguaXWarning.copy(alpha = 0.2f) else Color(0xFF1C2840))
                            .testTag("bookmark_vocab_${item.id}")
                    ) {
                        Icon(
                            imageVector = if (item.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (item.isBookmarked) LinguaXWarning else LinguaXTextSecondary,
                            modifier = Modifier.size(18.dp)
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
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = LinguaXTextSecondary
            )

            if (!item.exampleSentence.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F1826),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2B42)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"${item.exampleSentence}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = LinguaXTextTertiary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}
