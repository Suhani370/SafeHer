package com.safeher.app.core.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CrimsonEmergency,
    onPrimary = Color.White,
    primaryContainer = CrimsonEmergencyDark,
    onPrimaryContainer = Color.White,
    secondary = AlertAmber,
    onSecondary = Color.Black,
    secondaryContainer = AlertAmberDark,
    onSecondaryContainer = Color.White,
    tertiary = PrimaryPurple,
    background = NeutralDarkBackground,
    surface = NeutralDarkSurface,
    surfaceVariant = NeutralDarkCard,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    error = CrimsonEmergency
)

private val LightColorScheme = lightColorScheme(
    primary = CrimsonEmergency,
    onPrimary = Color.White,
    primaryContainer = CrimsonEmergencyLight,
    onPrimaryContainer = CrimsonEmergencyDark,
    secondary = AlertAmber,
    onSecondary = Color.Black,
    secondaryContainer = AlertAmberLight,
    onSecondaryContainer = AlertAmberDark,
    tertiary = PrimaryPurple,
    background = NeutralLightBackground,
    surface = NeutralLightSurface,
    surfaceVariant = NeutralLightCard,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    error = CrimsonEmergency
)

@Composable
fun SafeHerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
