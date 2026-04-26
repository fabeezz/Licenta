package com.example.outfitai.ui.itemdetails

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.outfitai.util.capitalizeFirst

@Composable
internal fun InfoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        color = cs.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, cs.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = cs.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
internal fun InfoRow(label: String, value: String) {
    val cs = MaterialTheme.colorScheme
    Column {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = cs.onSurface)
    }
}

@Composable
internal fun SeasonChip(season: String) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = cs.surfaceContainer,
        border = BorderStroke(1.dp, cs.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Text(
            text = season.capitalizeFirst(),
            style = MaterialTheme.typography.labelLarge,
            color = cs.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
internal fun ColorSwatch(
    name: String,
    role: String,
    size: Dp,
    color: Color,
    clickable: Boolean = false,
    onClick: () -> Unit = {},
) {
    val cs = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .run { if (clickable) clickable(onClick = onClick) else this }
            .padding(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, cs.outlineVariant.copy(alpha = 0.5f), CircleShape),
        )
        Text(text = name.capitalizeFirst(), style = MaterialTheme.typography.labelLarge, color = cs.onSurface, maxLines = 1)
        Text(text = role, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp), color = cs.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
internal fun OccasionChip(label: String, selected: Boolean, clickable: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = if (selected) cs.primary else cs.surfaceContainer,
        border = if (!selected) BorderStroke(1.dp, cs.outlineVariant.copy(alpha = 0.4f)) else null,
        modifier = if (clickable) Modifier.clickable(onClick = onClick) else Modifier,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) cs.onPrimary else cs.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
