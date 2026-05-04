package com.example.outfitai.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OutfitItemRefDto(
    val id: Int,
    val category: String? = null,
    @SerialName("image_no_bg_name") val imageNoBgName: String? = null,
    @SerialName("image_original_name") val imageOriginalName: String,
    @SerialName("dominant_color") val dominantColor: String? = null,
)

@Serializable
data class OutfitOutDto(
    val top: OutfitItemRefDto,
    val bottom: OutfitItemRefDto,
    val outer: OutfitItemRefDto,
    val shoes: OutfitItemRefDto,
    val score: Double,
)

@Serializable
data class OutfitSuggestRequest(
    val style: String? = null,
    val weather: String? = null,
    val modes: List<String>? = null,
)

@Serializable
data class OutfitSuggestResponse(
    val top: Int? = null,
    val bottom: Int? = null,
    val outer: Int? = null,
    val shoes: Int? = null,
)