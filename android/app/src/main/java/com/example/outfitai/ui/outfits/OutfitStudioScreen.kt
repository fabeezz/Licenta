package com.example.outfitai.ui.outfits

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.outfitai.ui.components.AppBottomBar
import com.example.outfitai.ui.components.BottomBarDest
import com.example.outfitai.ui.upload.rememberUploadLauncher
import com.example.outfitai.core.media.mediaUrl

@Composable
fun OutfitStudioRoute(
    onBack: () -> Unit,
    onWardrobeClick: () -> Unit,
    vm: OutfitStudioViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val upload = rememberUploadLauncher()

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            vm.consumeSnackbar()
        }
    }

    if (state.showSaveDialog) {
        SaveOutfitDialog(
            state = state,
            onDismiss = vm::closeSaveDialog,
            onSave = vm::save,
            onNameChange = vm::updateOutfitName,
            onSeasonChange = vm::updateSeason,
            onOccasionChange = vm::updateOccasion,
        )
    }

    if (state.showFilterDialog) {
        OutfitFilterDialog(
            initialState = state.filterState,
            onDismiss = vm::closeFilterDialog,
            onApply = vm::applyFilters,
        )
    }

    if (state.showWeatherSheet) {
        WeatherForecastSheet(
            isLoading = state.isFetchingWeather,
            forecast = state.weatherForecast,
            error = state.weatherError,
            onDismiss = vm::closeWeatherSheet,
            onApply = vm::applyWeatherFilter,
            onRetry = vm::openWeatherSheet,
        )
    }

    OutfitStudioScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onStep = vm::stepSlot,
        onToggleLayers = vm::toggleLayers,
        onShuffle = vm::shuffle,
        onSave = vm::openSaveDialog,
        onWardrobeClick = onWardrobeClick,
        onAddClick = upload.launch,
        onFilterClick = vm::openFilterDialog,
        onWeatherClick = vm::openWeatherSheet,
    )
}

@Composable
private fun OutfitStudioScreen(
    state: OutfitStudioUiState,
    snackbarHostState: SnackbarHostState,
    onStep: (Slot, Int) -> Unit,
    onToggleLayers: () -> Unit,
    onShuffle: () -> Unit,
    onSave: () -> Unit,
    onWardrobeClick: () -> Unit,
    onAddClick: () -> Unit,
    onFilterClick: () -> Unit,
    onWeatherClick: () -> Unit,
) {
    val canSave = state.top.current != null && state.bottom.current != null && state.shoes.current != null
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.surface,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(modifier = Modifier.padding(bottom = 24.dp)) {
                AppBottomBar(
                    active = BottomBarDest.STUDIO,
                    onStudio = {},
                    onAdd = onAddClick,
                    onWardrobe = onWardrobeClick,
                )
            }
        },
    ) { innerPad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPad.calculateTopPadding()),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                state.error?.let { err ->
                    Text(
                        text = err,
                        color = colors.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 8.dp),
                    )
                }

                // ── Slot area — fills remaining height, no scroll ────────────
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        // standard margin, items go under fixed control buttons
                        .padding(
                            top = 24.dp,
                            start = 20.dp,
                            end = 20.dp,
                            bottom = innerPad.calculateBottomPadding() + 16.dp
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    val availH = maxHeight
                    val availW = maxWidth
                    val gap = 0.dp

                    if (state.isLoading) {
                        CircularProgressIndicator()
                    } else if (state.includeOuter) {
                        FourPieceLayout(
                            state = state,
                            availH = availH,
                            availW = availW,
                            gap = gap,
                            onStep = onStep,
                        )
                    } else {
                        ThreePieceLayout(
                            state = state,
                            availH = availH,
                            availW = availW,
                            gap = gap,
                            onStep = onStep,
                        )
                    }
                }
            }

            // ── Floating right-edge controls (fixed, not scrolling) ──────────
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FloatingControlButton(
                    onClick = onWeatherClick,
                    icon = { Icon(Icons.Outlined.WbSunny, contentDescription = "Weather") },
                )
                FloatingControlButton(
                    onClick = onToggleLayers,
                    icon = { Icon(Icons.Outlined.Layers, contentDescription = "Toggle layers") },
                    highlighted = state.includeOuter,
                )
                FloatingControlButton(
                    onClick = onFilterClick,
                    icon = { Icon(Icons.Default.Tune, contentDescription = "Filter") },
                )
                FloatingControlButton(
                    onClick = onShuffle,
                    icon = { Icon(Icons.Outlined.Shuffle, contentDescription = "Shuffle") },
                )
                FloatingControlButton(
                    onClick = onSave,
                    icon = {
                        if (state.isSaving)
                            Icon(Icons.Filled.Bookmark, contentDescription = "Saving")
                        else
                            Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Save fit")
                    },
                    enabled = canSave,
                )
            }

        }
    }
}

