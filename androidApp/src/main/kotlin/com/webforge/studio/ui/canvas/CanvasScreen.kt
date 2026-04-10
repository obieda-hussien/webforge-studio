package com.webforge.studio.ui.canvas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webforge.studio.R
import com.webforge.studio.model.ElementType
import com.webforge.studio.ui.properties.PropertiesPanel
import com.webforge.studio.ui.theme.Dimens
import com.webforge.studio.ui.util.UiText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasScreen(
    onBack: () -> Unit,
    viewModel: CanvasViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddMenu by remember { mutableStateOf(false) }
    var showCodeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = (uiState as? CanvasUiState.Ready)?.project?.name
                        ?: stringResource(R.string.canvas_title)
                    Text(title)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
                actions = {
                    if (uiState is CanvasUiState.Ready) {
                        IconButton(onClick = {
                            viewModel.onGenerateCode()
                            showCodeDialog = true
                        }) {
                            Icon(
                                Icons.Default.Code,
                                contentDescription = stringResource(R.string.canvas_generate_code),
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (uiState is CanvasUiState.Ready) {
                Box {
                    FloatingActionButton(onClick = { showAddMenu = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.canvas_add_element),
                        )
                    }
                    DropdownMenu(
                        expanded = showAddMenu,
                        onDismissRequest = { showAddMenu = false },
                    ) {
                        ElementType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    viewModel.onAddElement(type)
                                    showAddMenu = false
                                },
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        when (val state = uiState) {
            is CanvasUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is CanvasUiState.ProjectNotFound -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.canvas_project_not_found))
                }
            }

            is CanvasUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(state.message.asString())
                }
            }

            is CanvasUiState.Ready -> {
                val (gestureState, gestureMod) = rememberCanvasGestureHandler(
                    onTap = { viewModel.onSelectElement(null) },
                )
                val selectedElement = state.elements.firstOrNull { it.id == state.selectedElementId }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    // Canvas area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .then(gestureMod),
                    ) {
                        CanvasEngineView(
                            elements = state.elements,
                            selectedId = state.selectedElementId,
                            panOffset = gestureState.panOffset,
                            onElementTapped = { viewModel.onSelectElement(it) },
                        )
                    }

                    // Properties panel (shown when an element is selected)
                    if (selectedElement != null) {
                        PropertiesPanel(
                            element = selectedElement,
                            onLabelChange = { viewModel.onUpdateElementLabel(selectedElement.id, it) },
                            onPropertiesChange = {
                                viewModel.onUpdateElementProperties(selectedElement.id, it)
                            },
                            onDelete = { viewModel.onRemoveElement(selectedElement.id) },
                            modifier = Modifier.width(Dimens.PropertiesPanelWidth),
                        )
                    }
                }

                // Generated-code preview dialog
                if (showCodeDialog) {
                    val code = state.generatedCode?.files?.entries?.firstOrNull()
                    AlertDialog(
                        onDismissRequest = { showCodeDialog = false },
                        title = { Text(stringResource(R.string.canvas_code_dialog_title)) },
                        text = {
                            Column {
                                code?.let { (fileName, content) ->
                                    Text(
                                        text = fileName,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(bottom = Dimens.SpaceSm),
                                    )
                                    Text(
                                        text = content,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                } ?: Text(stringResource(R.string.canvas_code_dialog_empty))
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showCodeDialog = false }) {
                                Text(stringResource(R.string.dialog_close))
                            }
                        },
                    )
                }
            }
        }
    }
}
