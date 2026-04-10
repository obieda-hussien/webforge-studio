package com.webforge.studio.ui.newproject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webforge.studio.domain.usecase.CreateProjectUseCase
import com.webforge.studio.model.TargetPlatform
import com.webforge.studio.ui.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ---------------------------------------------------------------------------
// Form state
// ---------------------------------------------------------------------------

/** All wizard form fields consolidated in one data class. */
data class NewProjectFormState(
    val name: String = "",
    val slug: String = "",
    val description: String = "",
    val targetPlatform: TargetPlatform = TargetPlatform.HTML,
    val colorSeed: Long = 0xFF6750A4L,
    val fontPair: String = "Inter / Roboto",
    val isDarkMode: Boolean = false,
    val nameError: UiText? = null,
)

// ---------------------------------------------------------------------------
// MVI State
// ---------------------------------------------------------------------------

/** Sealed MVI state for the 3-step New-Project wizard. */
sealed interface NewProjectUiState {

    /** The user is filling in wizard step [step] (1, 2, or 3). */
    data class Editing(
        val form: NewProjectFormState = NewProjectFormState(),
        val step: Int = 1,
    ) : NewProjectUiState

    /** A create request is in flight; show a loading indicator. */
    data object Saving : NewProjectUiState

    /** The project was created; navigation should proceed to Canvas. */
    data class Saved(val projectId: String) : NewProjectUiState

    /** An error occurred while creating the project. */
    data class Error(val message: UiText) : NewProjectUiState
}

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

/**
 * ViewModel for the 3-step New Project wizard.
 *
 * Step 1 — Identity: project name (with live slug preview) and description.
 * Step 2 — Output Format: [TargetPlatform] selection.
 * Step 3 — Visual Theme: color seed, font pair, dark-mode toggle.
 *
 * Validation happens before advancing from Step 1. [CreateProjectUseCase]
 * also re-validates and persists the project when the wizard is submitted.
 */
@HiltViewModel
class NewProjectViewModel @Inject constructor(
    private val createProject: CreateProjectUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewProjectUiState>(NewProjectUiState.Editing())
    val uiState: StateFlow<NewProjectUiState> = _uiState.asStateFlow()

    private val currentForm: NewProjectFormState
        get() = (_uiState.value as? NewProjectUiState.Editing)?.form ?: NewProjectFormState()

    private val currentStep: Int
        get() = (_uiState.value as? NewProjectUiState.Editing)?.step ?: 1

    // ------------------------------------------------------------------
    // Step 1 — Identity
    // ------------------------------------------------------------------

    fun onNameChange(name: String) {
        val slug = name.trim().lowercase()
            .replace(Regex("\\s+"), "-")
            .replace(Regex("[^a-z0-9\\-]"), "")
        _uiState.value = NewProjectUiState.Editing(
            form = currentForm.copy(name = name, slug = slug, nameError = null),
            step = currentStep,
        )
    }

    fun onDescriptionChange(description: String) {
        _uiState.value = NewProjectUiState.Editing(
            form = currentForm.copy(description = description),
            step = currentStep,
        )
    }

    // ------------------------------------------------------------------
    // Step 2 — Output Format
    // ------------------------------------------------------------------

    fun onTargetPlatformChange(platform: TargetPlatform) {
        _uiState.value = NewProjectUiState.Editing(
            form = currentForm.copy(targetPlatform = platform),
            step = currentStep,
        )
    }

    // ------------------------------------------------------------------
    // Step 3 — Visual Theme
    // ------------------------------------------------------------------

    fun onColorSeedChange(seed: Long) {
        _uiState.value = NewProjectUiState.Editing(
            form = currentForm.copy(colorSeed = seed),
            step = currentStep,
        )
    }

    fun onFontPairChange(fontPair: String) {
        _uiState.value = NewProjectUiState.Editing(
            form = currentForm.copy(fontPair = fontPair),
            step = currentStep,
        )
    }

    fun onDarkModeToggle(isDark: Boolean) {
        _uiState.value = NewProjectUiState.Editing(
            form = currentForm.copy(isDarkMode = isDark),
            step = currentStep,
        )
    }

    // ------------------------------------------------------------------
    // Navigation
    // ------------------------------------------------------------------

    /** Validates the current step and advances to the next one, or submits the form. */
    fun onNext() {
        val form = currentForm
        val step = currentStep

        if (step == 1) {
            val nameError = validateName(form.name)
            if (nameError != null) {
                _uiState.value = NewProjectUiState.Editing(
                    form = form.copy(nameError = nameError),
                    step = step,
                )
                return
            }
        }

        if (step < 3) {
            _uiState.value = NewProjectUiState.Editing(form = form, step = step + 1)
        } else {
            saveProject(form)
        }
    }

    /** Goes back to the previous wizard step. */
    fun onBack() {
        val step = currentStep
        if (step > 1) {
            _uiState.value = NewProjectUiState.Editing(form = currentForm, step = step - 1)
        }
    }

    /** Returns to the Editing state after an error without losing form data. */
    fun onErrorDismissed() {
        if (_uiState.value is NewProjectUiState.Error) {
            _uiState.value = NewProjectUiState.Editing(form = currentForm, step = currentStep)
        }
    }

    // ------------------------------------------------------------------
    // Persistence
    // ------------------------------------------------------------------

    private fun saveProject(form: NewProjectFormState) {
        val errorHandler = CoroutineExceptionHandler { _, throwable ->
            _uiState.value = NewProjectUiState.Error(
                UiText.Raw(throwable.localizedMessage ?: "Failed to create project"),
            )
        }
        _uiState.value = NewProjectUiState.Saving
        viewModelScope.launch(errorHandler) {
            val project = createProject(
                name = form.name,
                description = form.description,
                targetPlatform = form.targetPlatform,
                colorSeed = form.colorSeed,
                fontPair = form.fontPair,
                isDarkMode = form.isDarkMode,
            )
            _uiState.value = NewProjectUiState.Saved(project.id)
        }
    }

    // ------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------

    private fun validateName(name: String): UiText? {
        val trimmed = name.trim()
        return when {
            trimmed.isBlank() -> UiText.Raw("Project name is required.")
            trimmed.length < 3 -> UiText.Raw("Name must be at least 3 characters.")
            trimmed.length > 50 -> UiText.Raw("Name must be at most 50 characters.")
            !trimmed.matches(Regex("[\\w\\s\\-]+")) ->
                UiText.Raw("Name may only contain letters, digits, spaces, and hyphens.")
            else -> null
        }
    }
}
