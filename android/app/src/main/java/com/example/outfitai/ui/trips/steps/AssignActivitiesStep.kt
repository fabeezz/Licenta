package com.example.outfitai.ui.trips.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val SurfaceCard = Color(0xFFF2F2F7)
private val GlassBar = Color(0xFFFFFFFF)
private const val ContentMaxWidth = 560

private val ACTIVITY_LABELS: Map<String, String> = mapOf(
    "walking_tours" to "Walking tours",
    "jogging" to "Jogging",
    "cycling" to "Cycling",
    "yoga" to "Yoga",
    "gym" to "Gym session",
    "swimming" to "Swimming",
    "hiking" to "Hiking",
    "art_galleries" to "Art galleries",
    "museum_visits" to "Museum visits",
    "live_music" to "Live music",
    "theater" to "Theater shows",
    "cinema" to "Cinema",
    "opera" to "Opera / Ballet",
    "cafe_hopping" to "Cafe hopping",
    "wine_bars" to "Wine bars",
    "fine_dining" to "Fine dining",
    "food_markets" to "Food markets",
    "park_visits" to "Park visits",
    "river_cruise" to "River cruise",
    "scenic_walks" to "Scenic walks",
    "beach" to "Beach",
)

@Composable
fun AssignActivitiesStep(
    startDate: LocalDate?,
    endDate: LocalDate?,
    selectedActivities: Set<String>,
    dayActivities: Map<LocalDate, Set<String>>,
    onToggleActivityForDay: (LocalDate, String) -> Unit,
    onGenerate: () -> Unit,
) {
    val days = buildDayList(startDate, endDate)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = ContentMaxWidth.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp),
                ) {
                    Text(
                        "Plan your days",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Assign activities to each day — each gets its own outfit style.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(days) { date ->
                val assigned = dayActivities[date] ?: emptySet()
                DayCard(
                    date = date,
                    availableActivities = selectedActivities,
                    assignedActivities = assigned,
                    onToggle = { key -> onToggleActivityForDay(date, key) },
                )
                Spacer(Modifier.height(12.dp))
            }

            item { Spacer(Modifier.height(120.dp)) }
        }

        // Floating glassmorphic action bar
        Surface(
            color = GlassBar.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = ContentMaxWidth.dp),
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    onClick = onGenerate,
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
                    Text(
                        "Help Me Pack",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun DayCard(
    date: LocalDate,
    availableActivities: Set<String>,
    assignedActivities: Set<String>,
    onToggle: (String) -> Unit,
) {
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        .replaceFirstChar { it.uppercaseChar() }
    val month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val styleHint = when {
        availableActivities.isEmpty() -> "Pick activities first"
        assignedActivities.isEmpty() -> "Casual day"
        else -> "${assignedActivities.size} planned"
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = SurfaceCard,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        dayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "$month ${date.dayOfMonth}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                ) {
                    Text(
                        styleHint,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }

            if (availableActivities.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    availableActivities.forEach { key ->
                        val label = ACTIVITY_LABELS[key] ?: key
                        val isSelected = key in assignedActivities
                        ActivityPillChip(
                            label = label,
                            selected = isSelected,
                            onClick = { onToggle(key) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityPillChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = if (selected) Color.Black else Color.White,
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (selected) Color.White else Color.Black,
            )
        }
    }
}

private fun buildDayList(startDate: LocalDate?, endDate: LocalDate?): List<LocalDate> {
    if (startDate == null || endDate == null) return emptyList()
    val days = mutableListOf<LocalDate>()
    var current: LocalDate = startDate
    while (!current.isAfter(endDate)) {
        days.add(current)
        current = current.plusDays(1)
    }
    return days
}
