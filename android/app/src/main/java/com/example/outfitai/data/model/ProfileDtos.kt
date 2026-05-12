package com.example.outfitai.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OnboardingDto(
    @SerialName("display_name") val displayName: String,
    val gender: String,
    @SerialName("preferred_styles") val preferredStyles: List<String>,
    @SerialName("home_location_label") val homeLocationLabel: String,
    @SerialName("home_location_lat") val homeLocationLat: Double,
    @SerialName("home_location_lon") val homeLocationLon: Double,
)

@Serializable
data class ProfileUpdateDto(
    @SerialName("display_name") val displayName: String? = null,
    val gender: String? = null,
    @SerialName("preferred_styles") val preferredStyles: List<String>? = null,
    @SerialName("home_location_label") val homeLocationLabel: String? = null,
    @SerialName("home_location_lat") val homeLocationLat: Double? = null,
    @SerialName("home_location_lon") val homeLocationLon: Double? = null,
)
