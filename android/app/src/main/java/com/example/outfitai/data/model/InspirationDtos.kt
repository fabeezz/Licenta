package com.example.outfitai.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InspirationSlotMatchDto(
    val best: ItemMinimalDto? = null,
    val alternates: List<ItemMinimalDto> = emptyList(),
    val score: Float? = null,
)

@Serializable
data class InspirationResponseDto(
    @SerialName("source_image_url") val sourceImageUrl: String,
    val top: InspirationSlotMatchDto,
    val bottom: InspirationSlotMatchDto,
    val outer: InspirationSlotMatchDto,
    val shoes: InspirationSlotMatchDto,
)
