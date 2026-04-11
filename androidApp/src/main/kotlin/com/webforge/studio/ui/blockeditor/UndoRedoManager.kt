package com.webforge.studio.ui.blockeditor

class UndoRedoManager<T>(
    private val maxSize: Int = 100,
) {
    private val undo = ArrayDeque<T>()
    private val redo = ArrayDeque<T>()

    val canUndo: Boolean get() = undo.isNotEmpty()
    val canRedo: Boolean get() = redo.isNotEmpty()

    fun push(state: T) {
        undo.addLast(state)
        if (undo.size > maxSize) undo.removeFirst()
        redo.clear()
    }

    fun undo(current: T): T? {
        val previous = undo.removeLastOrNull() ?: return null
        redo.addLast(current)
        return previous
    }

    fun redo(current: T): T? {
        val next = redo.removeLastOrNull() ?: return null
        undo.addLast(current)
        return next
    }
}
