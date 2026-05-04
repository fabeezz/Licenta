package com.example.outfitai.ui.trips.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Activity data — mirrored from backend activity_map.py
private data class ActivityInfo(val key: String, val label: String, val section: String)

private val ALL_ACTIVITIES = listOf(
    ActivityInfo("walking_tours", "Walking tours", "Physical Activities"),
    ActivityInfo("jogging", "Jogging", "Physical Activities"),
    ActivityInfo("cycling", "Cycling", "Physical Activities"),
    ActivityInfo("yoga", "Yoga", "Physical Activities"),
    ActivityInfo("gym", "Gym session", "Physical Activities"),
    ActivityInfo("swimming", "Swimming", "Physical Activities"),
    ActivityInfo("hiking", "Hiking", "Physical Activities"),
    ActivityInfo("art_galleries", "Art galleries", "Culture & Entertainment"),
    ActivityInfo("museum_visits", "Museum visits", "Culture & Entertainment"),
    ActivityInfo("live_music", "Live music", "Culture & Entertainment"),
    ActivityInfo("theater", "Theater shows", "Culture & Entertainment"),
    ActivityInfo("cinema", "Cinema", "Culture & Entertainment"),
    ActivityInfo("opera", "Opera / Ballet", "Culture & Entertainment"),
    ActivityInfo("cafe_hopping", "Cafe hopping", "Food & Drink"),
    ActivityInfo("wine_bars", "Wine bars", "Food & Drink"),
    ActivityInfo("fine_dining", "Fine dining", "Food & Drink"),
    ActivityInfo("food_markets", "Food markets", "Food & Drink"),
    ActivityInfo("park_visits", "Park visits", "Outdoor & Nature"),
    ActivityInfo("river_cruise", "River cruise", "Outdoor & Nature"),
    ActivityInfo("scenic_walks", "Scenic walks", "Outdoor & Nature"),
    ActivityInfo("beach", "Beach", "Outdoor & Nature"),
)

private val SECTIONS = ALL_ACTIVITIES.map { it.section }.distinct()

@Composable
fun ActivitiesStep(
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onContinue: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        ) {
            item {
                Text("Add activities", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Pick activities for tailored outfit recommendations.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
                )
            }

            SECTIONS.forEach { section ->
                val sectionActivities = ALL_ACTIVITIES.filter { it.section == section }
                item {
                    Text(
                        text = section.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                    ) {
                        sectionActivities.forEach { activity ->
                            ActivityChip(
                                label = activity.label,
                                selected = activity.key in selected,
                                onClick = { onToggle(activity.key) },
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
        ) {
            Button(
                onClick = onContinue,
                enabled = selected.isNotEmpty(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color.Black,
                    contentColor = androidx.compose.ui.graphics.Color.White,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .navigationBarsPadding()
                    .height(56.dp),
            ) {
                Text(
                    "Next: Assign to days",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun ActivityChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = {
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            } else {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
        ),
    )
}
