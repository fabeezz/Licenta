package com.example.outfitai.ui.itemdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.outfitai.core.ui.color.colorNameToComposeColor
import com.example.outfitai.data.model.ItemConstants

@Composable
fun ColorSelectionDialog(
    title: String,
    currentSelected: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    isMultiple: Boolean = false,
) {
    val cs = MaterialTheme.colorScheme
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = cs.surfaceContainerHigh,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = cs.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.heightIn(max = 400.dp),
                ) {
                    items(ItemConstants.COLORS) { colorName ->
                        ColorOption(
                            name = colorName,
                            isSelected = currentSelected.contains(colorName),
                            onClick = { onSelect(colorName) },
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isMultiple) "Done" else "Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorOption(name: String, isSelected: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val color = colorNameToComposeColor(name, cs.surfaceVariant)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) cs.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, cs.outlineVariant.copy(alpha = 0.3f), CircleShape),
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                    modifier = Modifier.align(Alignment.Center).size(20.dp),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = name.replaceFirstChar { it.uppercaseChar() },
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) cs.onPrimaryContainer else cs.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

internal fun Color.luminance(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue
