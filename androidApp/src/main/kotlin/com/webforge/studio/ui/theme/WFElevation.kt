package com.webforge.studio.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Tonal elevation tokens aligned with Material Design 3 Expressive specification.
 *
 * Prefer these over raw [dp] values in surface / card / dialog composables.
 */
object WFElevation {
    /** No elevation — flat surface, no tonal overlay. */
    val level0 = 0.dp

    /** Subtle depth for bottom sheets and navigation rails. */
    val level1 = 1.dp

    /** Cards, list items. */
    val level2 = 3.dp

    /** Menus, popups, elevated cards. */
    val level3 = 6.dp

    /** FABs, bottom app bars. */
    val level4 = 8.dp

    /** Modals, dialogs. */
    val level5 = 12.dp
}
