package com.example.outfitai.ui.outfits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PillUnselectedBg = Color(0xFFF0F0F0)
private val PillSelectedBg   = Color(0xFF3A3A3A)
private val PillUnselectedFg = Color(0xFF666666)
private val PillSelectedFg   = Color.White

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null
) {
    val bg = if (selected) PillSelectedBg else PillUnselectedBg
    val fg = if (selected) PillSelectedFg else PillUnselectedFg

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = label,
            color = fg,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitFilterDialog(
    initialState: OutfitFilterState,
    onDismiss: () -> Unit,
    onApply: (style: String?, climate: String?) -> Unit
) {
    var style by remember { mutableStateOf(initialState.style) }
    var climate by remember { mutableStateOf(initialState.climate) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Style",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Casual", "Athleisure", "Formal").forEach { opt ->
                    FilterPill(
                        label = opt,
                        selected = style == opt,
                        onClick = { style = if (style == opt) null else opt }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Weather",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    Triple("Cold", Icons.Filled.AcUnit, Unit),
                    Triple("Warm", Icons.Filled.WbSunny, Unit),
                    Triple("Rainy", Icons.Filled.Umbrella, Unit)
                ).forEach { (opt, icon, _) ->
                    FilterPill(
                        label = opt,
                        selected = climate == opt,
                        onClick = { climate = if (climate == opt) null else opt },
                        icon = icon
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { style = null; climate = null },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PillUnselectedBg,
                        contentColor = Color(0xFF333333)
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Clear filters", fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = { onApply(style, climate) },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PillSelectedBg,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Apply", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
