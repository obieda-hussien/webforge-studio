package com.webforge.studio.domain.usecase

import com.webforge.studio.model.BlockChain
import com.webforge.studio.repository.BlockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBlockChainsUseCase @Inject constructor(
    private val repository: BlockRepository,
) {
    operator fun invoke(elementId: String?): Flow<List<BlockChain>> =
        repository.observeChainsByElement(elementId)
}
