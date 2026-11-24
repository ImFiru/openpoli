package dev.openpoli.app.presentation.login

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LoginViewModelTest {
    private val sut = LoginViewModel()

    @Test
    fun `When created then state is empty and the password is hidden`() {
        // create objects and fakes
        val expected = LoginUiState()

        // execute
        val actual = sut.uiState.value

        // assert
        assertEquals(expected = expected, actual = actual)
        assertFalse(actual.isPasswordVisible)
    }

    @Test
    fun `When username changes then it is kept in state`() {
        sut.onAction(LoginUiAction.UsernameChanged("student@upv.es"))

        assertEquals(expected = "student@upv.es", actual = sut.uiState.value.username)
    }

    @Test
    fun `When password changes then it is kept in state`() {
        sut.onAction(LoginUiAction.PasswordChanged("hunter2"))

        assertEquals(expected = "hunter2", actual = sut.uiState.value.password)
    }

    @Test
    fun `When visibility is toggled then the password becomes visible`() {
        sut.onAction(LoginUiAction.TogglePasswordVisibility)

        assertTrue(sut.uiState.value.isPasswordVisible)
    }

    @Test
    fun `When submitting with an empty form then a message is emitted`() =
        runTest {
            sut.uiEvents.test {
                sut.onAction(LoginUiAction.Submit)

                assertEquals(
                    expected = LoginUiEvent.ShowMessage("Enter your email or username"),
                    actual = awaitItem(),
                )
                expectNoEvents()
            }
        }

    @Test
    fun `When submitting without a password then a message is emitted`() =
        runTest {
            sut.onAction(LoginUiAction.UsernameChanged("student@upv.es"))

            sut.uiEvents.test {
                sut.onAction(LoginUiAction.Submit)

                assertEquals(
                    expected = LoginUiEvent.ShowMessage("Enter your password"),
                    actual = awaitItem(),
                )
                expectNoEvents()
            }
        }

    @Test
    fun `When submitting a complete form then a login is requested`() =
        runTest {
            sut.onAction(LoginUiAction.UsernameChanged("  student@upv.es  "))
            sut.onAction(LoginUiAction.PasswordChanged("hunter2"))

            sut.uiEvents.test {
                sut.onAction(LoginUiAction.Submit)

                assertEquals(
                    expected = LoginUiEvent.LoginRequested("student@upv.es"),
                    actual = awaitItem(),
                )
                expectNoEvents()
            }
        }

    @Test
    fun `When secondary actions fire then their events are emitted`() =
        runTest {
            sut.uiEvents.test {
                sut.onAction(LoginUiAction.ForgotPassword)
                assertEquals(expected = LoginUiEvent.OpenForgotPassword, actual = awaitItem())

                sut.onAction(LoginUiAction.ToggleTheme)
                assertEquals(expected = LoginUiEvent.ToggleTheme, actual = awaitItem())

                sut.onAction(LoginUiAction.SwitchLanguage)
                assertEquals(expected = LoginUiEvent.OpenLanguagePicker, actual = awaitItem())

                expectNoEvents()
            }
        }
}
