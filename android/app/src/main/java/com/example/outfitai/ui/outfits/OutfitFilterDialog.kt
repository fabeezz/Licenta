package com.example.outfitai.ui.outfits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.outfitai.ui.components.LoomButton
import com.example.outfitai.ui.components.LoomButtonVariant
import com.example.outfitai.ui.theme.Spacing

private val STYLE_OPTIONS = listOf(
    "Casual" to Icons.Filled.Checkroom,
    "Athleisure" to Icons.Filled.DirectionsRun,
    "Formal" to Icons.Filled.Work,
)

private val CLIMATE_OPTIONS = listOf(
    "Cold" to Icons.Filled.AcUnit,
    "Warm" to Icons.Filled.WbSunny,
    "Rainy" to Icons.Filled.Umbrella,
)

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
) {
    val cs = MaterialTheme.colorScheme
    val bg = if (selected) cs.primary else cs.surfaceContainer
    val fg = if (selected) cs.onPrimary else cs.onSurfaceVariant

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
    ) {
        icon?.let {
            Icon(
                imageVector        = it,
                contentDescription = null,
                tint               = fg,
                modifier           = Modifier.size(16.dp),
            )
        }
        Text(
            text       = label,
            color      = fg,
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitFilterDialog(
    initialState: OutfitFilterState,
    onDismiss: () -> Unit,
    onApply: (style: String?, climate: String?) -> Unit,
) {
    var style by rememberSaveable { mutableStateOf(initialState.style) }
    var climate by rememberSaveable { mutableStateOf(initialState.climate) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = "Style",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = Spacing.sm),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                STYLE_OPTIONS.forEach { (opt, icon) ->
                    FilterPill(
                        label    = opt,
                        selected = style == opt,
                        onClick  = { style = if (style == opt) null else opt },
                        icon     = icon,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text = "Weather",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = Spacing.sm),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                CLIMATE_OPTIONS.forEach { (opt, icon) ->
                    FilterPill(
                        label    = opt,
                        selected = climate == opt,
                        onClick  = { climate = if (climate == opt) null else opt },
                        icon     = icon,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                LoomButton(
                    text    = "Clear",
                    onClick = { style = null; climate = null },
                    variant = LoomButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                )
                LoomButton(
                    text     = "Apply",
                    onClick  = { onApply(style, climate) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
