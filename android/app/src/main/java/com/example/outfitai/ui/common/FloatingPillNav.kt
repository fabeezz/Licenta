package com.example.outfitai.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class PillDest { STYLIST, STUDIO, PROFILE }

@Composable
fun FloatingPillNav(
    active: PillDest,
    onSelect: (PillDest) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 16.dp,
        modifier = modifier
            .width(280.dp)
            .height(64.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        ) {
            PillNavItem(
                icon = { Icon(Icons.Outlined.AutoFixHigh, contentDescription = "Stylist") },
                active = active == PillDest.STYLIST,
                onClick = { onSelect(PillDest.STYLIST) },
            )
            PillNavItem(
                icon = { Icon(Icons.Outlined.Add, contentDescription = "Studio") },
                active = active == PillDest.STUDIO,
                onClick = { onSelect(PillDest.STUDIO) },
            )
            PillNavItem(
                icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") },
                active = active == PillDest.PROFILE,
                onClick = { onSelect(PillDest.PROFILE) },
            )
        }
    }
}

@Composable
private fun PillNavItem(
    icon: @Composable () -> Unit,
    active: Boolean,
    onClick: () -> Unit,
) {
    if (active) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
        ) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
                icon()
            }
        }
    } else {
        IconButton(onClick = onClick) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.outline) {
                icon()
            }
        }
    }
}
