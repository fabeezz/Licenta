package com.example.outfitai.ui.wardrobe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import com.example.outfitai.core.media.mediaUrl
import com.example.outfitai.ui.components.LoomImage
import com.example.outfitai.data.model.ItemMinimalDto
import com.example.outfitai.data.model.OutfitSavedDto
import com.example.outfitai.ui.components.LoomEmptyState
import com.example.outfitai.ui.components.LoomTile
import com.example.outfitai.ui.theme.Spacing

@Composable
internal fun FitsContent(
    state: WardrobeUiState,
    onOutfitClick: (Int) -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading && state.outfits.isEmpty() -> Box(
            modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        state.error != null && state.outfits.isEmpty() -> Box(
            modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text     = state.error,
                color    = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(Spacing.xl),
            )
        }
        state.outfits.isEmpty() -> LoomEmptyState(
            message  = "No outfits saved yet.",
            modifier = modifier.fillMaxWidth(),
        )
        else -> LazyVerticalGrid(
            columns               = GridCells.Fixed(2),
            contentPadding        = PaddingValues(bottom = bottomPadding + Spacing.lg),
            verticalArrangement   = Arrangement.spacedBy(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier              = modifier,
        ) {
            items(state.outfits, key = { it.id }) { outfit ->
                FitCard(
                    outfit   = outfit,
                    onClick  = { onOutfitClick(outfit.id) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
internal fun FitCard(outfit: OutfitSavedDto, onClick: () -> Unit, modifier: Modifier = Modifier) {
    LoomTile(
        onClick  = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.padding(Spacing.md)) {
                if (outfit.outer != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            FitCardItem(outfit.outer, Modifier.weight(1f))
                            FitCardItem(outfit.top, Modifier.weight(1f))
                        }
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            FitCardItem(outfit.bottom, Modifier.weight(1f))
                            FitCardItem(outfit.shoe, Modifier.weight(1f))
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        FitCardItem(outfit.top, Modifier.weight(1.5f))
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            FitCardItem(outfit.bottom, Modifier.weight(1f))
                            FitCardItem(outfit.shoe, Modifier.weight(1f))
                        }
                    }
                }
            }
            OutfitSourceBadge(
                source   = outfit.sourceEnum,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Spacing.sm),
            )
        }
    }
}

@Composable
internal fun FitCardItem(item: ItemMinimalDto, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center,
    ) {
        LoomImage(
            model              = mediaUrl(item.imageNoBgName ?: item.imageOriginalName),
            contentDescription = null,
            contentScale       = ContentScale.Fit,
            modifier           = Modifier
                .padding(Spacing.xs)
                .fillMaxSize(),
        )
    }
}
