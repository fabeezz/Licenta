package com.example.outfitai.data.api

import com.example.outfitai.data.model.TokenOutDto
import com.example.outfitai.data.model.UserCreateDto
import com.example.outfitai.data.model.UserLoginDto
import com.example.outfitai.data.model.UserOutDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
  @POST("auth/register")
  suspend fun register(@Body body: UserCreateDto): TokenOutDto

  @POST("auth/login")
  suspend fun login(@Body body: UserLoginDto): TokenOutDto

  @GET("auth/me")
  suspend fun me(): UserOutDto
}