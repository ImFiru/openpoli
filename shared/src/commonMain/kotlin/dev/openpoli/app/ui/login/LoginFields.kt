package dev.openpoli.app.ui.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType

@Composable
internal fun LoginEmailField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(LoginSpec.FieldHeight),
        placeholder = { Text(text = "Email or username", fontSize = LoginType.Field) },
        leadingIcon = { PersonGlyph(MaterialTheme.colorScheme.onSurfaceVariant) },
        singleLine = true,
        shape = RoundedCornerShape(LoginSpec.FieldRadius),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
    )
}

@Composable
internal fun LoginPasswordField(
    value: String,
    isVisible: Boolean,
    onValueChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
) {
    val state = rememberTextFieldState(initialText = value)
    val colors = MaterialTheme.colorScheme

    LaunchedEffect(state, onValueChange) {
        snapshotFlow { state.text.toString() }.collect(onValueChange)
    }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(LoginSpec.FieldHeight)
                .background(colors.surfaceVariant, RoundedCornerShape(LoginSpec.FieldRadius))
                .padding(horizontal = LoginSpec.BubblePadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LockGlyph(colors.onSurfaceVariant)
        Spacer(Modifier.width(LoginSpec.BubblePadding))
        Box(Modifier.weight(1f)) {
            if (state.text.isEmpty()) {
                Text(
                    text = "Password",
                    color = colors.onSurfaceVariant,
                    fontSize = LoginType.Field,
                )
            }
            BasicSecureTextField(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = colors.onSurface, fontSize = LoginType.Field),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                textObfuscationMode =
                    if (isVisible) {
                        TextObfuscationMode.Visible
                    } else {
                        TextObfuscationMode.RevealLastTyped
                    },
            )
        }
        IconButton(onClick = onToggleVisibility) {
            EyeGlyph()
        }
    }
}

@Composable
private fun PersonGlyph(tint: Color) {
    Canvas(Modifier.size(LoginSpec.LeadingIconSize)) {
        val stroke = size.minDimension * 0.09f
        drawCircle(
            color = tint,
            radius = size.width * 0.18f,
            center = Offset(size.width / 2f, size.height * 0.3f),
            style = Stroke(width = stroke),
        )
        drawArc(
            color = tint,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(size.width * 0.18f, size.height * 0.52f),
            size = Size(size.width * 0.64f, size.height * 0.42f),
            style = Stroke(width = stroke),
        )
    }
}

@Composable
private fun LockGlyph(tint: Color) {
    Canvas(Modifier.size(LoginSpec.LeadingIconSize)) {
        val stroke = size.minDimension * 0.09f
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.16f, size.height * 0.45f),
            size = Size(size.width * 0.68f, size.height * 0.45f),
            cornerRadius = CornerRadius(size.width * 0.1f),
            style = Stroke(width = stroke),
        )
        drawArc(
            color = tint,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(size.width * 0.3f, size.height * 0.12f),
            size = Size(size.width * 0.4f, size.height * 0.4f),
            style = Stroke(width = stroke),
        )
    }
}

@Composable
private fun EyeGlyph() {
    val tint = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(Modifier.size(LoginSpec.TrailingIconSize)) {
        val w = size.width
        val h = size.height
        val path =
            Path().apply {
                moveTo(0f, h / 2f)
                quadraticTo(w / 2f, -h * 0.15f, w, h / 2f)
                quadraticTo(w / 2f, h * 1.15f, 0f, h / 2f)
            }
        drawPath(path, tint, style = Stroke(width = h * 0.12f))
        drawCircle(tint, radius = h * 0.18f, center = Offset(w / 2f, h / 2f))
    }
}
