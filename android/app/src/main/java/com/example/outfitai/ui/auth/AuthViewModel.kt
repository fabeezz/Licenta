package com.example.outfitai.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfitai.data.api.AuthApi
import com.example.outfitai.data.auth.AuthRepository
import com.example.outfitai.data.auth.AuthStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
  private val authApi: AuthApi,
  private val repo: AuthRepository,
  private val authStore: AuthStore,
) : ViewModel() {

  private val _state = MutableStateFlow(AuthUiState())
  val state = _state.asStateFlow()

  init {
    bootstrap()
  }

  fun bootstrap() {
    _state.value = _state.value.copy(status = AuthStatus.Checking, error = null, isBusy = true)
    viewModelScope.launch {
      val token = authStore.token.first()
      if (token.isNullOrBlank()) {
        _state.value = _state.value.copy(status = AuthStatus.LoggedOut, isBusy = false)
        return@launch
      }

      try {
        // interceptorul tău va adăuga automat Authorization: Bearer <token>
        val me = authApi.me()
        _state.value = _state.value.copy(status = AuthStatus.LoggedIn(me), isBusy = false)
      } catch (e: HttpException) {
        if (e.code() == 401) {
          authStore.clear()
          _state.value = _state.value.copy(
            status = AuthStatus.LoggedOut,
            isBusy = false,
            error = "Sesiunea a expirat. Te rugăm să te loghezi din nou."
          )
        } else {
          _state.value = _state.value.copy(status = AuthStatus.LoggedOut, isBusy = false, error = "Eroare server (${e.code()}).")
        }
      } catch (_: IOException) {
        _state.value = _state.value.copy(status = AuthStatus.LoggedOut, isBusy = false, error = "Nu pot contacta serverul.")
      }
    }
  }

  fun onUsernameChange(v: String) { _state.value = _state.value.copy(username = v, error = null) }
  fun onEmailChange(v: String) { _state.value = _state.value.copy(email = v, error = null) }
  fun onPasswordChange(v: String) { _state.value = _state.value.copy(password = v, error = null) }

  fun login() {
    val s = _state.value
    if (s.username.isBlank() || s.password.isBlank()) {
      _state.value = s.copy(error = "Completează username și password.")
      return
    }

    _state.value = s.copy(isBusy = true, error = null)
    viewModelScope.launch {
      try {
        repo.login(s.username.trim(), s.password)
        val me = authApi.me()
        _state.value = _state.value.copy(status = AuthStatus.LoggedIn(me), isBusy = false, password = "")
      } catch (e: HttpException) {
        _state.value = _state.value.copy(isBusy = false, error = if (e.code() == 401) "Username/parolă greșite." else "Eroare server (${e.code()}).")
      } catch (_: IOException) {
        _state.value = _state.value.copy(isBusy = false, error = "Nu pot contacta serverul.")
      }
    }
  }

  fun register() {
    val s = _state.value
    if (s.username.isBlank() || s.email.isBlank() || s.password.isBlank()) {
      _state.value = s.copy(error = "Completează username, email și password.")
      return
    }

    _state.value = s.copy(isBusy = true, error = null)
    viewModelScope.launch {
      try {
        repo.register(s.username.trim(), s.email.trim(), s.password)
        val me = authApi.me()
        _state.value = _state.value.copy(status = AuthStatus.LoggedIn(me), isBusy = false, password = "")
      } catch (e: HttpException) {
        _state.value = _state.value.copy(isBusy = false, error = "Nu pot crea cont (${e.code()}).")
      } catch (_: IOException) {
        _state.value = _state.value.copy(isBusy = false, error = "Nu pot contacta serverul.")
      }
    }
  }

  fun resetPassword() {
    val s = _state.value
    if (s.username.isBlank() || s.email.isBlank() || s.password.isBlank()) {
      _state.value = s.copy(error = "Completează username, email și parola nouă.")
      return
    }

    _state.value = s.copy(isBusy = true, error = null)
    viewModelScope.launch {
      try {
        repo.resetPassword(s.username.trim(), s.email.trim(), s.password)
        val me = authApi.me()
        _state.value = _state.value.copy(status = AuthStatus.LoggedIn(me), isBusy = false, password = "")
      } catch (e: HttpException) {
        _state.value = _state.value.copy(isBusy = false, error = if (e.code() == 400) "Username și email nu se potrivesc." else "Eroare server (${e.code()}).")
      } catch (_: IOException) {
        _state.value = _state.value.copy(isBusy = false, error = "Nu pot contacta serverul.")
      }
    }
  }

  fun logout() {
    viewModelScope.launch {
      repo.logout()
      _state.value = AuthUiState(status = AuthStatus.LoggedOut)
    }
  }
}