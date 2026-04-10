package com.webforge.studio.ui.newproject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webforge.studio.R
import com.webforge.studio.domain.usecase.UpsertProjectUseCase
import com.webforge.studio.model.OutputType
import com.webforge.studio.model.ProjectModel
import com.webforge.studio.model.ThemeConfig
import com.webforge.studio.ui.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import javax.inject.Inject

// ---------------------------------------------------------------------------
// MVI State
// ---------------------------------------------------------------------------

/** Form field values edited by the user on the New-Project screen. */
data class NewProjectFormState(
    val name: String = "",
    val description: String = "",
    val outputType: OutputType = OutputType.HTML,
    val nameError: UiText? = null,
)

/** Sealed MVI state for the New-Project screen. */
sealed interface NewProjectUiState {
    /** The user is filling in the form — not yet saving. */
    data class Editing(val form: NewProjectFormState = NewProjectFormState()) : NewProjectUiState

    /** A save is in progress (show loading indicator). */
    data object Saving : NewProjectUiState

    /** The project was saved successfully; navigation should proceed. */
    data class Saved(val projectId: String) : NewProjectUiState

    /** An error occurred while saving. */
    data class Error(val message: UiText) : NewProjectUiState
}

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

/**
 * ViewModel for [NewProjectScreen].
 *
 * Manages form input validation and delegates persistence to [UpsertProjectUseCase].
 * Errors are surfaced as [NewProjectUiState.Error]; the form state is preserved so
 * the user can correct and retry without re-entering all fields.
 */
@HiltViewModel
class NewProjectViewModel @Inject constructor(
    private val upsertProject: UpsertProjectUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewProjectUiState>(NewProjectUiState.Editing())
    val uiState: StateFlow<NewProjectUiState> = _uiState.asStateFlow()

    private val currentForm: NewProjectFormState
        get() = (_uiState.value as? NewProjectUiState.Editing)?.form ?: NewProjectFormState()

    fun onNameChange(name: String) {
        _uiState.value = NewProjectUiState.Editing(
            currentForm.copy(name = name, nameError = null),
        )
    }

    fun onDescriptionChange(description: String) {
        _uiState.value = NewProjectUiState.Editing(
            currentForm.copy(description = description),
        )
    }

    fun onOutputTypeChange(outputType: OutputType) {
        _uiState.value = NewProjectUiState.Editing(
            currentForm.copy(outputType = outputType),
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onSaveProject() {
        val form = currentForm
        if (form.name.isBlank()) {
            _uiState.value = NewProjectUiState.Editing(
                form.copy(nameError = UiText.StringResource(R.string.new_project_name_empty_error)),
            )
            return
        }

        val errorHandler = CoroutineExceptionHandler { _, throwable ->
            _uiState.value = NewProjectUiState.Error(
                UiText.Raw(throwable.localizedMessage ?: "Failed to save project"),
            )
        }

        _uiState.value = NewProjectUiState.Saving

        viewModelScope.launch(errorHandler) {
            val now = System.currentTimeMillis()
            val project = ProjectModel(
                id = Uuid.random().toString(),
                name = form.name.trim(),
                description = form.description.trim(),
                createdAt = now,
                updatedAt = now,
                outputType = form.outputType,
                themeConfig = ThemeConfig(),
            )
            upsertProject(project)
            _uiState.value = NewProjectUiState.Saved(project.id)
        }
    }

    /** Call this to return to the Editing state after an error. */
    fun onErrorDismissed() {
        if (_uiState.value is NewProjectUiState.Error) {
            _uiState.value = NewProjectUiState.Editing(currentForm)
        }
    }
}
