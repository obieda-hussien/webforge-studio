package com.webforge.studio.ui.properties

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.webforge.studio.R
import com.webforge.studio.model.ElementNode
import com.webforge.studio.ui.theme.Dimens

/**
 * Side panel that displays and allows editing of the selected element's
 * label and CSS-like properties.
 *
 * @param element           The currently selected [ElementNode].
 * @param onLabelChange     Called when the user changes the element's label.
 * @param onPropertiesChange Called when the user edits any CSS property value.
 * @param onDelete          Called when the user taps the delete button.
 */
@Composable
fun PropertiesPanel(
    element: ElementNode,
    onLabelChange: (String) -> Unit,
    onPropertiesChange: (Map<String, String>) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = Dimens.ElevationMd,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.SpaceMd),
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = element.type.name,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.canvas_delete_element),
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.SpaceSm))

            // Label editor
            LabelEditor(
                label = element.label,
                onLabelChange = onLabelChange,
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceMd))

            // CSS property editors
            Text(
                text = stringResource(R.string.properties_styles_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceSm))

            CssPropertyEditor(
                properties = element.properties,
                onPropertiesChange = onPropertiesChange,
            )
        }
    }
}
