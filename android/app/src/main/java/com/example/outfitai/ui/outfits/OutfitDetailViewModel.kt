package com.example.outfitai.ui.outfits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.model.OutfitSavedDto
import com.example.outfitai.domain.usecase.outfits.DeleteOutfitUseCase
import com.example.outfitai.domain.usecase.outfits.GetOutfitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OutfitDetailUiState(
    val isLoading: Boolean = true,
    val outfit: OutfitSavedDto? = null,
    val error: String? = null,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
)

@HiltViewModel
class OutfitDetailViewModel @Inject constructor(
    private val getOutfit: GetOutfitUseCase,
    private val deleteOutfit: DeleteOutfitUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val outfitId: Int = checkNotNull(savedStateHandle["outfitId"])

    private val _state = MutableStateFlow(OutfitDetailUiState())
    val state = _state.asStateFlow()

    private fun reduce(block: OutfitDetailUiState.() -> OutfitDetailUiState) =
        _state.update { it.block() }

    init { load() }

    private fun load() {
        reduce { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = getOutfit(outfitId)) {
                is Resource.Success -> reduce { copy(isLoading = false, outfit = result.data) }
                is Resource.Error -> reduce { copy(isLoading = false, error = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun delete() {
        if (_state.value.isDeleting) return
        reduce { copy(isDeleting = true) }
        viewModelScope.launch {
            when (val result = deleteOutfit(outfitId)) {
                is Resource.Success -> reduce { copy(isDeleting = false, isDeleted = true) }
                is Resource.Error -> reduce { copy(isDeleting = false, error = result.message) }
                Resource.Loading -> Unit
            }
        }
    }
}
