package com.webforge.studio.model

import kotlinx.serialization.Serializable

/**
 * Theme configuration that drives both the design-system preview inside the
 * studio and the generated CSS / design tokens in the output.
 *
 * @param primaryColor    Hex string for the primary brand color (e.g. "#6750A4").
 * @param secondaryColor  Hex string for the secondary color.
 * @param backgroundColor Hex string for the page / surface background.
 * @param fontFamily      Primary font-family name (e.g. "Inter", "Roboto").
 * @param baseFontSizeSp  Base body font size in sp / rem units.
 * @param isDarkMode      Whether dark-mode tokens should be generated.
 */
@Serializable
data class ThemeConfig(
    val primaryColor: String = "#6750A4",
    val secondaryColor: String = "#625B71",
    val backgroundColor: String = "#FFFBFE",
    val fontFamily: String = "Roboto",
    val baseFontSizeSp: Float = 16f,
    val isDarkMode: Boolean = false,
)
