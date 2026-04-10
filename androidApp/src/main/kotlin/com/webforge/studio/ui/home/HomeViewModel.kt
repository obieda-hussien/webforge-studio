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
 * ViewModel for the Home screen.
 *
 * Responsibilities:
 * - Observe the project list via [ObserveProjectsUseCase] and expose it as
 *   [HomeUiState] [StateFlow].
 * - Maintain a [searchQuery] and apply client-side filtering so search is
 *   instantaneous without additional DB hits.
 * - Delegate project deletion to [DeleteProjectUseCase] with structured
 *   error handling via [CoroutineExceptionHandler].
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeProjects: ObserveProjectsUseCase,
    private val deleteProject: DeleteProjectUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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

    /** Updates the active search query. Filtering is done in the UI layer. */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /**
     * Deletes the project identified by [projectId].
     *
     * Errors surface as [HomeUiState.Error]; the existing list remains visible
     * on transient failures so the user can retry.
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
