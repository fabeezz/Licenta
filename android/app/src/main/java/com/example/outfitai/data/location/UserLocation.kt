package com.example.outfitai.data.location

data class UserLocation(
    val label: String,
    val lat: Double,
    val lon: Double,
    val source: Source,
) {
    enum class Source { DETECTED, MANUAL, DEFAULT }
}
