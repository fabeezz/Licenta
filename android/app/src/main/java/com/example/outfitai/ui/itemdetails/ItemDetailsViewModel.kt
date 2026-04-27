package com.example.outfitai.ui.itemdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.model.ItemOutDto
import com.example.outfitai.data.model.ItemUpdateDto
import com.example.outfitai.domain.usecase.item.DeleteItemUseCase
import com.example.outfitai.domain.usecase.item.GetItemUseCase
import com.example.outfitai.domain.usecase.item.UpdateItemUseCase
import com.example.outfitai.domain.usecase.item.WearItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

@HiltViewModel
class ItemDetailsViewModel @Inject constructor(
    private val getItem: GetItemUseCase,
    private val updateItem: UpdateItemUseCase,
    private val wearItem: WearItemUseCase,
    private val deleteItem: DeleteItemUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val itemId: Int = checkNotNull(savedStateHandle["itemId"])

    private val _state = MutableStateFlow(ItemDetailsUiState())
    val state = _state.asStateFlow()

    private fun reduce(block: ItemDetailsUiState.() -> ItemDetailsUiState) =
        _state.update { it.block() }

    init { load() }

    private fun extractColorList(colorTags: Map<String, JsonElement>?, key: String): List<String> {
        val arr = (colorTags?.get(key) as? JsonArray) ?: return emptyList()
        return arr.mapNotNull { it.jsonPrimitive.content }
    }

    private fun applyItem(item: ItemOutDto) = reduce {
        copy(
            isLoading = false,
            item = item,
            category = item.category.orEmpty(),
            brand = item.brand.orEmpty(),
            material = item.material.orEmpty(),
            weather = item.weather,
            style = item.style,
            dominantColors = extractColorList(item.colorTags, "dominant"),
            accentColors = extractColorList(item.colorTags, "accent"),
        )
    }

    fun load() {
        reduce { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = getItem(itemId)) {
                is Resource.Success -> applyItem(result.data)
                is Resource.Error -> reduce { copy(isLoading = false, error = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun toggleEdit() {
        val s = _state.value
        if (!s.isEditing && s.item != null) {
            applyItem(s.item)
            reduce { copy(isEditing = true, error = null) }
        } else {
            reduce { copy(isEditing = false, error = null) }
        }
    }

    fun setCategory(v: String) { reduce { copy(category = v) } }
    fun setBrand(v: String) { reduce { copy(brand = v) } }
    fun setMaterial(v: String) { reduce { copy(material = v) } }
    fun setWeather(v: List<String>) { reduce { copy(weather = v) } }
    fun setStyle(v: List<String>) { reduce { copy(style = v) } }

    fun setDominantColor(color: String) { reduce { copy(dominantColors = listOf(color)) } }

    fun toggleAccentColor(color: String) {
        reduce {
            val updated = accentColors.toMutableList().apply {
                if (contains(color)) remove(color) else add(color)
            }
            copy(accentColors = updated)
        }
    }

    fun save(onSaved: () -> Unit) {
        val s = _state.value
        reduce { copy(isBusy = true, error = null) }
        viewModelScope.launch {
            val body = ItemUpdateDto(
                category = s.category.blankToNull(),
                brand = s.brand.blankToNull(),
                material = s.material.blankToNull(),
                weather = s.weather.ifEmpty { null },
                style = s.style.ifEmpty { null },
                colorTags = mapOf("dominant" to s.dominantColors, "accent" to s.accentColors),
            )
            when (val result = updateItem(itemId, body)) {
                is Resource.Success -> {
                    applyItem(result.data)
                    reduce { copy(isBusy = false, isEditing = false) }
                    onSaved()
                }
                is Resource.Error -> reduce { copy(isBusy = false, error = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun wear(onChanged: () -> Unit) {
        reduce { copy(isBusy = true, error = null) }
        viewModelScope.launch {
            when (val result = wearItem(itemId)) {
                is Resource.Success -> {
                    reduce { copy(isBusy = false, item = result.data) }
                    onChanged()
                }
                is Resource.Error -> reduce { copy(isBusy = false, error = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun delete(onDeleted: () -> Unit) {
        reduce { copy(isBusy = true, error = null) }
        viewModelScope.launch {
            when (val result = deleteItem(itemId)) {
                is Resource.Success -> {
                    reduce { copy(isBusy = false) }
                    onDeleted()
                }
                is Resource.Error -> reduce { copy(isBusy = false, error = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    private fun String.blankToNull() = trim().takeIf { it.isNotBlank() }
}
