package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.Resource
import com.example.ui.theme.*

/**
 * Standard Elevated 3D Card with subtle neon border and interactive press feedback.
 */
@Composable
fun LinguaX3DCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = LinguaXSurfaceElevated,
    borderBrush: Brush = LinguaXBorderGradient,
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1f,
        animationSpec = tween(150),
        label = "card_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = LinguaXPrimary.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(1.dp, borderBrush, RoundedCornerShape(cornerRadius))
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(),
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(18.dp)
    ) {
        Column(content = content)
    }
}

/**
 * Frosted Glass Card for ambient elevated panels.
 */
@Composable
fun LinguaXGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderBrush: Brush = LinguaXBorderGradient,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    LinguaX3DCard(
        modifier = modifier,
        backgroundColor = LinguaXSurfaceGlass,
        borderBrush = borderBrush,
        cornerRadius = cornerRadius,
        elevation = 8.dp,
        onClick = onClick,
        content = content
    )
}

/**
 * High-craft Primary CTA Gradient Button with press scaling and glowing ambient shadow.
 */
@Composable
fun LinguaX3DButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    gradient: Brush = LinguaXButtonGradient,
    textColor: Color = Color.White,
    enabled: Boolean = true,
    height: Dp = 54.dp,
    testTag: String = "primary_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = tween(150),
        label = "btn_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (enabled) 10.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = LinguaXPrimary.copy(alpha = 0.45f),
                spotColor = LinguaXPrimaryDark
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (enabled) gradient else Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF141C38)))
            )
            .border(
                1.dp,
                if (enabled) LinguaXBorderLight else LinguaXBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) textColor else LinguaXTextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = if (enabled) textColor else LinguaXTextTertiary
            )
        }
    }
}

/**
 * Secondary Glass Outlined Button (e.g. "I already have an account").
 */
@Composable
fun LinguaXOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    textColor: Color = Color.White,
    enabled: Boolean = true,
    height: Dp = 54.dp,
    testTag: String = "outlined_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = tween(150),
        label = "btn_outlined_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(LinguaXSurfaceGlassLight)
            .border(
                1.5.dp,
                Brush.linearGradient(
                    listOf(LinguaXPrimary.copy(alpha = 0.6f), LinguaXBorderLight)
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = textColor
            )
        }
    }
}

/**
 * Hero XP Trophy Card matching the reference home design.
 * Features 3D Golden Trophy icon, Level indicator, XP stats, and glowing progress bar.
 */
@Composable
fun LinguaXHeroXpCard(
    currentXp: Int,
    targetXp: Int = 3000,
    level: Int = 12,
    levelTitle: String = "Advanced",
    modifier: Modifier = Modifier
) {
    val progress = (currentXp.toFloat() / targetXp.toFloat()).coerceIn(0f, 1f)

    LinguaX3DCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = LinguaXSurfaceElevated,
        borderBrush = Brush.linearGradient(
            listOf(LinguaXPrimary.copy(alpha = 0.7f), LinguaXAccent.copy(alpha = 0.3f), LinguaXBorder)
        ),
        cornerRadius = 24.dp,
        elevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "XP",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = LinguaXTextSecondary
                )
                Text(
                    text = String.format("%,d", currentXp),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Level $level",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = LinguaXTextSecondary
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelMedium,
                        color = LinguaXTextTertiary
                    )
                    Text(
                        text = levelTitle,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = LinguaXPrimaryLight
                    )
                }
            }

            // 3D Golden Trophy Visual
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .shadow(16.dp, shape = CircleShape, ambientColor = LinguaXGold, spotColor = LinguaXGoldDark)
                    .clip(CircleShape)
                    .background(LinguaXGoldGradient)
                    .border(2.dp, Color(0xFFFFE082), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🏆",
                    fontSize = 34.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // XP Progress Bar
        LinguaXProgressBar(
            progress = progress,
            fillBrush = LinguaXGoldGradient,
            trackColor = Color(0xFF1B2444),
            height = 8.dp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${String.format("%,d", currentXp)} / ${String.format("%,d", targetXp)} XP",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = LinguaXTextSecondary
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = LinguaXGold
            )
        }
    }
}

