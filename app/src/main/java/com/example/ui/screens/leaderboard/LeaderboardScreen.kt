package com.example.ui.screens.leaderboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.i18n.L10nStrings
import com.example.data.model.LanguageItem
import com.example.data.model.LeaderboardEntry
import com.example.data.model.Profile
import com.example.data.repository.Resource
import com.example.ui.components.LinguaX3DCard
import com.example.ui.components.ResourceContainer
import com.example.ui.theme.*

enum class LeaderboardFilter {
    GLOBAL,
    FRIENDS,
    COUNTRY
}

@Composable
fun LeaderboardScreen(
    l10n: L10nStrings,
    profile: Profile,
    selectedTargetLanguage: LanguageItem,
    leaderboardResource: Resource<List<LeaderboardEntry>>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(LeaderboardFilter.GLOBAL) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LinguaXBackground)
    ) {
        // Ambient Futuristic Glows
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(LinguaXGold.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.1f),
                    radius = size.width * 0.6f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(LinguaXPrimary.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.45f),
                    radius = size.width * 0.5f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .systemBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ==========================================
            // 1. LEADERBOARD HEADER
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = l10n.leaderboardTab,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            ),
                            color = LinguaXTextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LinguaXGold.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXGold.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "🏆",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${selectedTargetLanguage.name} • ${profile.xp} XP",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LinguaXTextSecondary
                    )
                }

                // Target Language Flag Pill
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = LinguaXSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXBorderLight)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = selectedTargetLanguage.flagEmoji, fontSize = 16.sp)
                        Text(
                            text = selectedTargetLanguage.code.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LinguaXTextPrimary
                        )
                    }
                }
            }

            // ==========================================
            // 2. FILTER TABS (Global, Friends, Country)
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF11192C))
                    .border(1.dp, LinguaXBorder, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LeaderboardFilterChip(
                    title = l10n.globalRank,
                    icon = Icons.Default.Public,
                    isSelected = selectedFilter == LeaderboardFilter.GLOBAL,
                    onClick = { selectedFilter = LeaderboardFilter.GLOBAL },
                    modifier = Modifier.weight(1f),
                    testTag = "tab_filter_global"
                )
                LeaderboardFilterChip(
                    title = l10n.friendsRank,
                    icon = Icons.Default.Group,
                    isSelected = selectedFilter == LeaderboardFilter.FRIENDS,
                    onClick = { selectedFilter = LeaderboardFilter.FRIENDS },
                    modifier = Modifier.weight(1f),
                    testTag = "tab_filter_friends"
                )
                LeaderboardFilterChip(
                    title = l10n.countryRank,
                    icon = Icons.Default.Flag,
                    isSelected = selectedFilter == LeaderboardFilter.COUNTRY,
                    onClick = { selectedFilter = LeaderboardFilter.COUNTRY },
                    modifier = Modifier.weight(1f),
                    testTag = "tab_filter_country"
                )
            }

            // Handle Filter Data States
            if (selectedFilter != LeaderboardFilter.GLOBAL) {
                // Empty state for unsupported Friends/Country tab without fabricating fake data
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = LinguaXPrimaryContainer,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (selectedFilter == LeaderboardFilter.FRIENDS) Icons.Default.GroupAdd else Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = LinguaXPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Text(
                            text = if (selectedFilter == LeaderboardFilter.FRIENDS) l10n.friendsRank else l10n.countryRank,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = LinguaXTextPrimary
                        )
                        Text(
                            text = l10n.filterUnsupported,
                            style = MaterialTheme.typography.bodySmall,
                            color = LinguaXTextTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // ==========================================
                // 3 & 4. PODIUM & RANKED LIST
                // ==========================================
                ResourceContainer(
                    resource = leaderboardResource,
                    loadingText = l10n.loading,
                    emptyText = l10n.emptyLeaderboard,
                    onRetry = onRetry
                ) { allEntries: List<LeaderboardEntry> ->
                    if (allEntries.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = l10n.emptyLeaderboard,
                                style = MaterialTheme.typography.bodyMedium,
                                color = LinguaXTextTertiary,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        val top3 = allEntries.take(3)
                        val remaining = if (allEntries.size > 3) allEntries.drop(3) else emptyList()

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            // Top 3 Podium Section
                            if (top3.isNotEmpty()) {
                                item {
                                    LeaderboardPodium(
                                        topEntries = top3,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    )
                                }
                            }

                            // Ranked List (Remaining Users)
                            items(remaining, key = { it.id }) { entry: LeaderboardEntry ->
                                LeaderboardRow(
                                    entry = entry,
                                    l10n = l10n,
                                    isCurrentUser = entry.isCurrentUser || entry.id == profile.id
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardFilterChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) LinguaXPrimary else Color.Transparent,
        modifier = modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else LinguaXTextSecondary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                ),
                color = if (isSelected) Color.White else LinguaXTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LeaderboardPodium(
    topEntries: List<LeaderboardEntry>,
    modifier: Modifier = Modifier
) {
    val first = topEntries.getOrNull(0)
    val second = topEntries.getOrNull(1)
    val third = topEntries.getOrNull(2)

    LinguaX3DCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF131C2E),
        borderBrush = Brush.linearGradient(
            listOf(LinguaXGold.copy(alpha = 0.6f), LinguaXBorderLight, LinguaXPrimary.copy(alpha = 0.4f))
        ),
        elevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // 2nd Place (Silver)
            if (second != null) {
                PodiumColumn(
                    entry = second,
                    rank = 2,
                    podiumHeight = 84.dp,
                    medal = "🥈",
                    accentColor = Color(0xFFC0C0C0),
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // 1st Place (Gold - Taller & Centered)
            if (first != null) {
                PodiumColumn(
                    entry = first,
                    rank = 1,
                    podiumHeight = 110.dp,
                    medal = "👑",
                    accentColor = LinguaXGold,
                    isChampion = true,
                    modifier = Modifier.weight(1.15f)
                )
            }

            // 3rd Place (Bronze)
            if (third != null) {
                PodiumColumn(
                    entry = third,
                    rank = 3,
                    podiumHeight = 68.dp,
                    medal = "🥉",
                    accentColor = Color(0xFFCD7F32),
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PodiumColumn(
    entry: LeaderboardEntry,
    rank: Int,
    podiumHeight: Dp,
    medal: String,
    accentColor: Color,
    isChampion: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Avatar + Crown / Medal
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(if (isChampion) 56.dp else 48.dp)
                    .shadow(
                        elevation = if (isChampion) 8.dp else 4.dp,
                        shape = CircleShape,
                        ambientColor = accentColor,
                        spotColor = accentColor
                    )
                    .clip(CircleShape)
                    .background(accentColor)
                    .padding(2.5.dp),
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
                        text = entry.displayName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = if (isChampion) 18.sp else 15.sp
                        ),
                        color = accentColor
                    )
                }
            }

            // Medal or Crown
            Text(
                text = medal,
                fontSize = if (isChampion) 18.sp else 15.sp
            )
        }

        // Display Name
        Text(
            text = entry.displayName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            ),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // XP Pill
        Text(
            text = "${entry.xp} XP",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            ),
            color = accentColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 3D Step Box
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(podiumHeight)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(accentColor.copy(alpha = 0.35f), Color(0xFF0F172A))
                    )
                )
                .border(
                    1.dp,
                    accentColor.copy(alpha = 0.5f),
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = if (isChampion) 24.sp else 20.sp
                ),
                color = accentColor
            )
        }
    }
}

