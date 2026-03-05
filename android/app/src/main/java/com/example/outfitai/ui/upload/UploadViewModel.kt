package com.example.outfitai.ui.upload

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfitai.data.item.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val repo: ItemRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UploadUiState())
    val state = _state.asStateFlow()

    fun setUri(uri: Uri?) {
        _state.value = _state.value.copy(selectedUri = uri, error = null, done = false)
    }

    fun setBrand(v: String) { _state.value = _state.value.copy(brand = v, error = null) }
    fun setMaterial(v: String) { _state.value = _state.value.copy(material = v, error = null) }
    fun setSeason(v: String) { _state.value = _state.value.copy(season = v, error = null) }
    fun setOccasion(v: String) { _state.value = _state.value.copy(occasion = v, error = null) }

    fun upload() {
        val s = _state.value
        val uri = s.selectedUri ?: run {
            _state.value = s.copy(error = "Alege o poză.")
            return
        }

        _state.value = s.copy(isUploading = true, error = null, done = false)
        viewModelScope.launch {
            try {
                repo.uploadItem(
                    uri = uri,
                    brand = s.brand,
                    material = s.material,
                    season = s.season,
                    occasion = s.occasion
                )
                _state.value = _state.value.copy(isUploading = false, done = true)
            } catch (e: HttpException) {
                _state.value = _state.value.copy(isUploading = false, error = "Eroare server (${e.code()}).")
            } catch (_: IOException) {
                _state.value = _state.value.copy(isUploading = false, error = "Nu pot contacta serverul.")
            }
        }
    }

    fun resetAfterDone() {
        _state.value = UploadUiState()
    }
}