package com.example.outfitai.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.Bold,
        fontSize     = 34.sp,
        letterSpacing = (-0.5).sp,
    ),
    titleLarge = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.Bold,
        fontSize     = 34.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 22.sp,
        letterSpacing = (-0.3).sp,
    ),
    bodyLarge = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.Normal,
        fontSize     = 17.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.Normal,
        fontSize     = 15.sp,
        letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 12.sp,
        letterSpacing = 0.5.sp,
    ),
)
