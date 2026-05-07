package com.example.outfitai.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DeviceLocationProvider(
    private val client: FusedLocationProviderClient,
    private val context: Context,
) {
    @SuppressLint("MissingPermission")
    suspend fun detectCurrentLocation(): UserLocation {
        val cts = CancellationTokenSource()
        val androidLocation = suspendCancellableCoroutine { cont ->
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) cont.resume(loc)
                    else cont.resumeWithException(Exception("Location unavailable"))
                }
                .addOnFailureListener { cont.resumeWithException(it) }
            cont.invokeOnCancellation { cts.cancel() }
        }

        val label = reverseGeocode(androidLocation.latitude, androidLocation.longitude)
            ?: "%.4f, %.4f".format(androidLocation.latitude, androidLocation.longitude)

        return UserLocation(
            label = label,
            lat = androidLocation.latitude,
            lon = androidLocation.longitude,
            source = UserLocation.Source.DETECTED,
        )
    }

    suspend fun geocodeCity(cityName: String): UserLocation? = withContext(Dispatchers.IO) {
        runCatching {
            val geocoder = Geocoder(context)
            @Suppress("DEPRECATION")
            val results = geocoder.getFromLocationName(cityName, 1)
            val addr = results?.firstOrNull() ?: return@runCatching null
            UserLocation(
                label = addr.locality ?: addr.adminArea ?: cityName,
                lat = addr.latitude,
                lon = addr.longitude,
                source = UserLocation.Source.MANUAL,
            )
        }.getOrNull()
    }

    private suspend fun reverseGeocode(lat: Double, lon: Double): String? = withContext(Dispatchers.IO) {
        runCatching {
            val geocoder = Geocoder(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(lat, lon, 1) { addresses ->
                        cont.resume(addresses.firstOrNull()?.locality ?: addresses.firstOrNull()?.adminArea)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()?.let { it.locality ?: it.adminArea }
            }
        }.getOrNull()
    }
}
