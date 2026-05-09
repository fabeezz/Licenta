package com.example.outfitai.data.inspiration

import android.net.Uri
import com.example.outfitai.core.media.MediaUploader
import com.example.outfitai.data.api.InspirationApi
import com.example.outfitai.data.model.InspirationResponseDto
import javax.inject.Inject

class InspirationRepositoryImpl @Inject constructor(
    private val api: InspirationApi,
    private val uploader: MediaUploader,
) : InspirationRepository {

    override suspend fun fromImage(uri: Uri): InspirationResponseDto =
        api.fromImage(image = uploader.part(uri, "image"))
}
