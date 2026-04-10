package com.webforge.studio.ui.canvas

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Combined gesture state for the canvas viewport (pan + zoom).
 *
 * @param panOffset  Accumulated pan translation in pixels.
 * @param zoom       Current zoom scale factor (1.0 = 100%).
 */
data class CanvasGestureState(
    val panOffset: Offset = Offset.Zero,
    val zoom: Float = 1f,
)

/**
 * Returns a [CanvasGestureState] updated by two-finger pan + pinch gestures,
 * and a [Modifier] to attach to the gesture-capturing composable.
 *
 * Single-finger drag is handled per-element in [DraggableElementView].
 * Two-finger pan and pinch are handled here on the canvas surface.
 *
 * @param onZoomChange  Optional callback when the zoom level changes.
 * @param onPanChange   Optional callback when the pan offset changes.
 */
@Composable
fun rememberCanvasGestureHandler(
    onZoomChange: (Float) -> Unit = {},
    onPanChange: (Offset) -> Unit = {},
): Pair<CanvasGestureState, Modifier> {
    var gestureState by remember { mutableStateOf(CanvasGestureState()) }

    val modifier = Modifier.pointerInput(Unit) {
        detectTransformGestures { _, pan, zoom, _ ->
            val newZoom = (gestureState.zoom * zoom).coerceIn(0.25f, 4f)
            val newOffset = gestureState.panOffset + pan
            gestureState = CanvasGestureState(panOffset = newOffset, zoom = newZoom)
            onZoomChange(newZoom)
            onPanChange(newOffset)
        }
    }

    return gestureState to modifier
}
