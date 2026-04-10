package com.webforge.studio.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive shape tokens for WebForge Studio.
 *
 * Uses the full M3 shape scale — extra-small through extra-large — with
 * rounded corners tuned to feel modern and tactile while staying
 * consistent with the M3 Expressive design language.
 */
val WebForgeShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
