package com.mtaanimation.growthos.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GrowthOSDarkColorScheme = darkColorScheme(
    primary          = BrandOrange,
    onPrimary        = BrandCharcoal,
    primaryContainer = BrandOrangeDim,
    onPrimaryContainer = BrandWhite,

    secondary        = BrandGray,
    onSecondary      = BrandWhite,
    secondaryContainer = BrandGrayDim,
    onSecondaryContainer = BrandWhite,

    background       = BrandCharcoal,
    onBackground     = BrandWhite,

    surface          = BrandSurface,
    onSurface        = BrandWhite,
    surfaceVariant   = BrandSurfaceVariant,
    onSurfaceVariant = BrandMuted,

    outline          = BrandDivider,
    error            = BrandBehind,
    onError          = BrandWhite
)

@Composable
fun GrowthOSTheme(content: @Composable () -> Unit) {
    // We enforce dark mode first — light theme is not yet designed.
    MaterialTheme(
        colorScheme = GrowthOSDarkColorScheme,
        typography = GrowthOSTypography,
        content = content
    )
}
