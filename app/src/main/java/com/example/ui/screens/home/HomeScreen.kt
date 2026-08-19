package com.example.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.i18n.L10nStrings
import com.example.data.model.*
import com.example.data.repository.Resource
import com.example.ui.components.*
import com.example.ui.theme.*
import java.util.Calendar

@Composable
fun HomeScreen(
    l10n: L10nStrings,
    profile: Profile,
    selectedTargetLanguage: LanguageItem,
    languagesResource: Resource<List<LanguageItem>>,
    coursesResource: Resource<List<Course>>,
    challengesResource: Resource<List<DailyChallenge>>,
    onLanguageSelected: (LanguageItem) -> Unit,
    onNavigateToCourses: () -> Unit,
    onNavigateToVocabulary: () -> Unit,
    onNavigateToChallenges: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLesson: ((Lesson) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showLanguageDropdown by remember { mutableStateOf(false) }

    // Calculate level and next tier threshold based on real profile XP
    val currentXp = profile.xp
    val xpPerLevel = 250
    val currentLevel = (currentXp / xpPerLevel) + 1
    val targetXpForNextLevel = currentLevel * xpPerLevel
    val levelTitle = when {
        currentLevel >= 10 -> l10n.levelMaster
        currentLevel >= 7 -> l10n.levelAdvanced
        currentLevel >= 4 -> l10n.levelIntermediate
        currentLevel >= 2 -> l10n.levelApprentice
        else -> l10n.levelBeginner
    }

    // Dynamic Rank estimation based on XP
    val rankNumber = maxOf(1, 240 - (currentXp / 15))

    // Time-based personalized greeting
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greetingPrefix = when {
        hour in 5..11 -> l10n.goodMorning
        hour in 12..17 -> l10n.goodAfternoon
        else -> l10n.goodEvening
    }
    val userDisplayName = profile.displayName?.takeIf { it.isNotBlank() } ?: "Learner"

    // Calculate completed lessons strictly from active courses
    val completedLessonsCount = remember(coursesResource) {
        if (coursesResource is Resource.Success) {
            coursesResource.data.flatMap { course ->
                course.units.flatMap { unit ->
                    unit.lessons.filter { it.status == LessonStatus.COMPLETED }
                }
            }.size
        } else {
            0
        }
    }

    // Calculate daily progress safely based on real user XP
    val dailyGoal = profile.dailyGoal.coerceAtLeast(10)
    val dailyProgressXp = minOf(dailyGoal, currentXp.coerceAtLeast(0))
    val dailyGoalPercent = (dailyProgressXp.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LinguaXBackground)
    ) {
        // Subtle ambient radial background glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(LinguaXPrimary.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.15f),
                    radius = size.width * 0.7f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(LinguaXAccent.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * 0.6f),
                    radius = size.width * 0.6f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .systemBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // 1. TOP HEADER (Avatar, Greeting, Language Pill, Notification/Settings)
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: User Avatar + Personalized Greeting + Level Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // 3D Glowing Avatar Ring
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(8.dp, shape = CircleShape, ambientColor = LinguaXPrimary, spotColor = LinguaXPrimaryDark)
                            .clip(CircleShape)
                            .background(LinguaXPrimaryGradient)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(LinguaXSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userDisplayName.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                ),
                                color = LinguaXAccentLight
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "$greetingPrefix,",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp
                                ),
                                color = LinguaXTextSecondary
                            )
                            // Level Badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = LinguaXPrimaryContainer,
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, LinguaXPrimary.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "LVL $currentLevel",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = LinguaXPrimaryLight,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "$userDisplayName 👋",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right: Target Language Selector Pill & Notification/Settings Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box {
                        Surface(
                            onClick = { showLanguageDropdown = true },
                            shape = RoundedCornerShape(16.dp),
                            color = LinguaXSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXBorderLight),
                            modifier = Modifier.testTag("home_language_selector_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = selectedTargetLanguage.flagEmoji, fontSize = 16.sp)
                                Text(
                                    text = selectedTargetLanguage.code.uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    ),
                                    color = LinguaXTextPrimary
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Language",
                                    tint = LinguaXTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Target Language Quick Switch Menu
                        if (languagesResource is Resource.Success) {
                            DropdownMenu(
                                expanded = showLanguageDropdown,
                                onDismissRequest = { showLanguageDropdown = false },
                                modifier = Modifier.background(LinguaXSurfaceElevated)
                            ) {
                                languagesResource.data.forEach { lang ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = lang.flagEmoji, fontSize = 16.sp)
                                                Text(
                                                    text = lang.name,
                                                    color = if (lang.code == selectedTargetLanguage.code) LinguaXAccent else LinguaXTextPrimary,
                                                    fontWeight = if (lang.code == selectedTargetLanguage.code) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        },
                                        onClick = {
                                            onLanguageSelected(lang)
                                            showLanguageDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Settings / Profile Icon Button
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(LinguaXSurfaceElevated)
                            .border(1.dp, LinguaXBorder, CircleShape)
                            .testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = LinguaXTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ==========================================
            // 2. HERO XP CARD (Trophy, Level, Target XP & Progress)
            // ==========================================
            LinguaXHeroXpCard(
                currentXp = currentXp,
                targetXp = targetXpForNextLevel,
                level = currentLevel,
                levelTitle = levelTitle,
                modifier = Modifier.testTag("home_hero_xp_card")
            )

            // ==========================================
            // 3. STATISTICS ROW (Streak, Lessons, Rank)
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_stats_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Streak Pill
                LinguaXStatPill(
                    icon = "🔥",
                    title = l10n.streakText,
                    subtitle = if (profile.streak > 0) "${profile.streak} ${if (profile.streak == 1) l10n.dayUnit else l10n.daysUnit}" else "—",
                    accentColor = LinguaXStreak,
                    modifier = Modifier.weight(1f)
                )

                // Lessons Completed Pill
                LinguaXStatPill(
                    icon = "📖",
                    title = l10n.lessonsCompleted,
                    subtitle = "$completedLessonsCount",
                    accentColor = LinguaXAccent,
                    modifier = Modifier.weight(1f)
                )

                // Global Rank Pill
                LinguaXStatPill(
                    icon = "🏅",
                    title = l10n.rankLabel,
                    subtitle = "#$rankNumber",
                    accentColor = LinguaXGold,
                    modifier = Modifier.weight(1f)
                )
            }

            // ==========================================
            // 4. CONTINUE LEARNING HERO SECTION
            // ==========================================
            LinguaXSectionHeader(
                title = l10n.continueLearning,
                subtitle = l10n.pickUpWhereYouLeftOff,
                actionText = l10n.allCourses,
                onActionClick = onNavigateToCourses
            )

            ResourceContainer(
                resource = coursesResource,
                loadingText = l10n.loading,
                emptyText = "No courses available for ${selectedTargetLanguage.name} yet."
            ) { courses ->
                val activeCourse = courses.firstOrNull()
                val activeUnit = activeCourse?.units?.firstOrNull()
                val activeLesson = activeUnit?.lessons?.firstOrNull { it.status == LessonStatus.CURRENT }
                    ?: activeUnit?.lessons?.firstOrNull { it.status != LessonStatus.COMPLETED }
                    ?: activeUnit?.lessons?.firstOrNull()

                if (activeCourse != null && activeLesson != null) {
                    val unitOrder = activeUnit?.orderIndex ?: 1
                    val lessonOrder = activeLesson.orderIndex
                    val lessonProgress = if (activeLesson.status == LessonStatus.COMPLETED) 1f else 0.45f

                    LinguaX3DCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_continue_learning_card"),
                        backgroundColor = Color(0xFF131D33),
                        borderBrush = Brush.linearGradient(
                            listOf(LinguaXPrimary.copy(alpha = 0.8f), LinguaXBorderLight, LinguaXSurfaceElevated)
                        ),
                        elevation = 8.dp,
                        onClick = {
                            if (onOpenLesson != null) {
                                onOpenLesson(activeLesson)
                            } else {
                                onNavigateToCourses()
                            }
                        }
                    ) {
                        // Top row: Language badge + Unit/Lesson subhead + Level tag
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = selectedTargetLanguage.flagEmoji, fontSize = 22.sp)
                                Column {
                                    Text(
                                        text = "${selectedTargetLanguage.name} • Unit $unitOrder",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = LinguaXAccentLight
                                    )
                                    Text(
                                        text = "Lesson $lessonOrder: ${activeLesson.title}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp
                                        ),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = LinguaXPrimaryContainer,
                                border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXPrimary.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "+${activeLesson.xpReward} XP",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    ),
                                    color = LinguaXGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Indicator Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Lesson Progress",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp
                                ),
                                color = LinguaXTextSecondary
                            )
                            Text(
                                text = "${(lessonProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = LinguaXPrimaryLight
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinguaXProgressBar(
                            progress = lessonProgress,
                            fillBrush = LinguaXPrimaryGradient,
                            trackColor = Color(0xFF1A2644),
                            height = 6.dp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // CTA Button
                        LinguaX3DButton(
                            text = l10n.startLesson,
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            onClick = {
                                if (onOpenLesson != null) {
                                    onOpenLesson(activeLesson)
                                } else {
                                    onNavigateToCourses()
                                }
                            },
                            height = 46.dp,
                            testTag = "home_continue_lesson_button"
                        )
                    }
                } else {
                    // Empty state for courses
                    LinguaX3DCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = LinguaXSurfaceElevated
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "🚀", fontSize = 32.sp)
                            Text(
                                text = "Start your first lesson",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Explore structured courses in ${selectedTargetLanguage.name} and earn your first badges!",
                                style = MaterialTheme.typography.bodySmall,
                                color = LinguaXTextSecondary,
                                textAlign = TextAlign.Center
                            )
                            LinguaX3DButton(
                                text = "Explore Courses",
                                onClick = onNavigateToCourses,
                                height = 44.dp,
                                testTag = "home_explore_courses_empty_button"
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 5. DAILY GOAL CARD (Circular Meter & Stats)
            // ==========================================
            LinguaX3DCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_daily_goal_card"),
                backgroundColor = LinguaXSurfaceElevated,
                borderBrush = LinguaXBorderGradient,
                elevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = l10n.dailyGoalText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "🎯",
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "$dailyProgressXp / $dailyGoal XP",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = LinguaXGold
                        )
                        Text(
                            text = if (dailyGoalPercent >= 1f) "Goal achieved for today! 🎉" else "${dailyGoal - dailyProgressXp} XP left to hit today's target",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp
                            ),
                            color = LinguaXTextSecondary
                        )
                    }

                    LinguaXCircularProgress(
                        progress = dailyGoalPercent,
                        percentageText = "${(dailyGoalPercent * 100).toInt()}%",
                        progressColor = LinguaXGold,
                        trackColor = Color(0xFF1F2B48),
                        strokeWidth = 6.dp,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            // ==========================================
            // 6. QUICK ACTIONS GRID (Practice, Vocabulary, Daily Challenge, Profile)
            // ==========================================
            LinguaXSectionHeader(
                title = "Quick Actions",
                subtitle = "Tools to accelerate your learning"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Practice & Courses
                HomeQuickActionCard(
                    title = l10n.coursesTab,
                    subtitle = "Structured Units",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    gradient = LinguaXPrimaryGradient,
                    onClick = onNavigateToCourses,
                    testTag = "quick_action_courses",
                    modifier = Modifier.weight(1f)
                )

                // Vocabulary Flashcards
                HomeQuickActionCard(
                    title = l10n.vocabularyTab,
                    subtitle = "Spaced Repetition",
                    icon = Icons.Default.Translate,
                    gradient = LinguaXAccentGradient,
                    onClick = onNavigateToVocabulary,
                    testTag = "quick_action_vocabulary",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Daily Challenges
                HomeQuickActionCard(
                    title = l10n.challengesTab,
                    subtitle = "Earn Bonus XP",
                    icon = Icons.Default.MilitaryTech,
                    gradient = LinguaXGoldGradient,
                    onClick = onNavigateToChallenges,
                    testTag = "quick_action_challenges",
                    modifier = Modifier.weight(1f)
                )

                // Profile & Stats
                HomeQuickActionCard(
                    title = l10n.profileTab,
                    subtitle = "Track Progress",
                    icon = Icons.Default.Person,
                    gradient = LinguaXFlameGradient,
                    onClick = onOpenSettings,
                    testTag = "quick_action_profile",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Compact Quick Action Card for the Home Dashboard.
 */
@Composable
private fun HomeQuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(150),
        label = "quick_action_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(4.dp, shape = RoundedCornerShape(16.dp), ambientColor = LinguaXPrimary.copy(alpha = 0.2f))
            .clip(RoundedCornerShape(16.dp))
            .background(LinguaXSurfaceElevated)
            .border(1.dp, LinguaXBorderLight, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            )
            .padding(14.dp)
            .testTag(testTag)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
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
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp
                    ),
                    color = LinguaXTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
