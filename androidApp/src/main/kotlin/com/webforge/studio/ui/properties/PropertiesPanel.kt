package com.webforge.studio.ui.properties

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.webforge.studio.model.ElementNode

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
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
        ) {
            // Header
            androidx.compose.foundation.layout.Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text(
                    text = element.type.name,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete element")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Label editor
            LabelEditor(
                label = element.label,
                onLabelChange = onLabelChange,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // CSS property editors
            Text(
                text = "Styles",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            CssPropertyEditor(
                properties = element.properties,
                onPropertiesChange = onPropertiesChange,
            )
        }
    }
}
