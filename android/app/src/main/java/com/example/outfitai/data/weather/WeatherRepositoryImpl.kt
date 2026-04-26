package com.example.outfitai.data.weather

import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.api.WeatherApi
import com.example.outfitai.data.remote.safeApiCall
import com.example.outfitai.domain.weather.HourPoint
import com.example.outfitai.domain.weather.WeatherForecast
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi,
) : WeatherRepository {

    override suspend fun getToday(lat: Double, lon: Double): Resource<WeatherForecast> =
        safeApiCall {
            val dto = api.getToday(lat, lon)
            WeatherForecast(
                currentTempC = dto.currentTempC,
                currentPrecipMm = dto.currentPrecipMm,
                currentCode = dto.currentCode,
                hours = dto.hours.map { h ->
                    HourPoint(
                        timeIso = h.time,
                        tempC = h.tempC,
                        precipMm = h.precipMm,
                        code = h.code,
                    )
                },
            )
        }
}
