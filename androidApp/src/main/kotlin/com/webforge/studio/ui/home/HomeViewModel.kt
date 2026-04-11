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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ---------------------------------------------------------------------------
// MVI State
// ---------------------------------------------------------------------------

/** Sealed MVI state for the Home screen. */
sealed interface HomeUiState {
    /** Database query is in progress. */
    data object Loading : HomeUiState

    /**
     * Projects loaded successfully.
     *
     * [projects] is already filtered by the active [HomeViewModel.searchQuery] —
     * Composables must not apply additional filtering.
     */
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    /**
     * Raw project list (Loading / Success(all) / Error) before filtering.
     * Kept private — callers should observe [uiState] which exposes filtered results.
     */
    private val _rawState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)

    /**
     * Filtered, search-aware UI state for the Home screen.
     *
     * Filtering is applied here in the ViewModel so that no business logic
     * leaks into Composables. The combination uses [SharingStarted.WhileSubscribed]
     * to clean up upstream flows when the screen is off-screen.
     */
    val uiState: StateFlow<HomeUiState> = combine(_rawState, _searchQuery) { state, query ->
        when (state) {
            is HomeUiState.Loading, is HomeUiState.Error -> state
            is HomeUiState.Success -> {
                val filtered = if (query.isBlank()) {
                    state.projects
                } else {
                    state.projects.filter { project ->
                        project.name.contains(query, ignoreCase = true) ||
                            project.description.contains(query, ignoreCase = true)
                    }
                }
                HomeUiState.Success(filtered)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = HomeUiState.Loading,
    )

    init {
        viewModelScope.launch {
            observeProjects()
                .catch { throwable ->
                    _rawState.value = HomeUiState.Error(
                        UiText.Raw(throwable.localizedMessage ?: "Unknown error loading projects"),
                    )
                }
                .collect { projects ->
                    _rawState.value = HomeUiState.Success(projects)
                }
        }
    }

    /** Updates the active search query. Filtering is applied in [uiState]. */
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
            _rawState.value = HomeUiState.Error(
                UiText.Raw(throwable.localizedMessage ?: "Failed to delete project"),
            )
        }
        viewModelScope.launch(errorHandler) {
            deleteProject(projectId)
        }
    }
}
