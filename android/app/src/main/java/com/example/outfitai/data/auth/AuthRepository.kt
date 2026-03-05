package com.example.outfitai.data.auth

import com.example.outfitai.data.api.AuthApi
import com.example.outfitai.data.model.UserCreateDto
import com.example.outfitai.data.model.UserLoginDto

class AuthRepository(
  private val authApi: AuthApi,
  private val authStore: AuthStore
) {
  suspend fun login(username: String, password: String) {
    val res = authApi.login(UserLoginDto(username, password))
    authStore.setToken(res.accessToken)
  }

  suspend fun register(username: String, email: String, password: String) {
    val res = authApi.register(UserCreateDto(username, email, password))
    authStore.setToken(res.accessToken)
  }

  suspend fun logout() {
    authStore.clear()
  }
}
