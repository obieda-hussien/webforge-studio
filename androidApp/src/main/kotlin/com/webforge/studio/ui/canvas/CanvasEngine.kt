package com.webforge.studio.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import com.webforge.studio.model.ElementNode
import com.webforge.studio.model.ElementType
import com.webforge.studio.ui.theme.Dimens

/**
 * Visual canvas engine responsible for rendering the element tree and the
 * grid / guide overlay.
 */
object CanvasEngine {

    /** Size of a single grid cell. */
    val GRID_CELL_SIZE_DP: Dp = Dimens.CanvasGridCellSize

    /** Dot-grid background drawn via [DrawScope]. */
    fun DrawScope.drawGrid(
        gridColor: Color,
        dotRadius: Float = Dimens.CanvasGridDotRadius,
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

    /** Snaps [value] to the nearest [gridPx]-pixel boundary. */
    fun snapToGrid(value: Float, gridPx: Float): Float =
        (value / gridPx).let { kotlin.math.round(it) * gridPx }
}

/**
 * Full canvas viewport: dot-grid background + element layer.
 *
 * Supports:
 * - Zoom via [zoom] and pan via [panOffset] applied via [graphicsLayer].
 * - Tap-to-select elements.
 * - Drag-to-move elements (snaps to 8dp grid).
 *
 * @param elements          Top-level elements to render.
 * @param selectedId        Currently selected element id (draws selection handles).
 * @param zoom              Current zoom scale factor (1f = 100%).
 * @param panOffset         Current pan translation in pixels.
 * @param onElementTapped   Called when the user taps an element.
 * @param onElementMoved    Called when an element has been dragged to a new position.
 * @param onBackgroundTap   Called when the user taps the canvas background.
 */
@Composable
fun CanvasEngineView(
    elements: List<ElementNode>,
    selectedId: String?,
    zoom: Float,
    panOffset: Offset,
    onElementTapped: (String) -> Unit,
    onElementMoved: (id: String, x: Float, y: Float) -> Unit,
    onBackgroundTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onBackgroundTap() })
            },
    ) {
        // Grid layer (not scaled — stays fixed as reference)
        Canvas(modifier = Modifier.fillMaxSize()) {
            with(CanvasEngine) { drawGrid(gridColor = gridColor) }
        }

        // Elements layer — zoom + pan applied as a graphics layer transform
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = zoom
                    scaleY = zoom
                    translationX = panOffset.x
                    translationY = panOffset.y
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
                },
        ) {
            elements.forEach { element ->
                DraggableElementView(
                    node = element,
                    isSelected = element.id == selectedId,
                    onTap = { onElementTapped(element.id) },
                    onDragEnd = { newX, newY -> onElementMoved(element.id, newX, newY) },
                )
            }
        }
    }
}

/**
 * Single element node that supports tap-to-select and drag-to-move.
 *
 * The element's canvas position ([ElementNode.x], [ElementNode.y]) is the
 * source of truth; dragging accumulates a delta and snaps to the grid on
 * drag end.
 */
@Composable
private fun DraggableElementView(
    node: ElementNode,
    isSelected: Boolean,
    onTap: () -> Unit,
    onDragEnd: (newX: Float, newY: Float) -> Unit,
) {
    var dragDelta by remember(node.id) { mutableStateOf(Offset.Zero) }

    val currentX = node.x + dragDelta.x
    val currentY = node.y + dragDelta.y

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(currentX.toInt(), currentY.toInt()) }
            .size(
                width = Dimens.CanvasElementWidth,
                height = Dimens.CanvasElementHeight,
            )
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = if (isSelected) Dimens.CanvasElementBorderSelected else Dimens.CanvasElementBorderNormal,
                color = borderColor,
            )
            .pointerInput(node.id) {
                detectTapGestures(onTap = { onTap() })
            }
            .pointerInput(node.id) {
                detectDragGestures(
                    onDrag = { change, amount ->
                        change.consume()
                        dragDelta += amount
                    },
                    onDragEnd = {
                        val gridPx = Dimens.CanvasGridCellSize.toPx()
                        val snappedX = CanvasEngine.snapToGrid(node.x + dragDelta.x, gridPx)
                        val snappedY = CanvasEngine.snapToGrid(node.y + dragDelta.y, gridPx)
                        dragDelta = Offset.Zero
                        onDragEnd(snappedX, snappedY)
                    },
                    onDragCancel = {
                        dragDelta = Offset.Zero
                    },
                )
            },
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
                modifier = Modifier.padding(top = Dimens.SpaceXxs),
            )
        }

        // Selection handles (8 corner + edge squares)
        if (isSelected) {
            SelectionHandles()
        }
    }
}

@Composable
private fun BoxScope.SelectionHandles() {
    val handleColor = MaterialTheme.colorScheme.primary
    val handleSize = Dimens.SpaceSm

    // Simplified handles at corners
    listOf(
        Alignment.TopStart, Alignment.TopCenter, Alignment.TopEnd,
        Alignment.CenterStart, Alignment.CenterEnd,
        Alignment.BottomStart, Alignment.BottomCenter, Alignment.BottomEnd,
    ).forEach { alignment ->
        Box(
            modifier = Modifier
                .align(alignment)
                .size(handleSize)
                .background(handleColor),
        )
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
