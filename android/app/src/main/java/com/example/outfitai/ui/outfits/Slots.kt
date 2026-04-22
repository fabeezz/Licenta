package com.example.outfitai.ui.outfits

// Mirrors backend/app/services/outfits/slots.py — keep in sync when categories change.
enum class Slot { TOP, BOTTOM, OUTER, SHOES }

private val TOP_CATEGORIES    = setOf("t-shirt", "shirt", "sweater")
private val BOTTOM_CATEGORIES = setOf("jeans", "pants", "shorts", "skirt")
private val OUTER_CATEGORIES  = setOf("hoodie", "jacket", "coat", "blazer")
private val SHOES_CATEGORIES  = setOf("sneakers", "shoes", "boots")

fun categoryToSlot(category: String?): Slot? {
    val c = category?.trim()?.lowercase() ?: return null
    return when {
        c in TOP_CATEGORIES    -> Slot.TOP
        c in BOTTOM_CATEGORIES -> Slot.BOTTOM
        c in OUTER_CATEGORIES  -> Slot.OUTER
        c in SHOES_CATEGORIES  -> Slot.SHOES
        else                   -> null
    }
}

fun slotCategories(slot: Slot): Set<String> = when (slot) {
    Slot.TOP    -> TOP_CATEGORIES
    Slot.BOTTOM -> BOTTOM_CATEGORIES
    Slot.OUTER  -> OUTER_CATEGORIES
    Slot.SHOES  -> SHOES_CATEGORIES
}
