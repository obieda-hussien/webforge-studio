package com.webforge.studio.ui.canvas

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webforge.studio.domain.usecase.AddPageToProjectUseCase
import com.webforge.studio.domain.usecase.GetPagesByProjectUseCase
import com.webforge.studio.domain.usecase.GetProjectByIdUseCase
import com.webforge.studio.engine.CodeGenerator
import com.webforge.studio.engine.GeneratedCode
import com.webforge.studio.engine.BlockJavaScriptGenerator
import com.webforge.studio.model.ElementNode
import com.webforge.studio.model.ElementType
import com.webforge.studio.model.Page
import com.webforge.studio.model.ProjectModel
import com.webforge.studio.repository.BlockRepository
import com.webforge.studio.ui.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import javax.inject.Inject

// ---------------------------------------------------------------------------
// Command pattern — undo/redo
// ---------------------------------------------------------------------------

/** Reversible canvas command for the undo/redo history stack. */
sealed interface CanvasCommand {
    data class AddElement(val element: ElementNode) : CanvasCommand
    data class RemoveElement(val element: ElementNode) : CanvasCommand
    data class MoveElement(
        val id: String,
        val oldX: Float,
        val oldY: Float,
        val newX: Float,
        val newY: Float,
    ) : CanvasCommand
    data class UpdateProperties(
        val id: String,
        val old: Map<String, String>,
        val new: Map<String, String>,
    ) : CanvasCommand
}

private const val MAX_HISTORY = 50

// ---------------------------------------------------------------------------
// MVI State
// ---------------------------------------------------------------------------

/** Sealed MVI state for the Canvas screen. */
sealed interface CanvasUiState {
    data object Loading : CanvasUiState

    data class Ready(
        val project: ProjectModel,
        val elements: List<ElementNode> = emptyList(),
        val selectedElementId: String? = null,
        val generatedCode: GeneratedCode? = null,
        val zoomLevel: Float = 1f,
        val panOffset: Offset = Offset.Zero,
        val pages: List<Page> = emptyList(),
        val currentPageId: String? = null,
        /** True when at least one undo action is available. */
        val canUndo: Boolean = false,
        /** True when at least one redo action is available. */
        val canRedo: Boolean = false,
        val showPalette: Boolean = true,
        val showProperties: Boolean = true,
    ) : CanvasUiState

    data object ProjectNotFound : CanvasUiState

    data class Error(val message: UiText) : CanvasUiState
}

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

/**
 * ViewModel for the Canvas screen.
 *
 * Reads the `projectId` navigation argument from [SavedStateHandle] so Hilt
 * can inject it without a custom factory.
 *
 * Manages:
 * - Element tree (add, move, resize, remove, update)
 * - Undo/redo via command pattern (max [MAX_HISTORY] entries); history is kept
 *   private — the UI state only exposes [CanvasUiState.Ready.canUndo] and
 *   [CanvasUiState.Ready.canRedo] booleans to avoid unnecessary recompositions
 *   on every history change.
 * - Zoom and pan state
 * - Page management (observe + add)
 * - Code generation delegation
 */
