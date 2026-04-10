package com.webforge.studio.domain.usecase

import com.webforge.studio.model.ProjectModel
import com.webforge.studio.repository.ProjectRepository
import javax.inject.Inject

/**
 * Retrieves a single project by its [id].
 *
 * Returns `null` when no project with the given id exists in the database.
 */
class GetProjectByIdUseCase @Inject constructor(
    private val repository: ProjectRepository,
) {
    suspend operator fun invoke(id: String): ProjectModel? = repository.getProjectById(id)
}
