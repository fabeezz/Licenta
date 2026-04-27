package com.example.outfitai.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemUpdateDto(
    val category: String? = null,
    val brand: String? = null,
    val material: String? = null,
    val weather: List<String>? = null,
    val style: List<String>? = null,
    @SerialName("color_tags")
    val colorTags: Map<String, List<String>>? = null
)