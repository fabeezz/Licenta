package com.example.outfitai.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.outfitai.data.model.UserOutDto

@Composable
fun HomeScreen(
  user: UserOutDto,
  onLogout: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(20.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text("Autentificat", style = MaterialTheme.typography.headlineSmall)
    Text("Username: ${user.username}")
    Text("Email: ${user.email}")

    Spacer(Modifier.height(12.dp))

    OutlinedButton(onClick = onLogout) {
      Text("Logout")
    }
  }
}