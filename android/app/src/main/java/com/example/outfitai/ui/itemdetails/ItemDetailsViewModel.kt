package com.example.outfitai.ui.itemdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfitai.data.item.ItemRepository
import com.example.outfitai.data.model.ItemUpdateDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ItemDetailsViewModel @Inject constructor(
    private val repo: ItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val itemId: Int = checkNotNull(savedStateHandle["itemId"])

    private val _state = MutableStateFlow(ItemDetailsUiState())
    val state = _state.asStateFlow()

    init { load() }

    private fun extractColorList(colorTags: Map<String, JsonElement>?, key: String): List<String> {
        val arr = (colorTags?.get(key) as? JsonArray) ?: return emptyList()
        return arr.mapNotNull { it.jsonPrimitive.content }
    }

    fun load() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val it = repo.getItem(itemId)
                _state.value = _state.value.copy(
                    isLoading = false,
                    item = it,
                    category = it.category.orEmpty(),
                    brand = it.brand.orEmpty(),
                    material = it.material.orEmpty(),
                    season = it.season.orEmpty(),
                    occasion = it.occasion.orEmpty(),
                    dominantColors = extractColorList(it.colorTags, "dominant"),
                    accentColors = extractColorList(it.colorTags, "accent"),
                )
            } catch (e: HttpException) {
                _state.value = _state.value.copy(isLoading = false, error = "Eroare server (${e.code()}).")
            } catch (_: IOException) {
                _state.value = _state.value.copy(isLoading = false, error = "Nu pot contacta serverul.")
            }
        }
    }

    fun toggleEdit() {
        val s = _state.value
        val isStartingEdit = !s.isEditing
        if (isStartingEdit && s.item != null) {
            // Reset to current item state when starting edit
            _state.value = s.copy(
                isEditing = true,
                error = null,
                category = s.item.category.orEmpty(),
                brand = s.item.brand.orEmpty(),
                material = s.item.material.orEmpty(),
                season = s.item.season.orEmpty(),
                occasion = s.item.occasion.orEmpty(),
                dominantColors = extractColorList(s.item.colorTags, "dominant"),
                accentColors = extractColorList(s.item.colorTags, "accent"),
            )
        } else {
            _state.value = s.copy(isEditing = false, error = null)
        }
    }

    fun setCategory(v: String) { _state.value = _state.value.copy(category = v) }
    fun setBrand(v: String) { _state.value = _state.value.copy(brand = v) }
    fun setMaterial(v: String) { _state.value = _state.value.copy(material = v) }
    fun setSeason(v: String) { _state.value = _state.value.copy(season = v) }
    fun setOccasion(v: String) { _state.value = _state.value.copy(occasion = v) }

    fun setDominantColor(color: String) {
        _state.value = _state.value.copy(dominantColors = listOf(color))
    }

    fun toggleAccentColor(color: String) {
        val current = _state.value.accentColors.toMutableList()
        if (current.contains(color)) {
            current.remove(color)
        } else {
            current.add(color)
        }
        _state.value = _state.value.copy(accentColors = current)
    }

    private fun blankToNull(s: String) = s.trim().takeIf { it.isNotBlank() }

    fun save(onSaved: () -> Unit) {
        val s = _state.value
        _state.value = s.copy(isBusy = true, error = null)
        viewModelScope.launch {
            try {
                val updated = repo.updateItem(
                    itemId,
                    ItemUpdateDto(
                        category = blankToNull(s.category),
                        brand = blankToNull(s.brand),
                        material = blankToNull(s.material),
                        season = blankToNull(s.season),
                        occasion = blankToNull(s.occasion),
                        colorTags = mapOf(
                            "dominant" to s.dominantColors,
                            "accent" to s.accentColors
                        )
                    )
                )
                _state.value = _state.value.copy(
                    isBusy = false,
                    isEditing = false,
                    item = updated,
                    dominantColors = extractColorList(updated.colorTags, "dominant"),
                    accentColors = extractColorList(updated.colorTags, "accent"),
                )
                onSaved()
            } catch (e: HttpException) {
                _state.value = _state.value.copy(isBusy = false, error = "Eroare server (${e.code()}).")
            } catch (_: IOException) {
                _state.value = _state.value.copy(isBusy = false, error = "Nu pot contacta serverul.")
            }
        }
    }

    fun wear(onChanged: () -> Unit) {
        _state.value = _state.value.copy(isBusy = true, error = null)
        viewModelScope.launch {
            try {
                val updated = repo.wearItem(itemId)
                _state.value = _state.value.copy(isBusy = false, item = updated)
                onChanged()
            } catch (e: HttpException) {
                _state.value = _state.value.copy(isBusy = false, error = "Eroare server (${e.code()}).")
            } catch (_: IOException) {
                _state.value = _state.value.copy(isBusy = false, error = "Nu pot contacta serverul.")
            }
        }
    }

    fun delete(onDeleted: () -> Unit) {
        _state.value = _state.value.copy(isBusy = true, error = null)
        viewModelScope.launch {
            try {
                repo.deleteItem(itemId)
                _state.value = _state.value.copy(isBusy = false)
                onDeleted()
            } catch (e: HttpException) {
                _state.value = _state.value.copy(isBusy = false, error = "Eroare server (${e.code()}).")
            } catch (_: IOException) {
                _state.value = _state.value.copy(isBusy = false, error = "Nu pot contacta serverul.")
            }
        }
    }
}