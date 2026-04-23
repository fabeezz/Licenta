package com.example.outfitai.ui.outfits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.outfitai.data.model.ItemConstants
import com.example.outfitai.data.model.ItemOutDto
import com.example.outfitai.ui.common.FormDropdownSelector
import com.example.outfitai.ui.common.FormTextField
import com.example.outfitai.util.mediaUrl

@Composable
fun SaveOutfitDialog(
    state: OutfitStudioUiState,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onNameChange: (String) -> Unit,
    onSeasonChange: (String) -> Unit,
    onOccasionChange: (String) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                        Text(
                            text = "Save Outfit",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.size(48.dp))
                    }
                }
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp,
                    modifier = Modifier.imePadding(),
                ) {
                    Button(
                        onClick = onSave,
                        enabled = !state.isSaving,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .heightIn(min = 56.dp),
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                "Confirm Save",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Spacer(Modifier.height(8.dp))

                // --- Visual Preview ---
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                ) {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val availH = maxHeight
                        val availW = maxWidth
                        val gap = 8.dp

                        if (state.includeOuter) {
                            StaticFourPieceLayout(state, availH, availW, gap)
                        } else {
                            StaticThreePieceLayout(state, availH, availW, gap)
                        }
                    }
                }

                // --- Form Fields ---
                FormTextField(
                    label = "Outfit Name",
                    value = state.outfitName,
                    onValueChange = onNameChange,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormDropdownSelector(
                        label = "Season",
                        selectedOption = state.selectedSeason,
                        options = listOf("") + ItemConstants.SEASONS,
                        onOptionSelected = onSeasonChange,
                        modifier = Modifier.weight(1f),
                    )
                    FormDropdownSelector(
                        label = "Occasion",
                        selectedOption = state.selectedOccasion,
                        options = listOf("") + ItemConstants.OCCASIONS,
                        onOptionSelected = onOccasionChange,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StaticThreePieceLayout(
    state: OutfitStudioUiState,
    availH: Dp,
    availW: Dp,
    gap: Dp,
) {
    val sizeByH = (availH - gap * 2) / 2.75f
    val itemSize = minOf(sizeByH, availW)

    Column(
        modifier = Modifier.width(itemSize),
        verticalArrangement = Arrangement.spacedBy(gap),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PreviewItem(state.top.current, itemSize, itemSize)
        PreviewItem(state.bottom.current, itemSize, itemSize)
        PreviewItem(state.shoes.current, itemSize, itemSize * 0.75f)
    }
}

@Composable
private fun StaticFourPieceLayout(
    state: OutfitStudioUiState,
    availH: Dp,
    availW: Dp,
    gap: Dp,
) {
    val itemW = (availW - gap) / 2
    val itemH = (availH - gap) / 2

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(gap),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(gap),
            modifier = Modifier.fillMaxWidth()
        ) {
            PreviewItem(state.outer.current, itemW, itemH)
            PreviewItem(state.top.current, itemW, itemH)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(gap),
            modifier = Modifier.fillMaxWidth()
        ) {
            PreviewItem(state.bottom.current, itemW, itemH)
            PreviewItem(state.shoes.current, itemW, itemH)
        }
    }
}

@Composable
private fun PreviewItem(
    item: ItemOutDto?,
    width: Dp,
    height: Dp,
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center
    ) {
        if (item != null) {
            AsyncImage(
                model = mediaUrl(item.imageNoBgName ?: item.imageOriginalName),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.padding(4.dp).fillMaxSize()
            )
        }
    }
}
