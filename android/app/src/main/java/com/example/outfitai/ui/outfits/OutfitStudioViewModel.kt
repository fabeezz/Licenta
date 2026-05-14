package com.example.outfitai.ui.outfits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.location.LocationStore
import com.example.outfitai.data.model.OutfitCreateDto
import com.example.outfitai.Config
import com.example.outfitai.domain.usecase.auth.GetCurrentUserUseCase
import com.example.outfitai.domain.usecase.outfits.CreateOutfitUseCase
import com.example.outfitai.domain.usecase.outfits.SuggestOutfitUseCase
import com.example.outfitai.domain.usecase.wardrobe.GetFilteredWardrobeUseCase
import com.example.outfitai.domain.usecase.weather.GetTodayWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class OutfitStudioViewModel @Inject constructor(
    private val getFilteredWardrobe: GetFilteredWardrobeUseCase,
    private val createOutfit: CreateOutfitUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val getTodayWeather: GetTodayWeatherUseCase,
    private val suggestOutfit: SuggestOutfitUseCase,
    private val locationStore: LocationStore,
) : ViewModel() {

    private val _state = MutableStateFlow(OutfitStudioUiState())
    val state = _state.asStateFlow()

    init {
        loadUsername()
        loadAllSlots()
    }

    private fun loadUsername() {
        viewModelScope.launch {
            val result = getCurrentUser()
            if (result is Resource.Success) {
                _state.update { it.copy(username = result.data?.username ?: "") }
            }
        }
    }

    private fun loadAllSlots() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val fState = _state.value.filterState

            val occ = when (fState.style) {
                "Casual"     -> "casual"
                "Athleisure" -> "sporty"
                "Formal"     -> "formal"
                else         -> null
            }

            runCatching {
                val slots = Slot.entries.map { slot ->
                    async {
                        val categories = slotCategories(slot)
                        var weatherOverride: String? = null
                        var materialOverride: String? = null
                        var skip = false

                        if (fState.climate == "Cold") {
                            weatherOverride = when (slot) {
                                Slot.OUTER -> "cold"
                                Slot.TOP -> null
                                Slot.BOTTOM, Slot.SHOES -> "all-weather,cold"
                            }
                        } else if (fState.climate == "Warm") {
                            if (slot == Slot.OUTER) skip = true
                            else weatherOverride = "warm,all-weather"
                        } else if (fState.climate == "Rainy") {
                            when (slot) {
                                Slot.OUTER -> materialOverride = "nylon"
                                Slot.TOP -> Unit
                                Slot.BOTTOM -> weatherOverride = "all-weather,cold"
                                Slot.SHOES -> weatherOverride = "all-weather"
                            }
                        }

                        if (skip) {
                            slot to SlotItems()
                        } else {
                            val items = categories.map { cat ->
                                async {
                                    val result = getFilteredWardrobe(
                                        category = cat,
                                        style = occ,
                                        colors = null,
                                        weather = weatherOverride,
                                        material = materialOverride,
                                        limit = 200,
                                    )
                                    if (result is Resource.Success) result.data else emptyList()
                                }
                            }.awaitAll().flatten()
                            slot to SlotItems(items = items)
                        }
                    }
                }.awaitAll().toMap()
                slots
            }.onSuccess { slots ->
                _state.update { s ->
                    s.copy(
                        isLoading = false,
                        top = slots[Slot.TOP] ?: SlotItems(),
                        bottom = slots[Slot.BOTTOM] ?: SlotItems(),
                        outer = slots[Slot.OUTER] ?: SlotItems(),
                        shoes = slots[Slot.SHOES] ?: SlotItems(),
                    )
                }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = "Failed to load wardrobe: ${e.message}") }
            }
        }
    }

    fun openContextSheet() {
        val s = _state.value
        val needsFetch = s.weatherFetchedAt == null ||
            Instant.now().epochSecond - s.weatherFetchedAt.epochSecond > 1800

        _state.update { it.copy(showContextSheet = true, weatherError = null) }

        if (needsFetch) {
            _state.update { it.copy(isFetchingWeather = true) }
            viewModelScope.launch {
                val loc = locationStore.location.first()
                _state.update { it.copy(locationLabel = loc.label) }
                when (val result = getTodayWeather(loc.lat, loc.lon)) {
                    is Resource.Success -> _state.update {
                        it.copy(
                            isFetchingWeather = false,
                            weatherForecast = result.data,
                            weatherFetchedAt = Instant.now(),
                        )
                    }
                    is Resource.Error -> _state.update {
                        it.copy(isFetchingWeather = false, weatherError = result.message)
                    }
                    Resource.Loading -> Unit
                }
            }
        }
    }

    fun closeContextSheet() { _state.update { it.copy(showContextSheet = false) } }

    fun refreshWeather() {
        _state.update { it.copy(isFetchingWeather = true, weatherError = null) }
        viewModelScope.launch {
            val loc = locationStore.location.first()
            when (val result = getTodayWeather(loc.lat, loc.lon)) {
                is Resource.Success -> _state.update {
                    it.copy(
                        isFetchingWeather = false,
                        weatherForecast = result.data,
                        weatherFetchedAt = Instant.now(),
                    )
                }
                is Resource.Error -> _state.update {
                    it.copy(isFetchingWeather = false, weatherError = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun applyFilters(style: String?, climate: String?) {
        _state.update { s ->
            val newIncludeOuter = when (climate) {
                "Cold", "Rainy" -> true
                "Warm"          -> false
                else            -> s.includeOuter
            }
            s.copy(
                filterState = OutfitFilterState(style, climate),
                showContextSheet = false,
                includeOuter = newIncludeOuter,
            )
        }
        loadAllSlots()
    }

    fun stepSlot(slot: Slot, direction: Int) {
        _state.update { s ->
            val slotItems = s.slotOf(slot)
            if (slotItems.items.isEmpty()) return@update s
            val newIndex = Math.floorMod(slotItems.index + direction, slotItems.items.size)
            s.withSlot(slot, slotItems.copy(index = newIndex))
        }
    }

    fun selectSlotItem(slot: Slot, index: Int) {
        _state.update { s ->
            val items = s.slotOf(slot)
            s.withSlot(slot, items.copy(index = index)).copy(pickerSlot = null)
        }
    }

    fun openPicker(slot: Slot) { _state.update { it.copy(pickerSlot = slot) } }
    fun closePicker() { _state.update { it.copy(pickerSlot = null) } }

    fun toggleLayers() {
        val s = _state.value
        if (!s.includeOuter && s.outer.items.isEmpty()) {
            _state.update { it.copy(snackbarMessage = "Add a jacket, coat or blazer to enable 4‑piece outfits") }
            return
        }
        _state.update { it.copy(includeOuter = !it.includeOuter) }
    }

    fun shuffle() {
        val s = _state.value
        val fState = s.filterState
        val style = when (fState.style) {
            "Casual"     -> "casual"
            "Athleisure" -> "sporty"
            "Formal"     -> "formal"
            else         -> null
        }
        val weather = when (fState.climate) {
            "Cold"  -> "cold"
            "Warm"  -> "warm"
            "Rainy" -> "rainy"
            else    -> null
        }

        viewModelScope.launch {
            val result = suggestOutfit(style = style, weather = weather)
            if (result is Resource.Success) {
                val suggestion = result.data
                _state.update { current ->
                    var next = current
                    fun applySlot(slot: Slot, suggestedId: Int?) {
                        val slotItems = current.slotOf(slot)
                        if (suggestedId != null) {
                            val idx = slotItems.items.indexOfFirst { it.id == suggestedId }
                            if (idx >= 0) {
                                next = next.withSlot(slot, slotItems.copy(index = idx))
                                return
                            }
                        }
                        if (slotItems.items.size > 1) {
                            next = next.withSlot(slot, slotItems.copy(index = Random.nextInt(slotItems.items.size)))
                        }
                    }
                    applySlot(Slot.TOP, suggestion?.top)
                    applySlot(Slot.BOTTOM, suggestion?.bottom)
                    applySlot(Slot.SHOES, suggestion?.shoes)
                    if (current.includeOuter) applySlot(Slot.OUTER, suggestion?.outer)
                    next
                }
            } else {
                _state.update { current ->
                    var next = current
                    for (slot in Slot.entries) {
                        val items = current.slotOf(slot)
                        if (slot == Slot.OUTER && !current.includeOuter) continue
                        if (items.items.size > 1) {
                            next = next.withSlot(slot, items.copy(index = Random.nextInt(items.items.size)))
                        }
                    }
                    next
                }
            }
        }
    }

    fun openSaveDialog() {
        val filter = _state.value.filterState
        val prefilledWeather = filter.climate?.lowercase()?.let { listOf(it) } ?: emptyList()
        val prefilledStyle = when (filter.style) {
            "Casual"     -> "casual"
            "Formal"     -> "formal"
            "Athleisure" -> "sporty"
            else         -> ""
        }
        _state.update {
            it.copy(
                showSaveDialog = true,
                outfitName = "",
                selectedWeather = prefilledWeather,
                selectedStyle = prefilledStyle,
            )
        }
    }

    fun closeSaveDialog() { _state.update { it.copy(showSaveDialog = false) } }

    fun updateOutfitName(name: String) { _state.update { it.copy(outfitName = name) } }
    fun updateWeather(tags: List<String>) { _state.update { it.copy(selectedWeather = tags) } }
    fun updateStyle(style: String) { _state.update { it.copy(selectedStyle = style) } }

    fun save() {
        val s = _state.value
        val top = s.top.current ?: return
        val bottom = s.bottom.current ?: return
        val shoes = s.shoes.current ?: return
        val outer = if (s.includeOuter) s.outer.current else null

        val finalName = if (s.outfitName.isBlank()) {
            "Outfit ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))}"
        } else {
            s.outfitName
        }

        _state.update { it.copy(isSaving = true, showSaveDialog = false) }
        viewModelScope.launch {
            val dto = OutfitCreateDto(
                name = finalName,
                topId = top.id,
                bottomId = bottom.id,
                shoeId = shoes.id,
                outerId = outer?.id,
                weather = s.selectedWeather,
                style = s.selectedStyle.takeIf { it.isNotBlank() },
                source = "manual",
            )
            when (val result = createOutfit(dto)) {
                is Resource.Success -> _state.update {
                    it.copy(isSaving = false, savedOutfitId = result.data, snackbarMessage = "Outfit saved!")
                }
                is Resource.Error -> _state.update {
                    it.copy(isSaving = false, snackbarMessage = "Save failed: ${result.message}")
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun consumeSnackbar() { _state.update { it.copy(snackbarMessage = null) } }
}

private fun OutfitStudioUiState.slotOf(slot: Slot) = when (slot) {
    Slot.TOP -> top
    Slot.BOTTOM -> bottom
    Slot.OUTER -> outer
    Slot.SHOES -> shoes
}

private fun OutfitStudioUiState.withSlot(slot: Slot, items: SlotItems) = when (slot) {
    Slot.TOP -> copy(top = items)
    Slot.BOTTOM -> copy(bottom = items)
    Slot.OUTER -> copy(outer = items)
    Slot.SHOES -> copy(shoes = items)
}
