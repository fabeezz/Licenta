package com.example.outfitai.ui.trips.steps

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
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
        LaunchedEffect(msg) {  }
        Snackbar(
            action = {
                TextButton(onClick = onClearError) { Text("Dismiss") }
            },
            modifier = Modifier.padding(16.dp),
        ) { Text(msg) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        ) {
            // Header
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                ) {
                    Text(
                        "${plan.flag} ${plan.city} Trip",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                    val fmt = DateTimeFormatter.ofPattern("MMM d")
                    val start = LocalDate.parse(plan.startDate).format(fmt)
                    val end = LocalDate.parse(plan.endDate).format(fmt)
                    val nights = java.time.temporal.ChronoUnit.DAYS.between(
                        LocalDate.parse(plan.startDate), LocalDate.parse(plan.endDate)
                    )
                    Text(
                        "$nights nights · $start – $end · ${plan.bagSize.replace('_', ' ')}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Global warnings
            if (plan.globalWarnings.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            plan.globalWarnings.forEach { w ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Warning, null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(w, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }
                    }
                }
            }

            // Forecast strip
            item {
                Spacer(Modifier.height(8.dp))
                Text("Forecast", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 12.dp))
                ForecastStrip(forecast = plan.forecast)
                Spacer(Modifier.height(20.dp))
            }

            // Outfit cards
            item {
                Text("Your Outfits", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 12.dp))
            }
            items(plan.outfits) { outfit ->
                OutfitDayCard(outfit = outfit)
                Spacer(Modifier.height(12.dp))
            }

            item { Spacer(Modifier.height(100.dp)) }
        }

        // Bottom save button
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
        ) {
            Button(
                onClick = { showSaveDialog = true },
                enabled = !isSaving,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .navigationBarsPadding()
                    .height(56.dp),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Collection", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ForecastStrip(forecast: List<DayForecastDto>) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            items(forecast) { day ->
                val date = LocalDate.parse(day.date)
                val dayName = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault())
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(dayName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Icon(
                        weatherIcon(day.weatherCode, day.precipMm),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("${day.tempMaxC.roundToInt()}°", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("${day.tempMinC.roundToInt()}°", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
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
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (outfit.isTravel) {
                        Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        outfit.dayLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (outfit.weatherNote.isNotEmpty()) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Text(
                            outfit.weatherNote,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            // Outfit warnings
            outfit.warnings.forEach { warning ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(warning, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
            }

            // Item images
            val items = listOfNotNull(outfit.slots.top, outfit.slots.bottom, outfit.slots.shoes, outfit.slots.outer)
            val columns = when (items.size) {
                1 -> 1
                2 -> 2
                else -> 3
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items) { item ->
                    OutfitItemTile(item)
                }
            }
        }
    }
}

@Composable
private fun OutfitItemTile(item: ItemMinimalDto) {
    val imageFile = item.imageNoBgName ?: item.imageOriginalName
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.size(width = 100.dp, height = 130.dp),
    ) {
        AsyncImage(
            model = mediaUrl(imageFile),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().padding(8.dp),
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
        title = { Text("Save Collection") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Collection name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank() && !isSaving,
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
