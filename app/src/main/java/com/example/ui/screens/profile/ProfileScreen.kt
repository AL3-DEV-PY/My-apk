package com.example.ui.screens.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.i18n.AppLanguage
import com.example.data.i18n.L10nStrings
import com.example.data.model.AchievementItem
import com.example.data.model.LanguageItem
import com.example.data.model.Profile
import com.example.data.repository.Resource
import com.example.ui.components.LinguaX3DButton
import com.example.ui.components.LinguaX3DCard
import com.example.ui.components.LinguaXAchievementBadge
import com.example.ui.components.LinguaXHeader
import com.example.ui.components.ResourceContainer
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    l10n: L10nStrings,
    profile: Profile,
    currentAppLanguage: AppLanguage,
    selectedTargetLanguage: LanguageItem,
    achievementsResource: Resource<List<AchievementItem>>,
    onAppLanguageChange: (AppLanguage) -> Unit,
    onUpdateProfile: (String, Int) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var displayName by remember(profile.displayName) { mutableStateOf(profile.displayName ?: "Learner") }
    var dailyGoal by remember(profile.dailyGoal) { mutableIntStateOf(profile.dailyGoal) }

    // Dialog / Sheet states for settings
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showDailyGoalDialog by remember { mutableStateOf(false) }

    // Level calculation from XP (every 100 XP is 1 level)
    val currentLevel = maxOf(1, (profile.xp / 100) + 1)
    val currentLevelProgressXp = profile.xp % 100
    val levelProgress = (currentLevelProgressXp.toFloat() / 100f).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LinguaXBackground)
    ) {
        // Futuristic Ambient Canvas Glows
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(LinguaXPrimary.copy(alpha = 0.14f), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.15f),
                    radius = size.width * 0.6f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(LinguaXAccent.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.7f),
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
            // Screen Header
            LinguaXHeader(
                title = l10n.profileTab,
                subtitle = "Track your learning journey, achievements, and stats"
            )

            // ==========================================
            // 1. HERO USER CARD (Avatar, Level Ring, XP Progress)
            // ==========================================
            LinguaX3DCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_hero_card"),
                backgroundColor = Color(0xFF131C2E),
                borderBrush = Brush.linearGradient(
                    listOf(LinguaXPrimary.copy(alpha = 0.7f), LinguaXBorderLight, LinguaXSurfaceElevated)
                ),
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Glowing 3D Avatar
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .shadow(8.dp, shape = CircleShape, ambientColor = LinguaXPrimary, spotColor = LinguaXPrimaryDark)
                                .clip(CircleShape)
                                .background(LinguaXPrimaryGradient)
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(LinguaXSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayName.take(1).uppercase(),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 28.sp
                                    ),
                                    color = LinguaXAccentLight
                                )
                            }
                        }

                        // Display name, username, and level badge
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp
                                    ),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = LinguaXPrimaryContainer,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXPrimary.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "LVL $currentLevel",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp
                                        ),
                                        color = LinguaXPrimaryLight,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "@${profile.username ?: "user_${profile.id.take(4)}"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = LinguaXTextTertiary
                            )

                            // Account Saved Status Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = LinguaXSuccess.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, LinguaXSuccess.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(LinguaXSuccess)
                                    )
                                    Text(
                                        text = l10n.accountSaved,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        ),
                                        color = LinguaXSuccess
                                    )
                                }
                            }
                        }
                    }

                    // Level Progress Meter
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${l10n.levelProgressTitle} ($currentLevel)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = LinguaXTextSecondary
                            )
                            Text(
                                text = "$currentLevelProgressXp / 100 XP",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = LinguaXGold
                            )
                        }

                        LinearProgressIndicator(
                            progress = { levelProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = LinguaXGold,
                            trackColor = Color(0xFF1B263E),
                        )
                    }
                }
            }

            // ==========================================
            // 2. STATISTICS GRID (Total XP, Streak, Coins, Daily Goal)
            // ==========================================
            Text(
                text = l10n.statisticsHeader,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = Color.White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileStatCard(
                    title = l10n.totalXpEarned,
                    value = "${profile.xp}",
                    icon = Icons.Default.Bolt,
                    accentColor = LinguaXGold,
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    title = l10n.streakText,
                    value = "${profile.streak} ${if (profile.streak == 1) l10n.dayUnit else l10n.daysUnit}",
                    icon = Icons.Default.LocalFireDepartment,
                    accentColor = LinguaXFlame,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileStatCard(
                    title = l10n.coinsText,
                    value = "${profile.coins}",
                    icon = Icons.Default.MonetizationOn,
                    accentColor = Color(0xFFFFD54F),
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    title = l10n.dailyGoalText,
                    value = "${profile.dailyGoal} XP",
                    icon = Icons.Default.TrackChanges,
                    accentColor = LinguaXAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            // ==========================================
            // 3. ACHIEVEMENTS BADGES SECTION
            // ==========================================
            Text(
                text = l10n.unlockedAchievements,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = Color.White
            )

            LinguaX3DCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFF131C2E)
            ) {
                ResourceContainer(
                    resource = achievementsResource,
                    loadingText = l10n.loading,
                    emptyText = l10n.noDataAvailable
                ) { achievementsList: List<AchievementItem> ->
                    if (achievementsList.isEmpty()) {
                        Text(
                            text = l10n.noDataAvailable,
                            style = MaterialTheme.typography.bodySmall,
                            color = LinguaXTextTertiary
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(achievementsList, key = { it.id }) { ach: AchievementItem ->
                                LinguaXAchievementBadge(
                                    title = ach.title,
                                    icon = when (ach.iconName) {
                                        "fire" -> "🔥"
                                        "globe" -> "🌐"
                                        "trophy" -> "🏆"
                                        "book" -> "📚"
                                        else -> "⭐"
                                    },
                                    gradient = if (ach.isUnlocked) LinguaXGoldGradient else Brush.linearGradient(listOf(Color(0xFF202C45), Color(0xFF141C30))),
                                    isUnlocked = ach.isUnlocked
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 4. ACCOUNT SETTINGS & PREFERENCES MENU
            // ==========================================
            Text(
                text = l10n.accountSettings,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = Color.White
            )

            LinguaX3DCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFF131C2E)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // App Language Selector Row
                    ProfileMenuRow(
                        icon = Icons.Default.Translate,
                        title = l10n.appLanguage,
                        subtitle = "${currentAppLanguage.displayName} (${currentAppLanguage.nativeName})",
                        onClick = { showLanguageDialog = true },
                        testTag = "menu_app_language"
                    )

                    HorizontalDivider(color = LinguaXBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                    // Daily Goal Selector Row
                    ProfileMenuRow(
                        icon = Icons.Default.TrackChanges,
                        title = l10n.dailyGoalText,
                        subtitle = "$dailyGoal XP / day",
                        onClick = { showDailyGoalDialog = true },
                        testTag = "menu_daily_goal"
                    )

                    HorizontalDivider(color = LinguaXBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                    // Edit Display Name Row
                    ProfileMenuRow(
                        icon = Icons.Default.Edit,
                        title = l10n.editName,
                        subtitle = displayName,
                        onClick = { showEditProfileDialog = true },
                        testTag = "menu_edit_profile"
                    )
                }
            }

            // ==========================================
            // 5. SECURE LOGOUT BUTTON
            // ==========================================
            LinguaX3DButton(
                text = l10n.logout,
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                gradient = Brush.linearGradient(listOf(Color(0xFF3B1820), Color(0xFF260D13))),
                textColor = LinguaXErrorLight,
                onClick = onLogout,
                height = 50.dp,
                testTag = "logout_button"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // ==========================================
        // DIALOGS & ACTION SHEETS
        // ==========================================

        // 1. Language Selection Dialog
        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = {
                    Text(
                        text = l10n.selectInterfaceLanguage,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppLanguage.supportedLanguages.forEach { lang ->
                            val isSelected = currentAppLanguage == lang
                            Surface(
                                onClick = {
                                    onAppLanguageChange(lang)
                                    showLanguageDialog = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) LinguaXPrimaryContainer else Color(0xFF162136),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) LinguaXPrimary else Color(0xFF243552)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(text = lang.flag, fontSize = 20.sp)
                                        Text(
                                            text = "${lang.displayName} (${lang.nativeName})",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) Color.White else LinguaXTextPrimary
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = LinguaXAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text(l10n.saveChanges, color = LinguaXPrimaryLight)
                    }
                },
                containerColor = Color(0xFF131C2E)
            )
        }

        // 2. Daily Goal Selection Dialog
        if (showDailyGoalDialog) {
            AlertDialog(
                onDismissRequest = { showDailyGoalDialog = false },
                title = {
                    Text(
                        text = l10n.dailyGoalText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(15 to "15 XP", 30 to "30 XP", 50 to "50 XP").forEach { (goal, label) ->
                            val isSelected = dailyGoal == goal
                            Surface(
                                onClick = {
                                    dailyGoal = goal
                                    onUpdateProfile(displayName, goal)
                                    showDailyGoalDialog = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) LinguaXPrimary else Color(0xFF162136),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) Color.White else LinguaXTextPrimary
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDailyGoalDialog = false }) {
                        Text(l10n.saveChanges, color = LinguaXTextSecondary)
                    }
                },
                containerColor = Color(0xFF131C2E)
            )
        }

        // 3. Edit Profile Dialog
        if (showEditProfileDialog) {
            var tempName by remember { mutableStateOf(displayName) }

            AlertDialog(
                onDismissRequest = { showEditProfileDialog = false },
                title = {
                    Text(
                        text = l10n.editName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                },
                text = {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text(l10n.displayNameLabel) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF162136),
                            unfocusedContainerColor = Color(0xFF111726),
                            focusedBorderColor = LinguaXAccent,
                            unfocusedBorderColor = LinguaXBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (tempName.isNotBlank()) {
                                displayName = tempName.trim()
                                onUpdateProfile(displayName, dailyGoal)
                            }
                            showEditProfileDialog = false
                        }
                    ) {
                        Text(l10n.saveChanges, color = LinguaXAccent, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditProfileDialog = false }) {
                        Text(l10n.quitButton, color = LinguaXTextSecondary)
                    }
                },
                containerColor = Color(0xFF131C2E)
            )
        }
    }
}

@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF131C2E),
        border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp
                    ),
                    color = LinguaXTextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
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
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LinguaXPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = LinguaXPrimaryLight,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = LinguaXTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = LinguaXTextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
