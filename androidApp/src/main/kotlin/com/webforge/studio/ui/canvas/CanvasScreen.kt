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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webforge.studio.model.ElementType
import com.webforge.studio.ui.properties.PropertiesPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasScreen(
    viewModel: CanvasViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddMenu by remember { mutableStateOf(false) }
    var showCodeDialog by remember { mutableStateOf(false) }

    val selectedElement = uiState.elements.firstOrNull { it.id == uiState.selectedElementId }
    val (gestureState, gestureMod) = rememberCanvasGestureHandler(
        onTap = { viewModel.selectElement(null) },
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.project?.name ?: "Canvas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.generateCode()
                        showCodeDialog = true
                    }) {
                        Icon(Icons.Default.Code, contentDescription = "Generate code")
                    }
                },
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { showAddMenu = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add element")
                }
                DropdownMenu(
                    expanded = showAddMenu,
                    onDismissRequest = { showAddMenu = false },
                ) {
                    ElementType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                viewModel.addElement(type)
                                showAddMenu = false
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
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
                            elements = uiState.elements,
                            selectedId = uiState.selectedElementId,
                            panOffset = gestureState.panOffset,
                            onElementTapped = { viewModel.selectElement(it) },
                        )
                    }

                    // Properties panel (shown when an element is selected)
                    if (selectedElement != null) {
                        PropertiesPanel(
                            element = selectedElement,
                            onLabelChange = { viewModel.updateElementLabel(selectedElement.id, it) },
                            onPropertiesChange = { viewModel.updateElementProperties(selectedElement.id, it) },
                            onDelete = { viewModel.removeElement(selectedElement.id) },
                            modifier = Modifier.width(240.dp),
                        )
                    }
                }
            }
        }
    }

    // Generated-code preview dialog
    if (showCodeDialog) {
        val code = uiState.generatedCode?.files?.entries?.firstOrNull()
        AlertDialog(
            onDismissRequest = { showCodeDialog = false },
            title = { Text("Generated Code") },
            text = {
                Column {
                    code?.let { (fileName, content) ->
                        Text(
                            text = fileName,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } ?: Text("No code generated yet.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showCodeDialog = false }) {
                    Text("Close")
                }
            },
        )
    }
}
