package com.example.outfitai.data.inspiration

import android.net.Uri
import com.example.outfitai.data.model.InspirationResponseDto

interface InspirationRepository {
    suspend fun fromImage(uri: Uri): InspirationResponseDto
}
