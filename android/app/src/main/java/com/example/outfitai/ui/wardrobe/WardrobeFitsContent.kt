package com.example.outfitai.ui.wardrobe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.outfitai.core.media.mediaUrl
import com.example.outfitai.data.model.ItemMinimalDto
import com.example.outfitai.data.model.OutfitSavedDto

@Composable
internal fun FitsContent(
    state: WardrobeUiState,
    onOutfitClick: (Int) -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading && state.outfits.isEmpty() -> Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.error != null && state.outfits.isEmpty() -> Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(20.dp),
            )
        }
        state.outfits.isEmpty() -> Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = "No outfits saved yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(20.dp),
            )
        }
        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = bottomPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier,
        ) {
            items(state.outfits, key = { it.id }) { outfit ->
                FitCard(outfit = outfit, onClick = { onOutfitClick(outfit.id) })
            }
        }
    }
}

@Composable
internal fun FitCard(outfit: OutfitSavedDto, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF9F9F9),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f),
    ) {
        val gap = 4.dp
        Box(modifier = Modifier.padding(12.dp)) {
            if (outfit.outer != null) {
                Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(gap)) {
                        FitCardItem(outfit.outer, Modifier.weight(1f))
                        FitCardItem(outfit.top, Modifier.weight(1f))
                    }
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(gap)) {
                        FitCardItem(outfit.bottom, Modifier.weight(1f))
                        FitCardItem(outfit.shoe, Modifier.weight(1f))
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                    FitCardItem(outfit.top, Modifier.weight(1.5f))
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(gap)) {
                        FitCardItem(outfit.bottom, Modifier.weight(1f))
                        FitCardItem(outfit.shoe, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
internal fun FitCardItem(item: ItemMinimalDto, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = mediaUrl(item.imageNoBgName ?: item.imageOriginalName),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(4.dp)
                .fillMaxSize(),
        )
    }
}
