package com.webforge.studio.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.webforge.studio.model.ProjectModel
import com.webforge.studio.repository.ProjectRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val projects: List<ProjectModel> = emptyList(),
    val isLoading: Boolean = true,
)

class HomeViewModel(
    private val repository: ProjectRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repository
        .observeAllProjects()
        .map { projects -> HomeUiState(projects = projects, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(isLoading = true),
        )

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
        }
    }
}

class HomeViewModelFactory(
    private val repository: ProjectRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        HomeViewModel(repository) as T
}
