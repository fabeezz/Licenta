package com.example.outfitai.data.auth

import com.example.outfitai.data.api.AuthApi
import com.example.outfitai.data.model.PasswordResetDto
import com.example.outfitai.data.model.UserCreateDto
import com.example.outfitai.data.model.UserLoginDto
import com.example.outfitai.data.model.UserOutDto
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val authStore: AuthStore,
) : AuthRepository {

    override suspend fun login(username: String, password: String) {
        val res = authApi.login(UserLoginDto(username, password))
        authStore.setToken(res.accessToken)
    }

    override suspend fun register(username: String, email: String, password: String) {
        val res = authApi.register(UserCreateDto(username, email, password))
        authStore.setToken(res.accessToken)
    }

    override suspend fun resetPassword(username: String, email: String, newPassword: String) {
        val res = authApi.resetPassword(PasswordResetDto(username, email, newPassword))
        authStore.setToken(res.accessToken)
    }

    override suspend fun me(): UserOutDto = authApi.me()

    override suspend fun logout() {
        authStore.clear()
    }
}