@HiltViewModel
class CanvasViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProjectById: GetProjectByIdUseCase,
    private val getPagesByProject: GetPagesByProjectUseCase,
    private val addPageToProject: AddPageToProjectUseCase,
    private val codeGenerator: CodeGenerator,
    private val blockRepository: BlockRepository,
) : ViewModel() {

    private val projectId: String = requireNotNull(savedStateHandle["projectId"]) {
        "CanvasViewModel requires 'projectId' in SavedStateHandle"
    }

    // Undo/redo history kept private — not part of UiState to avoid large list
    // equality checks on every recomposition.
    private val undoHistory = ArrayDeque<CanvasCommand>()
    private val redoHistory = ArrayDeque<CanvasCommand>()

    private val _uiState = MutableStateFlow<CanvasUiState>(CanvasUiState.Loading)
    val uiState: StateFlow<CanvasUiState> = _uiState.asStateFlow()

    init {
        loadProject()
    }

    private fun loadProject() {
        val errorHandler = CoroutineExceptionHandler { _, throwable ->
            _uiState.value = CanvasUiState.Error(
                UiText.Raw(throwable.localizedMessage ?: "Failed to load project"),
            )
        }
        viewModelScope.launch(errorHandler) {
            val project = getProjectById(projectId)
            if (project == null) {
                _uiState.value = CanvasUiState.ProjectNotFound
                return@launch
            }

            _uiState.value = CanvasUiState.Ready(project = project)

            // Observe pages reactively
            getPagesByProject(projectId)
                .onEach { pages ->
                    updateReady { state ->
                        state.copy(
                            pages = pages,
                            currentPageId = state.currentPageId ?: pages.firstOrNull()?.id,
                        )
                    }
                }
                .catch { /* Non-fatal: canvas works without persisted pages */ }
                .launchIn(viewModelScope)
        }
    }

    // ------------------------------------------------------------------
    // Element operations
    // ------------------------------------------------------------------

    @OptIn(ExperimentalUuidApi::class)
    fun onAddElement(type: ElementType) {
        updateReady { state ->
            val existingCount = state.elements.size
            val newElement = ElementNode(
                id = Uuid.random().toString(),
                type = type,
                label = type.name.lowercase().replaceFirstChar { it.uppercase() },
                pageId = state.currentPageId ?: "",
                order = existingCount,
                x = (existingCount % 5) * 136f + 16f,
                y = (existingCount / 5) * 76f + 16f,
            )
            pushUndo(CanvasCommand.AddElement(newElement))
            state.copy(
                elements = state.elements + newElement,
                canUndo = undoHistory.isNotEmpty(),
                canRedo = false,
                selectedElementId = newElement.id,
            )
        }
    }

    fun onSelectElement(elementId: String?) {
        updateReady { it.copy(selectedElementId = elementId) }
    }

    fun onRemoveElement(elementId: String) {
        updateReady { state ->
            val removed = state.elements.firstOrNull { it.id == elementId } ?: return@updateReady state
            pushUndo(CanvasCommand.RemoveElement(removed))
            state.copy(
                elements = state.elements.filterNot { it.id == elementId },
                selectedElementId = if (state.selectedElementId == elementId) null
                else state.selectedElementId,
                canUndo = undoHistory.isNotEmpty(),
                canRedo = false,
            )
        }
    }

    fun onMoveElement(elementId: String, newX: Float, newY: Float) {
        updateReady { state ->
            val element = state.elements.firstOrNull { it.id == elementId } ?: return@updateReady state
            pushUndo(
                CanvasCommand.MoveElement(
                    id = elementId,
                    oldX = element.x,
                    oldY = element.y,
                    newX = newX,
                    newY = newY,
                ),
            )
            state.copy(
                elements = state.elements.map {
                    if (it.id == elementId) it.copy(x = newX, y = newY) else it
                },
                canUndo = undoHistory.isNotEmpty(),
                canRedo = false,
            )
        }
    }

    fun onUpdateElementProperties(elementId: String, properties: Map<String, String>) {
        updateReady { state ->
            val old = state.elements.firstOrNull { it.id == elementId }?.properties ?: return@updateReady state
            pushUndo(CanvasCommand.UpdateProperties(id = elementId, old = old, new = properties))
            state.copy(
                elements = state.elements.map { element ->
                    if (element.id == elementId) element.copy(properties = properties) else element
                },
                canUndo = undoHistory.isNotEmpty(),
                canRedo = false,
            )
        }
    }

    fun onUpdateElementLabel(elementId: String, label: String) {
        updateReady { state ->
            state.copy(
                elements = state.elements.map { element ->
                    if (element.id == elementId) element.copy(label = label) else element
                },
            )
        }
    }

    // ------------------------------------------------------------------
    // Undo / Redo
    // ------------------------------------------------------------------

    fun onUndo() {
        val cmd = undoHistory.removeLastOrNull() ?: return
        redoHistory.addLast(cmd)
        updateReady { state ->
            state.copy(
                elements = applyInverse(cmd, state.elements),
                canUndo = undoHistory.isNotEmpty(),
                canRedo = redoHistory.isNotEmpty(),
            )
        }
    }

    fun onRedo() {
        val cmd = redoHistory.removeLastOrNull() ?: return
        undoHistory.addLast(cmd)
        updateReady { state ->
            state.copy(
                elements = applyCommand(cmd, state.elements),
                canUndo = undoHistory.isNotEmpty(),
                canRedo = redoHistory.isNotEmpty(),
            )
        }
    }

    /** Pushes [cmd] onto the undo stack, enforcing [MAX_HISTORY], and clears redo. */
    private fun pushUndo(cmd: CanvasCommand) {
        undoHistory.addLast(cmd)
        if (undoHistory.size > MAX_HISTORY) undoHistory.removeFirst()
        redoHistory.clear()
    }

    private fun applyCommand(cmd: CanvasCommand, elements: List<ElementNode>): List<ElementNode> =
        when (cmd) {
            is CanvasCommand.AddElement -> elements + cmd.element
            is CanvasCommand.RemoveElement -> elements.filterNot { it.id == cmd.element.id }
            is CanvasCommand.MoveElement -> elements.map {
                if (it.id == cmd.id) it.copy(x = cmd.newX, y = cmd.newY) else it
            }
            is CanvasCommand.UpdateProperties -> elements.map {
                if (it.id == cmd.id) it.copy(properties = cmd.new) else it
            }
        }

    private fun applyInverse(cmd: CanvasCommand, elements: List<ElementNode>): List<ElementNode> =
        when (cmd) {
            is CanvasCommand.AddElement -> elements.filterNot { it.id == cmd.element.id }
            is CanvasCommand.RemoveElement -> elements + cmd.element
            is CanvasCommand.MoveElement -> elements.map {
                if (it.id == cmd.id) it.copy(x = cmd.oldX, y = cmd.oldY) else it
            }
            is CanvasCommand.UpdateProperties -> elements.map {
                if (it.id == cmd.id) it.copy(properties = cmd.old) else it
            }
        }

    // ------------------------------------------------------------------
    // Zoom + Pan
    // ------------------------------------------------------------------

    fun onZoomChange(zoom: Float) {
        updateReady { it.copy(zoomLevel = zoom.coerceIn(0.25f, 4f)) }
    }

    fun onPanChange(offset: Offset) {
        updateReady { it.copy(panOffset = offset) }
    }

    // ------------------------------------------------------------------
    // Pages
    // ------------------------------------------------------------------

    fun onAddPage() {
        val state = _uiState.value as? CanvasUiState.Ready ?: return
        val errorHandler = CoroutineExceptionHandler { _, _ ->
            // Page creation failure is non-fatal; the canvas remains usable
        }
        viewModelScope.launch(errorHandler) {
            addPageToProject(
                projectId = projectId,
                name = "Page ${state.pages.size + 1}",
                order = state.pages.size,
            )
            // pages list updates via Flow observer in loadProject()
        }
    }

    fun onSelectPage(pageId: String) {
        updateReady { it.copy(currentPageId = pageId, selectedElementId = null) }
    }

    // ------------------------------------------------------------------
    // UI toggles
    // ------------------------------------------------------------------

    fun onTogglePalette() {
        updateReady { it.copy(showPalette = !it.showPalette) }
    }

    fun onToggleProperties() {
        updateReady { it.copy(showProperties = !it.showProperties) }
    }

    // ------------------------------------------------------------------
    // Code generation
    // ------------------------------------------------------------------

    fun onGenerateCode() {
        val ready = _uiState.value as? CanvasUiState.Ready ?: return
        viewModelScope.launch {
            val root = ElementNode(
                id = "root",
                type = ElementType.CONTAINER,
                label = "root",
                children = ready.elements,
            )
            val generated = codeGenerator.generate(ready.project, root)
            val jsGenerator = BlockJavaScriptGenerator()
            val chainsByElement = buildMap<String?, List<com.webforge.studio.model.BlockChain>> {
                put(null, blockRepository.getChainsByElement(null))
                ready.elements.forEach { element ->
                    put(element.id, blockRepository.getChainsByElement(element.id))
                }
            }.filterValues { it.isNotEmpty() }
            val appJs = jsGenerator.generateAppJs(chainsByElement)
            val merged = generated.copy(files = generated.files + mapOf("app.js" to appJs))
            _uiState.update { (it as? CanvasUiState.Ready)?.copy(generatedCode = merged) ?: it }
        }
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    private fun updateReady(transform: (CanvasUiState.Ready) -> CanvasUiState.Ready) {
        _uiState.update { current ->
            (current as? CanvasUiState.Ready)?.let(transform) ?: current
        }
    }
}
