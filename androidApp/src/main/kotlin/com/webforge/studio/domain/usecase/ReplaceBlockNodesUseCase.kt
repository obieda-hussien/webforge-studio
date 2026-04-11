package com.webforge.studio.domain.usecase

import com.webforge.studio.model.BlockNode
import com.webforge.studio.repository.BlockRepository
import javax.inject.Inject

class ReplaceBlockNodesUseCase @Inject constructor(
    private val repository: BlockRepository,
) {
    suspend operator fun invoke(chainId: String, nodes: List<BlockNode>) =
        repository.replaceNodes(chainId, nodes)
}
