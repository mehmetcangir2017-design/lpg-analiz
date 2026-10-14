package com.murat.lpganaliz.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Yesil = Color(0xFF0B6B4F)
private val YesilAcik = Color(0xFF4CAF7D)
private val Amber = Color(0xFFF2A93B)

private val AcikRenkler = lightColorScheme(
    primary = Yesil,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8EBD5),
    onPrimaryContainer = Color(0xFF00210F),
    secondary = Amber,
    onSecondary = Color(0xFF2A1A00),
    tertiary = YesilAcik,
    background = Color(0xFFF8FBF8),
    surface = Color.White,
    surfaceVariant = Color(0xFFE7EFE9)
)

private val KoyuRenkler = darkColorScheme(
    primary = YesilAcik,
    onPrimary = Color(0xFF00311C),
    primaryContainer = Color(0xFF00512F),
    onPrimaryContainer = Color(0xFFB8EBD5),
    secondary = Amber,
    onSecondary = Color(0xFF2A1A00),
    background = Color(0xFF101410),
    surface = Color(0xFF171C18),
    surfaceVariant = Color(0xFF25302A)
)

@Composable
fun LpgAnalizTema(icerik: @Composable () -> Unit) {
    val koyu = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (koyu) KoyuRenkler else AcikRenkler,
        content = icerik
    )
}
