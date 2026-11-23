package dev.openpoli.app.ui.login

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Layout values measured from the Penpot frame "Login / M3 Expressive" (390 x 844). */
internal object LoginSpec {
    val ScreenPadding = 24.dp
    val TopGap = 52.dp

    val ChipHeight = 34.dp
    val ChipRadius = 17.dp
    val ChipPadding = 14.dp
    val ChipIcon = 13.dp
    val ChipIconGap = 8.dp

    val GapChipsToBubble = 26.dp
    val GapBubbleEmoji = 6.dp
    val BubbleHeight = 30.dp
    val BubbleRadius = 15.dp
    val BubblePadding = 12.dp

    val HeroHeight = 276.dp
    val GapHeroToWordmark = 14.dp
    val GapWordmarkToSubtitle = 16.dp
    val GapBetweenSubtitles = 4.dp
    val GapSubtitleToField = 19.dp
    val GapBetweenFields = 20.dp
    val FieldHeight = 56.dp
    val FieldRadius = 10.dp
    val GapFieldToForgot = 10.dp
    val GapForgotToButton = 17.dp
    val ButtonHeight = 56.dp
    val ButtonRadius = 28.dp
    val LeadingIconSize = 24.dp
    val TrailingIconSize = 22.dp
}

internal object LoginType {
    val Chip = 12.5.sp
    val Bubble = 11.sp
    val Wordmark = 40.sp
    val Subtitle = 14.5.sp
    val SubtitleSmall = 12.sp
    val Field = 16.sp
    val Forgot = 12.5.sp
    val Button = 14.sp
    val Monogram = 24.sp
    val MonogramSmall = 12.sp
    val Pill = 13.sp
}
