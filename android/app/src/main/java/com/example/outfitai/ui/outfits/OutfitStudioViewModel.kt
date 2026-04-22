package com.example.outfitai.ui.outfits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfitai.data.auth.AuthRepository
import com.example.outfitai.data.model.OutfitCreateDto
import com.example.outfitai.data.outfits.OutfitRepository
import com.example.outfitai.data.wardrobe.WardrobeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class OutfitStudioViewModel @Inject constructor(
    private val wardrobeRepo: WardrobeRepository,
    private val outfitRepo: OutfitRepository,
    private val authRepo: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OutfitStudioUiState())
    val state = _state.asStateFlow()

    init {
        loadUsername()
        loadAllSlots()
    }

    private fun loadUsername() {
        viewModelScope.launch {
            runCatching { authRepo.me().username }
                .onSuccess { name -> _state.update { it.copy(username = name) } }
        }
    }

    private fun loadAllSlots() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val slots = Slot.entries.map { slot ->
                    async {
                        val categories = slotCategories(slot)
                        val items = categories.map { cat ->
                            async { wardrobeRepo.listItems(category = cat, limit = 200) }
                        }.awaitAll().flatten()
                        slot to SlotItems(items = items)
                    }
                }.awaitAll().toMap()
                slots
            }.onSuccess { slots ->
                _state.update { s ->
                    s.copy(
                        isLoading = false,
                        top   = slots[Slot.TOP]   ?: SlotItems(),
                        bottom = slots[Slot.BOTTOM] ?: SlotItems(),
                        outer = slots[Slot.OUTER] ?: SlotItems(),
                        shoes = slots[Slot.SHOES] ?: SlotItems(),
                    )
                }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = "Failed to load wardrobe: ${e.message}") }
            }
        }
    }

    fun stepSlot(slot: Slot, direction: Int) {
        _state.update { s ->
            val slotItems = s.slotOf(slot)
            if (slotItems.items.isEmpty()) return@update s
            val newIndex = Math.floorMod(slotItems.index + direction, slotItems.items.size)
            s.withSlot(slot, slotItems.copy(index = newIndex))
        }
    }

    fun toggleLayers() {
        val s = _state.value
        if (!s.includeOuter && s.outer.items.isEmpty()) {
            _state.update { it.copy(snackbarMessage = "Add a jacket, coat or blazer to enable 4‑piece outfits") }
            return
        }
        _state.update { it.copy(includeOuter = !it.includeOuter) }
    }

    fun shuffle() {
        _state.update { s ->
            var next = s
            for (slot in Slot.entries) {
                val items = s.slotOf(slot)
                if (slot == Slot.OUTER && !s.includeOuter) continue
                if (items.items.size > 1) {
                    next = next.withSlot(slot, items.copy(index = Random.nextInt(items.items.size)))
                }
            }
            next
        }
    }

    fun save() {
        val s = _state.value
        val top   = s.top.current   ?: return
        val bottom = s.bottom.current ?: return
        val shoes  = s.shoes.current  ?: return
        val outer  = if (s.includeOuter) s.outer.current else null

        val name = "Outfit ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))}"
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching {
                outfitRepo.create(
                    OutfitCreateDto(
                        name     = name,
                        topId    = top.id,
                        bottomId = bottom.id,
                        shoeId   = shoes.id,
                        outerId  = outer?.id,
                    )
                )
            }.onSuccess { id ->
                _state.update { it.copy(isSaving = false, savedOutfitId = id, snackbarMessage = "Outfit saved!") }
            }.onFailure { e ->
                _state.update { it.copy(isSaving = false, snackbarMessage = "Save failed: ${e.message}") }
            }
        }
    }

    fun consumeSnackbar() { _state.update { it.copy(snackbarMessage = null) } }
}

private fun OutfitStudioUiState.slotOf(slot: Slot) = when (slot) {
    Slot.TOP    -> top
    Slot.BOTTOM -> bottom
    Slot.OUTER  -> outer
    Slot.SHOES  -> shoes
}

private fun OutfitStudioUiState.withSlot(slot: Slot, items: SlotItems) = when (slot) {
    Slot.TOP    -> copy(top    = items)
    Slot.BOTTOM -> copy(bottom = items)
    Slot.OUTER  -> copy(outer  = items)
    Slot.SHOES  -> copy(shoes  = items)
}
