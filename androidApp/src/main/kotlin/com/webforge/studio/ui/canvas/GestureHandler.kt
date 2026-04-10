package com.webforge.studio.ui.canvas

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Tracks gesture state on the canvas surface (pan, tap, long-press).
 *
 * @param panOffset    Current accumulated pan translation in pixels.
 * @param tapPosition  Position of the last tap event, or null.
 */
data class CanvasGestureState(
    val panOffset: Offset = Offset.Zero,
    val tapPosition: Offset? = null,
)

/**
 * Returns a [CanvasGestureState] that is updated by pointer-input gestures,
 * and a [Modifier] that must be applied to the gesture-capturing composable.
 *
 * Usage:
 * ```kotlin
 * val (gestureState, gestureMod) = rememberCanvasGestureHandler(onTap = { … })
 * Box(modifier = gestureMod) { … }
 * ```
 *
 * @param onTap      Callback invoked on a single tap with the canvas-local position.
 * @param onLongPress Callback invoked on a long-press with the canvas-local position.
 */
@Composable
fun rememberCanvasGestureHandler(
    onTap: (Offset) -> Unit = {},
    onLongPress: (Offset) -> Unit = {},
): Pair<CanvasGestureState, Modifier> {
    var gestureState by remember { mutableStateOf(CanvasGestureState()) }

    val modifier = Modifier
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    gestureState = gestureState.copy(tapPosition = offset)
                    onTap(offset)
                },
                onLongPress = { offset -> onLongPress(offset) },
            )
        }
        .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                gestureState = gestureState.copy(
                    panOffset = gestureState.panOffset + dragAmount,
                )
            }
        }

    return gestureState to modifier
}
