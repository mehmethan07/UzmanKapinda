package com.example.uzmankapinda.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Uygulamanın açık tema renk paleti
private val LightColors = lightColorScheme(
    primary = Color(0xFF0F766E),        // Turkuaz / Teal
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF022C22),

    secondary = Color(0xFF38BDF8),      // Açık mavi accent
    onSecondary = Color.White,

    background = Color(0xFFF3F4F6),     // Açık gri arka plan
    onBackground = Color(0xFF020617),

    surface = Color.White,
    onSurface = Color(0xFF020617),

    surfaceVariant = Color(0xFFE5E7EB),
    onSurfaceVariant = Color(0xFF4B5563),

    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun UzmanKapindaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    // Şimdilik hep light theme kullanıyoruz, istersen darkTheme'e göre değiştirebiliriz
    val colorScheme = LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        // Typography ve Shapes parametrelerini kaldırdık,
        // böylece "Shapes", "Typography" hatası tamamen ortadan kalkıyor.
        content = content
    )
}
