package com.example.outfitai.ui.upload

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfitai.core.common.Resource
import com.example.outfitai.domain.usecase.item.UploadGarmentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val uploadGarment: UploadGarmentUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(UploadUiState())
    val state = _state.asStateFlow()

    private fun reduce(block: UploadUiState.() -> UploadUiState) =
        _state.update { it.block() }

    fun setUri(uri: Uri?) { reduce { copy(selectedUri = uri, error = null, done = false) } }
    fun setBrand(v: String) { reduce { copy(brand = v, error = null) } }
    fun setMaterial(v: String) { reduce { copy(material = v, error = null) } }
    fun setWeather(v: List<String>) { reduce { copy(weather = v, error = null) } }
    fun setStyle(v: List<String>) { reduce { copy(style = v, error = null) } }

    fun upload() {
        val s = _state.value
        val uri = s.selectedUri ?: run {
            reduce { copy(error = "Alege o poză.") }
            return
        }
        reduce { copy(isUploading = true, error = null, done = false) }
        viewModelScope.launch {
            when (val result = uploadGarment(uri, s.brand, s.material, s.weather.ifEmpty { null }, s.style.ifEmpty { null })) {
                is Resource.Success -> reduce { copy(isUploading = false, done = true) }
                is Resource.Error -> reduce { copy(isUploading = false, error = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun resetAfterDone() { _state.value = UploadUiState() }
}
