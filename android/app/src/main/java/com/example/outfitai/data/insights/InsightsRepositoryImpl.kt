package com.example.outfitai.data.insights

import com.example.outfitai.data.api.ItemApi
import com.example.outfitai.data.model.BasicStatsDto
import com.example.outfitai.data.model.ColorStatsDto
import com.example.outfitai.data.model.WeatherStatsDto
import javax.inject.Inject

class InsightsRepositoryImpl @Inject constructor(
    private val api: ItemApi,
) : InsightsRepository {
    override suspend fun getBasicStats(): BasicStatsDto = api.getStats()
    override suspend fun getColorStats(): ColorStatsDto = api.getColorStats()
    override suspend fun getWeatherStats(): WeatherStatsDto = api.getWeatherStats()
}
