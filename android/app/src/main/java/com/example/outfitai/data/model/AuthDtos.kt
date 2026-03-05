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
    val email: String
)

@Serializable
data class TokenOutDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String = "bearer",
    val user: UserOutDto
)