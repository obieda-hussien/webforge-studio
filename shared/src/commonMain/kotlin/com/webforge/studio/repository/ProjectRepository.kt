package com.webforge.studio.repository

import com.webforge.studio.model.ProjectModel
import kotlinx.coroutines.flow.Flow

/**
 * Contract for all persistence operations on [ProjectModel] entities.
 *
 * Implementations may use Room (Android), SQLDelight (KMP), or any other
 * storage back-end without affecting callers in the shared or UI layers.
 */
interface ProjectRepository {

    /** Returns a live [Flow] of all projects ordered by last-updated descending. */
    fun observeAllProjects(): Flow<List<ProjectModel>>

    /**
     * Returns the project with the given [id], or `null` if it does not exist.
     */
    suspend fun getProjectById(id: String): ProjectModel?

    /**
     * Inserts a new project or replaces an existing one with the same [id].
     */
    suspend fun upsertProject(project: ProjectModel)

    /**
     * Permanently deletes the project identified by [id].
     * This is a no-op if the project does not exist.
     */
    suspend fun deleteProject(id: String)
}
