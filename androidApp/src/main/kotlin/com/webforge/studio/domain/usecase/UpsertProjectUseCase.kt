package com.webforge.studio.domain.usecase

import com.webforge.studio.model.ProjectModel
import com.webforge.studio.repository.ProjectRepository
import javax.inject.Inject

/**
 * Persists a [ProjectModel] — inserts a new record or replaces an
 * existing one with the same [ProjectModel.id].
 */
class UpsertProjectUseCase @Inject constructor(
    private val repository: ProjectRepository,
) {
    suspend operator fun invoke(project: ProjectModel) = repository.upsertProject(project)
}
