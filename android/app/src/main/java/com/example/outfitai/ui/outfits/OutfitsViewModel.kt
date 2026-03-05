package com.example.outfitai.ui.outfits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfitai.data.outfits.OutfitsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class OutfitsViewModel @Inject constructor(
    private val repo: OutfitsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OutfitsUiState())
    val state = _state.asStateFlow()

    init { refresh() }

    fun setSeason(v: String) { _state.value = _state.value.copy(season = v, error = null) }
    fun setOccasion(v: String) { _state.value = _state.value.copy(occasion = v, error = null) }

    private fun blankToNull(s: String) = s.trim().takeIf { it.isNotBlank() }

    fun refresh() {
        val s = _state.value
        _state.value = s.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val outfits = repo.getOutfits(
                    season = blankToNull(s.season),
                    occasion = blankToNull(s.occasion),
                    limit = 6
                )
                _state.value = _state.value.copy(isLoading = false, outfits = outfits)
            } catch (e: HttpException) {
                _state.value = _state.value.copy(isLoading = false, error = "Eroare server (${e.code()}).")
            } catch (_: IOException) {
                _state.value = _state.value.copy(isLoading = false, error = "Nu pot contacta serverul.")
            }
        }
    }
}