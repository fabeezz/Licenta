package com.example.outfitai.data.api

import com.example.outfitai.data.auth.AuthStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
  private val authStore: AuthStore
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val token = runBlocking { authStore.token.first() }

    val req = if (!token.isNullOrBlank()) {
      chain.request().newBuilder()
        .addHeader("Authorization", "Bearer $token")
        .build()
    } else {
      chain.request()
    }

    return chain.proceed(req)
  }
}