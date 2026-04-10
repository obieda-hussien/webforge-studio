package com.webforge.studio.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.webforge.studio.model.ElementNode
import com.webforge.studio.model.ElementType

/**
 * Visual canvas engine responsible for rendering the element tree and the
 * grid / guide overlay.
 */
object CanvasEngine {

    /** Size of a single grid cell in density-independent pixels. */
    val GRID_CELL_SIZE_DP: Dp = 24.dp

    /**
     * Draws the dot-grid background used as a visual guide on the canvas surface.
     */
    fun DrawScope.drawGrid(
        gridColor: Color = Color(0xFFCAC4D0),
        dotRadius: Float = 1.5f,
    ) {
        val cellPx = GRID_CELL_SIZE_DP.toPx()
        var x = 0f
        while (x <= size.width) {
            var y = 0f
            while (y <= size.height) {
                drawCircle(color = gridColor, radius = dotRadius, center = Offset(x, y))
                y += cellPx
            }
            x += cellPx
        }
    }
}

/**
 * Renders the canvas area: dot-grid background followed by the element tree.
 *
 * @param elements         List of top-level elements to render.
 * @param selectedId       ID of the currently selected element (shows selection border).
 * @param panOffset        Current pan translation applied to the content layer.
 * @param onElementTapped  Callback when the user taps an element.
 */
@Composable
fun CanvasEngineView(
    elements: List<ElementNode>,
    selectedId: String?,
    panOffset: Offset,
    onElementTapped: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Box(modifier = modifier.fillMaxSize()) {
        // Grid layer
        Canvas(modifier = Modifier.fillMaxSize()) {
            with(CanvasEngine) { drawGrid(gridColor = gridColor) }
        }

        // Elements layer (pan translation applied via offset)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = panOffset.x.toInt(),
                        y = panOffset.y.toInt(),
                    )
                },
        ) {
            elements.forEachIndexed { index, element ->
                ElementNodeView(
                    node = element,
                    isSelected = element.id == selectedId,
                    offset = Offset(x = 16f + index * 8f, y = 16f + index * 8f),
                    onTap = { onElementTapped(element.id) },
                )
            }
        }
    }
}

/**
 * Visual representation of a single [ElementNode] on the canvas.
 */
@Composable
private fun ElementNodeView(
    node: ElementNode,
    isSelected: Boolean,
    offset: Offset,
    onTap: () -> Unit,
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
            .size(width = 120.dp, height = 60.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(width = if (isSelected) 2.dp else 1.dp, color = borderColor)
            .clickable(onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = elementIcon(node.type),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = node.label.ifBlank { node.type.name },
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

private fun elementIcon(type: ElementType): String = when (type) {
    ElementType.CONTAINER -> "▣"
    ElementType.TEXT -> "T"
    ElementType.IMAGE -> "🖼"
    ElementType.BUTTON -> "⬛"
    ElementType.INPUT -> "▭"
    ElementType.LINK -> "🔗"
    ElementType.DIVIDER -> "─"
    ElementType.CUSTOM -> "✦"
}