@Composable
private fun ThreePieceLayout(
    state: OutfitStudioUiState,
    availH: Dp,
    availW: Dp,
    gap: Dp,
    onStep: (Slot, Int) -> Unit,
) {
    // 3 items: top (1:1) + bottom (1:1) + shoes (4:3 → height = size * 0.75)
    // total height = size + size + size*0.75 + 2*gap = size*2.75 + 2*gap
    // solve for size: size = (availH - 2*gap) / 2.75
    val sizeByH = (availH - gap * 2) / 2.75f
    val itemSize = minOf(sizeByH, availW)

    Column(
        modifier = Modifier.width(itemSize),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        SwipeableSlotTile(
            slot = state.top,
            label = "Top",
            width = itemSize,
            height = itemSize,
            onStep = { onStep(Slot.TOP, it) },
        )
        SwipeableSlotTile(
            slot = state.bottom,
            label = "Bottom",
            width = itemSize,
            height = itemSize,
            onStep = { onStep(Slot.BOTTOM, it) },
        )
        SwipeableSlotTile(
            slot = state.shoes,
            label = "Shoes",
            width = itemSize,
            height = itemSize * 0.75f,
            onStep = { onStep(Slot.SHOES, it) },
        )
    }
}

@Composable
private fun FourPieceLayout(
    state: OutfitStudioUiState,
    availH: Dp,
    availW: Dp,
    gap: Dp,
    onStep: (Slot, Int) -> Unit,
) {
    // Top row: 2 items side-by-side, aspect 4:5 (portrait)
    // item width = (availW - gap) / 2, height = itemW * 5/4
    val itemW = (availW - gap) / 2
    val rowH = itemW * 1.25f   // 5/4
    // Remaining height split 3:2 between bottom and shoes
    val remaining = availH - rowH - gap * 2
    val bottomH = remaining * 0.6f
    val shoesH = remaining * 0.4f

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(gap),
            modifier = Modifier.fillMaxWidth(),
        ) {
            SwipeableSlotTile(
                slot = state.outer,
                label = "Outer",
                width = itemW,
                height = rowH,
                onStep = { onStep(Slot.OUTER, it) },
            )
            SwipeableSlotTile(
                slot = state.top,
                label = "Top",
                width = itemW,
                height = rowH,
                onStep = { onStep(Slot.TOP, it) },
            )
        }
        SwipeableSlotTile(
            slot = state.bottom,
            label = "Bottom",
            width = availW,
            height = bottomH,
            onStep = { onStep(Slot.BOTTOM, it) },
        )
        SwipeableSlotTile(
            slot = state.shoes,
            label = "Shoes",
            width = availW,
            height = shoesH,
            onStep = { onStep(Slot.SHOES, it) },
        )
    }
}

@Composable
private fun SwipeableSlotTile(
    slot: SlotItems,
    label: String,
    width: Dp,
    height: Dp,
    onStep: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragX by remember { mutableFloatStateOf(0f) }
    val colors = MaterialTheme.colorScheme

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surfaceContainerLow)
            .pointerInput(slot.items.size) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val threshold = 60.dp.toPx()
                        if (dragX > threshold) onStep(-1)
                        if (dragX < -threshold) onStep(+1)
                        dragX = 0f
                    },
                    onHorizontalDrag = { _, delta -> dragX += delta },
                )
            }
            .graphicsLayer { translationX = dragX * 0.35f },
    ) {
        if (slot.items.isEmpty()) {
            EmptySlotHint(label)
        } else {
            val item = slot.current!!
            AsyncImage(
                model = mediaUrl(item.imageNoBgName ?: item.imageOriginalName),
                contentDescription = label,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize(),
            )
        }

        if (slot.items.size > 1) {
            Text(
                text = "${slot.index + 1} / ${slot.items.size}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurface.copy(alpha = 0.4f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp),
            )
        }
    }
}

@Composable
private fun EmptySlotHint(label: String) {
    Text(
        text = "No ${label.lowercase()} added yet",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(16.dp),
    )
}

@Composable
private fun FloatingControlButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    highlighted: Boolean = false,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = if (highlighted) colors.primary else colors.surfaceContainerHighest,
        shadowElevation = 6.dp,
        modifier = Modifier.size(52.dp),
    ) {
        IconButton(onClick = onClick, enabled = enabled) {
            CompositionLocalProvider(
                LocalContentColor provides if (highlighted) colors.onPrimary else colors.onSurface,
            ) {
                icon()
            }
        }
    }
}
