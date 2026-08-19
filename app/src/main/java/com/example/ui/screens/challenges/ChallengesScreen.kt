package com.example.ui.screens.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.i18n.L10nStrings
import com.example.data.model.DailyChallenge
import com.example.data.repository.Resource
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ChallengesScreen(
    l10n: L10nStrings,
    challengesResource: Resource<List<DailyChallenge>>,
    onClaimReward: (DailyChallenge) -> Unit,
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
            title = l10n.challengesTab,
            subtitle = "Complete daily quests to accelerate your fluency & earn rewards"
        )

        ResourceContainer(
            resource = challengesResource,
            loadingText = l10n.loading,
            emptyText = l10n.noDataAvailable
        ) { challenges ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(challenges) { challenge ->
                    ChallengeCardItem(
                        challenge = challenge,
                        l10n = l10n,
                        onClaimReward = { onClaimReward(challenge) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengeCardItem(
    challenge: DailyChallenge,
    l10n: L10nStrings,
    onClaimReward: () -> Unit
) {
    var isClaimed by remember { mutableStateOf(challenge.isCompleted) }
    val progress = (challenge.currentProgress.toFloat() / challenge.target.toFloat()).coerceIn(0f, 1f)
    val canClaim = progress >= 1f && !isClaimed

    LinguaX3DCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF131C2E),
        borderBrush = if (canClaim) LinguaXBorderGradient else androidx.compose.ui.graphics.SolidColor(LinguaXBorder),
        elevation = if (canClaim) 6.dp else 3.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = challenge.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = LinguaXTextPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LinguaXPrimaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = LinguaXAccent, modifier = Modifier.size(14.dp))
                            Text(
                                text = "+${challenge.rewardXp} XP",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = LinguaXAccent
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF261D12)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = LinguaXWarning, modifier = Modifier.size(14.dp))
                            Text(
                                text = "+${challenge.rewardCoins}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = LinguaXWarning
                            )
                        }
                    }
                }
            }

            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodySmall,
                color = LinguaXTextSecondary
            )

            // Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${challenge.currentProgress} / ${challenge.target}",
                    style = MaterialTheme.typography.labelSmall,
                    color = LinguaXTextSecondary
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (progress >= 1f) LinguaXSuccess else LinguaXAccent
                )
            }

            LinguaXProgressBar(
                progress = progress,
                fillBrush = if (progress >= 1f) LinguaXGreenGradient else LinguaXAccentGradient,
                height = 8.dp
            )

            if (canClaim) {
                LinguaX3DButton(
                    text = l10n.claimReward,
                    icon = Icons.Default.EmojiEvents,
                    gradient = LinguaXGreenGradient,
                    onClick = {
                        isClaimed = true
                        onClaimReward()
                    },
                    height = 44.dp,
                    testTag = "claim_reward_${challenge.id}"
                )
            } else if (isClaimed) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = LinguaXSuccess.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = LinguaXSuccess, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = l10n.claimedReward,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = LinguaXSuccess
                        )
                    }
                }
            }
        }
    }
}
