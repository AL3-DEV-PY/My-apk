package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.i18n.AppLanguage
import com.example.data.i18n.L10nStrings
import com.example.ui.components.LinguaX3DButton
import com.example.ui.components.LinguaXOutlinedButton
import com.example.ui.components.LinguaXSphereHero
import com.example.ui.theme.*

@Composable
fun LandingScreen(
    l10n: L10nStrings,
    currentAppLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSignup: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLanguageMenu by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LinguaXBackground)
    ) {
        // Ambient Futuristic Glow Background Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(LinguaXPrimary.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.35f),
                    radius = size.width * 0.7f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(LinguaXSecondary.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.75f),
                    radius = size.width * 0.5f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .systemBarsPadding()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Language Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box {
                    Surface(
                        onClick = { showLanguageMenu = true },
                        shape = RoundedCornerShape(20.dp),
                        color = LinguaXSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXBorderLight)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = currentAppLanguage.flag, fontSize = 16.sp)
                            Text(
                                text = currentAppLanguage.displayName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = LinguaXTextPrimary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false },
                        modifier = Modifier.background(LinguaXSurfaceElevated)
                    ) {
                        AppLanguage.values().forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(text = lang.flag, fontSize = 16.sp)
                                        Text(
                                            text = "${lang.displayName} (${lang.nativeName})",
                                            color = LinguaXTextPrimary,
                                            fontWeight = if (lang == currentAppLanguage) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    onLanguageChange(lang)
                                    showLanguageMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Center Visual Hero: 3D Holographic Multilingual Sphere
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LinguaXSphereHero(
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Sleek Branding & Typography
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = l10n.appName,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 34.sp,
                            letterSpacing = 1.2.sp
                        ),
                        color = Color.White
                    )

                    Text(
                        text = "Learn. Speak. Master.",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = LinguaXAccentLight,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "One platform. Every language.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp
                        ),
                        color = LinguaXTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom CTAs
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LinguaX3DButton(
                    text = l10n.getStarted,
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = onNavigateToSignup,
                    testTag = "landing_signup_button"
                )

                LinguaXOutlinedButton(
                    text = l10n.signIn,
                    onClick = onNavigateToLogin,
                    testTag = "landing_login_button"
                )
            }
        }
    }
}
