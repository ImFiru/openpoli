package dev.openpoli.app.ui.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun LoginChips(
    onTheme: () -> Unit,
    onLanguage: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        LoginChip(label = "Theme", onClick = onTheme) { PaletteGlyph() }
        LoginChip(label = "English", onClick = onLanguage) { GlobeGlyph() }
    }
}

@Composable
private fun LoginChip(
    label: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .height(LoginSpec.ChipHeight)
                .background(MaterialTheme.colorScheme.outline, RoundedCornerShape(LoginSpec.ChipRadius))
                .clickable(onClick = onClick)
                .padding(horizontal = LoginSpec.ChipPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LoginSpec.ChipIconGap),
    ) {
        icon()
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = LoginType.Chip,
        )
    }
}

@Composable
private fun PaletteGlyph() {
    val tint = MaterialTheme.colorScheme.onPrimary
    Canvas(Modifier.size(LoginSpec.ChipIcon)) {
        val radius = size.minDimension / 2f
        drawCircle(tint, radius, style = Stroke(width = size.minDimension * 0.16f))
        drawCircle(tint, radius * 0.2f, center = Offset(size.width * 0.34f, size.height * 0.62f))
        drawCircle(tint, radius * 0.16f, center = Offset(size.width * 0.62f, size.height * 0.3f))
    }
}

@Composable
private fun GlobeGlyph() {
    val tint = MaterialTheme.colorScheme.onPrimary
    Canvas(Modifier.size(LoginSpec.ChipIcon)) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(tint, radius, center, style = Stroke(width = radius * 0.18f))
        drawLine(tint, Offset(center.x, 0f), Offset(center.x, size.height), strokeWidth = radius * 0.12f)
        drawLine(tint, Offset(0f, center.y), Offset(size.width, center.y), strokeWidth = radius * 0.12f)
    }
}

@Composable
internal fun SpeechBubble() {
    Row(
        modifier =
            Modifier
                .height(LoginSpec.BubbleHeight)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(LoginSpec.BubbleRadius),
                ).padding(horizontal = LoginSpec.BubblePadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "By students, for students",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = LoginType.Bubble,
        )
        Spacer(Modifier.width(LoginSpec.GapBubbleEmoji))
        Text(text = "\uD83E\uDD1D", fontSize = LoginType.Bubble)
    }
}

@Composable
internal fun Wordmark() {
    Text(
        text = "openpoli",
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = LoginType.Wordmark,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-1).sp,
        textAlign = TextAlign.Center,
    )
}

@Composable
internal fun Subtitles() {
    Text(
        text = "An unofficial app, not made by the UPV.",
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = LoginType.Subtitle,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(LoginSpec.GapBetweenSubtitles))
    Text(
        text = "Sign in to continue.",
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = LoginType.SubtitleSmall,
        textAlign = TextAlign.Center,
    )
}

@Composable
internal fun ForgotPassword(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(
            text = "Forgot password?",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.primary,
            fontSize = LoginType.Forgot,
        )
    }
}

@Composable
internal fun SubmitButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(LoginSpec.ButtonHeight),
        shape = RoundedCornerShape(LoginSpec.ButtonRadius),
        colors =
            ButtonDefaults.textButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
    ) {
        Text(text = "Log in", fontSize = LoginType.Button)
    }
}
