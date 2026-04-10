package com.webforge.studio.ui.canvas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webforge.studio.domain.usecase.GetProjectByIdUseCase
import com.webforge.studio.engine.CodeGenerator
import com.webforge.studio.engine.GeneratedCode
import com.webforge.studio.model.ElementNode
import com.webforge.studio.model.ElementType
import com.webforge.studio.model.ProjectModel
import com.webforge.studio.ui.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import javax.inject.Inject

// ---------------------------------------------------------------------------
// MVI State
// ---------------------------------------------------------------------------

/** Sealed MVI state for the Canvas screen. */
sealed interface CanvasUiState {
    /** Project data is being loaded from the database. */
    data object Loading : CanvasUiState

    /** Project loaded; canvas is ready for interaction. */
    data class Ready(
        val project: ProjectModel,
        val elements: List<ElementNode> = emptyList(),
        val selectedElementId: String? = null,
        val generatedCode: GeneratedCode? = null,
    ) : CanvasUiState

    /** The project with the given id was not found. */
    data object ProjectNotFound : CanvasUiState

    /** An unexpected error occurred. */
    data class Error(val message: UiText) : CanvasUiState
}

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

/**
 * ViewModel for [CanvasScreen].
 *
 * Reads the `projectId` navigation argument from [SavedStateHandle] so Hilt
 * can inject it without a custom factory. Manages the element tree and
 * delegates code generation to [CodeGenerator].
 */
@HiltViewModel
class CanvasViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProjectById: GetProjectByIdUseCase,
    private val codeGenerator: CodeGenerator,
) : ViewModel() {

    private val projectId: String = requireNotNull(savedStateHandle["projectId"]) {
        "CanvasViewModel requires 'projectId' in SavedStateHandle"
    }

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
            _uiState.value = if (project != null) {
                CanvasUiState.Ready(project = project)
            } else {
                CanvasUiState.ProjectNotFound
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onAddElement(type: ElementType) {
        updateReady { state ->
            val newElement = ElementNode(
                id = Uuid.random().toString(),
                type = type,
                label = type.name.lowercase().replaceFirstChar { it.uppercase() },
            )
            state.copy(elements = state.elements + newElement)
        }
    }

    fun onSelectElement(elementId: String?) {
        updateReady { it.copy(selectedElementId = elementId) }
    }

    fun onRemoveElement(elementId: String) {
        updateReady { state ->
            state.copy(
                elements = state.elements.filterNot { it.id == elementId },
                selectedElementId = if (state.selectedElementId == elementId) null
                else state.selectedElementId,
            )
        }
    }

    fun onUpdateElementProperties(elementId: String, properties: Map<String, String>) {
        updateReady { state ->
            state.copy(
                elements = state.elements.map { element ->
                    if (element.id == elementId) element.copy(properties = properties)
                    else element
                },
            )
        }
    }

    fun onUpdateElementLabel(elementId: String, label: String) {
        updateReady { state ->
            state.copy(
                elements = state.elements.map { element ->
                    if (element.id == elementId) element.copy(label = label)
                    else element
                },
            )
        }
    }

    fun onGenerateCode() {
        val ready = _uiState.value as? CanvasUiState.Ready ?: return
        val root = ElementNode(
            id = "root",
            type = ElementType.CONTAINER,
            label = "root",
            children = ready.elements,
        )
        val generated = codeGenerator.generate(ready.project, root)
        _uiState.update { (it as? CanvasUiState.Ready)?.copy(generatedCode = generated) ?: it }
    }

    /** Applies [transform] only when the state is [CanvasUiState.Ready]. */
    private fun updateReady(transform: (CanvasUiState.Ready) -> CanvasUiState.Ready) {
        _uiState.update { current ->
            (current as? CanvasUiState.Ready)?.let(transform) ?: current
        }
    }
}
