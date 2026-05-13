package com.example.outfitai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun OutfitAITheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LoomLightColorScheme,
        typography  = Typography,
        shapes      = AppShapes,
        content     = content,
    )
}
