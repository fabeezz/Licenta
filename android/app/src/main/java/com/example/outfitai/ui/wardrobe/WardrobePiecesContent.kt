package com.example.outfitai.ui.wardrobe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.outfitai.core.media.mediaUrl
import com.example.outfitai.data.model.ItemOutDto

@Composable
internal fun PiecesContent(
    state: WardrobeUiState,
    onItemClick: (Int) -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading && state.items.isEmpty() -> Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.error != null && state.items.isEmpty() -> Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(20.dp),
            )
        }
        state.items.isEmpty() -> Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = "No items yet. Tap + to upload your first piece.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(20.dp),
            )
        }
        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(bottom = bottomPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier,
        ) {
            items(state.items, key = { it.id }) { item ->
                ItemTile(item = item, onClick = { onItemClick(item.id) })
            }
        }
    }
}

@Composable
internal fun ItemTile(item: ItemOutDto, onClick: () -> Unit) {
    val filename = item.imageNoBgName ?: item.imageOriginalName
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF9F9F9),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f),
    ) {
        AsyncImage(
            model = mediaUrl(filename),
            contentDescription = item.category,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.High,
        )
    }
}
