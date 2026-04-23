package com.example.outfitai.ui.wardrobe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfitai.data.wardrobe.WardrobeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class WardrobeViewModel @Inject constructor(
    private val repo: WardrobeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WardrobeUiState())
    val state = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val s = _state.value
        _state.value = s.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                if (s.selectedTab == WardrobeTab.Pieces) {
                    val items = repo.listItems(
                        category = s.filterCategory,
                        dominantColor = s.filterColor,
                        season = s.filterSeason,
                        occasion = s.filterOccasion,
                    )
                    _state.value = _state.value.copy(isLoading = false, items = items, error = null)
                } else {
                    val outfits = repo.listOutfits(
                        season = s.filterSeason,
                        occasion = s.filterOccasion,
                    )
                    _state.value = _state.value.copy(isLoading = false, outfits = outfits, error = null)
                }
            } catch (e: HttpException) {
                _state.value = _state.value.copy(isLoading = false, error = "Eroare server (${e.code()}).")
            } catch (_: IOException) {
                _state.value = _state.value.copy(isLoading = false, error = "Nu pot contacta serverul.")
            }
        }
    }

    fun setTab(tab: WardrobeTab) {
        _state.value = _state.value.copy(selectedTab = tab)
        refresh()
    }

    fun setFilterCategory(value: String?) {
        _state.value = _state.value.copy(filterCategory = value)
        refresh()
    }

    fun setFilterColor(value: String?) {
        _state.value = _state.value.copy(filterColor = value)
        refresh()
    }

    fun setFilterSeason(value: String?) {
        _state.value = _state.value.copy(filterSeason = value)
        refresh()
    }

    fun setFilterOccasion(value: String?) {
        _state.value = _state.value.copy(filterOccasion = value)
        refresh()
    }

    fun clearFilters() {
        _state.value = _state.value.copy(
            filterCategory = null,
            filterColor = null,
            filterSeason = null,
            filterOccasion = null,
        )
        refresh()
    }
}
