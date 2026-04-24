package com.example.outfitai.ui.wardrobe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfitai.core.common.Resource
import com.example.outfitai.domain.usecase.wardrobe.GetFilteredWardrobeUseCase
import com.example.outfitai.domain.usecase.wardrobe.GetWardrobeOutfitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface WardrobeIntent {
    data class SelectTab(val tab: WardrobeTab) : WardrobeIntent
    data class SetCategoryFilter(val value: String?) : WardrobeIntent
    data class SetColorFilter(val value: String?) : WardrobeIntent
    data class SetSeasonFilter(val value: String?) : WardrobeIntent
    data class SetOccasionFilter(val value: String?) : WardrobeIntent
    data object ClearFilters : WardrobeIntent
    data object Refresh : WardrobeIntent
}

private data class ActiveFilters(
    val tab: WardrobeTab = WardrobeTab.Pieces,
    val category: String? = null,
    val color: String? = null,
    val season: String? = null,
    val occasion: String? = null,
    val nonce: Int = 0, // incremented on Refresh to re-trigger same filters
)

@HiltViewModel
class WardrobeViewModel @Inject constructor(
    private val getFilteredWardrobe: GetFilteredWardrobeUseCase,
    private val getWardrobeOutfits: GetWardrobeOutfitsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(WardrobeUiState())
    val state = _state.asStateFlow()

    private val _filters = MutableStateFlow(ActiveFilters())

    private fun reduce(block: WardrobeUiState.() -> WardrobeUiState) =
        _state.update { it.block() }

    init {
        viewModelScope.launch {
            @Suppress("OPT_IN_USAGE")
            _filters
                .debounce(200L)
                .collectLatest { f ->
                    reduce { copy(isLoading = true, error = null) }
                    if (f.tab == WardrobeTab.Pieces) {
                        when (val result = getFilteredWardrobe(
                            category = f.category,
                            dominantColor = f.color,
                            season = f.season,
                            occasion = f.occasion,
                        )) {
                            is Resource.Success -> reduce {
                                copy(isLoading = false, items = result.data, error = null)
                            }
                            is Resource.Error -> reduce { copy(isLoading = false, error = result.message) }
                            Resource.Loading -> Unit
                        }
                    } else {
                        when (val result = getWardrobeOutfits(
                            season = f.season,
                            occasion = f.occasion,
                        )) {
                            is Resource.Success -> reduce {
                                copy(isLoading = false, outfits = result.data, error = null)
                            }
                            is Resource.Error -> reduce { copy(isLoading = false, error = result.message) }
                            Resource.Loading -> Unit
                        }
                    }
                }
        }
    }

    fun onIntent(intent: WardrobeIntent) {
        when (intent) {
            is WardrobeIntent.SelectTab -> {
                reduce { copy(selectedTab = intent.tab) }
                _filters.update { it.copy(tab = intent.tab) }
            }
            is WardrobeIntent.SetCategoryFilter -> {
                reduce { copy(filterCategory = intent.value) }
                _filters.update { it.copy(category = intent.value) }
            }
            is WardrobeIntent.SetColorFilter -> {
                reduce { copy(filterColor = intent.value) }
                _filters.update { it.copy(color = intent.value) }
            }
            is WardrobeIntent.SetSeasonFilter -> {
                reduce { copy(filterSeason = intent.value) }
                _filters.update { it.copy(season = intent.value) }
            }
            is WardrobeIntent.SetOccasionFilter -> {
                reduce { copy(filterOccasion = intent.value) }
                _filters.update { it.copy(occasion = intent.value) }
            }
            WardrobeIntent.ClearFilters -> {
                reduce {
                    copy(filterCategory = null, filterColor = null, filterSeason = null, filterOccasion = null)
                }
                _filters.update { it.copy(category = null, color = null, season = null, occasion = null) }
            }
            WardrobeIntent.Refresh -> _filters.update { it.copy(nonce = it.nonce + 1) }
        }
    }

    // Convenience delegates for existing call sites in WardrobeScreen
    fun refresh() = onIntent(WardrobeIntent.Refresh)
    fun setTab(tab: WardrobeTab) = onIntent(WardrobeIntent.SelectTab(tab))
    fun setFilterCategory(value: String?) = onIntent(WardrobeIntent.SetCategoryFilter(value))
    fun setFilterColor(value: String?) = onIntent(WardrobeIntent.SetColorFilter(value))
    fun setFilterSeason(value: String?) = onIntent(WardrobeIntent.SetSeasonFilter(value))
    fun setFilterOccasion(value: String?) = onIntent(WardrobeIntent.SetOccasionFilter(value))
    fun clearFilters() = onIntent(WardrobeIntent.ClearFilters)
}
