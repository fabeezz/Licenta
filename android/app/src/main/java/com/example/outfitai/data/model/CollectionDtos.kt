package com.example.outfitai.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CollectionCreateDto(
    val name: String,
    @SerialName("outfit_ids") val outfitIds: List<Int>,
)

@Serializable
data class CollectionUpdateDto(
    val name: String,
)

@Serializable
data class CollectionResponseDto(
    val id: Int,
    val name: String,
    val outfits: List<OutfitSavedDto>,
)
