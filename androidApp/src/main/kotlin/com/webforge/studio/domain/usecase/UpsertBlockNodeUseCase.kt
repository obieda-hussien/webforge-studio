package com.webforge.studio.domain.usecase

import com.webforge.studio.model.BlockNode
import com.webforge.studio.repository.BlockRepository
import javax.inject.Inject

class UpsertBlockNodeUseCase @Inject constructor(
    private val repository: BlockRepository,
) {
    suspend operator fun invoke(node: BlockNode) = repository.upsertNode(node)
}
