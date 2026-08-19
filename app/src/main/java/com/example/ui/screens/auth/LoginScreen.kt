package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.i18n.L10nStrings
import com.example.ui.components.LinguaX3DButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthState

@Composable
fun LoginScreen(
    l10n: L10nStrings,
    authState: AuthState,
    onLogin: (String, String) -> Unit,
    onNavigateToSignup: () -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    val isFormValid = email.trim().isNotBlank() && password.trim().length >= 6

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LinguaXBackground)
    ) {
        // Ambient background glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(LinguaXPrimary.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.2f),
                    radius = size.width * 0.6f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("login_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            }

            // Center Form Card
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = l10n.loginButton,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Sign in to access your cloud-synced lessons and XP.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LinguaXTextSecondary
                    )
                }

                // Error Message Box
                AnimatedVisibility(
                    visible = !errorMessage.isNullOrBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (!errorMessage.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x33EF4444), RoundedCornerShape(12.dp))
                                .border(1.dp, LinguaXError.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = LinguaXError,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = LinguaXErrorLight
                            )
                        }
                    }
                }

                // Email Input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(l10n.emailLabel) },
                    placeholder = { Text("you@example.com", color = LinguaXTextTertiary) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = LinguaXPrimaryLight
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LinguaXSurfaceElevated,
                        unfocusedContainerColor = LinguaXSurface,
                        focusedBorderColor = LinguaXPrimary,
                        unfocusedBorderColor = LinguaXBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = LinguaXPrimaryLight,
                        unfocusedLabelColor = LinguaXTextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_email_field")
                )

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(l10n.passwordLabel) },
                    placeholder = { Text("••••••••", color = LinguaXTextTertiary) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = LinguaXPrimaryLight
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { isPasswordVisible = !isPasswordVisible },
                            modifier = Modifier.testTag("toggle_password_visibility")
                        ) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility",
                                tint = LinguaXTextSecondary
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (isFormValid && !isLoading) {
                                onLogin(email.trim(), password.trim())
                            }
                        }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LinguaXSurfaceElevated,
                        unfocusedContainerColor = LinguaXSurface,
                        focusedBorderColor = LinguaXPrimary,
                        unfocusedBorderColor = LinguaXBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = LinguaXPrimaryLight,
                        unfocusedLabelColor = LinguaXTextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_field")
                )

                // Submit Login Button
                LinguaX3DButton(
                    text = if (isLoading) l10n.loading else l10n.loginButton,
                    enabled = isFormValid && !isLoading,
                    onClick = {
                        focusManager.clearFocus()
                        onLogin(email.trim(), password.trim())
                    },
                    testTag = "login_submit_button"
                )
            }

            // Bottom Switch to Signup
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = l10n.dontHaveAccount,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LinguaXTextSecondary
                )
                TextButton(
                    onClick = onNavigateToSignup,
                    modifier = Modifier.testTag("login_goto_signup")
                ) {
                    Text(
                        text = l10n.signupButton,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = LinguaXPrimaryLight
                    )
                }
            }
        }
    }
}
