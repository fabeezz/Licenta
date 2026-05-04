package com.example.outfitai.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Destinations ─────────────────────────────────────────────────────────────

@Serializable
data class DestinationDto(
    val key: String,
    val city: String,
    val country: String,
    val flag: String,
    val lat: Double,
    val lon: Double,
)

// ── Request bodies ────────────────────────────────────────────────────────────

@Serializable
data class DayActivitiesDto(
    val date: String,
    val activities: List<String> = emptyList(),
)

@Serializable
data class TripGenerateRequestDto(
    @SerialName("city_key") val cityKey: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    @SerialName("bag_size") val bagSize: String,
    val activities: List<String>,
    @SerialName("day_activities") val dayActivities: List<DayActivitiesDto>? = null,
)

@Serializable
data class GeneratedOutfitInDto(
    @SerialName("day_label") val dayLabel: String,
    @SerialName("is_travel") val isTravel: Boolean = false,
    @SerialName("top_id") val topId: Int? = null,
    @SerialName("bottom_id") val bottomId: Int? = null,
    @SerialName("shoe_id") val shoeId: Int? = null,
    @SerialName("outer_id") val outerId: Int? = null,
    @SerialName("bag_id") val bagId: Int? = null,
    val style: String? = null,
    @SerialName("weather_tags") val weatherTags: List<String> = emptyList(),
)

@Serializable
data class TripSaveRequestDto(
    @SerialName("city_key") val cityKey: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    @SerialName("bag_size") val bagSize: String,
    val activities: List<String>,
    @SerialName("collection_name") val collectionName: String,
    val outfits: List<GeneratedOutfitInDto>,
)

// ── Response bodies ───────────────────────────────────────────────────────────

@Serializable
data class DayForecastDto(
    val date: String,
    @SerialName("temp_max_c") val tempMaxC: Double,
    @SerialName("temp_min_c") val tempMinC: Double,
    @SerialName("precip_mm") val precipMm: Double,
    @SerialName("weather_code") val weatherCode: Int,
)

@Serializable
data class GeneratedOutfitSlotsDto(
    val top: ItemMinimalDto? = null,
    val bottom: ItemMinimalDto? = null,
    val shoes: ItemMinimalDto? = null,
    val outer: ItemMinimalDto? = null,
    val bag: ItemMinimalDto? = null,
)

@Serializable
data class GeneratedOutfitDto(
    @SerialName("day_label") val dayLabel: String,
    @SerialName("is_travel") val isTravel: Boolean = false,
    val slots: GeneratedOutfitSlotsDto,
    val style: String? = null,
    @SerialName("weather_note") val weatherNote: String = "",
    @SerialName("weather_tags") val weatherTags: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
)

@Serializable
data class TripPlanResponseDto(
    val city: String,
    val country: String,
    @SerialName("city_key") val cityKey: String,
    val flag: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    @SerialName("bag_size") val bagSize: String,
    val forecast: List<DayForecastDto>,
    val outfits: List<GeneratedOutfitDto>,
    @SerialName("global_warnings") val globalWarnings: List<String> = emptyList(),
)

@Serializable
data class TripReadDto(
    val id: Int,
    val city: String,
    val country: String,
    @SerialName("city_key") val cityKey: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    @SerialName("bag_size") val bagSize: String,
    val activities: List<String>,
    @SerialName("collection_id") val collectionId: Int? = null,
)
