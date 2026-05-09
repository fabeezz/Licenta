package com.example.outfitai.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class BottomBarDest { TRIP, STUDIO, WARDROBE, PROFILE }

@Composable
fun AppBottomBar(
    active: BottomBarDest,
    onTrip: () -> Unit,
    onStudio: () -> Unit,
    onAdd: () -> Unit,
    onWardrobe: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val animatedOffset by animateDpAsState(
        targetValue = highlightOffset(active),
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioNoBouncy,
        ),
        label = "highlightOffset",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = colors.surface.copy(alpha = 0.9f),
            border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)),
            shadowElevation = 16.dp,
            modifier = Modifier
                .width(340.dp)
                .height(64.dp),
        ) {
            Box(Modifier.fillMaxSize()) {
                // Animated sliding highlight (drawn behind the icons)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.CenterStart)
                            .offset(x = animatedOffset)
                            .clip(CircleShape)
                            .background(colors.primary),
                    )
                }
                // Icons row
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                ) {
                    BarItem(
                        icon = { Icon(Icons.Default.FlightTakeoff, contentDescription = "Trip Planner") },
                        active = active == BottomBarDest.TRIP,
                        onClick = onTrip,
                    )
                    BarItem(
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Outfit Studio") },
                        active = active == BottomBarDest.STUDIO,
                        onClick = onStudio,
                    )
                    // Add — action button, not a tab destination
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                            .clickable(onClick = onAdd),
                    ) {
                        CompositionLocalProvider(LocalContentColor provides colors.onPrimary) {
                            Icon(Icons.Default.Add, contentDescription = "Add item")
                        }
                    }
                    BarItem(
                        icon = { Icon(Icons.Default.Checkroom, contentDescription = "Wardrobe") },
                        active = active == BottomBarDest.WARDROBE,
                        onClick = onWardrobe,
                    )
                    BarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        active = active == BottomBarDest.PROFILE,
                        onClick = onProfile,
                    )
                }
            }
        }
    }
}

// Computes the left-edge offset of the highlight circle within the pill's padded area.
// Layout: 340dp pill, 8dp h-padding → 324dp inner, SpaceEvenly over 5×44dp items.
// gap = (324 - 5*44) / 6 = 17.33dp; offset(slot) = gap + slot*(44+gap)
// Slots: TRIP=0, STUDIO=1, [Add FAB=2 not a dest], WARDROBE=3, PROFILE=4
private fun highlightOffset(dest: BottomBarDest): Dp {
    val pillInnerWidth = 324f
    val itemWidth = 44f
    val gap = (pillInnerWidth - 5f * itemWidth) / 6f
    val slot = when (dest) {
        BottomBarDest.TRIP -> 0f
        BottomBarDest.STUDIO -> 1f
        BottomBarDest.WARDROBE -> 3f
        BottomBarDest.PROFILE -> 4f
    }
    return (gap + slot * (itemWidth + gap)).dp
}

@Composable
private fun BarItem(
    icon: @Composable () -> Unit,
    active: Boolean,
    onClick: () -> Unit,
) {
    val iconColor by animateColorAsState(
        targetValue = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline,
        animationSpec = tween(durationMillis = 200),
        label = "iconColor",
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
    ) {
        CompositionLocalProvider(LocalContentColor provides iconColor) {
            icon()
        }
    }
}
