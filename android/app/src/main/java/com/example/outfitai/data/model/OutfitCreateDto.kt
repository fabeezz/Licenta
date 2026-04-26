package com.example.outfitai.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OutfitCreateDto(
    val name: String,
    @SerialName("top_id")   val topId: Int,
    @SerialName("bottom_id") val bottomId: Int,
    @SerialName("shoe_id")  val shoeId: Int,
    @SerialName("outer_id") val outerId: Int? = null,
    val weather: List<String> = emptyList(),
    val occasion: String? = null,
)

@Serializable
data class ItemMinimalDto(
    val id: Int,
    @SerialName("image_original_name") val imageOriginalName: String,
    @SerialName("image_no_bg_name") val imageNoBgName: String? = null,
    @SerialName("dominant_color") val dominantColor: String? = null,
)

@Serializable
data class OutfitSavedDto(
    val id: Int,
    val name: String,
    val weather: List<String> = emptyList(),
    val occasion: String? = null,
    val top: ItemMinimalDto,
    val bottom: ItemMinimalDto,
    val shoe: ItemMinimalDto,
    val outer: ItemMinimalDto? = null,
)
