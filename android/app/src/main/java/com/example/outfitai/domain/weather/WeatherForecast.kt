package com.example.outfitai.domain.weather

data class HourPoint(
    val timeIso: String,
    val tempC: Double,
    val precipMm: Double,
    val code: Int,
) {
    val displayTime: String
        get() = timeIso.substringAfter("T").take(5)
}

data class WeatherForecast(
    val currentTempC: Double,
    val currentPrecipMm: Double,
    val currentCode: Int,
    val hours: List<HourPoint>,
) {
    fun toClimate(): String = when {
        currentPrecipMm > 0.0 -> "Rainy"
        currentTempC < 15.0   -> "Cold"
        else                  -> "Warm"
    }
}
