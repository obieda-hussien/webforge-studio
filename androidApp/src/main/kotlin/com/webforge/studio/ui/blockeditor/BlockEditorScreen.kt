package com.webforge.studio.ui.blockeditor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webforge.studio.model.BlockCategory
import com.webforge.studio.model.BlockColorToken
import com.webforge.studio.model.BlockDescriptors
import com.webforge.studio.model.BlockEventType
import com.webforge.studio.model.BlockParameterDefinition
import com.webforge.studio.model.BlockParameterType
import com.webforge.studio.model.BlockType
import com.webforge.studio.model.displayName
import com.webforge.studio.ui.component.WFEmptyState
import com.webforge.studio.ui.component.WFTextField
import com.webforge.studio.ui.theme.WFMotion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockEditorScreen(
    onBack: () -> Unit,
    viewModel: BlockEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        BlockEditorUiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading block editor...")
        }

        is BlockEditorUiState.Ready -> {
            var eventPickerExpanded by remember { mutableStateOf(false) }
            var variableDialogVisible by remember { mutableStateOf(false) }
            var varName by remember { mutableStateOf("") }
            var varType by remember { mutableStateOf("any") }
            var varDefault by remember { mutableStateOf("") }
            val dragAndDropState = remember { DragAndDropState() }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text("Interactions · ${state.elementId ?: "Page"}")
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            IconButton(onClick = viewModel::onUndo, enabled = state.canUndo) {
                                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                            }
                            IconButton(onClick = viewModel::onRedo, enabled = state.canRedo) {
                                Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                            }
                            IconButton(onClick = viewModel::onToggleVariableManager) {
                                Icon(Icons.Default.Code, contentDescription = "Toggle Variable Manager")
                            }
                        },
                    )
                },
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    ScrollableTabRow(selectedTabIndex = state.chains.indexOfFirst { it.id == state.activeChainId }.coerceAtLeast(0)) {
                        state.chains.forEach { chain ->
                            Tab(
                                selected = state.activeChainId == chain.id,
                                onClick = { viewModel.onSelectChain(chain.id) },
                                text = { Text(chain.eventType.displayName) },
                            )
                        }
                        Tab(
                            selected = false,
                            onClick = { eventPickerExpanded = true },
                            icon = { Icon(Icons.Default.Add, contentDescription = "Add Event") },
                        )
                    }

                    DropdownMenu(
                        expanded = eventPickerExpanded,
                        onDismissRequest = { eventPickerExpanded = false },
                    ) {
                        BlockEventType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    eventPickerExpanded = false
                                    viewModel.onAddChain(type)
                                },
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxSize()) {
                        BlockPalettePanel(
                            query = state.searchQuery,
                            onQueryChange = viewModel::onSearchQueryChange,
                            onAddBlock = viewModel::onAddBlock,
                            modifier = Modifier
                                .width(320.dp)
                                .fillMaxHeight(),
                        )

                        HorizontalDivider(modifier = Modifier.width(1.dp).fillMaxHeight())

                        BlockChainCanvas(
                            state = state,
                            onDeleteChain = viewModel::onDeleteChain,
                            onMoveBlock = viewModel::onMoveBlock,
                            onReorderBlocks = viewModel::onReorderBlocks,
                            onDeleteBlock = viewModel::onDeleteBlock,
                            onDuplicateBlock = viewModel::onDuplicateBlock,
                            onToggleDisabled = viewModel::onToggleBlockDisabled,
                            onToggleCollapsed = viewModel::onToggleBlockCollapsed,
                            onUpdateParameter = viewModel::onUpdateParameter,
                            dragAndDropState = dragAndDropState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )

                        AnimatedVisibility(
                            visible = state.showVariableManager,
                            enter = WFMotion.enterFade,
                            exit = WFMotion.exitFade,
                        ) {
                            VariableManagerPanel(
                                variables = viewModel.variablesInScope(),
                                onAdd = { variableDialogVisible = true },
                                onRename = viewModel::onRenameVariable,
                                onDelete = viewModel::onDeleteVariable,
                                modifier = Modifier
                                    .width(320.dp)
                                    .fillMaxHeight(),
                            )
                        }
                    }
                }
            }

            if (variableDialogVisible) {
                AlertDialog(
                    onDismissRequest = { variableDialogVisible = false },
                    title = { Text("New Variable") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            WFTextField(value = varName, onValueChange = { varName = it }, label = "Name")
                            WFTextField(value = varType, onValueChange = { varType = it }, label = "Type")
                            WFTextField(value = varDefault, onValueChange = { varDefault = it }, label = "Default Value")
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.onCreateVariable(varName, varType, varDefault)
                            varName = ""
                            varType = "any"
                            varDefault = ""
                            variableDialogVisible = false
                        }) {
                            Text("Create")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { variableDialogVisible = false }) {
                            Text("Cancel")
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun BlockPalettePanel(
    query: String,
    onQueryChange: (String) -> Unit,
    onAddBlock: (BlockType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val grouped = remember(query) {
        BlockDescriptors.byCategory().mapValues { (_, values) ->
            values.filter {
                query.isBlank() || it.second.displayName.contains(query, ignoreCase = true)
            }
        }.filterValues { it.isNotEmpty() }
    }

    val listState = rememberLazyListState()

    Column(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        WFTextField(
            value = query,
            onValueChange = onQueryChange,
            label = "Search blocks",
            placeholder = "Type a block name",
            modifier = Modifier.padding(8.dp),
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            grouped.forEach { (category, blocks) ->
                item {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
                items(blocks, key = { it.first.name }) { (type, descriptor) ->
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        onClick = { onAddBlock(type) },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(40.dp)
                                    .background(categoryColor(descriptor.colorToken), RoundedCornerShape(8.dp)),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(descriptor.displayName)
                                Text(
                                    text = descriptor.icon,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockChainCanvas(
    state: BlockEditorUiState.Ready,
    onDeleteChain: (String) -> Unit,
    onMoveBlock: (String, Int) -> Unit,
    onReorderBlocks: (List<String>) -> Unit,
    onDeleteBlock: (String) -> Unit,
    onDuplicateBlock: (String) -> Unit,
    onToggleDisabled: (String) -> Unit,
    onToggleCollapsed: (String) -> Unit,
    onUpdateParameter: (String, String, String) -> Unit,
    dragAndDropState: DragAndDropState,
    modifier: Modifier = Modifier,
) {
    val chain = state.activeChain
    if (chain == null) {
        WFEmptyState(
            title = "No events yet",
            message = "Tap + in the event tabs to create your first chain.",
            modifier = modifier,
        )
        return
    }

    val topBlocks = chain.blocks.filter { it.parentBlockId == null }.sortedBy { it.order }
    var orderedTopBlockIds by remember(chain.id) { mutableStateOf(topBlocks.map { it.id }) }
    val blockById = remember(topBlocks) { topBlocks.associateBy { it.id } }
    val visibleBlocks by remember(orderedTopBlockIds, blockById) {
        derivedStateOf { orderedTopBlockIds.mapNotNull { blockById[it] } }
    }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val itemHeightsPx = remember { mutableStateMapOf<String, Int>() }
    var persistReorderJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(topBlocks.map { it.id to it.order }) {
        if (dragAndDropState.draggingBlockId == null) {
            orderedTopBlockIds = topBlocks.map { it.id }
        }
    }

    Column(modifier = modifier.padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(chain.eventType.displayName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = { onDeleteChain(chain.id) }) {
                Text("Delete Event")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (topBlocks.isEmpty()) {
            WFEmptyState(
                title = "Drag a block here to start",
                message = "Use the palette on the left to add logic blocks.",
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Box(modifier = Modifier.fillMaxWidth()) {
                val connectorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                val connectorLines = remember(visibleBlocks, itemHeightsPx) {
                    val centers = mutableListOf<Float>()
                    var yCursor = 0f
                    visibleBlocks.forEach { block ->
                        val itemHeight = (itemHeightsPx[block.id] ?: DEFAULT_BLOCK_CARD_HEIGHT_PX).toFloat()
                        centers += yCursor + (itemHeight / 2f)
                        yCursor += itemHeight + BLOCK_CARD_SPACING_PX
                    }
                    centers.zipWithNext()
                }
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(start = 18.dp),
                ) {
                    connectorLines.forEach { (startY, endY) ->
                        drawLine(
                            color = connectorColor,
                            start = androidx.compose.ui.geometry.Offset(0f, startY),
                            end = androidx.compose.ui.geometry.Offset(0f, endY),
                            strokeWidth = 3f,
                        )
                    }
                }

                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    userScrollEnabled = dragAndDropState.draggingBlockId == null,
                ) {
                    items(visibleBlocks, key = { it.id }) { node ->
                    val descriptor = BlockDescriptors.all.getValue(node.type)
                    val isCollapsed = state.collapsedBlockIds.contains(node.id)
                    var menuExpanded by remember { mutableStateOf(false) }
                    val topIndex = visibleBlocks.indexOfFirst { it.id == node.id }
                    val cardElevation by animateDpAsState(
                        targetValue = if (dragAndDropState.draggingBlockId == node.id) 8.dp else 1.dp,
                        animationSpec = WFMotion.fastSpatialDp,
                        label = "blockElevation",
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onSizeChanged { itemHeightsPx[node.id] = it.height }
                            .pointerInput(node.id, visibleBlocks.size) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        dragAndDropState.startDragging(node.id, topIndex)
                                    },
                                    onDragEnd = {
                                        val from = orderedTopBlockIds.indexOf(node.id)
                                        val to = dragAndDropState.dropTargetIndex
                                            .takeIf { it >= 0 && it < orderedTopBlockIds.size }
                                            ?: from
                                        if (from != -1 && to != -1 && from != to) {
                                            val next = orderedTopBlockIds.toMutableList()
                                            next.removeAt(from)
                                            next.add(to, node.id)
                                            orderedTopBlockIds = next
                                            persistReorderJob?.cancel()
                                            persistReorderJob = coroutineScope.launch {
                                                delay(REORDER_ANIMATION_DEBOUNCE_MS)
                                                onReorderBlocks(next)
                                            }
                                        }
                                        dragAndDropState.clear()
                                    },
                                    onDragCancel = { dragAndDropState.clear() },
                                ) { _, dragAmount ->
                                    val itemHeight = itemHeightsPx[node.id]?.toFloat() ?: DEFAULT_BLOCK_CARD_HEIGHT_PX.toFloat()
                                    dragAndDropState.updateDrag(
                                        dragDeltaY = dragAmount.y,
                                        itemHeightPx = itemHeight,
                                        listSize = visibleBlocks.size,
                                        currentIndex = topIndex,
                                    )
                                }
                            }
                            .animateContentSize(
                                animationSpec = WFMotion.defaultSpatialSize,
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(48.dp)
                                        .background(categoryColor(descriptor.colorToken), RoundedCornerShape(10.dp)),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        descriptor.displayName,
                                        textDecoration = if (node.isDisabled) TextDecoration.LineThrough else null,
                                        color = if (node.isDisabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        descriptor.icon,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(onClick = { onMoveBlock(node.id, -1) }) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = "Move up")
                                }
                                IconButton(onClick = { onMoveBlock(node.id, 1) }) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = "Move down")
                                }
                                IconButton(onClick = { onToggleCollapsed(node.id) }) {
                                    Icon(Icons.Default.Visibility, contentDescription = "Collapse")
                                }
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                                }
                                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Duplicate") },
                                        onClick = {
                                            menuExpanded = false
                                            onDuplicateBlock(node.id)
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (node.isDisabled) "Enable" else "Disable") },
                                        onClick = {
                                            menuExpanded = false
                                            onToggleDisabled(node.id)
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete") },
                                        onClick = {
                                            menuExpanded = false
                                            onDeleteBlock(node.id)
                                        },
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = !isCollapsed,
                                enter = WFMotion.enterFade,
                                exit = WFMotion.exitFade,
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                                    descriptor.parameters.forEach { def ->
                                        ParameterEditor(
                                            definition = def,
                                            value = node.parameters[def.name]?.content ?: def.defaultValue,
                                            onValueChange = { onUpdateParameter(node.id, def.name, it) },
                                        )
                                    }

                                    if (descriptor.hasBodySlot) {
                                        Text(
                                            text = "Body",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        Text(
                                            text = "Nested blocks are supported via parent link",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun ParameterEditor(
    definition: BlockParameterDefinition,
    value: String,
    onValueChange: (String) -> Unit,
) {
    when (definition.type) {
        BlockParameterType.TEXT,
        BlockParameterType.CSS_PROPERTY_NAME,
        BlockParameterType.EVENT_TYPE,
        BlockParameterType.VARIABLE_REF,
        BlockParameterType.URL,
        BlockParameterType.ELEMENT_SELECTOR,
        -> {
            WFTextField(
                value = value,
                onValueChange = onValueChange,
                label = definition.label,
                placeholder = definition.defaultValue,
            )
        }

        BlockParameterType.MULTILINE_TEXT,
        BlockParameterType.JSON_OBJECT,
        BlockParameterType.EXPRESSION,
        -> {
            WFTextField(
                value = value,
                onValueChange = onValueChange,
                label = definition.label,
                placeholder = definition.defaultValue,
                singleLine = false,
                minLines = 2,
                maxLines = 6,
            )
        }

        BlockParameterType.NUMBER,
        BlockParameterType.CSS_UNIT_VALUE,
        -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WFTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = definition.label,
                    placeholder = definition.defaultValue,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = {
                    val parsed = value.toDoubleOrNull() ?: definition.defaultValue.toDoubleOrNull() ?: 0.0
                    onValueChange((parsed - 1).toString())
                }) {
                    Text("-")
                }
                IconButton(onClick = {
                    val parsed = value.toDoubleOrNull() ?: definition.defaultValue.toDoubleOrNull() ?: 0.0
                    onValueChange((parsed + 1).toString())
                }) {
                    Text("+")
                }
            }
        }

        BlockParameterType.BOOLEAN -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(definition.label, modifier = Modifier.weight(1f))
                Switch(
                    checked = value.equals("true", ignoreCase = true),
                    onCheckedChange = { onValueChange(it.toString()) },
                )
            }
        }

        BlockParameterType.COLOR -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(definition.label, modifier = Modifier.weight(1f))
                FilterChip(
                    selected = false,
                    onClick = {},
                    label = { Text(value.ifBlank { definition.defaultValue.ifBlank { "#6750A4" } }) },
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun VariableManagerPanel(
    variables: List<BlockVariable>,
    onAdd: () -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Variables", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            IconButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }

        if (variables.isEmpty()) {
            Text("No variables yet", style = MaterialTheme.typography.bodySmall)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(variables, key = { it.name }) { variable ->
                    var editName by remember(variable.name) { mutableStateOf(variable.name) }
                    Card {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Type: ${variable.type}")
                            Text("Default: ${variable.defaultValue}")
                            WFTextField(value = editName, onValueChange = { editName = it }, label = "Name")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { onRename(variable.name, editName) }) {
                                    Text("Rename")
                                }
                                TextButton(onClick = { onDelete(variable.name) }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val REORDER_ANIMATION_DEBOUNCE_MS = 220L
private const val DEFAULT_BLOCK_CARD_HEIGHT_PX = 120
private const val BLOCK_CARD_SPACING_PX = 10f

private fun categoryColor(token: BlockColorToken): Color = when (token) {
    BlockColorToken.RED -> Color(0xFFE53935)
    BlockColorToken.ORANGE -> Color(0xFFFB8C00)
    BlockColorToken.YELLOW -> Color(0xFFFDD835)
    BlockColorToken.GREEN -> Color(0xFF43A047)
    BlockColorToken.BLUE -> Color(0xFF1E88E5)
    BlockColorToken.PURPLE -> Color(0xFF8E24AA)
}
