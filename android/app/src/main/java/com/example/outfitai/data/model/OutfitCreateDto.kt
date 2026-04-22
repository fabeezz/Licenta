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
    val season: String? = null,
    val occasion: String? = null,
)

@Serializable
data class OutfitSavedDto(
    val id: Int,
    val name: String,
)
