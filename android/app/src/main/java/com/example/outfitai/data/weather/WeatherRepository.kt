package com.example.outfitai.data.weather

import com.example.outfitai.core.common.Resource
import com.example.outfitai.domain.weather.WeatherForecast

interface WeatherRepository {
    suspend fun getToday(lat: Double, lon: Double): Resource<WeatherForecast>
}
