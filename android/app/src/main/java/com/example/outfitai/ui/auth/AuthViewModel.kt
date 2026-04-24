package com.example.outfitai.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.auth.AuthRepository
import com.example.outfitai.domain.usecase.auth.GetCurrentUserUseCase
import com.example.outfitai.domain.usecase.auth.LoginUseCase
import com.example.outfitai.domain.usecase.auth.RegisterUseCase
import com.example.outfitai.domain.usecase.auth.ResetPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val getCurrentUser: GetCurrentUserUseCase,
    private val login: LoginUseCase,
    private val register: RegisterUseCase,
    private val resetPassword: ResetPasswordUseCase,
    private val authRepo: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    private fun reduce(block: AuthUiState.() -> AuthUiState) =
        _state.update { it.block() }

    init {
        bootstrap()
    }

    fun bootstrap() {
        reduce { copy(status = AuthStatus.Checking, error = null, isBusy = true) }
        viewModelScope.launch {
            when (val result = getCurrentUser()) {
                is Resource.Success -> {
                    val user = result.data
                    reduce {
                        if (user == null) copy(status = AuthStatus.LoggedOut, isBusy = false)
                        else copy(status = AuthStatus.LoggedIn(user), isBusy = false)
                    }
                }
                is Resource.Error -> reduce {
                    copy(status = AuthStatus.LoggedOut, isBusy = false, error = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onUsernameChange(v: String) { reduce { copy(username = v, error = null) } }
    fun onEmailChange(v: String) { reduce { copy(email = v, error = null) } }
    fun onPasswordChange(v: String) { reduce { copy(password = v, error = null) } }

    fun login() {
        val s = _state.value
        if (s.username.isBlank() || s.password.isBlank()) {
            reduce { copy(error = "Completează username și password.") }
            return
        }
        reduce { copy(isBusy = true, error = null) }
        viewModelScope.launch {
            when (val result = login(s.username.trim(), s.password)) {
                is Resource.Success -> reduce {
                    copy(status = AuthStatus.LoggedIn(result.data), isBusy = false, password = "")
                }
                is Resource.Error -> reduce {
                    val msg = if (result.httpCode == 401) "Username/parolă greșite." else result.message
                    copy(isBusy = false, error = msg)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun register() {
        val s = _state.value
        if (s.username.isBlank() || s.email.isBlank() || s.password.isBlank()) {
            reduce { copy(error = "Completează username, email și password.") }
            return
        }
        reduce { copy(isBusy = true, error = null) }
        viewModelScope.launch {
            when (val result = register(s.username.trim(), s.email.trim(), s.password)) {
                is Resource.Success -> reduce {
                    copy(status = AuthStatus.LoggedIn(result.data), isBusy = false, password = "")
                }
                is Resource.Error -> reduce { copy(isBusy = false, error = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun resetPassword() {
        val s = _state.value
        if (s.username.isBlank() || s.email.isBlank() || s.password.isBlank()) {
            reduce { copy(error = "Completează username, email și parola nouă.") }
            return
        }
        reduce { copy(isBusy = true, error = null) }
        viewModelScope.launch {
            when (val result = resetPassword(s.username.trim(), s.email.trim(), s.password)) {
                is Resource.Success -> reduce {
                    copy(status = AuthStatus.LoggedIn(result.data), isBusy = false, password = "")
                }
                is Resource.Error -> reduce {
                    val msg = if (result.httpCode == 400) "Username și email nu se potrivesc." else result.message
                    copy(isBusy = false, error = msg)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.logout()
            _state.value = AuthUiState(status = AuthStatus.LoggedOut)
        }
    }
}
