package com.example.outfitai.util

import androidx.compose.ui.graphics.Color

fun colorNameToComposeColor(name: String?): Color {
    return when (name?.lowercase()) {
        "black" -> Color(0xFF000000)
        "white" -> Color(0xFFFFFFFF)
        "gray" -> Color(0xFF808080)
        "beige" -> Color(0xFFF5F5DC)
        "burgundy" -> Color(0xFF800020)
        "pink" -> Color(0xFFFFC0CB)
        "red" -> Color(0xFFFF0000)
        "brown" -> Color(0xFF8B4513)
        "orange" -> Color(0xFFFFA500)
        "olive" -> Color(0xFF808000)
        "yellow" -> Color(0xFFFFFF00)
        "dark green" -> Color(0xFF006400)
        "green" -> Color(0xFF008000)
        "cyan" -> Color(0xFF00FFFF)
        "navy" -> Color(0xFF000080)
        "blue" -> Color(0xFF0000FF)
        "purple" -> Color(0xFF800080)
        else -> Color.Transparent
    }
}
