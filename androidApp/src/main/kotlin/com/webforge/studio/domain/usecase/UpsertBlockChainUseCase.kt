package com.webforge.studio.domain.usecase

import com.webforge.studio.model.BlockChain
import com.webforge.studio.repository.BlockRepository
import javax.inject.Inject

class UpsertBlockChainUseCase @Inject constructor(
    private val repository: BlockRepository,
) {
    suspend operator fun invoke(chain: BlockChain) = repository.upsertChain(chain)
}
