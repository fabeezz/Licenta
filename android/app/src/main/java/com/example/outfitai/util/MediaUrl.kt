package com.example.outfitai.util

import com.example.outfitai.Config

fun mediaUrl(filename: String): String =
    Config.BASE_URL.trimEnd('/') + "/media/" + filename