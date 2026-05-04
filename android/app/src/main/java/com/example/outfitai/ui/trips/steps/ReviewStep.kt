package com.example.outfitai.ui.trips.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.outfitai.core.media.mediaUrl
import com.example.outfitai.data.model.DayForecastDto
import com.example.outfitai.data.model.GeneratedOutfitDto
import com.example.outfitai.data.model.ItemMinimalDto
import com.example.outfitai.data.model.TripPlanResponseDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

// ── DESIGN.md tokens ─────────────────────────────────────────────────────────
private val SurfaceCard = Color(0xFFF2F2F7)       // Level 1 container
private val HeroTile    = Color(0xFFF9F9F9)       // Clothing hero card background
private val GlassBar    = Color(0xFFFFFFFF)       // Floating bar (alpha applied)
private const val ContentMaxWidth = 560

@Composable
fun ReviewStep(
    plan: TripPlanResponseDto?,
    isSaving: Boolean,
    error: String?,
    onSave: (String) -> Unit,
    onClearError: () -> Unit,
) {
    if (plan == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var showSaveDialog by remember { mutableStateOf(false) }
    val defaultName = remember(plan) { "${plan.city} · ${plan.startDate} – ${plan.endDate}" }

    if (showSaveDialog) {
        SaveTripDialog(
            defaultName = defaultName,
            isSaving = isSaving,
            onDismiss = { showSaveDialog = false },
            onConfirm = { name ->
                showSaveDialog = false
                onSave(name)
            },
        )
    }

    error?.let { msg ->
        Snackbar(
            action = { TextButton(onClick = onClearError) { Text("Dismiss") } },
            modifier = Modifier.padding(16.dp),
        ) { Text(msg) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Centered scrollable content (max-width clamps on wider screens)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = ContentMaxWidth.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp),
                ) {
                    Text(
                        "${plan.flag} ${plan.city}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    val fmt = DateTimeFormatter.ofPattern("MMM d")
                    val start = LocalDate.parse(plan.startDate).format(fmt)
                    val end = LocalDate.parse(plan.endDate).format(fmt)
                    val nights = java.time.temporal.ChronoUnit.DAYS.between(
                        LocalDate.parse(plan.startDate), LocalDate.parse(plan.endDate)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "$nights nights · $start – $end · ${plan.bagSize.replace('_', ' ')}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Global warnings
            if (plan.globalWarnings.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            plan.globalWarnings.forEach { w ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Warning, null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        w,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Forecast strip — soft-gray card, centered
            item {
                SectionTitle("Forecast")
                ForecastStrip(forecast = plan.forecast)
                Spacer(Modifier.height(32.dp))
            }

            // Outfit cards
            item { SectionTitle("Your Outfits") }

            items(plan.outfits) { outfit ->
                OutfitDayCard(outfit = outfit)
                Spacer(Modifier.height(12.dp))
            }

            item { Spacer(Modifier.height(120.dp)) } // room for floating bar
        }

        // Floating glassmorphic save bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            Surface(
                color = GlassBar.copy(alpha = 0.85f),
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = ContentMaxWidth.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(
                        onClick = { showSaveDialog = true },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .navigationBarsPadding()
                            .height(56.dp),
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Save Collection",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 12.dp),
    )
}

@Composable
private fun ForecastStrip(forecast: List<DayForecastDto>) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = SurfaceCard,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (forecast.size <= 5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                forecast.forEach { day -> ForecastDay(day) }
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                items(forecast) { day -> ForecastDay(day) }
            }
        }
    }
}

@Composable
private fun ForecastDay(day: DayForecastDto) {
    val date = LocalDate.parse(day.date)
    val dayName = date.dayOfWeek.getDisplayName(
        java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            dayName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Icon(
            weatherIcon(day.weatherCode, day.precipMm),
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "${day.tempMaxC.roundToInt()}°",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            "${day.tempMinC.roundToInt()}°",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun weatherIcon(code: Int, precipMm: Double): ImageVector = when {
    precipMm >= 1.0 -> Icons.Default.Grain
    code in 1..2 -> Icons.Default.WbCloudy
    code >= 3 -> Icons.Default.Cloud
    else -> Icons.Default.LightMode
}

@Composable
private fun OutfitDayCard(outfit: GeneratedOutfitDto) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = SurfaceCard,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header row: day label + chips
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (outfit.isTravel) {
                    Icon(
                        Icons.Default.LightMode,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    outfit.dayLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
            }

            // Chip row
            if (outfit.weatherNote.isNotEmpty() || (!outfit.isTravel && !outfit.style.isNullOrEmpty())) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (outfit.weatherNote.isNotEmpty()) {
                        TagChip(outfit.weatherNote, isPrimary = false)
                    }
                    if (!outfit.isTravel && !outfit.style.isNullOrEmpty()) {
                        TagChip(
                            outfit.style.replaceFirstChar { it.uppercaseChar() },
                            isPrimary = true,
                        )
                    }
                }
            }

            // Outfit warnings
            if (outfit.warnings.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                outfit.warnings.forEach { warning ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp),
                    ) {
                        Icon(
                            Icons.Default.Warning, null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            warning,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // 2x2 hero grid of outfit items
            OutfitItemGrid(outfit)
        }
    }
}

@Composable
private fun OutfitItemGrid(outfit: GeneratedOutfitDto) {
    val slots = listOfNotNull(
        outfit.slots.top?.let { it to "Top" },
        outfit.slots.bottom?.let { it to "Bottom" },
        outfit.slots.shoes?.let { it to "Shoes" },
        outfit.slots.outer?.let { it to "Outer" },
        outfit.slots.bag?.let { it to "Bag" },
    )
    if (slots.isEmpty()) return

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        slots.forEach { (item, label) ->
            HeroTileBox(item, label, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun HeroTileBox(item: ItemMinimalDto?, slotLabel: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = HeroTile,
        modifier = modifier.aspectRatio(0.72f),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (item != null) {
                val imageFile = item.imageNoBgName ?: item.imageOriginalName
                AsyncImage(
                    model = mediaUrl(imageFile),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                )
            } else {
                Text(
                    slotLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
private fun TagChip(text: String, isPrimary: Boolean) {
    val bg = if (isPrimary) Color.Black else Color.White
    val fg = if (isPrimary) Color.White else Color.Black
    Surface(
        shape = CircleShape,
        color = bg,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = fg,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun SaveTripDialog(
    defaultName: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf(defaultName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Save Collection", fontWeight = FontWeight.SemiBold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Collection name") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank() && !isSaving,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White,
                ),
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
