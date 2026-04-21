package com.example.outfitai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary                = Primary,
    onPrimary              = OnPrimary,
    surface                = Surface,
    onSurface              = OnSurface,
    surfaceVariant         = SurfaceVariant,
    onSurfaceVariant       = OnSurfaceVariant,
    surfaceContainerLow    = SurfaceContainerLow,
    surfaceContainerLowest = SurfaceContainerLowest,
    background             = Background,
    onBackground           = OnSurface,
    outlineVariant         = OutlineVariant,
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
