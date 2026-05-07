package com.example.outfitai.data.location

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.outfitai.Config
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.locationDataStore by preferencesDataStore(name = "location")

class LocationStore(private val context: Context) {
    private val KEY_LABEL = stringPreferencesKey("loc_label")
    private val KEY_LAT   = doublePreferencesKey("loc_lat")
    private val KEY_LON   = doublePreferencesKey("loc_lon")
    private val KEY_SOURCE = stringPreferencesKey("loc_source")

    val location: Flow<UserLocation> = context.locationDataStore.data.map { prefs ->
        UserLocation(
            label  = prefs[KEY_LABEL]  ?: Config.DEFAULT_LOCATION_LABEL,
            lat    = prefs[KEY_LAT]    ?: Config.DEFAULT_LAT,
            lon    = prefs[KEY_LON]    ?: Config.DEFAULT_LON,
            source = prefs[KEY_SOURCE]?.let { runCatching { UserLocation.Source.valueOf(it) }.getOrNull() }
                ?: UserLocation.Source.DEFAULT,
        )
    }

    suspend fun set(location: UserLocation) {
        context.locationDataStore.edit { prefs ->
            prefs[KEY_LABEL]  = location.label
            prefs[KEY_LAT]    = location.lat
            prefs[KEY_LON]    = location.lon
            prefs[KEY_SOURCE] = location.source.name
        }
    }
}
