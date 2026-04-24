package com.example.outfitai.ui.outfits

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitFilterDialog(
    initialState: OutfitFilterState,
    onDismiss: () -> Unit,
    onApply: (style: String?, climate: String?) -> Unit
) {
    var style by remember { mutableStateOf(initialState.style) }
    var climate by remember { mutableStateOf(initialState.climate) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Style
            Text(
                text = "Style",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
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

            // Weather
            Text(
                text = "Weather",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    style = null
                    climate = null
                }) {
                    Text("Clear All")
                }
                Button(onClick = { onApply(style, climate) }) {
                    Text("Apply")
                }
            }
        }
    }
}
