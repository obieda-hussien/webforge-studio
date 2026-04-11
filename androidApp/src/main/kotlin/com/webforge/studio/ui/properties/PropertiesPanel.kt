package com.webforge.studio.ui.properties

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.webforge.studio.R
import com.webforge.studio.model.ElementNode
import com.webforge.studio.model.ElementType
import com.webforge.studio.ui.theme.Dimens
import com.webforge.studio.ui.theme.WFElevation

/**
 * Side panel displaying collapsible property sections for the selected element.
 *
 * Sections:
 * 1. Element Info — type badge, id, label
 * 2. Layout — display, dimensions (via CSS properties)
 * 3. Typography — font size, weight, color, etc. (text elements only)
 * 4. Appearance — background, border, opacity
 * 5. Custom Attributes — free-form key-value CSS properties
 */
@Composable
fun PropertiesPanel(
    element: ElementNode,
    onLabelChange: (String) -> Unit,
    onPropertiesChange: (Map<String, String>) -> Unit,
    onEditInteractions: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = WFElevation.level2,
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
                    text = stringResource(R.string.properties_panel_title),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.canvas_delete_element),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
            AssistChip(
                onClick = onEditInteractions,
                label = { Text(stringResource(R.string.properties_interactions)) },
                modifier = Modifier.padding(top = Dimens.SpaceXs),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.SpaceSm))

            // 1. Element Info
            CollapsibleSection(title = stringResource(R.string.properties_section_info)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = element.type.name,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.SpaceSm))
                LabelEditor(
                    label = element.label,
                    onLabelChange = onLabelChange,
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceXs))
                Text(
                    text = stringResource(R.string.properties_id_label, element.id.take(8)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.SpaceXs))

            // 2. Layout
            CollapsibleSection(title = stringResource(R.string.properties_section_layout)) {
                LayoutPropertyEditor(
                    properties = element.properties,
                    onPropertiesChange = onPropertiesChange,
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.SpaceXs))

            // 3. Typography (text elements only)
            if (element.type == ElementType.TEXT || element.type == ElementType.BUTTON) {
                CollapsibleSection(title = stringResource(R.string.properties_section_typography)) {
                    TypographyPropertyEditor(
                        properties = element.properties,
                        onPropertiesChange = onPropertiesChange,
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.SpaceXs))
            }

            // 4. Appearance
            CollapsibleSection(title = stringResource(R.string.properties_section_appearance)) {
                AppearancePropertyEditor(
                    properties = element.properties,
                    onPropertiesChange = onPropertiesChange,
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.SpaceXs))

            // 5. Custom Attributes
            CollapsibleSection(
                title = stringResource(R.string.properties_section_custom),
                initiallyExpanded = false,
            ) {
                CssPropertyEditor(
                    properties = element.properties,
                    onPropertiesChange = onPropertiesChange,
                )
            }
        }
    }
}

/**
 * Animated collapsible section with a header row showing the [title] and an
 * expand/collapse icon.
 */
@Composable
private fun CollapsibleSection(
    title: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = true,
    content: @Composable () -> Unit,
) {
    var expanded by rememberSaveable(title) { mutableStateOf(initiallyExpanded) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = Dimens.SpaceXs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) {
                    stringResource(R.string.section_collapse)
                } else {
                    stringResource(R.string.section_expand)
                },
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            ),
            exit = shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.SpaceSm),
            ) {
                content()
            }
        }
    }
}

/**
 * Quick-access layout property fields (width, height, display, margin, padding).
 * Changes are written directly into the properties map.
 */
@Composable
fun LayoutPropertyEditor(
    properties: Map<String, String>,
    onPropertiesChange: (Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keys = listOf("width", "height", "display", "margin", "padding", "position")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
        keys.forEach { key ->
            InlinePropertyRow(
                label = key,
                value = properties[key] ?: "",
                onValueChange = { newVal ->
                    val updated = properties.toMutableMap()
                    if (newVal.isBlank()) updated.remove(key) else updated[key] = newVal
                    onPropertiesChange(updated)
                },
            )
        }
    }
}

/**
 * Typography-specific property fields (font-size, font-weight, color, text-align).
 */
@Composable
fun TypographyPropertyEditor(
    properties: Map<String, String>,
    onPropertiesChange: (Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keys = listOf("font-size", "font-weight", "color", "text-align", "line-height", "letter-spacing")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
        keys.forEach { key ->
            InlinePropertyRow(
                label = key,
                value = properties[key] ?: "",
                onValueChange = { newVal ->
                    val updated = properties.toMutableMap()
                    if (newVal.isBlank()) updated.remove(key) else updated[key] = newVal
                    onPropertiesChange(updated)
                },
            )
        }
    }
}

/**
 * Appearance-specific property fields (background, border, opacity, border-radius).
 */
@Composable
fun AppearancePropertyEditor(
    properties: Map<String, String>,
    onPropertiesChange: (Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keys = listOf("background", "background-color", "opacity", "border", "border-radius", "box-shadow")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
        keys.forEach { key ->
            InlinePropertyRow(
                label = key,
                value = properties[key] ?: "",
                onValueChange = { newVal ->
                    val updated = properties.toMutableMap()
                    if (newVal.isBlank()) updated.remove(key) else updated[key] = newVal
                    onPropertiesChange(updated)
                },
            )
        }
    }
}

@Composable
private fun InlinePropertyRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.45f),
        )
        androidx.compose.material3.OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .weight(0.55f)
                .height(Dimens.Space3xl),
        )
    }
}
