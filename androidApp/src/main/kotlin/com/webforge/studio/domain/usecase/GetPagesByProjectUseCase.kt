package com.webforge.studio.domain.usecase

import com.webforge.studio.model.Page
import com.webforge.studio.repository.PageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes the ordered list of pages for a given project as a [Flow].
 */
class GetPagesByProjectUseCase @Inject constructor(
    private val pageRepository: PageRepository,
) {
    operator fun invoke(projectId: String): Flow<List<Page>> =
        pageRepository.observeByProject(projectId)
}
