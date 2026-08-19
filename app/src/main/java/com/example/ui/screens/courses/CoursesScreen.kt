package com.example.ui.screens.courses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.i18n.L10nStrings
import com.example.data.model.*
import com.example.data.repository.Resource
import com.example.ui.components.LinguaX3DCard
import com.example.ui.components.LinguaXHeader
import com.example.ui.components.LinguaXProgressBar
import com.example.ui.components.ResourceContainer
import com.example.ui.theme.*

@Composable
fun CoursesScreen(
    l10n: L10nStrings,
    selectedTargetLanguage: LanguageItem,
    languagesResource: Resource<List<LanguageItem>>,
    coursesResource: Resource<List<Course>>,
    onLanguageSelected: (LanguageItem) -> Unit,
    onLessonClicked: (Lesson) -> Unit,
    onRetryLanguages: (() -> Unit)? = null,
    onRetryCourses: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedLevelFilter by remember { mutableStateOf("ALL") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinguaXBackground)
            .padding(16.dp)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LinguaXHeader(
            title = l10n.coursesTab,
            subtitle = "${selectedTargetLanguage.flagEmoji} ${selectedTargetLanguage.name} • ${l10n.allCourses}"
        )

        // 3D Horizontal Language Switcher
        ResourceContainer(
            resource = languagesResource,
            loadingText = l10n.loading,
            emptyText = l10n.noDataAvailable,
            onRetry = onRetryLanguages
        ) { languages ->
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(languages) { lang ->
                    val isSelected = lang.code.equals(selectedTargetLanguage.code, ignoreCase = true)
                    Surface(
                        onClick = { onLanguageSelected(lang) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) LinguaXPrimary else Color(0xFF131C2E),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) LinguaXAccent else LinguaXBorder
                        ),
                        modifier = Modifier.testTag("lang_chip_${lang.code}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = lang.flagEmoji, fontSize = 18.sp)
                            Column {
                                Text(
                                    text = lang.name,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color.White else LinguaXTextPrimary
                                )
                                if (!lang.nativeName.isNullOrBlank()) {
                                    Text(
                                        text = lang.nativeName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else LinguaXTextTertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Level Filter Chips
        val filterOptions = listOf(
            "ALL" to l10n.allLevelsFilter,
            "BEGINNER" to l10n.beginnerFilter,
            "INTERMEDIATE" to l10n.intermediateFilter,
            "ADVANCED" to l10n.advancedFilter
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filterOptions) { (key, label) ->
                val isSelected = selectedLevelFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedLevelFilter = key },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LinguaXPrimaryContainer,
                        selectedLabelColor = LinguaXAccentLight,
                        containerColor = Color(0xFF131D2F),
                        labelColor = LinguaXTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) LinguaXAccent else Color(0xFF202C42)
                    ),
                    modifier = Modifier.testTag("filter_chip_$key")
                )
            }
        }

        // Dynamic Course Hierarchy (Courses -> Units -> Lessons)
        ResourceContainer(
            resource = coursesResource,
            loadingText = l10n.loading,
            emptyText = l10n.noDataAvailable,
            onRetry = onRetryCourses
        ) { courses ->
            val filteredCourses = remember(courses, selectedLevelFilter) {
                if (selectedLevelFilter == "ALL") courses
                else courses.filter {
                    when (selectedLevelFilter) {
                        "BEGINNER" -> it.level.contains("A1", ignoreCase = true) || it.level.contains("A2", ignoreCase = true) || it.level.contains("Beginner", ignoreCase = true)
                        "INTERMEDIATE" -> it.level.contains("B1", ignoreCase = true) || it.level.contains("B2", ignoreCase = true) || it.level.contains("Intermediate", ignoreCase = true)
                        "ADVANCED" -> it.level.contains("C1", ignoreCase = true) || it.level.contains("C2", ignoreCase = true) || it.level.contains("Advanced", ignoreCase = true)
                        else -> true
                    }
                }
            }

            if (filteredCourses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = LinguaXTextTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = l10n.noCoursesMatch,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LinguaXTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredCourses) { course ->
                        CourseCardView(
                            course = course,
                            l10n = l10n,
                            onLessonClicked = onLessonClicked
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CourseCardView(
    course: Course,
    l10n: L10nStrings,
    onLessonClicked: (Lesson) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    // Calculate total lessons and completed lessons
    val allLessons = course.units.flatMap { it.lessons }
    val completedCount = allLessons.count { it.status == LessonStatus.COMPLETED }
    val progress = if (allLessons.isNotEmpty()) completedCount.toFloat() / allLessons.size.toFloat() else 0f

    LinguaX3DCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF121B2C),
        borderBrush = LinguaXBorderGradient,
        elevation = 6.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header Row: Level Badge + Units Count / Collapse Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = LinguaXPrimaryContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXPrimary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = course.level,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LinguaXAccentLight,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isExpanded = !isExpanded }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${course.units.size} ${l10n.unitsCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = LinguaXTextSecondary
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle",
                        tint = LinguaXAccentLight
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 19.sp
                    ),
                    color = LinguaXTextPrimary
                )

                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LinguaXTextSecondary
                )
            }

            // Course Overall Progress
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Course Progress",
                        style = MaterialTheme.typography.labelSmall,
                        color = LinguaXTextTertiary
                    )
                    Text(
                        text = "$completedCount / ${allLessons.size} Lessons (${(progress * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LinguaXAccentLight
                    )
                }
                LinguaXProgressBar(
                    progress = progress,
                    fillBrush = if (progress >= 1f) LinguaXGreenGradient else LinguaXAccentGradient,
                    height = 6.dp
                )
            }

            // Units & Lessons Dropdown Accordion
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    course.units.forEach { unit ->
                        UnitSectionView(
                            unit = unit,
                            l10n = l10n,
                            onLessonClicked = onLessonClicked
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UnitSectionView(
    unit: UnitItem,
    l10n: L10nStrings,
    onLessonClicked: (Lesson) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF162136))
            .border(1.dp, Color(0xFF243552), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(LinguaXAccent)
            )
            Text(
                text = unit.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = LinguaXAccentLight
            )
        }

        if (unit.description.isNotBlank()) {
            Text(
                text = unit.description,
                style = MaterialTheme.typography.bodySmall,
                color = LinguaXTextSecondary
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            unit.lessons.forEach { lesson ->
                LessonItemRow(
                    lesson = lesson,
                    l10n = l10n,
                    onClick = { onLessonClicked(lesson) }
                )
            }
        }
    }
}

