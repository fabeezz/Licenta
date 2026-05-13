package com.example.outfitai.ui.wardrobe

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import com.example.outfitai.core.media.mediaUrl
import com.example.outfitai.data.model.ItemOutDto
import com.example.outfitai.ui.components.LoomEmptyState
import com.example.outfitai.ui.components.LoomImage
import com.example.outfitai.ui.components.LoomTile
import com.example.outfitai.ui.theme.Spacing

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun PiecesContent(
    state: WardrobeUiState,
    onItemClick: (Int) -> Unit,
    bottomPadding: Dp,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading && state.items.isEmpty() -> Box(
            modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        state.error != null && state.items.isEmpty() -> Box(
            modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text     = state.error,
                color    = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(Spacing.xl),
            )
        }
        state.items.isEmpty() -> LoomEmptyState(
            message  = if (state.searchQuery.isNotBlank())
                "No matches for \"${state.searchQuery}\""
            else
                "No items yet.\nTap + to upload your first piece.",
            modifier = modifier.fillMaxWidth(),
        )
        else -> LazyVerticalGrid(
            columns               = GridCells.Fixed(3),
            contentPadding        = PaddingValues(bottom = bottomPadding + Spacing.lg),
            verticalArrangement   = Arrangement.spacedBy(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier              = modifier,
        ) {
            items(state.items, key = { it.id }) { item ->
                ItemTile(
                    item                    = item,
                    onClick                 = { onItemClick(item.id) },
                    sharedTransitionScope   = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    modifier                = Modifier.animateItem(),
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun ItemTile(
    item: ItemOutDto,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    val filename = item.imageNoBgName ?: item.imageOriginalName
    LoomTile(
        onClick  = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f),
    ) {
        with(sharedTransitionScope) {
            LoomImage(
                model              = mediaUrl(filename),
                contentDescription = item.category,
                contentScale       = ContentScale.Fit,
                modifier           = Modifier
                    .fillMaxSize()
                    .padding(Spacing.sm)
                    .sharedElement(
                        rememberSharedContentState(key = "item-image-${item.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    ),
            )
        }
    }
}
