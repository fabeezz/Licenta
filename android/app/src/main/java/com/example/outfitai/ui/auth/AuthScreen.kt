package com.example.outfitai.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import com.example.outfitai.ui.theme.Elevation
import com.example.outfitai.ui.theme.Spacing
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

internal enum class AuthMode { Login, Register, Reset }

// spacing aliases kept for readability; resolve to Spacing tokens at usage sites

@Composable
fun AuthScreen(
    state: AuthUiState,
    onUsername: (String) -> Unit,
    onEmail: (String) -> Unit,
    onPassword: (String) -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onResetPassword: () -> Unit,
) {
    var mode by remember { mutableStateOf(AuthMode.Login) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(Spacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp)) {
            DecorativeBackdrop(modifier = Modifier.matchParentSize().offset(y = (-Spacing.xxl)))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxxl),
            ) {
                AuthHeader(mode = mode)
                if (mode != AuthMode.Reset) {
                    AuthSegmentedToggle(
                        selected = mode,
                        onSelect = { mode = it },
                        enabled = !state.isBusy,
                    )
                }
                AuthCard(
                    state = state,
                    mode = mode,
                    onUsername = onUsername,
                    onEmail = onEmail,
                    onPassword = onPassword,
                    onPrimaryAction = {
                        when (mode) {
                            AuthMode.Login -> onLogin()
                            AuthMode.Register -> onRegister()
                            AuthMode.Reset -> onResetPassword()
                        }
                    },
                    onForgotPassword = { mode = AuthMode.Reset },
                    onBackToLogin = { mode = AuthMode.Login },
                )
            }
        }
    }
}

@Composable
private fun AuthCard(
    state: AuthUiState,
    mode: AuthMode,
    onUsername: (String) -> Unit,
    onEmail: (String) -> Unit,
    onPassword: (String) -> Unit,
    onPrimaryAction: () -> Unit,
    onForgotPassword: () -> Unit,
    onBackToLogin: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = MaterialTheme.shapes.extraLarge,
            ),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = Elevation.Level3),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xxl, vertical = Spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            AuthTextField(
                value = state.username,
                onValueChange = onUsername,
                placeholder = "Username",
                leadingIcon = Icons.Outlined.Person,
                iconDescription = "Username",
                enabled = !state.isBusy,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Text,
                ),
            )

            if (mode == AuthMode.Register || mode == AuthMode.Reset) {
                AuthTextField(
                    value = state.email,
                    onValueChange = onEmail,
                    placeholder = "Email",
                    leadingIcon = Icons.Outlined.Email,
                    iconDescription = "Email",
                    enabled = !state.isBusy,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Email,
                    ),
                )
            }

            AuthTextField(
                value = state.password,
                onValueChange = onPassword,
                placeholder = if (mode == AuthMode.Reset) "New Password" else "Password",
                leadingIcon = Icons.Outlined.Lock,
                iconDescription = "Password",
                enabled = !state.isBusy,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Password,
                ),
                visualTransformation = PasswordVisualTransformation(),
            )

            if (state.error != null) {
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            if (mode == AuthMode.Login) {
                AuthTextLink(
                    text = "Forgot password?",
                    onClick = onForgotPassword,
                    enabled = !state.isBusy,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            AuthPrimaryButton(
                label = when (mode) {
                    AuthMode.Login -> "Sign In"
                    AuthMode.Register -> "Create Account"
                    AuthMode.Reset -> "Reset Password"
                },
                onClick = onPrimaryAction,
                enabled = !state.isBusy,
                busy = state.isBusy,
            )

            if (mode == AuthMode.Reset) {
                AuthTextLink(
                    text = "Back to Login",
                    onClick = onBackToLogin,
                    enabled = !state.isBusy,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}
