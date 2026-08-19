package com.example.ui.screens.practice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.i18n.L10nStrings
import com.example.data.model.VocabularyItem
import com.example.ui.audio.AudioPlayerManager
import com.example.ui.audio.rememberAudioPlayerManager
import com.example.ui.components.LinguaX3DCard
import com.example.ui.components.LinguaXProgressBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlashcardUiState

@Composable
fun FlashcardReviewScreen(
    l10n: L10nStrings,
    flashcardState: FlashcardUiState,
    targetLanguageCode: String = "en",
    onFlip: () -> Unit,
    onRate: (known: Boolean) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val audioPlayer = rememberAudioPlayerManager()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LinguaXBackground)
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        when (flashcardState) {
            is FlashcardUiState.Reviewing -> {
                val item = flashcardState.items.getOrNull(flashcardState.currentIndex)
                if (item != null) {
                    FlashcardActiveView(
                        item = item,
                        currentIndex = flashcardState.currentIndex,
                        totalCount = flashcardState.items.size,
                        isFlipped = flashcardState.isFlipped,
                        isSmartReview = flashcardState.isSmartReview,
                        targetLanguageCode = targetLanguageCode,
                        audioPlayer = audioPlayer,
                        l10n = l10n,
                        onFlip = onFlip,
                        onRate = {
                            audioPlayer.stop()
                            onRate(it)
                        },
                        onExit = {
                            audioPlayer.stop()
                            onExit()
                        }
                    )
                }
            }
            is FlashcardUiState.Completed -> {
                FlashcardCompletedView(
                    completedState = flashcardState,
                    l10n = l10n,
                    onDone = {
                        audioPlayer.stop()
                        onExit()
                    }
                )
            }
            is FlashcardUiState.Idle -> {
                // Inactive state
            }
        }
    }
}

