package com.webforge.studio.domain.usecase

import com.webforge.studio.model.Page
import com.webforge.studio.repository.PageRepository
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Adds a new page to the given project, auto-generating an id and a route.
 */
class AddPageToProjectUseCase @Inject constructor(
    private val pageRepository: PageRepository,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        projectId: String,
        name: String,
        route: String = "/${name.lowercase().replace(" ", "-")}",
        isHome: Boolean = false,
        order: Int = 0,
    ): Page {
        val page = Page(
            id = Uuid.random().toString(),
            projectId = projectId,
            name = name.trim(),
            route = route,
            isHome = isHome,
            order = order,
        )
        pageRepository.upsertPage(page)
        return page
    }
}
