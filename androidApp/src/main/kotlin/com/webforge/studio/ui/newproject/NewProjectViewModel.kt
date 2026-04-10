package com.webforge.studio.ui.newproject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.webforge.studio.model.OutputType
import com.webforge.studio.model.ProjectModel
import com.webforge.studio.model.ThemeConfig
import com.webforge.studio.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class NewProjectUiState(
    val name: String = "",
    val description: String = "",
    val outputType: OutputType = OutputType.HTML,
    val nameError: String? = null,
    val isSaving: Boolean = false,
    val savedProjectId: String? = null,
)

class NewProjectViewModel(
    private val repository: ProjectRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewProjectUiState())
    val uiState: StateFlow<NewProjectUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onOutputTypeChange(outputType: OutputType) {
        _uiState.update { it.copy(outputType = outputType) }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveProject() {
        val current = _uiState.value
        if (current.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Project name cannot be empty") }
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val project = ProjectModel(
                id = Uuid.random().toString(),
                name = current.name.trim(),
                description = current.description.trim(),
                createdAt = now,
                updatedAt = now,
                outputType = current.outputType,
                themeConfig = ThemeConfig(),
            )
            repository.upsertProject(project)
            _uiState.update { it.copy(isSaving = false, savedProjectId = project.id) }
        }
    }
}

class NewProjectViewModelFactory(
    private val repository: ProjectRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        NewProjectViewModel(repository) as T
}
