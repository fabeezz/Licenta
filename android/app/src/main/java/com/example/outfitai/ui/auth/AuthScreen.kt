package com.example.outfitai.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType

private enum class AuthMode { Login, Register }

@Composable
fun AuthScreen(
  state: AuthUiState,
  onUsername: (String) -> Unit,
  onEmail: (String) -> Unit,
  onPassword: (String) -> Unit,
  onLogin: () -> Unit,
  onRegister: () -> Unit,
) {
  var mode by remember { mutableStateOf(AuthMode.Login) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(20.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text("OutfitAI", style = MaterialTheme.typography.headlineMedium)

    SingleChoiceSegmentedButtonRow {
      SegmentedButton(
        selected = mode == AuthMode.Login,
        onClick = { mode = AuthMode.Login },
        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
      ) { Text("Login") }

      SegmentedButton(
        selected = mode == AuthMode.Register,
        onClick = { mode = AuthMode.Register },
        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
      ) { Text("Register") }
    }

    OutlinedTextField(
      value = state.username,
      onValueChange = onUsername,
      label = { Text("Username") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      enabled = !state.isBusy,
      keyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Words,
        keyboardType = KeyboardType.Text
      )
    )

    if (mode == AuthMode.Register) {
      OutlinedTextField(
        value = state.email,
        onValueChange = onEmail,
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = !state.isBusy,
        keyboardOptions = KeyboardOptions(
          capitalization = KeyboardCapitalization.None,
          keyboardType = KeyboardType.Email
        )
      )
    }

    OutlinedTextField(
      value = state.password,
      onValueChange = onPassword,
      label = { Text("Password") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      visualTransformation = PasswordVisualTransformation(),
      enabled = !state.isBusy,
      keyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        keyboardType = KeyboardType.Password
      )
    )

    if (state.error != null) {
      Text(state.error, color = MaterialTheme.colorScheme.error)
    }

    Button(
      onClick = { if (mode == AuthMode.Login) onLogin() else onRegister() },
      modifier = Modifier.fillMaxWidth(),
      enabled = !state.isBusy
    ) {
      if (state.isBusy) {
        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
      }
      Text(if (mode == AuthMode.Login) "Login" else "Create account")
    }
  }
}