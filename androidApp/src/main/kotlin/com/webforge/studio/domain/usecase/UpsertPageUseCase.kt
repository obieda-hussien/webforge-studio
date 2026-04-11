package com.webforge.studio.domain.usecase

import com.webforge.studio.model.Page
import com.webforge.studio.repository.PageRepository
import javax.inject.Inject

class UpsertPageUseCase @Inject constructor(
    private val repository: PageRepository,
) {
    suspend operator fun invoke(page: Page) = repository.upsertPage(page)
}
