package com.example.outfitai.ui.outfits

import com.example.outfitai.data.model.ItemOutDto
import com.example.outfitai.domain.weather.WeatherForecast

data class SlotItems(
    val items: List<ItemOutDto> = emptyList(),
    val index: Int = 0,
) {
    val current: ItemOutDto? get() = items.getOrNull(index)
}

data class OutfitFilterState(
    val style: String? = null,
    val climate: String? = null,
)

data class OutfitStudioUiState(
    val isLoading: Boolean = true,
    val username: String = "",
    val includeOuter: Boolean = false,
    val top: SlotItems = SlotItems(),
    val bottom: SlotItems = SlotItems(),
    val outer: SlotItems = SlotItems(),
    val shoes: SlotItems = SlotItems(),
    val isSaving: Boolean = false,
    val showSaveDialog: Boolean = false,
    val outfitName: String = "",
    val selectedSeason: String = "",
    val selectedOccasion: String = "",
    val savedOutfitId: Int? = null,
    val snackbarMessage: String? = null,
    val error: String? = null,
    val filterState: OutfitFilterState = OutfitFilterState(),
    val showFilterDialog: Boolean = false,
    val showWeatherSheet: Boolean = false,
    val isFetchingWeather: Boolean = false,
    val weatherForecast: WeatherForecast? = null,
    val weatherError: String? = null,
)
