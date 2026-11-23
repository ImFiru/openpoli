package dev.openpoli.app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.openpoli.app.presentation.login.LoginUiAction
import dev.openpoli.app.presentation.login.LoginUiState
import dev.openpoli.app.ui.theme.AppTheme

@Composable
internal fun LoginContent(
    uiState: LoginUiState,
    onAction: (LoginUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = LoginSpec.ScreenPadding),
    ) {
        Spacer(Modifier.height(LoginSpec.TopGap))

        LoginChips(
            onTheme = { onAction(LoginUiAction.ToggleTheme) },
            onLanguage = { onAction(LoginUiAction.SwitchLanguage) },
        )

        Spacer(Modifier.height(LoginSpec.GapChipsToBubble))
        SpeechBubble()
        LoginHero()

        Spacer(Modifier.height(LoginSpec.GapHeroToWordmark))
        Wordmark()

        Spacer(Modifier.height(LoginSpec.GapWordmarkToSubtitle))
        Subtitles()

        Spacer(Modifier.height(LoginSpec.GapSubtitleToField))
        LoginEmailField(
            value = uiState.username,
            onValueChange = { onAction(LoginUiAction.UsernameChanged(it)) },
        )

        Spacer(Modifier.height(LoginSpec.GapBetweenFields))
        LoginPasswordField(
            value = uiState.password,
            isVisible = uiState.isPasswordVisible,
            onValueChange = { onAction(LoginUiAction.PasswordChanged(it)) },
            onToggleVisibility = { onAction(LoginUiAction.TogglePasswordVisibility) },
        )

        Spacer(Modifier.height(LoginSpec.GapFieldToForgot))
        ForgotPassword { onAction(LoginUiAction.ForgotPassword) }

        Spacer(Modifier.height(LoginSpec.GapForgotToButton))
        SubmitButton(
            enabled = !uiState.isSubmitting,
            onClick = { onAction(LoginUiAction.Submit) },
        )

        Spacer(Modifier.height(LoginSpec.ScreenPadding))
    }
}

@Preview
@Composable
private fun LoginContentPreview() {
    AppTheme {
        LoginContent(uiState = LoginUiState(), onAction = {})
    }
}

@Preview
@Composable
private fun LoginContentDarkPreview() {
    AppTheme(darkTheme = true) {
        LoginContent(uiState = LoginUiState(), onAction = {})
    }
}
