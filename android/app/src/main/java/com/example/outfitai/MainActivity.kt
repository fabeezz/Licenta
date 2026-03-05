package com.example.outfitai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.outfitai.ui.auth.AuthViewModel
import com.example.outfitai.ui.theme.OutfitAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OutfitAITheme {
                val vm: AuthViewModel = hiltViewModel()
                AppRoot(vm = vm)
            }
        }
    }
}