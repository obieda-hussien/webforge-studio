package com.webforge.studio.domain.usecase

import com.webforge.studio.model.ProjectModel
import com.webforge.studio.repository.ProjectRepository
import javax.inject.Inject

/**
 * Updates an existing project, stamping [ProjectModel.updatedAt] to now.
 */
class UpdateProjectUseCase @Inject constructor(
    private val repository: ProjectRepository,
) {
    suspend operator fun invoke(project: ProjectModel) {
        repository.upsertProject(project.copy(updatedAt = System.currentTimeMillis()))
    }
}
