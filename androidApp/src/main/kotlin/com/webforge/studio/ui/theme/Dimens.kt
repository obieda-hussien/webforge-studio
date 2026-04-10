package com.webforge.studio.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Design-system spacing and sizing tokens for WebForge Studio.
 *
 * All spacing values must come from this object — no hardcoded `dp` values
 * anywhere else in the UI layer.
 */
object Dimens {

    // ---------------------------------------------------------------------------
    // Spacing
    // ---------------------------------------------------------------------------
    val SpaceXxs = 2.dp
    val SpaceXs = 4.dp
    val SpaceSm = 8.dp
    val SpaceMd = 12.dp
    val SpaceLg = 16.dp
    val SpaceXl = 24.dp
    val SpaceXxl = 32.dp
    val Space3xl = 48.dp
    val Space4xl = 64.dp

    // ---------------------------------------------------------------------------
    // Component sizes
    // ---------------------------------------------------------------------------

    /** Width of the properties side panel on the canvas screen. */
    val PropertiesPanelWidth = 240.dp

    /** Default width of a canvas element placeholder box. */
    val CanvasElementWidth = 120.dp

    /** Default height of a canvas element placeholder box. */
    val CanvasElementHeight = 60.dp

    /** Grid cell size (distance between dot-grid dots) on the canvas. */
    val CanvasGridCellSize = 24.dp

    /** Dot radius used to render a single grid dot. */
    val CanvasGridDotRadius = 1.5f

    /** Border width for a non-selected element on the canvas. */
    val CanvasElementBorderNormal = 1.dp

    /** Border width for the currently selected element on the canvas. */
    val CanvasElementBorderSelected = 2.dp

    // ---------------------------------------------------------------------------
    // Elevation / tonal elevation
    // ---------------------------------------------------------------------------
    val ElevationNone = 0.dp
    val ElevationSm = 1.dp
    val ElevationMd = 2.dp
    val ElevationLg = 4.dp
    val ElevationXl = 8.dp
}
