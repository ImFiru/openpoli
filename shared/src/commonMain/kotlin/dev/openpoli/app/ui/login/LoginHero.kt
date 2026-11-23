package dev.openpoli.app.ui.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import dev.openpoli.app.ui.theme.appColors

/**
 * Structural placeholder for the Penpot illustration cluster: blobs, monograms and the
 * cap are drawn from frame measurements; the emoji artwork lands as an exported SVG later.
 */
@Composable
internal fun LoginHero() {
    val brand = MaterialTheme.appColors
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(LoginSpec.HeroHeight),
    ) {
        Blob(Triple(23.dp, 9.dp, 78.dp), brand.brandTeal)
        Blob(Triple(239.dp, 0.dp, 68.dp), brand.brandOrange)
        Blob(Triple(114.dp, 51.dp, 116.dp), MaterialTheme.colorScheme.primary)
        Blob(Triple(34.dp, 157.dp, 70.dp), brand.brandYellow)
        Blob(Triple(225.dp, 144.dp, 73.dp), brand.brandPurple)
        Blob(Triple(173.dp, 216.dp, 49.dp), brand.brandBlue)

        Monogram("PB", Pair(251.dp, 32.dp), LoginType.Monogram, brand.brandInk)
        Monogram("FA", Pair(186.dp, 243.dp), LoginType.MonogramSmall, brand.brandInk)
        GraduationCap(Triple(148.dp, 88.dp, 47.dp))
        AlwaysPill()
    }
}

@Composable
private fun Blob(
    spec: Triple<Dp, Dp, Dp>,
    color: Color,
) {
    val (x, y, blobSize) = spec
    Box(
        Modifier
            .offset(x = x, y = y)
            .size(blobSize)
            .background(color, CircleShape),
    )
}

@Composable
private fun Monogram(
    text: String,
    spec: Pair<Dp, Dp>,
    fontSize: TextUnit,
    color: Color,
) {
    val (x, y) = spec
    Text(
        text = text,
        modifier = Modifier.offset(x = x, y = y),
        color = color,
        fontSize = fontSize,
    )
}

@Composable
private fun AlwaysPill() {
    val (x, y, pillHeight) = Triple(14.dp, 251.dp, 30.dp)
    Box(
        modifier =
            Modifier
                .offset(x = x, y = y)
                .width(92.dp)
                .height(pillHeight)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(pillHeight / 2)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Always",
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = LoginType.Pill,
        )
    }
}

@Composable
private fun GraduationCap(spec: Triple<Dp, Dp, Dp>) {
    val (x, y, capSize) = spec
    Canvas(
        Modifier
            .offset(x = x, y = y)
            .size(capSize, capSize * 0.83f),
    ) {
        val w = size.width
        val h = size.height
        val board =
            Path().apply {
                moveTo(w * 0.5f, h * 0.05f)
                lineTo(w * 0.97f, h * 0.3f)
                lineTo(w * 0.5f, h * 0.55f)
                lineTo(w * 0.03f, h * 0.3f)
                close()
            }
        drawPath(board, Color.White)
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.24f, h * 0.62f),
            size = Size(w * 0.52f, h * 0.12f),
            cornerRadius = CornerRadius(h * 0.06f),
        )
        drawLine(
            color = Color.White,
            start = Offset(w * 0.93f, h * 0.35f),
            end = Offset(w * 0.93f, h * 0.82f),
            strokeWidth = h * 0.06f,
        )
    }
}
