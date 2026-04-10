package com.webforge.studio.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webforge.studio.domain.usecase.DeleteProjectUseCase
import com.webforge.studio.domain.usecase.ObserveProjectsUseCase
import com.webforge.studio.model.ProjectModel
import com.webforge.studio.ui.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

// ---------------------------------------------------------------------------
// MVI State
// ---------------------------------------------------------------------------

/** Sealed MVI state for the Home screen. */
sealed interface HomeUiState {
    /** Database query is in progress. */
    data object Loading : HomeUiState

    /** Projects loaded successfully (may be an empty list). */
    data class Success(val projects: List<ProjectModel>) : HomeUiState

    /** An error occurred while loading or deleting projects. */
    data class Error(val message: UiText) : HomeUiState
}

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

/**
 * ViewModel for [HomeScreen].
 *
 * Responsibilities:
 * - Observe the project list via [ObserveProjectsUseCase] and expose it as
 *   a [HomeUiState] [StateFlow].
 * - Delegate project deletion to [DeleteProjectUseCase] with structured
 *   error handling.
 *
 * Injected by Hilt — no manual factory required.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeProjects: ObserveProjectsUseCase,
    private val deleteProject: DeleteProjectUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeProjects()
            .onEach { projects -> _uiState.value = HomeUiState.Success(projects) }
            .catch { throwable ->
                _uiState.value = HomeUiState.Error(
                    UiText.Raw(throwable.localizedMessage ?: "Unknown error loading projects"),
                )
            }
            .launchIn(viewModelScope)
    }

    /**
     * Deletes the project identified by [projectId].
     *
     * Errors are surfaced as [HomeUiState.Error]; the previous success state
     * is preserved so the list remains visible on transient failures.
     */
    fun onDeleteProject(projectId: String) {
        val errorHandler = CoroutineExceptionHandler { _, throwable ->
            _uiState.value = HomeUiState.Error(
                UiText.Raw(throwable.localizedMessage ?: "Failed to delete project"),
            )
        }
        viewModelScope.launch(errorHandler) {
            deleteProject(projectId)
        }
    }
}
