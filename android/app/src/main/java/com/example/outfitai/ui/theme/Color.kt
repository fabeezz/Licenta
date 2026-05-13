package com.example.outfitai.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ── Raw palette ────────────────────────────────────────────────────────────────

private val WarmBone             = Color(0xFFFAF8F4) // app background
private val WarmWhite            = Color(0xFFFFFFFF) // surface
private val SurfaceContLowest    = Color(0xFFFFFFFF)
private val SurfaceContLow       = Color(0xFFF4F1EB) // replaces hardcoded #F9F9F9 tiles
private val SurfaceCont          = Color(0xFFEDE9E1) // raised tiles, filter chips
private val SurfaceContHigh      = Color(0xFFE4DFD4)
private val SurfaceContHighest   = Color(0xFFDAD4C8)
private val SurfaceVar           = Color(0xFFEDE9E1)
private val WarmInk              = Color(0xFF1A1714) // primary text + CTAs
private val InkSecondary         = Color(0xFF6B645B) // secondary text, captions
private val Hairline             = Color(0xFFD8D1C5) // 1dp borders
private val Divider              = Color(0xFFEBE5DA) // section dividers
private val WarmUmber            = Color(0xFF8B6F47) // selected chip, active tab
private val UmberOnPrimary       = Color(0xFFFFFFFF)
private val UmberContainer       = Color(0xFFF0E5D5)
private val UmberContainerOn     = Color(0xFF3D2D18)
private val MutedKhaki           = Color(0xFFA89B82) // info chips
private val KhakiOn              = Color(0xFFFFFFFF)
private val KhakiContainer       = Color(0xFFEDE7DA)
private val KhakiContainerOn     = Color(0xFF3D3526)
private val BrickRed             = Color(0xFFA33A2A) // error — not bright red
private val BrickRedOn           = Color(0xFFFFFFFF)
private val BrickRedContainer    = Color(0xFFFFDED9)
private val BrickRedContainerOn  = Color(0xFF410002)
private val Scrim                = Color(0xFF000000)

// ── Light color scheme ─────────────────────────────────────────────────────────

val LoomLightColorScheme = lightColorScheme(
    primary                 = WarmInk,
    onPrimary               = WarmBone,
    primaryContainer        = SurfaceCont,
    onPrimaryContainer      = WarmInk,
    secondary               = WarmUmber,
    onSecondary             = UmberOnPrimary,
    secondaryContainer      = UmberContainer,
    onSecondaryContainer    = UmberContainerOn,
    tertiary                = MutedKhaki,
    onTertiary              = KhakiOn,
    tertiaryContainer       = KhakiContainer,
    onTertiaryContainer     = KhakiContainerOn,
    error                   = BrickRed,
    onError                 = BrickRedOn,
    errorContainer          = BrickRedContainer,
    onErrorContainer        = BrickRedContainerOn,
    background              = WarmBone,
    onBackground            = WarmInk,
    surface                 = WarmWhite,
    onSurface               = WarmInk,
    surfaceVariant          = SurfaceVar,
    onSurfaceVariant        = InkSecondary,
    surfaceContainerLowest  = SurfaceContLowest,
    surfaceContainerLow     = SurfaceContLow,
    surfaceContainer        = SurfaceCont,
    surfaceContainerHigh    = SurfaceContHigh,
    surfaceContainerHighest = SurfaceContHighest,
    outline                 = Hairline,
    outlineVariant          = Divider,
    scrim                   = Scrim,
)