@Composable
private fun FlashcardActiveView(
    item: VocabularyItem,
    currentIndex: Int,
    totalCount: Int,
    isFlipped: Boolean,
    isSmartReview: Boolean,
    targetLanguageCode: String,
    audioPlayer: AudioPlayerManager,
    l10n: L10nStrings,
    onFlip: () -> Unit,
    onRate: (known: Boolean) -> Unit,
    onExit: () -> Unit
) {
    val progress = (currentIndex.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
    val isPlaying by audioPlayer.isPlaying.collectAsStateWithLifecycle()

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "flashcard_flip"
    )

    val pulseScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.15f else 1f,
        animationSpec = tween(300),
        label = "flashcard_audio_pulse"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header Bar
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onExit,
                    modifier = Modifier.testTag("flashcard_exit_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Exit Flashcards",
                        tint = LinguaXTextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSmartReview) LinguaXSecondaryContainer else LinguaXPrimaryContainer
                ) {
                    Text(
                        text = if (isSmartReview) l10n.smartReview else l10n.flashcards,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isSmartReview) LinguaXSecondaryLight else LinguaXAccentLight,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = "${currentIndex + 1} / $totalCount",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = LinguaXTextSecondary
                )
            }

            LinguaXProgressBar(
                progress = progress,
                fillBrush = LinguaXAccentGradient,
                height = 6.dp
            )
        }

        // Center 3D Flippable Flashcard
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            LinguaX3DCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onFlip
                    )
                    .testTag("flashcard_interactive_surface"),
                backgroundColor = Color(0xFF131D2F),
                borderBrush = if (isFlipped) LinguaXAccentGradient else LinguaXBorderGradient,
                elevation = 8.dp
            ) {
                // Determine whether to show front or back depending on rotation degree
                if (rotation <= 90f) {
                    // Front of Card (Word, Phonetic, Part of Speech, Audio)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = LinguaXPrimaryContainer
                            ) {
                                Text(
                                    text = item.partOfSpeech,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LinguaXAccentLight,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Slow audio button
                                IconButton(
                                    onClick = {
                                        audioPlayer.play(
                                            text = item.word,
                                            languageCode = targetLanguageCode,
                                            isSlow = true,
                                            audioUrl = item.audioUrl
                                        )
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1C2840))
                                        .testTag("flashcard_slow_audio_btn")
                                ) {
                                    Text(text = "🐢", fontSize = 14.sp)
                                }

                                // Normal audio button
                                IconButton(
                                    onClick = {
                                        audioPlayer.play(
                                            text = item.word,
                                            languageCode = targetLanguageCode,
                                            isSlow = false,
                                            audioUrl = item.audioUrl
                                        )
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .scale(pulseScale)
                                        .clip(CircleShape)
                                        .background(if (isPlaying) LinguaXAccent else Color(0xFF1B2840))
                                        .testTag("flashcard_pronounce_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Pronounce Word",
                                        tint = if (isPlaying) Color.Black else LinguaXAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = item.word,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 32.sp
                                ),
                                color = LinguaXTextPrimary,
                                textAlign = TextAlign.Center
                            )

                            if (!item.phonetic.isNullOrBlank()) {
                                Text(
                                    text = item.phonetic,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LinguaXTextTertiary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = LinguaXAccentLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = l10n.tapToReveal,
                                style = MaterialTheme.typography.labelSmall,
                                color = LinguaXAccentLight
                            )
                        }
                    }
                } else {
                    // Back of Card (Flipped: Meaning, Translation, Example sentence)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f }
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = LinguaXSecondaryContainer
                            ) {
                                Text(
                                    text = l10n.vocabularyTab,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LinguaXSecondaryLight,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    audioPlayer.play(
                                        text = item.word,
                                        languageCode = targetLanguageCode,
                                        isSlow = false,
                                        audioUrl = item.audioUrl
                                    )
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) LinguaXAccent else Color(0xFF1B2840))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Pronounce Word",
                                    tint = if (isPlaying) Color.Black else LinguaXAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = item.translation,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 28.sp
                                ),
                                color = LinguaXTextPrimary,
                                textAlign = TextAlign.Center
                            )

                            if (!item.exampleSentence.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF0F1826),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2E47)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "\"${item.exampleSentence}\"",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        ),
                                        color = LinguaXTextSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Rate your confidence below",
                            style = MaterialTheme.typography.labelSmall,
                            color = LinguaXTextTertiary
                        )
                    }
                }
            }
        }

        // Bottom Action Buttons (Rate confidence)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onRate(false) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("flashcard_need_practice_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LinguaXWarning.copy(alpha = 0.18f),
                    contentColor = LinguaXWarning
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXWarning.copy(alpha = 0.6f))
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        text = l10n.needPractice,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Button(
                onClick = { onRate(true) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("flashcard_mastered_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LinguaXSuccess,
                    contentColor = Color.White
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        text = l10n.masteredWord,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun FlashcardCompletedView(
    completedState: FlashcardUiState.Completed,
    l10n: L10nStrings,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LinguaX3DCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF131D2F),
            borderBrush = LinguaXBorderGradient,
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(LinguaXGreenGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Text(
                    text = l10n.cardsReviewedSummary,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = LinguaXTextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = l10n.lessonCompletedTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LinguaXTextSecondary,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = LinguaXSuccess.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXSuccess.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${completedState.knownCount}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = LinguaXSuccessLight
                            )
                            Text(
                                text = l10n.masteredWord,
                                style = MaterialTheme.typography.labelSmall,
                                color = LinguaXSuccessLight
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = LinguaXWarning.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXWarning.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${completedState.reviewCount}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = LinguaXWarning
                            )
                            Text(
                                text = l10n.needPractice,
                                style = MaterialTheme.typography.labelSmall,
                                color = LinguaXWarning
                            )
                        }
                    }
                }

                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("flashcard_done_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LinguaXPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = l10n.practiceTab,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
