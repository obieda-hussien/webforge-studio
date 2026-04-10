package com.webforge.studio.repository

import com.webforge.studio.model.Page
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for [Page] persistence.
 *
 * All implementations must be thread-safe.
 */
interface PageRepository {

    /** Observes all pages for [projectId] ordered by [Page.order]. */
    fun observeByProject(projectId: String): Flow<List<Page>>

    /** Returns a page by its [id], or null if not found. */
    suspend fun getById(id: String): Page?

    /** Inserts or replaces a page record. */
    suspend fun upsertPage(page: Page)

    /** Deletes the page identified by [id]. */
    suspend fun deletePage(id: String)
}
