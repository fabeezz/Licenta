package com.example.outfitai.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ItemOutDto(
  val id: Int,
  @SerialName("image_original_name") val imageOriginalName: String,
  @SerialName("image_no_bg_name") val imageNoBgName: String? = null,
  val category: String? = null,
  @SerialName("color_tags") val colorTags: Map<String, JsonElement>? = null,
  val brand: String? = null,
  val material: String? = null,
  val season: String? = null,
  val occasion: String? = null,
  @SerialName("wear_count") val wearCount: Int,
  @SerialName("last_worn_at") val lastWornAt: String? = null,
  @SerialName("created_at") val createdAt: String
)