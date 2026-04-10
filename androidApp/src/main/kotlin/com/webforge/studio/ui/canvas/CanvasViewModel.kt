package com.webforge.studio.ui.canvas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.webforge.studio.engine.CodeGenerator
import com.webforge.studio.engine.GeneratedCode
import com.webforge.studio.engine.HtmlCodeGenerator
import com.webforge.studio.model.ElementNode
import com.webforge.studio.model.ElementType
import com.webforge.studio.model.ProjectModel
import com.webforge.studio.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class CanvasUiState(
    val project: ProjectModel? = null,
    val elements: List<ElementNode> = emptyList(),
    val selectedElementId: String? = null,
    val isLoading: Boolean = true,
    val generatedCode: GeneratedCode? = null,
)

class CanvasViewModel(
    private val projectId: String,
    private val repository: ProjectRepository,
    private val codeGenerator: CodeGenerator = HtmlCodeGenerator(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(CanvasUiState())
    val uiState: StateFlow<CanvasUiState> = _uiState.asStateFlow()

    init {
        loadProject()
    }

    private fun loadProject() {
        viewModelScope.launch {
            val project = repository.getProjectById(projectId)
            _uiState.update { it.copy(project = project, isLoading = false) }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addElement(type: ElementType) {
        val newElement = ElementNode(
            id = Uuid.random().toString(),
            type = type,
            label = type.name.lowercase().replaceFirstChar { it.uppercase() },
        )
        _uiState.update { state ->
            state.copy(elements = state.elements + newElement)
        }
    }

    fun selectElement(elementId: String?) {
        _uiState.update { it.copy(selectedElementId = elementId) }
    }

    fun removeElement(elementId: String) {
        _uiState.update { state ->
            state.copy(
                elements = state.elements.filterNot { it.id == elementId },
                selectedElementId = if (state.selectedElementId == elementId) null
                else state.selectedElementId,
            )
        }
    }

    fun updateElementProperties(elementId: String, properties: Map<String, String>) {
        _uiState.update { state ->
            state.copy(
                elements = state.elements.map { element ->
                    if (element.id == elementId) element.copy(properties = properties)
                    else element
                },
            )
        }
    }

    fun updateElementLabel(elementId: String, label: String) {
        _uiState.update { state ->
            state.copy(
                elements = state.elements.map { element ->
                    if (element.id == elementId) element.copy(label = label)
                    else element
                },
            )
        }
    }

    fun generateCode() {
        val state = _uiState.value
        val project = state.project ?: return
        val root = ElementNode(
            id = "root",
            type = ElementType.CONTAINER,
            label = "root",
            children = state.elements,
        )
        val generated = codeGenerator.generate(project, root)
        _uiState.update { it.copy(generatedCode = generated) }
    }
}

class CanvasViewModelFactory(
    private val projectId: String,
    private val repository: ProjectRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CanvasViewModel(projectId = projectId, repository = repository) as T
}
