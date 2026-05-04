package com.example.outfitai.ui.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.model.DestinationDto
import com.example.outfitai.data.model.DayActivitiesDto
import com.example.outfitai.data.model.GeneratedOutfitInDto
import com.example.outfitai.data.model.TripGenerateRequestDto
import com.example.outfitai.data.model.TripSaveRequestDto
import com.example.outfitai.domain.usecase.trips.GenerateTripPlanUseCase
import com.example.outfitai.domain.usecase.trips.GetDestinationsUseCase
import com.example.outfitai.domain.usecase.trips.SaveTripUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TripPlannerViewModel @Inject constructor(
    private val getDestinations: GetDestinationsUseCase,
    private val generatePlan: GenerateTripPlanUseCase,
    private val saveTripUseCase: SaveTripUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(TripPlannerUiState())
    val state = _state.asStateFlow()

    init {
        loadDestinations()
    }

    private fun loadDestinations() {
        viewModelScope.launch {
            _state.update { it.copy(destinationsLoading = true) }
            when (val result = getDestinations()) {
                is Resource.Success -> _state.update {
                    it.copy(destinations = result.data ?: emptyList(), destinationsLoading = false)
                }
                is Resource.Error -> _state.update {
                    it.copy(error = result.message, destinationsLoading = false)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun selectDestination(dest: DestinationDto) {
        _state.update { it.copy(selectedDestination = dest) }
    }

    fun goToStep(step: TripStep) {
        _state.update { it.copy(step = step, error = null) }
    }

    fun setStartDate(date: LocalDate) {
        _state.update { state ->
            val endDate = if (state.endDate != null && date > state.endDate) null else state.endDate
            val pruned = state.dayActivities.filterKeys { k ->
                k >= date && (endDate == null || k <= endDate)
            }
            state.copy(startDate = date, endDate = endDate, dayActivities = pruned)
        }
    }

    fun setEndDate(date: LocalDate) {
        _state.update { state ->
            val start = state.startDate
            val pruned = state.dayActivities.filterKeys { k ->
                k <= date && (start == null || k >= start)
            }
            state.copy(endDate = date, dayActivities = pruned)
        }
    }

    fun toggleActivityForDay(date: LocalDate, activityKey: String) {
        _state.update { state ->
            val current = state.dayActivities[date]?.toMutableSet() ?: mutableSetOf()
            if (activityKey in current) current.remove(activityKey) else current.add(activityKey)
            state.copy(dayActivities = state.dayActivities + (date to current))
        }
    }

    fun setBagSize(size: BagSize) {
        _state.update { it.copy(bagSize = size) }
    }

    fun toggleActivity(key: String) {
        _state.update { state ->
            val updated = state.selectedActivities.toMutableSet()
            if (key in updated) updated.remove(key) else updated.add(key)
            state.copy(selectedActivities = updated)
        }
    }

    fun generateTrip() {
        val s = _state.value
        val dest = s.selectedDestination ?: return
        val start = s.startDate ?: return
        val end = s.endDate ?: return

        viewModelScope.launch {
            _state.update { it.copy(step = TripStep.LOADING, isLoading = true, error = null) }

            val dayActivitiesList = s.dayActivities
                .takeIf { it.isNotEmpty() }
                ?.map { (date, acts) -> DayActivitiesDto(date = date.toString(), activities = acts.toList()) }

            val request = TripGenerateRequestDto(
                cityKey = dest.key,
                startDate = start.toString(),
                endDate = end.toString(),
                bagSize = s.bagSize.key,
                activities = s.selectedActivities.toList(),
                dayActivities = dayActivitiesList,
            )

            when (val result = generatePlan(request)) {
                is Resource.Success -> _state.update {
                    it.copy(plan = result.data, isLoading = false, step = TripStep.REVIEW)
                }
                is Resource.Error -> _state.update {
                    it.copy(error = result.message, isLoading = false, step = TripStep.ASSIGN_ACTIVITIES)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun saveTrip(collectionName: String) {
        val s = _state.value
        val dest = s.selectedDestination ?: return
        val start = s.startDate ?: return
        val end = s.endDate ?: return
        val plan = s.plan ?: return

        val outfitDtos = plan.outfits.map { o ->
            GeneratedOutfitInDto(
                dayLabel = o.dayLabel,
                isTravel = o.isTravel,
                topId = o.slots.top?.id,
                bottomId = o.slots.bottom?.id,
                shoeId = o.slots.shoes?.id,
                outerId = o.slots.outer?.id,
                bagId = o.slots.bag?.id,
                style = o.style,
                weatherTags = o.weatherTags,
            )
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            val request = TripSaveRequestDto(
                cityKey = dest.key,
                startDate = start.toString(),
                endDate = end.toString(),
                bagSize = s.bagSize.key,
                activities = s.selectedActivities.toList(),
                collectionName = collectionName,
                outfits = outfitDtos,
            )

            when (val result = saveTripUseCase(request)) {
                is Resource.Success -> _state.update {
                    it.copy(isSaving = false, savedCollectionId = result.data?.collectionId)
                }
                is Resource.Error -> _state.update {
                    it.copy(isSaving = false, error = result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
