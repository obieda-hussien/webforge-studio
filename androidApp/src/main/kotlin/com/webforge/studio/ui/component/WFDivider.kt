package com.webforge.studio.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.webforge.studio.ui.theme.Dimens

/**
 * Horizontal divider with an optional centred label.
 *
 * When [label] is null this renders a plain [HorizontalDivider].
 * When [label] is provided the line is split with the label in the centre.
 */
@Composable
fun WFDivider(
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    if (label == null) {
        HorizontalDivider(modifier = modifier)
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Dimens.SpaceSm),
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
    }
}
