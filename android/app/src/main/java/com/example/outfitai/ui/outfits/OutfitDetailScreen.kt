package com.example.outfitai.ui.outfits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.outfitai.data.model.ItemMinimalDto
import com.example.outfitai.ui.components.LoomImage
import com.example.outfitai.data.model.OutfitSavedDto
import com.example.outfitai.core.ui.color.colorNameToComposeColor
import com.example.outfitai.core.media.mediaUrl
import com.example.outfitai.ui.components.LoomConfirmDialog
import com.example.outfitai.ui.components.LoomTopBarWithBack
import com.example.outfitai.ui.theme.Elevation
import com.example.outfitai.ui.theme.Spacing
import com.example.outfitai.util.capitalizeFirst

@Composable
fun OutfitDetailRoute(
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    vm: OutfitDetailViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            onDeleted()
        }
    }

    if (state.showDeleteConfirmation) {
        LoomConfirmDialog(
            title = "Delete this outfit?",
            message = "The outfit will be removed from your wardrobe. This can't be undone.",
            confirmText = "Delete",
            destructive = true,
            isLoading = state.isDeleting,
            onConfirm = vm::confirmDelete,
            onDismiss = vm::dismissDeleteConfirmation,
        )
    }

    OutfitDetailScreen(
        state    = state,
        onBack   = onBack,
        onDelete = vm::requestDelete,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OutfitDetailScreen(
    state: OutfitDetailUiState,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            LoomTopBarWithBack(
                title = state.outfit?.name ?: "",
                onBack = onBack,
                actions = {
                    IconButton(onClick = onDelete, enabled = !state.isDeleting && state.outfit != null) {
                        if (state.isDeleting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = colors.error)
                        }
                    }
                },
            )
        }
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = Spacing.md, vertical = Spacing.md),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.error != null -> Text(state.error, color = colors.error)
                state.outfit != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Metadata Row - Centered
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            state.outfit.weather.forEach { tag ->
                                MetadataChip(tag)
                                Spacer(Modifier.width(4.dp))
                            }
                            state.outfit.style?.let {
                                Spacer(Modifier.width(8.dp))
                                MetadataChip(it)
                            }
                        }

                        // Slot area - Uses Weights for consistency and size
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            val gap = 6.dp

                            if (state.outfit.outer != null) {
                                StaticFourPieceLayout(
                                    outfit = state.outfit,
                                    gap = gap
                                )
                            } else {
                                StaticThreePieceLayout(
                                    outfit = state.outfit,
                                    gap = gap
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Color Palette Section - Centered with Label
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "PALETTE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(12.dp))
                            ColorPalette(outfit = state.outfit)
                        }
                        
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataChip(text: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Text(
            text = text.capitalizeFirst(),
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StaticThreePieceLayout(
    outfit: OutfitSavedDto,
    gap: Dp
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(gap),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StaticSlotTile(item = outfit.top, modifier = Modifier.weight(1f).fillMaxWidth())
        StaticSlotTile(item = outfit.bottom, modifier = Modifier.weight(1f).fillMaxWidth())
        StaticSlotTile(item = outfit.shoe, modifier = Modifier.weight(0.7f).fillMaxWidth())
    }
}

@Composable
private fun StaticFourPieceLayout(
    outfit: OutfitSavedDto,
    gap: Dp
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(gap),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            StaticSlotTile(item = outfit.outer!!, modifier = Modifier.weight(1f).fillMaxHeight())
            StaticSlotTile(item = outfit.top, modifier = Modifier.weight(1f).fillMaxHeight())
        }
        StaticSlotTile(item = outfit.bottom, modifier = Modifier.weight(1f).fillMaxWidth())
        StaticSlotTile(item = outfit.shoe, modifier = Modifier.weight(0.7f).fillMaxWidth())
    }
}

@Composable
private fun StaticSlotTile(
    item: ItemMinimalDto,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
    ) {
        LoomImage(
            model = mediaUrl(item.imageNoBgName ?: item.imageOriginalName),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(Spacing.xs)
                .fillMaxSize()
        )
    }
}

@Composable
private fun ColorPalette(outfit: OutfitSavedDto) {
    val colors = remember(outfit.top.dominantColor, outfit.bottom.dominantColor, outfit.outer?.dominantColor, outfit.shoe.dominantColor) {
        listOfNotNull(
            outfit.top.dominantColor,
            outfit.bottom.dominantColor,
            outfit.outer?.dominantColor,
            outfit.shoe.dominantColor
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.forEach { colorName ->
            val composeColor = colorNameToComposeColor(colorName)
            if (composeColor != Color.Transparent) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = composeColor,
                    shadowElevation = Elevation.Level3,
                    border = if (composeColor == Color.White || colorName.lowercase() == "white" || colorName.lowercase() == "beige") {
                        androidx.compose.foundation.BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.1f))
                    } else {
                        null
                    }
                ) {}
            }
        }
    }
}
