package com.example.outfitai.ui.auth

import com.example.outfitai.data.model.UserOutDto

sealed interface AuthStatus {
  data object Checking : AuthStatus
  data object LoggedOut : AuthStatus
  data class LoggedIn(val user: UserOutDto) : AuthStatus
}

data class AuthUiState(
  val status: AuthStatus = AuthStatus.Checking,
  val username: String = "",
  val email: String = "",
  val password: String = "",
  val isBusy: Boolean = false,
  val error: String? = null,
)