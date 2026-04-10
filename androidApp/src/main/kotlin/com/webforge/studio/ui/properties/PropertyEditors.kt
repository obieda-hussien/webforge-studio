package com.webforge.studio.ui.properties

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Editable text field for the element's display label.
 */
@Composable
fun LabelEditor(
    label: String,
    onLabelChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = label,
        onValueChange = onLabelChange,
        label = { Text("Label") },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
    )
}

/**
 * Dynamic key-value editor for CSS-like style properties.
 *
 * The user can add, edit, and remove individual property entries.
 * Changes are propagated via [onPropertiesChange] after each edit.
 */
@Composable
fun CssPropertyEditor(
    properties: Map<String, String>,
    onPropertiesChange: (Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Work with a mutable list of pairs so we can edit keys in-place
    val pairs = remember(properties) { properties.entries.map { it.key to it.value }.toMutableList() }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        pairs.forEachIndexed { index, (key, value) ->
            var currentKey by remember(key) { mutableStateOf(key) }
            var currentValue by remember(value) { mutableStateOf(value) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                OutlinedTextField(
                    value = currentKey,
                    onValueChange = { newKey ->
                        currentKey = newKey
                        pairs[index] = newKey to currentValue
                        onPropertiesChange(pairs.toMap())
                    },
                    label = { Text("Property") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodySmall,
                )
                OutlinedTextField(
                    value = currentValue,
                    onValueChange = { newValue ->
                        currentValue = newValue
                        pairs[index] = currentKey to newValue
                        onPropertiesChange(pairs.toMap())
                    },
                    label = { Text("Value") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodySmall,
                )
                IconButton(
                    onClick = {
                        pairs.removeAt(index)
                        onPropertiesChange(pairs.toMap())
                    },
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove property",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        TextButton(
            onClick = {
                pairs.add("" to "")
                onPropertiesChange(pairs.toMap())
            },
            modifier = Modifier.padding(top = 4.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text(text = "Add property", modifier = Modifier.padding(start = 4.dp))
        }
    }
}

private fun List<Pair<String, String>>.toMap(): Map<String, String> =
    associate { (k, v) -> k to v }
