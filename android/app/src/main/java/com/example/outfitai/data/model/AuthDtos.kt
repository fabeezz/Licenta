package com.example.outfitai.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserCreateDto(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class UserLoginDto(
    val username: String,
    val password: String
)

@Serializable
data class UserOutDto(
    val id: Int,
    val username: String,
    val email: String,
    @SerialName("display_name") val displayName: String? = null,
    val gender: String? = null,
    @SerialName("preferred_styles") val preferredStyles: List<String>? = null,
    @SerialName("home_location_label") val homeLocationLabel: String? = null,
    @SerialName("home_location_lat") val homeLocationLat: Double? = null,
    @SerialName("home_location_lon") val homeLocationLon: Double? = null,
    @SerialName("onboarded_at") val onboardedAt: String? = null,
)

@Serializable
data class TokenOutDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String = "bearer",
    val user: UserOutDto
)

@Serializable
data class PasswordResetDto(
    val username: String,
    val email: String,
    @SerialName("new_password") val newPassword: String,
)