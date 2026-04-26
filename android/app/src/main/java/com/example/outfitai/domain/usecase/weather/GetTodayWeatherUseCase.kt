package com.example.outfitai.domain.usecase.weather

import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.weather.WeatherRepository
import com.example.outfitai.domain.weather.WeatherForecast
import javax.inject.Inject

class GetTodayWeatherUseCase @Inject constructor(
    private val repo: WeatherRepository,
) {
    suspend operator fun invoke(lat: Double, lon: Double): Resource<WeatherForecast> =
        repo.getToday(lat, lon)
}
