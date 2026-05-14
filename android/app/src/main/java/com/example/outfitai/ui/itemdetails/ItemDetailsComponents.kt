package com.example.outfitai.ui.itemdetails

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.outfitai.ui.theme.Spacing
import com.example.outfitai.util.capitalizeFirst

// ── Key-value list rows ────────────────────────────────────────────────────

@Composable
internal fun DetailRow(
    label: String,
    modifier: Modifier = Modifier,
    editable: Boolean = false,
    onClick: (() -> Unit)? = null,
    valueContent: @Composable RowScope.() -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .run {
                if (editable && onClick != null) clickable(onClick = onClick) else this
            }
            .padding(vertical = Spacing.md),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = cs.onSurfaceVariant,
        )
        Spacer(Modifier.width(Spacing.md))
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            valueContent()
        }
        if (editable && onClick != null) {
            Spacer(Modifier.width(Spacing.xs))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = cs.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
internal fun DetailValueText(value: String?) {
    val cs = MaterialTheme.colorScheme
    val isEmpty = value.isNullOrBlank()
    Text(
        text = if (isEmpty) "—" else value!!.capitalizeFirst(),
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        color = if (isEmpty) cs.onSurfaceVariant else cs.onSurface,
        maxLines = 2,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DetailChipsRow(items: List<String>) {
    if (items.isEmpty()) {
        DetailValueText(null)
    } else {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            items.forEach { item ->
                OccasionChip(label = item.capitalizeFirst(), selected = true, clickable = false, onClick = {})
            }
        }
    }
}

// ── Color swatches ─────────────────────────────────────────────────────────

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
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .run { if (clickable) clickable(onClick = onClick) else this }
            .padding(Spacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = name.capitalizeFirst(),
            style = MaterialTheme.typography.labelLarge,
            color = cs.onSurface,
            maxLines = 1,
        )
        if (role.isNotEmpty()) {
            Text(
                text = role,
                style = MaterialTheme.typography.labelSmall,
                color = cs.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

// ── Chips ──────────────────────────────────────────────────────────────────

@Composable
internal fun WeatherChips(tags: List<String>) {
    val cs = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        tags.forEach { tag ->
            Surface(
                shape = CircleShape,
                color = cs.surfaceContainer,
                border = BorderStroke(1.dp, cs.outlineVariant.copy(alpha = 0.4f)),
            ) {
                Text(
                    text = tag.capitalizeFirst(),
                    style = MaterialTheme.typography.labelLarge,
                    color = cs.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }
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
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) cs.onPrimary else cs.onSurface,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        )
    }
}
