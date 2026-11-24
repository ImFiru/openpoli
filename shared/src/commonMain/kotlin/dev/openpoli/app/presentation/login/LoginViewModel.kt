package dev.openpoli.app.presentation.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<LoginUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<LoginUiEvent> = _uiEvents.asSharedFlow()

    fun onAction(action: LoginUiAction) {
        when (action) {
            is LoginUiAction.UsernameChanged -> {
                _uiState.update { it.copy(username = action.value) }
            }

            is LoginUiAction.PasswordChanged -> {
                _uiState.update { it.copy(password = action.value) }
            }

            LoginUiAction.TogglePasswordVisibility -> {
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }

            LoginUiAction.Submit -> {
                submit()
            }

            LoginUiAction.ForgotPassword -> {
                _uiEvents.tryEmit(LoginUiEvent.OpenForgotPassword)
            }

            LoginUiAction.ToggleTheme -> {
                _uiEvents.tryEmit(LoginUiEvent.ToggleTheme)
            }

            LoginUiAction.SwitchLanguage -> {
                _uiEvents.tryEmit(LoginUiEvent.OpenLanguagePicker)
            }
        }
    }

    private fun submit() {
        val state = _uiState.value
        val error = validationError(state)
        if (error != null) {
            _uiEvents.tryEmit(LoginUiEvent.ShowMessage(error))
            return
        }
        // Authentication lands in a later slice; this only reports the request.
        _uiEvents.tryEmit(LoginUiEvent.LoginRequested(state.username.trim()))
    }

    private fun validationError(state: LoginUiState): String? =
        when {
            state.username.isBlank() -> "Enter your email or username"
            state.password.isBlank() -> "Enter your password"
            else -> null
        }
}
