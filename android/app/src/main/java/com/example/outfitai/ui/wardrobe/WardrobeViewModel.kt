package com.example.outfitai.ui.wardrobe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfitai.core.common.Resource
import com.example.outfitai.domain.usecase.collections.CreateCollectionUseCase
import com.example.outfitai.domain.usecase.collections.DeleteCollectionUseCase
import com.example.outfitai.domain.usecase.collections.GetCollectionsUseCase
import com.example.outfitai.domain.usecase.collections.RenameCollectionUseCase
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
    data class SetWeatherFilter(val value: String?) : WardrobeIntent
    data class SetStyleFilter(val value: String?) : WardrobeIntent
    data object ClearFilters : WardrobeIntent
    data object Refresh : WardrobeIntent
}

private data class ActiveFilters(
    val tab: WardrobeTab = WardrobeTab.Pieces,
    val category: String? = null,
    val color: String? = null,
    val weather: String? = null,
    val style: String? = null,
    val nonce: Int = 0, // incremented on Refresh to re-trigger same filters
)

@HiltViewModel
class WardrobeViewModel @Inject constructor(
    private val getFilteredWardrobe: GetFilteredWardrobeUseCase,
    private val getWardrobeOutfits: GetWardrobeOutfitsUseCase,
    private val getCollections: GetCollectionsUseCase,
    private val createCollectionUseCase: CreateCollectionUseCase,
    private val renameCollectionUseCase: RenameCollectionUseCase,
    private val deleteCollectionUseCase: DeleteCollectionUseCase,
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
                            weather = f.weather,
                            style = f.style,
                        )) {
                            is Resource.Success -> reduce {
                                copy(isLoading = false, items = result.data, error = null)
                            }
                            is Resource.Error -> reduce { copy(isLoading = false, error = result.message) }
                            Resource.Loading -> Unit
                        }
                    } else if (f.tab == WardrobeTab.Fits) {
                        when (val result = getWardrobeOutfits(
                            weather = f.weather,
                            style = f.style,
                        )) {
                            is Resource.Success -> reduce {
                                copy(isLoading = false, outfits = result.data, error = null)
                            }
                            is Resource.Error -> reduce { copy(isLoading = false, error = result.message) }
                            Resource.Loading -> Unit
                        }
                    } else {
                        when (val result = getCollections()) {
                            is Resource.Success -> reduce {
                                copy(isLoading = false, collections = result.data, error = null)
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
            is WardrobeIntent.SetWeatherFilter -> {
                reduce { copy(filterWeather = intent.value) }
                _filters.update { it.copy(weather = intent.value) }
            }
            is WardrobeIntent.SetStyleFilter -> {
                reduce { copy(filterStyle = intent.value) }
                _filters.update { it.copy(style = intent.value) }
            }
            WardrobeIntent.ClearFilters -> {
                reduce {
                    copy(filterCategory = null, filterColor = null, filterWeather = null, filterStyle = null)
                }
                _filters.update { it.copy(category = null, color = null, weather = null, style = null) }
            }
            WardrobeIntent.Refresh -> _filters.update { it.copy(nonce = it.nonce + 1) }
        }
    }

    // Convenience delegates for existing call sites in WardrobeScreen
    fun refresh() = onIntent(WardrobeIntent.Refresh)
    fun setTab(tab: WardrobeTab) = onIntent(WardrobeIntent.SelectTab(tab))
    fun setFilterCategory(value: String?) = onIntent(WardrobeIntent.SetCategoryFilter(value))
    fun setFilterColor(value: String?) = onIntent(WardrobeIntent.SetColorFilter(value))
    fun setFilterWeather(value: String?) = onIntent(WardrobeIntent.SetWeatherFilter(value))
    fun setFilterStyle(value: String?) = onIntent(WardrobeIntent.SetStyleFilter(value))
    fun clearFilters() = onIntent(WardrobeIntent.ClearFilters)

    fun createCollection(name: String, outfitIds: List<Int>) {
        viewModelScope.launch {
            when (createCollectionUseCase(name, outfitIds)) {
                is Resource.Success -> refresh()
                is Resource.Error -> Unit
                Resource.Loading -> Unit
            }
        }
    }

    fun renameCollection(id: Int, name: String) {
        viewModelScope.launch {
            when (renameCollectionUseCase(id, name)) {
                is Resource.Success -> refresh()
                is Resource.Error -> Unit
                Resource.Loading -> Unit
            }
        }
    }

    fun deleteCollection(id: Int) {
        viewModelScope.launch {
            when (deleteCollectionUseCase(id)) {
                is Resource.Success -> refresh()
                is Resource.Error -> Unit
                Resource.Loading -> Unit
            }
        }
    }
}
