package com.example.outfitai.ui.outfits

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitFilterDialog(
    initialState: OutfitFilterState,
    onDismiss: () -> Unit,
    onApply: (style: String?, climate: String?, colors: Set<String>) -> Unit
) {
    var style by remember { mutableStateOf(initialState.style) }
    var climate by remember { mutableStateOf(initialState.climate) }
    var colors by remember { mutableStateOf(initialState.colors) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Style
            Text(
                text = "Style",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Casual", "Athleisure", "Formal").forEach { opt ->
                    FilterChip(
                        selected = style == opt,
                        onClick = { style = if (style == opt) null else opt },
                        label = { Text(opt) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Color
            Text(
                text = "Color",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Light", "Dark", "Colorful").forEach { opt ->
                    FilterChip(
                        selected = colors.contains(opt),
                        onClick = {
                            val newSet = colors.toMutableSet()
                            if (newSet.contains(opt)) newSet.remove(opt) else newSet.add(opt)
                            colors = newSet
                        },
                        label = { Text(opt) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Climate
            Text(
                text = "Climate",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Cold", "Warm", "Rainy").forEach { opt ->
                    FilterChip(
                        selected = climate == opt,
                        onClick = { climate = if (climate == opt) null else opt },
                        label = { Text(opt) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    style = null
                    climate = null
                    colors = emptySet()
                }) {
                    Text("Clear All")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onApply(style, climate, colors) }) {
                    Text("Apply")
                }
            }
        }
    }
}
