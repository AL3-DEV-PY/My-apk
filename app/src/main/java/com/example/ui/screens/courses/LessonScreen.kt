package com.example.ui.screens.courses

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.i18n.L10nStrings
import com.example.data.model.Exercise
import com.example.data.model.Lesson
import com.example.ui.audio.AudioPlayerManager
import com.example.ui.audio.rememberAudioPlayerManager
import com.example.ui.components.LinguaX3DButton
import com.example.ui.components.LinguaXProgressBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.LessonUiState

@Composable
fun LessonScreen(
    l10n: L10nStrings,
    lessonState: LessonUiState,
    targetLanguageCode: String = "en",
    onSelectOption: (String) -> Unit,
    onCheckAnswer: () -> Unit,
    onProceed: () -> Unit,
    onRetrySave: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showExitDialog by remember { mutableStateOf(false) }
    val audioPlayer = rememberAudioPlayerManager()

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = LinguaXSurfaceElevated,
            titleContentColor = LinguaXTextPrimary,
            textContentColor = LinguaXTextSecondary,
            title = {
                Text(
                    text = l10n.quitLessonTitle,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = l10n.quitLessonMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        audioPlayer.stop()
                        showExitDialog = false
                        onExit()
                    },
                    modifier = Modifier.testTag("confirm_exit_button")
                ) {
                    Text(l10n.quitButton, color = LinguaXError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitDialog = false },
                    modifier = Modifier.testTag("dismiss_exit_button")
                ) {
                    Text(l10n.keepLearningButton, color = LinguaXAccent, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LinguaXBackground)
            .systemBarsPadding()
    ) {
        when (lessonState) {
            is LessonUiState.LoadingExercises -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = LinguaXAccent)
                        Text(
                            text = l10n.loading,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LinguaXTextSecondary
                        )
                    }
                }
            }
            is LessonUiState.Playing -> {
                LessonPlayingContent(
                    l10n = l10n,
                    playing = lessonState,
                    targetLanguageCode = targetLanguageCode,
                    audioPlayer = audioPlayer,
                    onSelectOption = onSelectOption,
                    onCheckAnswer = onCheckAnswer,
                    onProceed = {
                        audioPlayer.stop()
                        onProceed()
                    },
                    onRequestExit = { showExitDialog = true }
                )
            }
            is LessonUiState.Completed -> {
                LessonResultContent(
                    l10n = l10n,
                    completed = lessonState,
                    onFinish = {
                        audioPlayer.stop()
                        onExit()
                    }
                )
            }
            is LessonUiState.Error -> {
                LessonErrorContent(
                    l10n = l10n,
                    error = lessonState,
                    onRetry = onRetrySave,
                    onExit = {
                        audioPlayer.stop()
                        onExit()
                    }
                )
            }
            is LessonUiState.Idle -> {
                // Inactive state
            }
        }
    }
}

