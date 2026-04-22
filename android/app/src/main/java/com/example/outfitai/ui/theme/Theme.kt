package com.example.outfitai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary                 = Primary,
    onPrimary               = OnPrimary,
    surface                 = Surface,
    onSurface               = OnSurface,
    surfaceVariant          = SurfaceVariant,
    onSurfaceVariant        = OnSurfaceVariant,
    surfaceContainerLowest  = SurfaceContainerLowest,
    surfaceContainerLow     = SurfaceContainerLow,
    surfaceContainer        = SurfaceContainer,
    surfaceContainerHigh    = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    background              = Background,
    onBackground            = OnSurface,
    outline                 = Outline,
    outlineVariant          = OutlineVariant,
    error                   = ErrorColor,
    onError                 = OnError,
    errorContainer          = ErrorContainer,
    onErrorContainer        = OnErrorContainer,
)

@Composable
fun OutfitAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = Typography,
        shapes      = AppShapes,
        content     = content,
    )
}