@Composable
private fun LeaderboardRow(
    entry: LeaderboardEntry,
    l10n: L10nStrings,
    isCurrentUser: Boolean
) {
    val containerColor = if (isCurrentUser) Color(0xFF1E2F52) else LinguaXSurfaceElevated
    val borderColor = if (isCurrentUser) LinguaXPrimary else LinguaXBorder

    LinguaX3DCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = containerColor,
        borderBrush = Brush.linearGradient(
            listOf(borderColor, LinguaXBorderLight)
        ),
        elevation = if (isCurrentUser) 6.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Rank Number + Avatar + Display Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Rank number
                Box(
                    modifier = Modifier.width(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${entry.rank}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        ),
                        color = if (isCurrentUser) LinguaXAccentLight else LinguaXTextTertiary
                    )
                }

                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isCurrentUser) LinguaXPrimaryGradient else Brush.linearGradient(listOf(Color(0xFF24344D), Color(0xFF162136))))
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF131C2E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = entry.displayName.take(1).uppercase(),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = if (isCurrentUser) LinguaXAccentLight else Color.White
                        )
                    }
                }

                // Name & You Badge
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = entry.displayName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (isCurrentUser) FontWeight.Black else FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = if (isCurrentUser) Color.White else LinguaXTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isCurrentUser) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = LinguaXPrimary,
                                modifier = Modifier.padding(start = 2.dp)
                            ) {
                                Text(
                                    text = l10n.youBadge,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    val usernameText = entry.username ?: "user_${entry.id.take(4)}"
                    Text(
                        text = "@$usernameText",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = LinguaXTextTertiary
                    )
                }
            }

            // Right: XP pill
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isCurrentUser) LinguaXPrimaryContainer else Color(0xFF1A263D),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCurrentUser) LinguaXPrimary.copy(alpha = 0.5f) else LinguaXBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = LinguaXGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${entry.xp} XP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        ),
                        color = LinguaXGold
                    )
                }
            }
        }
    }
}
