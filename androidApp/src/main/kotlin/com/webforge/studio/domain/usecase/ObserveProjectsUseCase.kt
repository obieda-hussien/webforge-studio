package com.webforge.studio.domain.usecase

import com.webforge.studio.model.ProjectModel
import com.webforge.studio.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes all projects ordered by last-modified date (newest first).
 *
 * Returns a live [Flow] so callers react to database changes automatically.
 * This is a read-only, side-effect-free use case — always safe to restart.
 */
class ObserveProjectsUseCase @Inject constructor(
    private val repository: ProjectRepository,
) {
    operator fun invoke(): Flow<List<ProjectModel>> = repository.observeAllProjects()
}
