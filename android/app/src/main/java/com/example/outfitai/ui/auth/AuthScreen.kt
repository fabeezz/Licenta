package com.example.outfitai.ui.auth

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class AuthMode { Login, Register, Reset }

private val PageMargin = 20.dp
private val StackLarge = 32.dp
private val StackMedium = 16.dp
private val StackSmall = 8.dp

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
            .padding(PageMargin),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
        ) {
            DecorativeBackdrop(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = (-24).dp),
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(StackLarge),
            ) {
                Header(mode = mode)

                if (mode != AuthMode.Reset) {
                    SegmentedToggle(
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
private fun DecorativeBackdrop(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(40.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.04f),
            )
            .rotate(-3f)
            .clip(RoundedCornerShape(40.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    )
}

@Composable
private fun Header(mode: AuthMode) {
    val (title, subtitle) = when (mode) {
        AuthMode.Login, AuthMode.Register -> "Welcome" to "Your personal digital tailor awaits."
        AuthMode.Reset -> "Reset Password" to "Enter your details to secure your account."
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StackSmall),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SegmentedToggle(
    selected: AuthMode,
    onSelect: (AuthMode) -> Unit,
    enabled: Boolean,
) {
    val options = listOf(AuthMode.Login, AuthMode.Register)
    val targetIndex = options.indexOf(selected).coerceAtLeast(0)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
    ) {
        val pillWidth = maxWidth / 2
        val offsetX by animateDpAsState(
            targetValue = pillWidth * targetIndex,
            animationSpec = tween(durationMillis = 260),
            label = "segmentedIndicator",
        )

        Box(
            modifier = Modifier
                .offset(x = offsetX)
                .width(pillWidth)
                .fillMaxHeight()
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.08f),
                )
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        )

        Row(Modifier.fillMaxSize()) {
            options.forEach { option ->
                val isSelected = option == selected
                val label = when (option) {
                    AuthMode.Login -> "Login"
                    AuthMode.Register -> "Register"
                    AuthMode.Reset -> ""
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .clickable(enabled = enabled) { onSelect(option) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
                BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                ),
                shape = RoundedCornerShape(24.dp),
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(StackMedium),
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
                InlineTextLink(
                    text = "Forgot password?",
                    onClick = onForgotPassword,
                    enabled = !state.isBusy,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            PrimaryButton(
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
                InlineTextLink(
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

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    iconDescription: String,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        enabled = enabled,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = iconDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
        ),
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            errorContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
    )
}

@Composable
private fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    busy: Boolean,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
        ),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
    ) {
        if (busy) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = label,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun InlineTextLink(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = color,
        )
    }
}
