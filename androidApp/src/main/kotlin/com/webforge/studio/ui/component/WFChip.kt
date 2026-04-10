package com.webforge.studio.ui.component

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * M3 filter chip — use for toggleable category filters or option selectors.
 *
 * @param label    Text label displayed inside the chip.
 * @param selected Whether the chip is currently selected.
 * @param onClick  Selection toggle callback.
 */
@Composable
fun WFFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
    )
}
