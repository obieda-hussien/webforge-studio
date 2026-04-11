package com.webforge.studio.domain.usecase

import com.webforge.studio.repository.BlockRepository
import javax.inject.Inject

class DeleteBlockChainUseCase @Inject constructor(
    private val repository: BlockRepository,
) {
    suspend operator fun invoke(chainId: String) = repository.deleteChain(chainId)
}
