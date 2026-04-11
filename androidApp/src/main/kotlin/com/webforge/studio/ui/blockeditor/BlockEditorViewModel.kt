package com.webforge.studio.ui.blockeditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webforge.studio.domain.usecase.DeleteBlockChainUseCase
import com.webforge.studio.domain.usecase.ObserveBlockChainsUseCase
import com.webforge.studio.domain.usecase.ReplaceBlockNodesUseCase
import com.webforge.studio.domain.usecase.UpsertBlockChainUseCase
import com.webforge.studio.model.BlockChain
import com.webforge.studio.model.BlockDescriptors
import com.webforge.studio.model.BlockEventType
import com.webforge.studio.model.BlockNode
import com.webforge.studio.model.BlockType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface BlockEditorUiState {
    data object Loading : BlockEditorUiState

    data class Ready(
        val elementId: String?,
        val chains: List<BlockChain> = emptyList(),
        val activeChainId: String? = null,
        val searchQuery: String = "",
        val showVariableManager: Boolean = false,
        val collapsedBlockIds: Set<String> = emptySet(),
        val canUndo: Boolean = false,
        val canRedo: Boolean = false,
    ) : BlockEditorUiState {
        val activeChain: BlockChain?
            get() = chains.firstOrNull { it.id == activeChainId }
    }
}

data class BlockVariable(
    val name: String,
    val type: String,
    val defaultValue: String,
)