@Composable
fun LessonItemRow(
    lesson: Lesson,
    l10n: L10nStrings,
    onClick: () -> Unit
) {
    val isLocked = lesson.status == LessonStatus.LOCKED
    val isCompleted = lesson.status == LessonStatus.COMPLETED

    Surface(
        onClick = {
            if (!isLocked) {
                onClick()
            }
        },
        shape = RoundedCornerShape(14.dp),
        color = when {
            isCompleted -> Color(0xFF132822)
            !isLocked -> Color(0xFF1A2840)
            else -> Color(0xFF121824)
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                isCompleted -> LinguaXSuccess.copy(alpha = 0.5f)
                !isLocked -> LinguaXPrimary.copy(alpha = 0.5f)
                else -> Color(0xFF1E2838)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lesson_row_${lesson.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> LinguaXGreenGradient
                                !isLocked -> LinguaXPrimaryGradient
                                else -> Brush.linearGradient(listOf(Color(0xFF23324D), Color(0xFF1A2436)))
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            isCompleted -> Icons.Default.Check
                            !isLocked -> Icons.Default.PlayArrow
                            else -> Icons.Default.Lock
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = if (isLocked) LinguaXTextTertiary else LinguaXTextPrimary
                    )
                    Text(
                        text = "${lesson.durationMins} min • +${lesson.xpReward} XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCompleted) LinguaXSuccessLight else LinguaXTextSecondary
                    )
                }
            }

            if (!isLocked) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCompleted) LinguaXSuccess.copy(alpha = 0.2f) else LinguaXPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isCompleted) l10n.completedLesson else l10n.startLesson,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
