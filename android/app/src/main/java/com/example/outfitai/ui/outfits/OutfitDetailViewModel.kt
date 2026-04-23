package com.example.outfitai.ui.outfits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfitai.data.api.OutfitApi
import com.example.outfitai.data.model.OutfitSavedDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

data class OutfitDetailUiState(
    val isLoading: Boolean = true,
    val outfit: OutfitSavedDto? = null,
    val error: String? = null,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false
)

@HiltViewModel
class OutfitDetailViewModel @Inject constructor(
    private val api: OutfitApi,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val outfitId: Int = checkNotNull(savedStateHandle["outfitId"])

    private val _state = MutableStateFlow(OutfitDetailUiState())
    val state = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val outfit = api.get(outfitId)
                _state.value = _state.value.copy(isLoading = false, outfit = outfit)
            } catch (e: HttpException) {
                _state.value = _state.value.copy(isLoading = false, error = "Server error (${e.code()})")
            } catch (e: IOException) {
                _state.value = _state.value.copy(isLoading = false, error = "Network error")
            }
        }
    }

    fun delete() {
        if (_state.value.isDeleting) return
        _state.value = _state.value.copy(isDeleting = true)
        viewModelScope.launch {
            try {
                api.delete(outfitId)
                _state.value = _state.value.copy(isDeleting = false, isDeleted = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isDeleting = false, error = "Failed to delete")
            }
        }
    }
}
