package com.webforge.studio.ui.blockeditor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt

class DragAndDropState {
    var draggingBlockId: String? by mutableStateOf(null)
        private set
    var dropTargetIndex: Int by mutableIntStateOf(-1)
        private set
    var dragOffsetY: Float by mutableFloatStateOf(0f)
        private set

    fun startDragging(blockId: String, initialIndex: Int) {
        draggingBlockId = blockId
        dropTargetIndex = initialIndex
        dragOffsetY = 0f
    }

    fun updateDrag(
        dragDeltaY: Float,
        itemHeightPx: Float,
        listSize: Int,
        currentIndex: Int,
    ) {
        if (draggingBlockId == null || listSize <= 0 || itemHeightPx <= 0f) return
        dragOffsetY += dragDeltaY
        val shift = (dragOffsetY / itemHeightPx).roundToInt()
        dropTargetIndex = (currentIndex + shift).coerceIn(0, listSize - 1)
    }

    fun clear() {
        draggingBlockId = null
        dropTargetIndex = -1
        dragOffsetY = 0f
    }
}
