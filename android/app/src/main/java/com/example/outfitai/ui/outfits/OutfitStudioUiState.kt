package com.example.outfitai.ui.outfits

import androidx.compose.runtime.Immutable
import com.example.outfitai.data.model.ItemOutDto
import com.example.outfitai.domain.weather.WeatherForecast
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.Instant

@Immutable
data class SlotItems(
    val items: ImmutableList<ItemOutDto> = persistentListOf(),
    val index: Int = 0,
) {
    val current: ItemOutDto? get() = items.getOrNull(index)
}

@Immutable
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
    val pickerSlot: Slot? = null,
    val isSaving: Boolean = false,
    val showSaveDialog: Boolean = false,
    val outfitName: String = "",
    val selectedWeather: ImmutableList<String> = persistentListOf(),
    val selectedStyle: String = "",
    val savedOutfitId: Int? = null,
    val snackbarMessage: String? = null,
    val error: String? = null,
    val filterState: OutfitFilterState = OutfitFilterState(),
    val showContextSheet: Boolean = false,
    val isFetchingWeather: Boolean = false,
    val weatherForecast: WeatherForecast? = null,
    val weatherError: String? = null,
    val locationLabel: String = "",
    val weatherFetchedAt: Instant? = null,
)