@Composable
private fun LessonPlayingContent(
    l10n: L10nStrings,
    playing: LessonUiState.Playing,
    targetLanguageCode: String,
    audioPlayer: AudioPlayerManager,
    onSelectOption: (String) -> Unit,
    onCheckAnswer: () -> Unit,
    onProceed: () -> Unit,
    onRequestExit: () -> Unit
) {
    val currentExercise = playing.exercises.getOrNull(playing.currentExerciseIndex) ?: return
    val progress = (playing.currentExerciseIndex + 1).toFloat() / playing.exercises.size.toFloat()
    val scrollState = rememberScrollState()
    val isAudioPlaying by audioPlayer.isPlaying.collectAsStateWithLifecycle()

    var typedAnswer by remember(currentExercise.id) { mutableStateOf("") }

    val isListeningExercise = currentExercise.type.contains("LISTEN", ignoreCase = true)
    val isFillBlankOrType = currentExercise.type.contains("TYPE", ignoreCase = true) ||
            (currentExercise.type.contains("FILL", ignoreCase = true) && currentExercise.options.isEmpty())

    // Infinite pulse animation for active audio
    val infiniteTransition = rememberInfiniteTransition(label = "audio_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Automatically speak prompt once if it is a listening exercise
    LaunchedEffect(currentExercise.id) {
        if (isListeningExercise) {
            audioPlayer.play(
                text = currentExercise.question,
                languageCode = targetLanguageCode,
                audioUrl = currentExercise.audioUrl
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Bar: Exit button, Progress bar, Combo & Hearts
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onRequestExit,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1B263B))
                    .testTag("lesson_exit_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit Lesson",
                    tint = LinguaXTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            LinguaXProgressBar(
                progress = progress,
                fillBrush = LinguaXAccentGradient,
                height = 10.dp,
                modifier = Modifier.weight(1f)
            )

            // Combo Badge
            if (playing.combo > 1) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LinguaXWarning.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXWarning.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "🔥", fontSize = 13.sp)
                        Text(
                            text = "${playing.combo}x",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LinguaXWarning
                        )
                    }
                }
            }

            // Hearts Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(3) { index ->
                    val isFull = index < playing.lives
                    Icon(
                        imageVector = if (isFull) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Heart",
                        tint = if (isFull) LinguaXError else Color(0xFF4A5568),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Main Exercise Interactive Body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Exercise Type & Lesson Progress Meta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = LinguaXPrimary.copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXPrimary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = currentExercise.type.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LinguaXAccentLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "${playing.currentExerciseIndex + 1} / ${playing.exercises.size}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = LinguaXTextTertiary
                )
            }

            // Question Card with Pronunciation Controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF131D30),
                border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = currentExercise.question,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                lineHeight = 26.sp
                            ),
                            color = LinguaXTextPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        // Audio Controls: Normal Speed & Slow Speed
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Turtle / Slow Speed Pronunciation
                            IconButton(
                                onClick = {
                                    audioPlayer.play(
                                        text = currentExercise.question,
                                        languageCode = targetLanguageCode,
                                        isSlow = true,
                                        audioUrl = currentExercise.audioUrl
                                    )
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1C2840))
                                    .testTag("audio_slow_button")
                            ) {
                                Text(text = "🐢", fontSize = 16.sp)
                            }

                            // Normal Speed Pronunciation
                            IconButton(
                                onClick = {
                                    audioPlayer.play(
                                        text = currentExercise.question,
                                        languageCode = targetLanguageCode,
                                        isSlow = false,
                                        audioUrl = currentExercise.audioUrl
                                    )
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .scale(if (isAudioPlaying) pulseScale else 1f)
                                    .clip(CircleShape)
                                    .background(if (isAudioPlaying) LinguaXAccent else LinguaXPrimary.copy(alpha = 0.25f))
                                    .testTag("audio_play_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Listen",
                                    tint = if (isAudioPlaying) Color.Black else LinguaXAccentLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Options or Text Input
            if (isFillBlankOrType) {
                // Free text input mode
                Text(
                    text = "Type your answer:",
                    style = MaterialTheme.typography.labelMedium,
                    color = LinguaXTextSecondary
                )

                OutlinedTextField(
                    value = typedAnswer,
                    onValueChange = {
                        if (!playing.isAnswerChecked) {
                            typedAnswer = it
                            onSelectOption(it)
                        }
                    },
                    placeholder = { Text("Enter translated text...", color = LinguaXTextTertiary) },
                    enabled = !playing.isAnswerChecked,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF131E31),
                        unfocusedContainerColor = Color(0xFF131E31),
                        focusedBorderColor = LinguaXAccent,
                        unfocusedBorderColor = LinguaXBorder,
                        focusedTextColor = LinguaXTextPrimary,
                        unfocusedTextColor = LinguaXTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lesson_text_input")
                )
            } else {
                // Multiple Choice / True-False / Options mode
                Text(
                    text = "Select the correct answer:",
                    style = MaterialTheme.typography.labelMedium,
                    color = LinguaXTextSecondary
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    currentExercise.options.forEach { option ->
                        val isSelected = playing.selectedOption == option
                        val isCorrectAnswer = option.trim().equals(currentExercise.correctAnswer.trim(), ignoreCase = true)

                        val containerColor = when {
                            playing.isAnswerChecked && isCorrectAnswer -> LinguaXSuccess.copy(alpha = 0.25f)
                            playing.isAnswerChecked && isSelected && !isCorrectAnswer -> LinguaXError.copy(alpha = 0.25f)
                            isSelected -> LinguaXPrimary.copy(alpha = 0.4f)
                            else -> Color(0xFF131E31)
                        }

                        val borderColor = when {
                            playing.isAnswerChecked && isCorrectAnswer -> LinguaXSuccess
                            playing.isAnswerChecked && isSelected && !isCorrectAnswer -> LinguaXError
                            isSelected -> LinguaXAccent
                            else -> LinguaXBorder
                        }

                        Surface(
                            onClick = {
                                if (!playing.isAnswerChecked) {
                                    onSelectOption(option)
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = containerColor,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected || playing.isAnswerChecked) 2.dp else 1.dp,
                                borderColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("option_${option.take(6)}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 16.sp
                                    ),
                                    color = LinguaXTextPrimary,
                                    modifier = Modifier.weight(1f)
                                )

                                if (playing.isAnswerChecked && isCorrectAnswer) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Correct",
                                        tint = LinguaXSuccess,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else if (playing.isAnswerChecked && isSelected && !isCorrectAnswer) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "Incorrect",
                                        tint = LinguaXError,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom Feedback Bar & Action Button
        AnimatedVisibility(
            visible = playing.isAnswerChecked,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val isCorrect = playing.isCurrentAnswerCorrect
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isCorrect) Color(0xFF0C2419) else Color(0xFF2E1015),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCorrect) LinguaXSuccess.copy(alpha = 0.6f) else LinguaXError.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (isCorrect) LinguaXSuccess else LinguaXError,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isCorrect) l10n.correctMessage else l10n.incorrectMessage,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isCorrect) LinguaXSuccessLight else LinguaXErrorLight
                        )
                    }

                    if (!isCorrect) {
                        Text(
                            text = "Correct answer: ${currentExercise.correctAnswer}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = LinguaXTextPrimary
                        )
                    }

                    if (!currentExercise.explanation.isNullOrBlank()) {
                        Text(
                            text = currentExercise.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = LinguaXTextSecondary
                        )
                    }
                }
            }
        }

        // Action CTA Button
        if (!playing.isAnswerChecked) {
            LinguaX3DButton(
                text = l10n.checkAnswer,
                enabled = !playing.selectedOption.isNullOrBlank() && !playing.isSaving,
                onClick = onCheckAnswer,
                testTag = "check_answer_button"
            )
        } else {
            val isLast = playing.currentExerciseIndex + 1 >= playing.exercises.size
            LinguaX3DButton(
                text = if (playing.isSaving) l10n.savingProgress else if (isLast) l10n.finishLesson else l10n.nextQuestion,
                enabled = !playing.isSaving,
                icon = if (isLast) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                gradient = if (isLast) LinguaXGreenGradient else LinguaXPrimaryGradient,
                onClick = onProceed,
                testTag = "next_exercise_button"
            )
        }
    }
}

