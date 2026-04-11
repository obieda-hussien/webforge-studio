package com.webforge.studio.domain.usecase

import com.webforge.studio.repository.BlockRepository
import javax.inject.Inject

class DeleteBlockNodeUseCase @Inject constructor(
    private val repository: BlockRepository,
) {
    suspend operator fun invoke(nodeId: String) = repository.deleteNode(nodeId)
}
