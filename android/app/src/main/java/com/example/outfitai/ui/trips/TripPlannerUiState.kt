package com.example.outfitai.ui.trips

import com.example.outfitai.data.model.DestinationDto
import com.example.outfitai.data.model.TripPlanResponseDto
import java.time.LocalDate

enum class TripStep {
    WHERE_TO,
    DATES,
    BAG_TYPE,
    ACTIVITIES,
    ASSIGN_ACTIVITIES,
    LOADING,
    REVIEW,
}

enum class BagSize(val key: String, val label: String, val emoji: String, val description: String) {
    CARRY_ON("carry_on", "Carry-on only", "🎒", "Light packing, fewer outfits"),
    CHECKED("checked", "Checked bag", "🧳", "More variety, room for shoes"),
    BOTH("both", "Carry-on + checked", "🎒🧳", "Maximum flexibility for longer trips"),
}

data class TripPlannerUiState(
    val step: TripStep = TripStep.WHERE_TO,
    val destinations: List<DestinationDto> = emptyList(),
    val destinationsLoading: Boolean = false,
    val selectedDestination: DestinationDto? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val bagSize: BagSize = BagSize.CARRY_ON,
    val selectedActivities: Set<String> = emptySet(),
    /** Maps each trip date to the activities assigned to that day. */
    val dayActivities: Map<LocalDate, Set<String>> = emptyMap(),
    val plan: TripPlanResponseDto? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedCollectionId: Int? = null,
)
