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
  val weather: List<String> = emptyList(),
  val style: List<String> = emptyList(),
  @SerialName("dominant_color") val dominantColor: String? = null,
  @SerialName("wear_count") val wearCount: Int,
  @SerialName("last_worn_at") val lastWornAt: String? = null,
  @SerialName("created_at") val createdAt: String
)

@Serializable
data class ItemMinimalDto(
    val id: Int,
    @SerialName("image_original_name") val imageOriginalName: String,
    @SerialName("image_no_bg_name") val imageNoBgName: String? = null,
    @SerialName("dominant_color") val dominantColor: String? = null,
)

@Serializable
data class StatBucketDto(val key: String, val count: Int)

@Serializable
data class CategoryBucketDto(val category: String, val count: Int)

@Serializable
data class BasicStatsDto(
    @SerialName("total_items") val totalItems: Int,
    @SerialName("by_category") val byCategory: List<CategoryBucketDto>,
)

@Serializable
data class ColorStatsDto(
    @SerialName("total_items") val totalItems: Int,
    @SerialName("by_color") val byColor: List<StatBucketDto>,
)

@Serializable
data class WeatherStatsDto(
    @SerialName("total_items") val totalItems: Int,
    @SerialName("by_weather") val byWeather: List<StatBucketDto>,
)

@Serializable
data class GapDto(
    val dimension: String,
    val key: String,
    val severity: String,
    val suggestion: String,
)

@Serializable
data class GapsResponseDto(
    val gaps: List<GapDto>,
)