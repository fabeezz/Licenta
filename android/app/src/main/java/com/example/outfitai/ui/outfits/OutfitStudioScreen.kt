package com.example.outfitai.ui.outfits

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.outfitai.ui.components.AppBottomBar
import com.example.outfitai.ui.components.LoomImage
import com.example.outfitai.ui.components.BottomBarDest
import com.example.outfitai.ui.theme.Elevation
import com.example.outfitai.ui.theme.Spacing
import com.example.outfitai.ui.upload.rememberUploadLauncher
import com.example.outfitai.core.media.mediaUrl

@Composable
fun OutfitStudioRoute(
    onBack: () -> Unit,
    onWardrobeClick: () -> Unit,
    onTripClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onInspirationClick: () -> Unit = {},
    vm: OutfitStudioViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
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
            state           = state,
            onDismiss       = vm::closeSaveDialog,
            onSave          = vm::save,
            onNameChange    = vm::updateOutfitName,
            onWeatherChange = vm::updateWeather,
            onStyleChange   = vm::updateStyle,
        )
    }

    if (state.showContextSheet) {
        OutfitContextSheet(
            isLoading        = state.isFetchingWeather,
            forecast         = state.weatherForecast,
            error            = state.weatherError,
            locationLabel    = state.locationLabel,
            initialState     = state.filterState,
            onDismiss        = vm::closeContextSheet,
            onApply          = vm::applyFilters,
            onRefreshWeather = vm::refreshWeather,
        )
    }

    state.pickerSlot?.let { slot ->
        SlotPickerSheet(
            slot     = slot,
            items    = when (slot) {
                Slot.TOP    -> state.top
                Slot.BOTTOM -> state.bottom
                Slot.OUTER  -> state.outer
                Slot.SHOES  -> state.shoes
            },
            onSelect  = vm::selectSlotItem,
            onDismiss = vm::closePicker,
        )
    }

    OutfitStudioScreen(
        state              = state,
        snackbarHostState  = snackbarHostState,
        onStep             = vm::stepSlot,
        onToggleLayers     = vm::toggleLayers,
        onShuffle          = vm::shuffle,
        onSave             = vm::openSaveDialog,
        onWardrobeClick    = onWardrobeClick,
        onTripClick        = onTripClick,
        onProfileClick     = onProfileClick,
        onAddClick         = upload.launch,
        onContextClick     = vm::openContextSheet,
        onInspirationClick = onInspirationClick,
        onOpenPicker       = vm::openPicker,
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
    onTripClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddClick: () -> Unit,
    onContextClick: () -> Unit,
    onInspirationClick: () -> Unit,
    onOpenPicker: (Slot) -> Unit,
) {
    val canSave = state.top.current != null && state.bottom.current != null && state.shoes.current != null
    val colors = MaterialTheme.colorScheme
    var pillExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = colors.surface,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        bottomBar      = {
            Box(modifier = Modifier.padding(bottom = Spacing.xxl)) {
                AppBottomBar(
                    active     = BottomBarDest.STUDIO,
                    onTrip     = onTripClick,
                    onStudio   = {},
                    onAdd      = onAddClick,
                    onWardrobe = onWardrobeClick,
                    onProfile  = onProfileClick,
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
                        text     = err,
                        color    = colors.error,
                        style    = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(horizontal = Spacing.xl)
                            .padding(top = Spacing.lg, bottom = Spacing.sm),
                    )
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(
                            top    = Spacing.xxl,
                            start  = Spacing.xl,
                            end    = Spacing.xl,
                            bottom = innerPad.calculateBottomPadding() + Spacing.lg,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    val availH = maxHeight
                    val availW = maxWidth
                    val gap    = 0.dp

                    if (state.isLoading) {
                        CircularProgressIndicator()
                    } else if (state.includeOuter) {
                        FourPieceLayout(state = state, availH = availH, availW = availW, gap = gap, onStep = onStep, onOpenPicker = onOpenPicker)
                    } else {
                        ThreePieceLayout(state = state, availH = availH, availW = availW, gap = gap, onStep = onStep, onOpenPicker = onOpenPicker)
                    }
                }
            }

            // Right-edge control rail
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                horizontalAlignment = Alignment.End,
            ) {
                // Primary actions
                FloatingControlButton(
                    onClick = onShuffle,
                    icon    = { Icon(Icons.Outlined.Shuffle, contentDescription = "Shuffle") },
                )
                FloatingControlButton(
                    onClick  = onSave,
                    enabled  = canSave,
                    icon     = {
                        if (state.isSaving) Icon(Icons.Filled.Bookmark, contentDescription = "Saving")
                        else Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Save fit")
                    },
                )

                // Expand/collapse toggle
                FloatingControlButton(
                    onClick = { pillExpanded = !pillExpanded },
                    icon    = {
                        Icon(
                            imageVector        = if (pillExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = if (pillExpanded) "Collapse" else "More options",
                        )
                    },
                )

                // Secondary actions — expands downward below the arrow
                AnimatedVisibility(
                    visible = pillExpanded,
                    enter   = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit    = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(top = Spacing.xs),
                    ) {
                        SecondaryPillRow(
                            icon    = Icons.Outlined.CameraAlt,
                            label   = "Match image",
                            onClick = { pillExpanded = false; onInspirationClick() },
                        )
                        SecondaryPillRow(
                            icon    = Icons.Outlined.Layers,
                            label   = if (state.includeOuter) "Remove outer" else "Add outer",
                            onClick = { onToggleLayers() },
                        )
                        SecondaryPillRow(
                            icon    = Icons.Filled.Tune,
                            label   = "Filter",
                            onClick = { pillExpanded = false; onContextClick() },
                        )
                    }
                }
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
    onOpenPicker: (Slot) -> Unit,
) {
    val sizeByH = (availH - gap * 2) / 2.75f
    val itemSize = minOf(sizeByH, availW)

    Column(
        modifier = Modifier.width(itemSize),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        SwipeableSlotTile(slot = state.top, label = "Top", width = itemSize, height = itemSize, onStep = { onStep(Slot.TOP, it) }, onClick = { onOpenPicker(Slot.TOP) })
        SwipeableSlotTile(slot = state.bottom, label = "Bottom", width = itemSize, height = itemSize, onStep = { onStep(Slot.BOTTOM, it) }, onClick = { onOpenPicker(Slot.BOTTOM) })
        SwipeableSlotTile(slot = state.shoes, label = "Shoes", width = itemSize, height = itemSize * 0.75f, onStep = { onStep(Slot.SHOES, it) }, onClick = { onOpenPicker(Slot.SHOES) })
    }
}

@Composable
private fun FourPieceLayout(
    state: OutfitStudioUiState,
    availH: Dp,
    availW: Dp,
    gap: Dp,
    onStep: (Slot, Int) -> Unit,
    onOpenPicker: (Slot) -> Unit,
) {
    val itemW     = (availW - gap) / 2
    val rowH      = itemW * 1.25f
    val remaining = availH - rowH - gap * 2
    val bottomH   = remaining * 0.6f
    val shoesH    = remaining * 0.4f

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(gap)) {
        Row(horizontalArrangement = Arrangement.spacedBy(gap), modifier = Modifier.fillMaxWidth()) {
            SwipeableSlotTile(slot = state.outer, label = "Outer", width = itemW, height = rowH, onStep = { onStep(Slot.OUTER, it) }, onClick = { onOpenPicker(Slot.OUTER) })
            SwipeableSlotTile(slot = state.top, label = "Top", width = itemW, height = rowH, onStep = { onStep(Slot.TOP, it) }, onClick = { onOpenPicker(Slot.TOP) })
        }
        SwipeableSlotTile(slot = state.bottom, label = "Bottom", width = availW, height = bottomH, onStep = { onStep(Slot.BOTTOM, it) }, onClick = { onOpenPicker(Slot.BOTTOM) })
        SwipeableSlotTile(slot = state.shoes, label = "Shoes", width = availW, height = shoesH, onStep = { onStep(Slot.SHOES, it) }, onClick = { onOpenPicker(Slot.SHOES) })
    }
}

