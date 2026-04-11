package com.webforge.studio.ui.blockeditor

class DragAndDropState {
    var draggingBlockId: String? = null
        private set

    fun startDragging(blockId: String) {
        draggingBlockId = blockId
    }

    fun clear() {
        draggingBlockId = null
    }
}
