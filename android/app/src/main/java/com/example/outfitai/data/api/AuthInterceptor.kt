package com.example.outfitai.data.api

import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor() : Interceptor {

    private val cachedToken = AtomicReference<String?>()

    fun updateToken(token: String?) = cachedToken.set(token)

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = cachedToken.get()

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