@Composable
private fun LessonResultContent(
    l10n: L10nStrings,
    completed: LessonUiState.Completed,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Trophy / Celebration Icon
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(LinguaXGreenGradient)
                .shadow(16.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Celebration",
                tint = Color.White,
                modifier = Modifier.size(54.dp)
            )
        }

        Text(
            text = l10n.lessonCompletedTitle,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                fontSize = 24.sp
            ),
            color = LinguaXTextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = completed.lesson.title,
            style = MaterialTheme.typography.bodyLarge,
            color = LinguaXAccentLight,
            textAlign = TextAlign.Center
        )

        // Summary Stats Grid
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF102133),
                border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXAccent.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "+${completed.xpEarned}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        ),
                        color = LinguaXAccent
                    )
                    Text(
                        text = l10n.xpEarned,
                        style = MaterialTheme.typography.labelSmall,
                        color = LinguaXTextSecondary
                    )
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF261E14),
                border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXWarning.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "+${completed.coinsEarned}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        ),
                        color = LinguaXWarning
                    )
                    Text(
                        text = l10n.coinsEarned,
                        style = MaterialTheme.typography.labelSmall,
                        color = LinguaXTextSecondary
                    )
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF0F261D),
                border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXSuccess.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${completed.accuracyPercent}%",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        ),
                        color = LinguaXSuccess
                    )
                    Text(
                        text = l10n.accuracyLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = LinguaXTextSecondary
                    )
                }
            }
        }

        // Breakdown Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = LinguaXSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Correct",
                        tint = LinguaXSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${completed.correctCount} ✓",
                        style = MaterialTheme.typography.labelMedium,
                        color = LinguaXTextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Mistakes",
                        tint = LinguaXError,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${completed.incorrectCount} ✗",
                        style = MaterialTheme.typography.labelMedium,
                        color = LinguaXTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        LinguaX3DButton(
            text = l10n.continueLearning,
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            gradient = LinguaXPrimaryGradient,
            onClick = onFinish,
            testTag = "return_dashboard_button"
        )
    }
}

@Composable
private fun LessonErrorContent(
    l10n: L10nStrings,
    error: LessonUiState.Error,
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = "Error",
            tint = LinguaXError,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Oops! Something went wrong",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = LinguaXTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error.message,
            style = MaterialTheme.typography.bodyMedium,
            color = LinguaXTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        LinguaX3DButton(
            text = "Retry",
            icon = Icons.Default.Refresh,
            onClick = onRetry,
            testTag = "retry_lesson_button"
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onExit,
            modifier = Modifier.testTag("exit_error_button")
        ) {
            Text("Cancel and Exit", color = LinguaXTextTertiary)
        }
    }
}
