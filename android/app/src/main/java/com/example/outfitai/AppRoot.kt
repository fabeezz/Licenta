package com.example.outfitai

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import com.example.outfitai.ui.auth.*
import com.example.outfitai.ui.nav.AppNav
import com.example.outfitai.ui.wardrobe.WardrobeRoute

@Composable
fun AppRoot(vm: AuthViewModel) {
  val state by vm.state.collectAsState()

  when (val st = state.status) {
    AuthStatus.Checking -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator()
    }

    AuthStatus.LoggedOut -> AuthScreen(
      state = state,
      onUsername = vm::onUsernameChange,
      onEmail = vm::onEmailChange,
      onPassword = vm::onPasswordChange,
      onLogin = vm::login,
      onRegister = vm::register
    )

    is AuthStatus.LoggedIn -> AppNav(onLogout = vm::logout)
  }
}