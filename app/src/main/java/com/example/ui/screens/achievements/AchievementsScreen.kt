package com.example.ui.screens.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.i18n.L10nStrings
import com.example.data.model.AchievementItem
import com.example.data.repository.Resource
import com.example.ui.components.LinguaX3DCard
import com.example.ui.components.LinguaXHeader
import com.example.ui.components.LinguaXProgressBar
import com.example.ui.components.ResourceContainer
import com.example.ui.theme.*

@Composable
fun AchievementsScreen(
    l10n: L10nStrings,
    achievementsResource: Resource<List<AchievementItem>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinguaXBackground)
            .padding(16.dp)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LinguaXHeader(
            title = l10n.achievementsTab,
            subtitle = l10n.unlockedAchievements
        )

        ResourceContainer(
            resource = achievementsResource,
            loadingText = l10n.loading,
            emptyText = l10n.noDataAvailable
        ) { achievements ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(achievements) { item ->
                    AchievementBadgeCard(item = item)
                }
            }
        }
    }
}

@Composable
fun AchievementBadgeCard(item: AchievementItem) {
    val isUnlocked = item.isUnlocked
    val icon = when (item.iconName) {
        "fire" -> Icons.Default.LocalFireDepartment
        "globe" -> Icons.Default.Public
        "trophy" -> Icons.Default.EmojiEvents
        "book" -> Icons.AutoMirrored.Filled.MenuBook
        else -> Icons.Default.Star
    }

    LinguaX3DCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (isUnlocked) Color(0xFF141F33) else Color(0xFF101624),
        borderBrush = if (isUnlocked) LinguaXBorderGradient else androidx.compose.ui.graphics.SolidColor(LinguaXBorder),
        elevation = if (isUnlocked) 6.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 3D Badge Icon
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) LinguaXGoldGradient else androidx.compose.ui.graphics.SolidColor(Color(0xFF1A2438))
                    )
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(if (isUnlocked) Color(0xFF261D12) else Color(0xFF121A2B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = item.title,
                        tint = if (isUnlocked) LinguaXWarning else LinguaXTextTertiary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = if (isUnlocked) LinguaXTextPrimary else LinguaXTextSecondary
            )

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = LinguaXTextTertiary,
                maxLines = 2
            )

            if (isUnlocked) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = LinguaXSuccess.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Unlocked",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LinguaXSuccess,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else {
                val progress = (item.progress.toFloat() / item.maxProgress.toFloat()).coerceIn(0f, 1f)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LinguaXProgressBar(
                        progress = progress,
                        fillBrush = LinguaXPrimaryGradient,
                        height = 5.dp
                    )
                    Text(
                        text = "${item.progress}/${item.maxProgress}",
                        style = MaterialTheme.typography.labelSmall,
                        color = LinguaXTextTertiary
                    )
                }
            }
        }
    }
}
