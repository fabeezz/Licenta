package com.example.outfitai.ui.outfits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.model.OutfitCreateDto
import com.example.outfitai.Config
import com.example.outfitai.domain.usecase.auth.GetCurrentUserUseCase
import com.example.outfitai.domain.usecase.outfits.CreateOutfitUseCase
import com.example.outfitai.domain.usecase.wardrobe.GetFilteredWardrobeUseCase
import com.example.outfitai.domain.usecase.weather.GetTodayWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
                                Slot.TOP -> Unit // no weather filter — warm/cold/rainy/all-weather all valid
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
                    val shouldIncludeOuter = when (fState.climate) {
                        "Cold" -> true
                        "Rainy" -> true
                        "Warm" -> false
                        else -> s.includeOuter
                    }
                    s.copy(
                        isLoading = false,
                        includeOuter = shouldIncludeOuter,
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

    fun openFilterDialog() { _state.update { it.copy(showFilterDialog = true) } }
    fun closeFilterDialog() { _state.update { it.copy(showFilterDialog = false) } }

    fun openWeatherSheet() {
        _state.update { it.copy(showWeatherSheet = true, isFetchingWeather = true, weatherError = null) }
        viewModelScope.launch {
            when (val result = getTodayWeather(Config.DEFAULT_LAT, Config.DEFAULT_LON)) {
                is Resource.Success -> _state.update {
                    it.copy(isFetchingWeather = false, weatherForecast = result.data)
                }
                is Resource.Error -> _state.update {
                    it.copy(isFetchingWeather = false, weatherError = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun closeWeatherSheet() { _state.update { it.copy(showWeatherSheet = false) } }

    fun applyWeatherFilter() {
        val forecast = _state.value.weatherForecast ?: return
        val style = _state.value.filterState.style
        applyFilters(style, forecast.toClimate())
        _state.update { it.copy(showWeatherSheet = false) }
    }

    fun applyFilters(style: String?, climate: String?) {
        _state.update { it.copy(filterState = OutfitFilterState(style, climate), showFilterDialog = false) }
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

    fun toggleLayers() {
        val s = _state.value
        if (!s.includeOuter && s.outer.items.isEmpty()) {
            _state.update { it.copy(snackbarMessage = "Add a jacket, coat or blazer to enable 4‑piece outfits") }
            return
        }
        _state.update { it.copy(includeOuter = !it.includeOuter) }
    }

    fun shuffle() {
        _state.update { s ->
            var next = s
            for (slot in Slot.entries) {
                val items = s.slotOf(slot)
                if (slot == Slot.OUTER && !s.includeOuter) continue
                if (items.items.size > 1) {
                    next = next.withSlot(slot, items.copy(index = Random.nextInt(items.items.size)))
                }
            }
            next
        }
    }

    fun openSaveDialog() {
        _state.update { it.copy(showSaveDialog = true, outfitName = "", selectedWeather = emptyList(), selectedStyle = "") }
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