@Composable
private fun SwipeableSlotTile(
    slot: SlotItems,
    label: String,
    width: Dp,
    height: Dp,
    onStep: (Int) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragX by remember { mutableFloatStateOf(0f) }
    val colors = MaterialTheme.colorScheme

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(width)
            .height(height)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(colors.surfaceContainerLow)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
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
            LoomImage(
                model              = mediaUrl(item.imageNoBgName ?: item.imageOriginalName),
                contentDescription = label,
                contentScale       = ContentScale.Fit,
                modifier           = Modifier
                    .padding(Spacing.md)
                    .fillMaxSize(),
            )
        }

        if (slot.items.size > 1) {
            Text(
                text     = "${slot.index + 1} / ${slot.items.size}",
                style    = MaterialTheme.typography.labelSmall,
                color    = colors.onSurface.copy(alpha = 0.4f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = Spacing.sm),
            )
        }
    }
}

@Composable
private fun EmptySlotHint(label: String) {
    Text(
        text     = "No ${label.lowercase()} added yet",
        style    = MaterialTheme.typography.bodyMedium,
        color    = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(Spacing.lg),
    )
}

@Composable
private fun SecondaryPillRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape           = MaterialTheme.shapes.extraLarge,
        color           = colors.surfaceContainerHighest,
        shadowElevation = Elevation.Level2,
        modifier        = Modifier.clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurface,
            )
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = colors.onSurface,
                modifier           = Modifier.size(20.dp),
            )
        }
    }
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
        shape           = CircleShape,
        color           = if (highlighted) colors.primary else colors.surfaceContainerHighest,
        shadowElevation = Elevation.Level3,
        modifier        = Modifier.size(52.dp),
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
