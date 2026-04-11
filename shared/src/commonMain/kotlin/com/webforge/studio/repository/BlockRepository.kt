package com.webforge.studio.repository

import com.webforge.studio.model.BlockChain
import com.webforge.studio.model.BlockNode
import kotlinx.coroutines.flow.Flow

interface BlockRepository {
    fun observeChainsByElement(elementId: String?): Flow<List<BlockChain>>
    suspend fun getChainsByElement(elementId: String?): List<BlockChain>
    suspend fun getChainById(chainId: String): BlockChain?
    suspend fun upsertChain(chain: BlockChain)
    suspend fun deleteChain(chainId: String)

    fun observeNodes(chainId: String): Flow<List<BlockNode>>
    suspend fun upsertNode(node: BlockNode)
    suspend fun deleteNode(nodeId: String)
    suspend fun replaceNodes(chainId: String, nodes: List<BlockNode>)
}