@HiltViewModel
class BlockEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeBlockChains: ObserveBlockChainsUseCase,
    private val upsertBlockChain: UpsertBlockChainUseCase,
    private val deleteBlockChain: DeleteBlockChainUseCase,
    private val replaceBlockNodes: ReplaceBlockNodesUseCase,
) : ViewModel() {

    private val elementId: String? = savedStateHandle.get<String>("elementId")?.takeUnless { it == "null" }
    private val undoRedo = UndoRedoManager<BlockEditorUiState.Ready>()

    private val _uiState = MutableStateFlow<BlockEditorUiState>(BlockEditorUiState.Loading)
    val uiState: StateFlow<BlockEditorUiState> = _uiState.asStateFlow()

    init {
        observeChains()
    }

    private fun observeChains() {
        viewModelScope.launch {
            observeBlockChains(elementId).collect { chains ->
                _uiState.update { current ->
                    val ready = (current as? BlockEditorUiState.Ready)
                    val active = ready?.activeChainId?.takeIf { id -> chains.any { it.id == id } }
                        ?: chains.firstOrNull()?.id
                    BlockEditorUiState.Ready(
                        elementId = elementId,
                        chains = chains.sortedBy { it.eventType.name },
                        activeChainId = active,
                        searchQuery = ready?.searchQuery.orEmpty(),
                        showVariableManager = ready?.showVariableManager ?: false,
                        collapsedBlockIds = ready?.collapsedBlockIds.orEmpty(),
                        canUndo = undoRedo.canUndo,
                        canRedo = undoRedo.canRedo,
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        updateReady { it.copy(searchQuery = query) }
    }

    fun onToggleVariableManager() {
        updateReady { it.copy(showVariableManager = !it.showVariableManager) }
    }

    fun onSelectChain(chainId: String) {
        updateReady { it.copy(activeChainId = chainId) }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onAddChain(eventType: BlockEventType) {
        mutateState { state ->
            if (state.chains.any { it.eventType == eventType }) return@mutateState
            val chain = BlockChain(
                id = Uuid.random().toString(),
                elementId = state.elementId,
                eventType = eventType,
                blocks = emptyList(),
            )
            upsertBlockChain(chain)
        }
    }

    fun onDeleteChain(chainId: String) {
        mutateState {
            deleteBlockChain(chainId)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onAddBlock(type: BlockType) {
        mutateActiveChain { chain ->
            val descriptor = BlockDescriptors.all.getValue(type)
            val node = BlockNode(
                id = Uuid.random().toString(),
                chainId = chain.id,
                type = type,
                order = chain.blocks.size,
                parameters = descriptor.parameters.associate { it.name to JsonPrimitive(it.defaultValue) },
            )
            chain.copy(blocks = chain.blocks + node)
        }
    }

    fun onDeleteBlock(blockId: String) {
        mutateActiveChain { chain ->
            val updated = chain.blocks
                .filterNot { it.id == blockId || it.parentBlockId == blockId }
                .mapIndexed { index, node -> node.copy(order = index) }
            chain.copy(blocks = updated)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onDuplicateBlock(blockId: String) {
        mutateActiveChain { chain ->
            val block = chain.blocks.firstOrNull { it.id == blockId } ?: return@mutateActiveChain chain
            val copy = block.copy(
                id = Uuid.random().toString(),
                order = chain.blocks.size,
            )
            chain.copy(blocks = chain.blocks + copy)
        }
    }

    fun onMoveBlock(blockId: String, delta: Int) {
        mutateActiveChain { chain ->
            val blocks = chain.blocks.sortedBy { it.order }.toMutableList()
            val index = blocks.indexOfFirst { it.id == blockId }
            if (index == -1) return@mutateActiveChain chain
            val newIndex = (index + delta).coerceIn(0, blocks.lastIndex)
            if (index == newIndex) return@mutateActiveChain chain
            val moved = blocks.removeAt(index)
            blocks.add(newIndex, moved)
            chain.copy(blocks = blocks.mapIndexed { idx, node -> node.copy(order = idx) })
        }
    }

    fun onReorderBlocks(orderedTopLevelBlockIds: List<String>) {
        mutateActiveChain { chain ->
            if (orderedTopLevelBlockIds.isEmpty()) return@mutateActiveChain chain
            val orderLookup = orderedTopLevelBlockIds.withIndex().associate { it.value to it.index }
            val topLevel = chain.blocks.filter { it.parentBlockId == null }
            if (topLevel.none { it.id in orderLookup }) return@mutateActiveChain chain

            val reorderedTop = topLevel
                .sortedBy { orderLookup[it.id] ?: Int.MAX_VALUE }
                .mapIndexed { index, node -> node.copy(order = index) }
            val reorderedTopById = reorderedTop.associateBy { it.id }

            val merged = chain.blocks
                .map { node -> reorderedTopById[node.id] ?: node }
                .sortedWith(
                    compareBy<BlockNode> { it.parentBlockId != null }
                        .thenBy { it.order },
                )
                .mapIndexed { index, node -> node.copy(order = index) }

            chain.copy(blocks = merged)
        }
    }

    fun onUpdateParameter(blockId: String, key: String, value: String) {
        mutateActiveChain { chain ->
            chain.copy(
                blocks = chain.blocks.map { node ->
                    if (node.id == blockId) {
                        node.copy(parameters = node.parameters + (key to JsonPrimitive(value)))
                    } else {
                        node
                    }
                },
            )
        }
    }

    fun onToggleBlockCollapsed(blockId: String) {
        updateReady { state ->
            val next = state.collapsedBlockIds.toMutableSet()
            if (!next.add(blockId)) next.remove(blockId)
            state.copy(collapsedBlockIds = next)
        }
    }

    fun onToggleBlockDisabled(blockId: String) {
        mutateActiveChain { chain ->
            chain.copy(
                blocks = chain.blocks.map { node ->
                    if (node.id == blockId) node.copy(isDisabled = !node.isDisabled) else node
                },
            )
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onCreateVariable(name: String, type: String, defaultValue: String) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return
        mutateActiveChain { chain ->
            val node = BlockNode(
                id = Uuid.random().toString(),
                chainId = chain.id,
                type = BlockType.DECLARE_VARIABLE,
                order = chain.blocks.size,
                parameters = mapOf(
                    "name" to JsonPrimitive(cleanName),
                    "type" to JsonPrimitive(type),
                    "value" to JsonPrimitive(defaultValue),
                ),
            )
            chain.copy(blocks = chain.blocks + node)
        }
    }

    fun onRenameVariable(oldName: String, newName: String) {
        val clean = newName.trim()
        if (clean.isBlank()) return
        val variableKeys = BlockDescriptors.all.values
            .flatMap { descriptor ->
                descriptor.parameters
                    .filter { it.acceptsVariableReference }
                    .map { it.name }
            }
            .toSet() + "name"
        mutateActiveChain { chain ->
            chain.copy(
                blocks = chain.blocks.map { block ->
                    val updatedParams = block.parameters.mapValues { (key, value) ->
                        if (key in variableKeys && value.content == oldName) {
                            JsonPrimitive(clean)
                        } else {
                            value
                        }
                    }
                    block.copy(parameters = updatedParams)
                },
            )
        }
    }

    fun onDeleteVariable(name: String) {
        mutateActiveChain { chain ->
            chain.copy(
                blocks = chain.blocks.filterNot {
                    it.type == BlockType.DECLARE_VARIABLE && it.parameters["name"]?.content == name
                },
            )
        }
    }

    fun onUndo() {
        val ready = _uiState.value as? BlockEditorUiState.Ready ?: return
        val previous = undoRedo.undo(ready) ?: return
        viewModelScope.launch { applySnapshot(previous) }
    }

    fun onRedo() {
        val ready = _uiState.value as? BlockEditorUiState.Ready ?: return
        val next = undoRedo.redo(ready) ?: return
        viewModelScope.launch { applySnapshot(next) }
    }

    fun variablesInScope(): List<BlockVariable> {
        val chain = (_uiState.value as? BlockEditorUiState.Ready)?.activeChain ?: return emptyList()
        return chain.blocks
            .filter { it.type == BlockType.DECLARE_VARIABLE }
            .mapNotNull { block ->
                val name = block.parameters["name"]?.content ?: return@mapNotNull null
                BlockVariable(
                    name = name,
                    type = block.parameters["type"]?.content ?: "any",
                    defaultValue = block.parameters["value"]?.content.orEmpty(),
                )
            }
            .distinctBy { it.name }
    }

    private fun updateReady(transform: (BlockEditorUiState.Ready) -> BlockEditorUiState.Ready) {
        _uiState.update { state ->
            val ready = state as? BlockEditorUiState.Ready ?: return@update state
            transform(ready).copy(
                canUndo = undoRedo.canUndo,
                canRedo = undoRedo.canRedo,
            )
        }
    }

    private fun mutateState(action: suspend (BlockEditorUiState.Ready) -> Unit) {
        val current = _uiState.value as? BlockEditorUiState.Ready ?: return
        undoRedo.push(current)
        _uiState.update { (it as? BlockEditorUiState.Ready)?.copy(canUndo = undoRedo.canUndo, canRedo = undoRedo.canRedo) ?: it }
        viewModelScope.launch {
            action(current)
        }
    }

    private fun mutateActiveChain(transform: (BlockChain) -> BlockChain) {
        val current = _uiState.value as? BlockEditorUiState.Ready ?: return
        val active = current.activeChain ?: return
        undoRedo.push(current)
        viewModelScope.launch {
            val updated = transform(active)
            upsertBlockChain(updated)
            replaceBlockNodes(updated.id, updated.blocks)
            _uiState.update {
                (it as? BlockEditorUiState.Ready)?.copy(canUndo = undoRedo.canUndo, canRedo = undoRedo.canRedo) ?: it
            }
        }
    }

    private suspend fun applySnapshot(snapshot: BlockEditorUiState.Ready) {
        val current = (_uiState.value as? BlockEditorUiState.Ready) ?: return
        val currentIds = current.chains.map { it.id }.toSet()
        val snapshotIds = snapshot.chains.map { it.id }.toSet()
        (currentIds - snapshotIds).forEach { deleteBlockChain(it) }
        snapshot.chains.forEach { chain ->
            upsertBlockChain(chain)
            replaceBlockNodes(chain.id, chain.blocks)
        }
    }
}
