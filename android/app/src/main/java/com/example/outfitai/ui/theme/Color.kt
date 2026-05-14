package com.example.outfitai.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ── Raw palette ────────────────────────────────────────────────────────────────

private val Ivory                = Color(0xFFFAFAFA) // app background
private val White                = Color(0xFFFFFFFF) // surface
private val SurfaceContLowest    = Color(0xFFFFFFFF)
private val SurfaceContLow       = Color(0xFFF2F2F5) // barely-there lavender tiles
private val SurfaceCont          = Color(0xFFE6E6EE) // raised tiles, filter chips
private val SurfaceContHigh      = Color(0xFFD9D9EA)
private val SurfaceContHighest   = Color(0xFFCCCCE2)
private val SurfaceVar           = Color(0xFFE6E6EE)
private val Charcoal             = Color(0xFF18181C) // primary text + CTAs
private val SlateSecondary       = Color(0xFF616170) // secondary text, captions
private val CoolHairline         = Color(0xFFD0D0DC) // 1dp borders
private val CoolDivider          = Color(0xFFE2E2EE) // section dividers
private val Indigo               = Color(0xFF4F52C7) // selected chip, active tab
private val IndigoOn             = Color(0xFFFFFFFF)
private val IndigoContainer      = Color(0xFFDEE0F5) // light indigo tint
private val IndigoContainerOn    = Color(0xFF0A0D5E)
private val SlateAccent          = Color(0xFF7B7D9E) // info chips
private val SlateAccentOn        = Color(0xFFFFFFFF)
private val SlateContainer       = Color(0xFFE8E9F5)
private val SlateContainerOn     = Color(0xFF252657)
private val CrimsonRed           = Color(0xFFB33A3A) // error
private val CrimsonRedOn         = Color(0xFFFFFFFF)
private val CrimsonRedContainer  = Color(0xFFFFE0E0)
private val CrimsonRedContainerOn = Color(0xFF410002)
private val Scrim                = Color(0xFF000000)

// ── Light color scheme ─────────────────────────────────────────────────────────

val LoomLightColorScheme = lightColorScheme(
    primary                 = Charcoal,
    onPrimary               = Ivory,
    primaryContainer        = SurfaceCont,
    onPrimaryContainer      = Charcoal,
    secondary               = Indigo,
    onSecondary             = IndigoOn,
    secondaryContainer      = IndigoContainer,
    onSecondaryContainer    = IndigoContainerOn,
    tertiary                = SlateAccent,
    onTertiary              = SlateAccentOn,
    tertiaryContainer       = SlateContainer,
    onTertiaryContainer     = SlateContainerOn,
    error                   = CrimsonRed,
    onError                 = CrimsonRedOn,
    errorContainer          = CrimsonRedContainer,
    onErrorContainer        = CrimsonRedContainerOn,
    background              = Ivory,
    onBackground            = Charcoal,
    surface                 = White,
    onSurface               = Charcoal,
    surfaceVariant          = SurfaceVar,
    onSurfaceVariant        = SlateSecondary,
    surfaceContainerLowest  = SurfaceContLowest,
    surfaceContainerLow     = SurfaceContLow,
    surfaceContainer        = SurfaceCont,
    surfaceContainerHigh    = SurfaceContHigh,
    surfaceContainerHighest = SurfaceContHighest,
    outline                 = CoolHairline,
    outlineVariant          = CoolDivider,
    scrim                   = Scrim,
)
