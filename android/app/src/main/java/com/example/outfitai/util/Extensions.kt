package com.example.outfitai.util

import java.util.Locale

fun String.capitalizeFirst(): String {
    if (this.isEmpty()) return this
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}

fun String?.capitalizeFirstOrNull(): String? {
    return this?.capitalizeFirst()
}
