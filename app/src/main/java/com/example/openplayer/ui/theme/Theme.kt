package com.example.openplayer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = OrangeSecondary,
    onPrimary = OrangeOnSecondary,
    primaryContainer = OrangeListLightDark,
    onPrimaryContainer = OrangeListLight,
    secondary = OrangeTertiary,
    tertiary = OrangePrimary,
    surface = OrangeListDarkDark,
    surfaceVariant = OrangeListLightDark,
)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = OrangeOnPrimary,
    primaryContainer = OrangeListDark,
    onPrimaryContainer = OrangePrimary,
    secondary = OrangeSecondary,
    tertiary = OrangeTertiary,
    surface = OrangeListLight,
    surfaceVariant = OrangeListDark,
)

@Composable
fun OpenPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme) {
                androidx.compose.material3.dynamicDarkColorScheme(context)
            } else {
                androidx.compose.material3.dynamicLightColorScheme(context)
            }
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
