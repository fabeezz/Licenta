package com.example.outfitai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

enum class BottomBarDest { TRIP, STUDIO, WARDROBE }

@Composable
fun AppBottomBar(
    active: BottomBarDest,
    onTrip: () -> Unit,
    onStudio: () -> Unit,
    onAdd: () -> Unit,
    onWardrobe: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            shadowElevation = 16.dp,
            modifier = Modifier
                .width(340.dp)
                .height(64.dp),
        ) {
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
                // Add is always primary-styled (action, not destination)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onAdd),
                ) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
                        Icon(Icons.Default.Add, contentDescription = "Add item")
                    }
                }
                BarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Wardrobe") },
                    active = active == BottomBarDest.WARDROBE,
                    onClick = onWardrobe,
                )
            }
        }
    }
}

@Composable
private fun BarItem(
    icon: @Composable () -> Unit,
    active: Boolean,
    onClick: () -> Unit,
) {
    if (active) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        ) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
                icon()
            }
        }
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick),
        ) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.outline) {
                icon()
            }
        }
    }
}