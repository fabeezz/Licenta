package com.example.outfitai.data.insights

import com.example.outfitai.data.model.BasicStatsDto
import com.example.outfitai.data.model.ColorStatsDto
import com.example.outfitai.data.model.GapsResponseDto
import com.example.outfitai.data.model.WeatherStatsDto

interface InsightsRepository {
    suspend fun getBasicStats(): BasicStatsDto
    suspend fun getColorStats(): ColorStatsDto
    suspend fun getWeatherStats(): WeatherStatsDto
    suspend fun getGaps(): GapsResponseDto
}
