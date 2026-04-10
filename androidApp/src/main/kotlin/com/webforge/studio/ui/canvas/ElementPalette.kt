package com.webforge.studio.ui.canvas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.webforge.studio.model.ElementType
import com.webforge.studio.ui.theme.Dimens
import com.webforge.studio.ui.theme.WFElevation

/** Describes a draggable palette item. */
private data class PaletteItem(
    val type: ElementType,
    val icon: String,
    val label: String,
    val description: String,
)

private data class PaletteGroup(val name: String, val items: List<PaletteItem>)

private val PALETTE_GROUPS = listOf(
    PaletteGroup(
        name = "Layout",
        items = listOf(
            PaletteItem(ElementType.CONTAINER, "▣", "Div / Container", "Block-level wrapper element"),
        ),
    ),
    PaletteGroup(
        name = "Text",
        items = listOf(
            PaletteItem(ElementType.TEXT, "T", "Paragraph", "Body text paragraph"),
        ),
    ),
    PaletteGroup(
        name = "Media",
        items = listOf(
            PaletteItem(ElementType.IMAGE, "🖼", "Image", "Raster or SVG image"),
        ),
    ),
    PaletteGroup(
        name = "Form",
        items = listOf(
            PaletteItem(ElementType.BUTTON, "⬛", "Button", "Clickable action button"),
            PaletteItem(ElementType.INPUT, "▭", "Input", "Text input field"),
        ),
    ),
    PaletteGroup(
        name = "Navigation",
        items = listOf(
            PaletteItem(ElementType.LINK, "🔗", "Link", "Anchor / hyperlink"),
        ),
    ),
    PaletteGroup(
        name = "UI",
        items = listOf(
            PaletteItem(ElementType.DIVIDER, "─", "Divider", "Horizontal rule"),
        ),
    ),
)

/**
 * Element palette panel shown on the left side of the canvas screen.
 *
 * Tapping an item calls [onAddElement] with the corresponding [ElementType].
 */
@Composable
fun ElementPalette(
    onAddElement: (ElementType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .width(Dimens.PropertiesPanelWidth)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = WFElevation.level2,
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            PALETTE_GROUPS.forEach { group ->
                item(key = "header_${group.name}") {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = group.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        },
                    )
                    HorizontalDivider()
                }
                items(
                    items = group.items,
                    key = { "${group.name}_${it.type.name}" },
                ) { item ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        leadingContent = {
                            Text(
                                text = item.icon,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        },
                        supportingContent = {
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        modifier = Modifier.clickable(
                            role = Role.Button,
                            onClickLabel = "Add ${item.label} to canvas",
                            onClick = { onAddElement(item.type) },
                        ),
                    )
                }
            }
        }
    }
}
