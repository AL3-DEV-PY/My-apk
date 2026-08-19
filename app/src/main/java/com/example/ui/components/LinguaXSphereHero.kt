package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-craft 3D Language Sphere Hero Visual with smooth orbiting characters,
 * multi-lingual greetings ("مرحبا", "Hello", "Bonjour", "Hola", "こんにちは", "Ciao", "안녕하세요", "Merhaba"),
 * glowing geometric rings, and dynamic particle depth.
 */
@Composable
fun LinguaXSphereHero(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sphere_transition")

    // Slow cinematic rotation angle (0 to 360 deg over 16 seconds)
    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_rot"
    )

    // Reverse slow rotation for inner ring
    val innerRingRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rot"
    )

    // Gentle 3D breathing/pulsing scale
    val spherePulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sphere_pulse"
    )

    // Floating parallax vertical offset for greeting chips
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    val greetings = remember {
        listOf(
            GreetingOrbitItem("مرحبا", "🇸🇦", 0f, 115.dp, Color(0xFF22D3EE)),
            GreetingOrbitItem("Hello", "🇺🇸", 45f, 130.dp, Color(0xFF4F7CFF)),
            GreetingOrbitItem("Bonjour", "🇫🇷", 90f, 120.dp, Color(0xFF818CF8)),
            GreetingOrbitItem("Hola", "🇪🇸", 135f, 135.dp, Color(0xFFF59E0B)),
            GreetingOrbitItem("こんにちは", "🇯🇵", 180f, 125.dp, Color(0xFFF43F5E)),
            GreetingOrbitItem("Ciao", "🇮🇹", 225f, 120.dp, Color(0xFF10B981)),
            GreetingOrbitItem("안녕하세요", "🇰🇷", 270f, 135.dp, Color(0xFFA855F7)),
            GreetingOrbitItem("Merhaba", "🇹🇷", 315f, 120.dp, Color(0xFFEC4899))
        )
    }

    Box(
        modifier = modifier
            .size(310.dp)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background Ambient Glow Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f

            // Outer Radial Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x404F7CFF),
                        Color(0x206D4AFF),
                        Color(0x0A22D3EE),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = radius * 0.95f
                ),
                radius = radius * 0.95f,
                center = centerOffset
            )

            // Orbital Ring 1 (Electric Blue)
            rotate(orbitRotation, pivot = centerOffset) {
                drawCircle(
                    color = Color(0x334F7CFF),
                    radius = radius * 0.78f,
                    center = centerOffset,
                    style = Stroke(width = 1.8f)
                )

                // Orbiting satellite particles
                drawCircle(
                    color = Color(0xFF22D3EE),
                    radius = 3.5f,
                    center = Offset(centerOffset.x + radius * 0.78f, centerOffset.y)
                )
                drawCircle(
                    color = Color(0xFF6D4AFF),
                    radius = 3f,
                    center = Offset(centerOffset.x - radius * 0.78f, centerOffset.y)
                )
            }

            // Orbital Ring 2 (Cyan / Violet Reverse)
            rotate(innerRingRotation, pivot = centerOffset) {
                drawCircle(
                    color = Color(0x2222D3EE),
                    radius = radius * 0.62f,
                    center = centerOffset,
                    style = Stroke(width = 1.2f)
                )

                drawCircle(
                    color = Color(0xFF38BDF8),
                    radius = 2.8f,
                    center = Offset(centerOffset.x, centerOffset.y + radius * 0.62f)
                )
            }
        }

        // Central 3D Core Sphere Container
        Box(
            modifier = Modifier
                .size(118.dp)
                .scale(spherePulse)
                .shadow(24.dp, shape = CircleShape, ambientColor = LinguaXPrimary, spotColor = LinguaXAccent)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2B4C8C),
                            Color(0xFF131D38),
                            Color(0xFF070B18)
                        ),
                        center = Offset(118f * 0.35f, 118f * 0.35f),
                        radius = 160f
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF38BDF8), Color(0xFF6D4AFF), Color(0xFF1E293B))
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Core Futuristic Emblem / LX Monogram
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "LinguaX",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 1.2.sp
                    ),
                    color = Color.White
                )

                Text(
                    text = "🌐 AI SPHERE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp,
                        letterSpacing = 1.sp
                    ),
                    color = LinguaXAccent
                )
            }
        }

        // Orbiting Greeting Badges in 3D Space
        greetings.forEachIndexed { index, item ->
            // Compute dynamic angle
            val currentAngleRad = Math.toRadians((item.baseAngle + orbitRotation).toDouble())
            val xOffsetDp = (cos(currentAngleRad) * 116).dp
            val yOffsetDp = (sin(currentAngleRad) * 78).dp + (if (index % 2 == 0) floatOffset.dp else -floatOffset.dp)

            // Depth calculation: elements at bottom/front are larger and more opaque
            val depthScale = (0.80f + (sin(currentAngleRad) * 0.20f).toFloat()).coerceIn(0.70f, 1.05f)
            val depthAlpha = (0.65f + (sin(currentAngleRad) * 0.35f).toFloat()).coerceIn(0.40f, 1f)

            Box(
                modifier = Modifier
                    .offset(x = xOffsetDp, y = yOffsetDp)
                    .scale(depthScale)
                    .alpha(depthAlpha)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xDD0D1626),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.linearGradient(listOf(item.accentColor.copy(alpha = 0.8f), Color(0x334F7CFF)))
                    ),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = item.flag, fontSize = 11.sp)
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private data class GreetingOrbitItem(
    val text: String,
    val flag: String,
    val baseAngle: Float,
    val orbitDistance: androidx.compose.ui.unit.Dp,
    val accentColor: Color
)
