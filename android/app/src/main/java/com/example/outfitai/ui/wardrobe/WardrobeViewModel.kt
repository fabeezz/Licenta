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
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val items = repo.listItems()
                _state.value = WardrobeUiState(isLoading = false, items = items, error = null)
            } catch (e: HttpException) {
                _state.value = WardrobeUiState(isLoading = false, items = emptyList(), error = "Eroare server (${e.code()}).")
            } catch (_: IOException) {
                _state.value = WardrobeUiState(isLoading = false, items = emptyList(), error = "Nu pot contacta serverul.")
            }
        }
    }
}