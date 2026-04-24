package com.example.outfitai.data.auth

import com.example.outfitai.data.model.UserOutDto

interface AuthRepository {
    suspend fun login(username: String, password: String)
    suspend fun register(username: String, email: String, password: String)
    suspend fun resetPassword(username: String, email: String, newPassword: String)
    suspend fun me(): UserOutDto
    suspend fun logout()
}
