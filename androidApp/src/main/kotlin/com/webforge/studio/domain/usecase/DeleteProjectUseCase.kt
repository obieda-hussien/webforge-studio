package com.webforge.studio.domain.usecase

import com.webforge.studio.repository.ProjectRepository
import javax.inject.Inject

/**
 * Permanently removes the project identified by [id].
 *
 * This is a no-op when the project does not exist.
 */
class DeleteProjectUseCase @Inject constructor(
    private val repository: ProjectRepository,
) {
    suspend operator fun invoke(id: String) = repository.deleteProject(id)
}
