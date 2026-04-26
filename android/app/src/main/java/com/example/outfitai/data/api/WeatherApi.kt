package com.example.outfitai.data.api

import com.example.outfitai.data.model.WeatherTodayDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("weather/today")
    suspend fun getToday(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
    ): WeatherTodayDto
}