/**
 * Stat Pill for the 3-column stats row: Streak 🔥 12 days, Lessons 📖 36 completed, Rank 🏅 #128 Global.
 */
@Composable
fun LinguaXStatPill(
    icon: String,
    title: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(LinguaXSurfaceElevated)
            .border(1.dp, LinguaXBorderLight, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = LinguaXTextSecondary
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = icon,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Circular Progress Meter for the Daily Goal Card (e.g. 66%).
 */
@Composable
fun LinguaXCircularProgress(
    progress: Float,
    percentageText: String,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 5.dp,
    progressColor: Color = LinguaXGold,
    trackColor: Color = Color(0xFF1E293B)
) {
    Box(
        modifier = modifier.size(52.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset(stroke / 2, stroke / 2)
            val arcSize = Size(diameter, diameter)

            // Track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Fill
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = (progress.coerceIn(0f, 1f) * 360f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Text(
            text = percentageText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            ),
            color = Color.White
        )
    }
}

/**
 * Filter Chip for category selection (e.g., "All", "Beginner", "Intermediate", "Advanced").
 */
@Composable
fun LinguaXChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) LinguaXPrimary else LinguaXSurfaceElevated)
            .border(
                1.dp,
                if (isSelected) LinguaXPrimaryLight else LinguaXBorder,
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isSelected) Color.White else LinguaXTextSecondary
        )
    }
}

/**
 * Glowing Hexagonal / Circular Achievement Badge for Profile & Home.
 */
@Composable
fun LinguaXAchievementBadge(
    title: String,
    icon: String,
    gradient: Brush,
    isUnlocked: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(
                    elevation = if (isUnlocked) 8.dp else 0.dp,
                    shape = CircleShape,
                    ambientColor = LinguaXPrimary.copy(alpha = 0.5f)
                )
                .clip(CircleShape)
                .background(
                    if (isUnlocked) gradient else Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                )
                .border(
                    1.5.dp,
                    if (isUnlocked) Color(0x88FFFFFF) else LinguaXBorder,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            ),
            color = if (isUnlocked) Color.White else LinguaXTextTertiary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Standard Header for screen sections with optional "View all >" action.
 */
@Composable
fun LinguaXSectionHeader(
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = LinguaXTextPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = LinguaXTextSecondary
                )
            }
        }

        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = LinguaXPrimaryLight,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * Top Bar Header Component.
 */
@Composable
fun LinguaXHeader(
    title: String,
    subtitle: String? = null,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                ),
                color = LinguaXTextPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LinguaXTextSecondary
                )
            }
        }
        if (action != null) {
            action()
        }
    }
}

/**
 * Segmented or Smooth Glowing Progress Bar.
 */
@Composable
fun LinguaXProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    fillBrush: Brush = LinguaXPrimaryGradient,
    trackColor: Color = Color(0xFF1E2D4A),
    height: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(height / 2))
                .background(fillBrush)
        )
    }
}

/**
 * Stat Card for feature metrics.
 */
@Composable
fun LinguaXStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    gradient: Brush,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    LinguaX3DCard(
        modifier = modifier,
        cornerRadius = 18.dp,
        elevation = 4.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(gradient)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = LinguaXTextPrimary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = LinguaXTextSecondary
                )
            }
        }
    }
}

/**
 * Safe Resource State Container (Loading, Success, Error with Retry, Empty).
 */
@Composable
fun <T> ResourceContainer(
    resource: Resource<T>,
    loadingText: String = "Loading...",
    emptyText: String = "No data available",
    onRetry: (() -> Unit)? = null,
    content: @Composable (T) -> Unit
) {
    when (resource) {
        is Resource.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CircularProgressIndicator(
                        color = LinguaXPrimary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = loadingText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LinguaXTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        is Resource.Success -> {
            content(resource.data)
        }
        is Resource.Error -> {
            LinguaX3DCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFF20131E)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = LinguaXError,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = resource.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LinguaXTextPrimary,
                        textAlign = TextAlign.Center
                    )
                    if (onRetry != null) {
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = LinguaXPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        is Resource.Empty -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LinguaXTextTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
