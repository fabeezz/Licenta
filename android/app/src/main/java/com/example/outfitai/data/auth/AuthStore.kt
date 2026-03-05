package com.example.outfitai.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth")

class AuthStore(private val context: Context) {
  private val KEY_TOKEN = stringPreferencesKey("access_token")

  val token: Flow<String?> =
    context.dataStore.data.map { prefs -> prefs[KEY_TOKEN] }

  suspend fun setToken(value: String) {
    context.dataStore.edit { it[KEY_TOKEN] = value }
  }

  suspend fun clear() {
    context.dataStore.edit { it.remove(KEY_TOKEN) }
  }
}