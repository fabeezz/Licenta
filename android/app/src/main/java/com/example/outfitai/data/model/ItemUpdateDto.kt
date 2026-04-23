package com.example.outfitai.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemUpdateDto(
    val category: String? = null,
    val brand: String? = null,
    val material: String? = null,
    val season: String? = null,
    val occasion: String? = null,
    @SerialName("color_tags")
    val colorTags: Map<String, List<String>>? = null
)