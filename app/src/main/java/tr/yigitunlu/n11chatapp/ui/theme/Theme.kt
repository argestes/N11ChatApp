package tr.yigitunlu.n11chatapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = N11Orange,
    onPrimary = N11Surface,
    primaryContainer = N11OrangeLight,
    onPrimaryContainer = N11TextPrimary,
    secondary = N11Blue,
    onSecondary = N11Surface,
    secondaryContainer = N11BlueLight,
    onSecondaryContainer = N11TextPrimary,
    tertiary = N11Success,
    error = N11Error,
    background = N11Background,
    surface = N11Surface,
    onBackground = N11TextPrimary,
    onSurface = N11TextPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = N11OrangeLight,
    onPrimary = N11TextPrimary,
    primaryContainer = N11Orange,
    onPrimaryContainer = N11Surface,
    secondary = N11BlueLight,
    onSecondary = N11TextPrimary,
    secondaryContainer = N11Blue,
    onSecondaryContainer = N11Surface,
    tertiary = N11Success,
    error = N11Error,
    background = N11TextPrimary,
    surface = N11TextSecondary,
    onBackground = N11Surface,
    onSurface = N11Surface
)

@Composable
fun N11ChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
