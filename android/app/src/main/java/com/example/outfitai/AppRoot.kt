package com.example.outfitai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.outfitai.ui.auth.*
import com.example.outfitai.ui.nav.AppNav
import com.example.outfitai.ui.onboarding.*

@Composable
fun AppRoot(vm: AuthViewModel) {
    val state by vm.state.collectAsState()

    when (val st = state.status) {
        AuthStatus.Checking -> Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }

        AuthStatus.LoggedOut -> AuthScreen(
            state = state,
            onUsername = vm::onUsernameChange,
            onEmail = vm::onEmailChange,
            onPassword = vm::onPasswordChange,
            onLogin = vm::login,
            onRegister = vm::register,
            onResetPassword = vm::resetPassword,
        )

        is AuthStatus.LoggedIn -> {
            if (st.user.onboardedAt == null) {
                val onboardingVm: OnboardingViewModel = hiltViewModel()
                val onboardingState by onboardingVm.state.collectAsState()

                LaunchedEffect(st.user.username) {
                    onboardingVm.initDisplayName(st.user.username)
                }

                if (onboardingState.step == OnboardingStep.WELCOME) {
                    WelcomeStepContent(onGetStarted = onboardingVm::next)
                } else {
                    OnboardingScreen(
                        state = onboardingState,
                        stepIndex = onboardingVm.stepIndex,
                        totalSteps = onboardingVm.totalSteps,
                        isStepValid = onboardingVm.isStepValid(),
                        onGender = onboardingVm::setGender,
                        onToggleStyle = onboardingVm::toggleStyle,
                        onDetectLocation = onboardingVm::detectLocation,
                        onCityInput = onboardingVm::updateCityInput,
                        onCitySubmit = onboardingVm::submitCityInput,
                        onDismissDetectError = onboardingVm::dismissDetectError,
                        onDisplayName = onboardingVm::setDisplayName,
                        onNext = onboardingVm::next,
                        onBack = onboardingVm::back,
                        onFinished = { vm.bootstrap() },
                    )
                }
            } else {
                AppNav(onLogout = vm::logout)
            }
        }
    }
}