package com.example.outfitai.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class OutfitSource { MANUAL, INSPIRATION }

@Serializable
data class OutfitCreateDto(
    val name: String,
    @SerialName("top_id")   val topId: Int,
    @SerialName("bottom_id") val bottomId: Int,
    @SerialName("shoe_id")  val shoeId: Int,
    @SerialName("outer_id") val outerId: Int? = null,
    val weather: List<String> = emptyList(),
    val style: String? = null,
    val source: String,
)

@Serializable
data class OutfitSavedDto(
    val id: Int,
    val name: String,
    val weather: List<String> = emptyList(),
    val style: String? = null,
    val top: ItemMinimalDto,
    val bottom: ItemMinimalDto,
    val shoe: ItemMinimalDto,
    val outer: ItemMinimalDto? = null,
    val source: String = "manual",
) {
    val sourceEnum: OutfitSource get() = when (source) {
        "inspiration" -> OutfitSource.INSPIRATION
        else -> OutfitSource.MANUAL
    }
}
