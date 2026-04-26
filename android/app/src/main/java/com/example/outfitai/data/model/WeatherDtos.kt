package com.example.outfitai.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HourPointDto(
    val time: String,
    @SerialName("temp_c") val tempC: Double,
    @SerialName("precip_mm") val precipMm: Double,
    val code: Int,
)

@Serializable
data class WeatherTodayDto(
    @SerialName("current_temp_c") val currentTempC: Double,
    @SerialName("current_precip_mm") val currentPrecipMm: Double,
    @SerialName("current_code") val currentCode: Int,
    val hours: List<HourPointDto>,
)
