package com.example.outfitai.ui.outfits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.outfitai.data.model.OutfitOutDto
import com.example.outfitai.util.mediaUrl

@Composable
fun OutfitsRoute(
    onBack: () -> Unit,
    vm: OutfitsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    OutfitsScreen(
        state = state,
        onBack = onBack,
        onSeason = vm::setSeason,
        onOccasion = vm::setOccasion,
        onRefresh = vm::refresh
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OutfitsScreen(
    state: OutfitsUiState,
    onBack: () -> Unit,
    onSeason: (String) -> Unit,
    onOccasion: (String) -> Unit,
    onRefresh: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Outfits") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
                actions = { TextButton(onClick = onRefresh, enabled = !state.isLoading) { Text("Refresh") } }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.season,
                    onValueChange = onSeason,
                    label = { Text("Season (optional)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.occasion,
                    onValueChange = onOccasion,
                    label = { Text("Occasion (optional)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null -> Text(state.error, color = MaterialTheme.colorScheme.error)
                state.outfits.isEmpty() -> Text("Nu am găsit outfit-uri pentru filtrele alese.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.outfits) { outfit ->
                        OutfitCard(outfit)
                    }
                }
            }
        }
    }
}

@Composable
private fun OutfitCard(outfit: OutfitOutDto) {
    ElevatedCard {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Score: ${"%.3f".format(outfit.score)}",
                style = MaterialTheme.typography.titleMedium
            )

            // 2x2 grid (top/bottom/outer/shoes)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutfitImage(outfit.top.imageNoBgName ?: outfit.top.imageOriginalName, "Top", Modifier.weight(1f))
                OutfitImage(outfit.bottom.imageNoBgName ?: outfit.bottom.imageOriginalName, "Bottom", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutfitImage(outfit.outer.imageNoBgName ?: outfit.outer.imageOriginalName, "Outer", Modifier.weight(1f))
                OutfitImage(outfit.shoes.imageNoBgName ?: outfit.shoes.imageOriginalName, "Shoes", Modifier.weight(1f))
            }

            val meta = listOfNotNull(
                outfit.top.dominantColor?.let { "top:$it" },
                outfit.bottom.dominantColor?.let { "bottom:$it" },
                outfit.outer.dominantColor?.let { "outer:$it" },
                outfit.shoes.dominantColor?.let { "shoes:$it" },
            ).joinToString(" • ")

            if (meta.isNotBlank()) {
                Text(meta, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun OutfitImage(filename: String, label: String, modifier: Modifier) {
    Column(modifier) {
        AsyncImage(
            model = mediaUrl(filename),
            contentDescription = label,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        )
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}