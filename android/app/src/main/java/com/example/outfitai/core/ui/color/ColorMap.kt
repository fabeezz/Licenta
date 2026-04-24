package com.example.outfitai.core.ui.color

import androidx.compose.ui.graphics.Color

fun colorNameToComposeColor(name: String?, fallback: Color = Color.Transparent): Color {
    return when (name?.lowercase()?.trim()) {
        "black" -> Color(0xFF1A1A1A)
        "white" -> Color(0xFFFFFFFF)
        "gray", "grey" -> Color(0xFF808080)
        "silver" -> Color(0xFFC0C0C0)
        "charcoal" -> Color(0xFF36454F)
        "beige" -> Color(0xFFF5F5DC)
        "cream", "ivory" -> Color(0xFFFFFDD0)
        "burgundy" -> Color(0xFF800020)
        "maroon" -> Color(0xFF800000)
        "pink" -> Color(0xFFFFC0CB)
        "rose" -> Color(0xFFFF66CC)
        "red" -> Color(0xFFFF0000)
        "crimson" -> Color(0xFFDC143C)
        "brown" -> Color(0xFF8B4513)
        "tan" -> Color(0xFFD2B48C)
        "camel" -> Color(0xFFC19A6B)
        "khaki" -> Color(0xFFF0E68C)
        "orange" -> Color(0xFFFFA500)
        "rust" -> Color(0xFFB7410E)
        "olive" -> Color(0xFF808000)
        "yellow" -> Color(0xFFFFFF00)
        "mustard" -> Color(0xFFFFDB58)
        "dark green" -> Color(0xFF006400)
        "green" -> Color(0xFF008000)
        "forest green" -> Color(0xFF228B22)
        "cyan" -> Color(0xFF00FFFF)
        "teal" -> Color(0xFF008080)
        "turquoise" -> Color(0xFF40E0D0)
        "navy", "navy blue" -> Color(0xFF000080)
        "blue" -> Color(0xFF0000FF)
        "cobalt" -> Color(0xFF0047AB)
        "purple" -> Color(0xFF800080)
        "violet" -> Color(0xFF8F00FF)
        "lavender" -> Color(0xFFE6E6FA)
        else -> fallback
    }
}
