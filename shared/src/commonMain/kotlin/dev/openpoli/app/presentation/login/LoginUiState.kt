package dev.openpoli.app.presentation.login

import androidx.compose.runtime.Immutable

@Immutable
data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isSubmitting: Boolean = false,
)

@Immutable
sealed interface LoginUiAction {
    data class UsernameChanged(
        val value: String,
    ) : LoginUiAction

    data class PasswordChanged(
        val value: String,
    ) : LoginUiAction

    data object TogglePasswordVisibility : LoginUiAction

    data object ForgotPassword : LoginUiAction

    data object Submit : LoginUiAction

    data object ToggleTheme : LoginUiAction

    data object SwitchLanguage : LoginUiAction
}
