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
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webforge.studio.R
import com.webforge.studio.ui.component.WFEmptyState
import com.webforge.studio.ui.properties.PropertiesPanel
import com.webforge.studio.ui.theme.Dimens
import android.webkit.WebView
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasScreen(
    onBack: () -> Unit,
    onOpenInteractions: (String) -> Unit,
    onOpenThemeManager: () -> Unit,
    onOpenSeoManager: (String) -> Unit,
    viewModel: CanvasViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCodeDialog by rememberSaveable { mutableStateOf(false) }
    var codePreviewMode by rememberSaveable { mutableStateOf(false) }

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
                    val ready = uiState as? CanvasUiState.Ready
                    if (ready != null) {
                        // Undo
                        IconButton(
                            onClick = viewModel::onUndo,
                            enabled = ready.canUndo,
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Undo,
                                contentDescription = stringResource(R.string.canvas_undo),
                            )
                        }
                        // Redo
                        IconButton(
                            onClick = viewModel::onRedo,
                            enabled = ready.canRedo,
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Redo,
                                contentDescription = stringResource(R.string.canvas_redo),
                            )
                        }
                        // Toggle palette
                        IconButton(onClick = viewModel::onTogglePalette) {
                            Icon(
                                Icons.Default.Layers,
                                contentDescription = stringResource(R.string.canvas_toggle_palette),
                            )
                        }
                        // Toggle properties
                        IconButton(onClick = viewModel::onToggleProperties) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = stringResource(R.string.canvas_toggle_properties),
                            )
                        }
                        // Generate + preview code
                        IconButton(onClick = {
                            viewModel.onGenerateCode()
                            showCodeDialog = true
                        }) {
                            Icon(
                                Icons.Default.Code,
                                contentDescription = stringResource(R.string.canvas_generate_code),
                            )
                        }
                        IconButton(onClick = onOpenThemeManager) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = stringResource(R.string.canvas_theme_manager),
                            )
                        }
                        IconButton(
                            onClick = {
                                ready.currentPageId?.let(onOpenSeoManager)
                            },
                            enabled = ready.currentPageId != null,
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = stringResource(R.string.canvas_seo_manager),
                            )
                        }
                    }
                },
            )
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
                WFEmptyState(
                    title = stringResource(R.string.canvas_project_not_found),
                    message = "",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
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
                    onZoomChange = viewModel::onZoomChange,
                    onPanChange = viewModel::onPanChange,
                )

                val selectedElement = state.elements.firstOrNull { it.id == state.selectedElementId }
                val zoomPct = (state.zoomLevel * 100).toInt()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    // Page tabs + zoom indicator
                    if (state.pages.isNotEmpty()) {
                        ScrollableTabRow(
                            selectedTabIndex = state.pages.indexOfFirst {
                                it.id == state.currentPageId
                            }.coerceAtLeast(0),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            state.pages.forEachIndexed { index, page ->
                                Tab(
                                    selected = page.id == state.currentPageId,
                                    onClick = { viewModel.onSelectPage(page.id) },
                                    text = { Text(page.name) },
                                )
                            }
                            Tab(
                                selected = false,
                                onClick = viewModel::onAddPage,
                                icon = {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = stringResource(R.string.canvas_add_page),
                                    )
                                },
                            )
                        }
                    } else {
                        // Show add-page button when no pages exist
                        TextButton(
                            onClick = viewModel::onAddPage,
                            modifier = Modifier.padding(horizontal = Dimens.SpaceLg),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text(stringResource(R.string.canvas_add_page))
                        }
                    }

                    Row(modifier = Modifier.weight(1f)) {
                        // Left palette
                        if (state.showPalette) {
                            ElementPalette(
                                onAddElement = viewModel::onAddElement,
                            )
                        }

                        // Canvas viewport
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .then(gestureMod),
                        ) {
                            CanvasEngineView(
                                elements = state.elements,
                                selectedId = state.selectedElementId,
                                zoom = state.zoomLevel,
                                panOffset = state.panOffset,
                                onElementTapped = viewModel::onSelectElement,
                                onElementMoved = viewModel::onMoveElement,
                                onBackgroundTap = { viewModel.onSelectElement(null) },
                            )

                            // Zoom level badge
                            Text(
                                text = "$zoomPct%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(Dimens.SpaceSm),
                            )
                        }

                        // Right properties panel
                        if (state.showProperties && selectedElement != null) {
                            PropertiesPanel(
                                element = selectedElement,
                                onLabelChange = { viewModel.onUpdateElementLabel(selectedElement.id, it) },
                                onPropertiesChange = {
                                    viewModel.onUpdateElementProperties(selectedElement.id, it)
                                },
                                onEditInteractions = { onOpenInteractions(selectedElement.id) },
                                onDelete = { viewModel.onRemoveElement(selectedElement.id) },
                                modifier = Modifier.width(Dimens.PropertiesPanelWidth),
                            )
                        }
                    }
                }

                // Code preview dialog
                if (showCodeDialog) {
                    val code = state.generatedCode?.files?.entries?.firstOrNull()
                    val html = state.generatedCode?.files?.get("index.html")
                    var debouncedHtml by remember { mutableStateOf(html) }
                    var previewWebView by remember { mutableStateOf<WebView?>(null) }
                    LaunchedEffect(html, codePreviewMode) {
                        if (codePreviewMode) {
                            delay(500)
                            debouncedHtml = html
                        } else {
                            debouncedHtml = html
                        }
                    }
                    AlertDialog(
                        onDismissRequest = { showCodeDialog = false },
                        title = { Text(stringResource(R.string.canvas_code_dialog_title)) },
                        text = {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                                ) {
                                    TextButton(onClick = { codePreviewMode = false }) { Text("Code") }
                                    TextButton(onClick = { codePreviewMode = true }) { Text("Preview") }
                                }
                                code?.let { (fileName, content) ->
                                    if (!codePreviewMode || debouncedHtml.isNullOrBlank()) {
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
                                    } else {
                                        DisposableEffect(Unit) {
                                            onDispose {
                                                previewWebView?.apply {
                                                    stopLoading()
                                                    loadUrl("about:blank")
                                                    removeAllViews()
                                                    destroy()
                                                }
                                                previewWebView = null
                                            }
                                        }
                                        AndroidView(
                                            factory = { context ->
                                                WebView(context).apply {
                                                    // Prevent execution of generated/user-provided scripts in preview mode.
                                                    settings.javaScriptEnabled = false
                                                    settings.allowFileAccess = false
                                                    settings.allowContentAccess = false
                                                    settings.domStorageEnabled = false
                                                    previewWebView = this
                                                }
                                            },
                                            update = { webView ->
                                                webView.loadDataWithBaseURL(
                                                    null,
                                                    debouncedHtml.orEmpty(),
                                                    "text/html",
                                                    "utf-8",
                                                    null,
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = Dimens.SpaceSm),
                                        )
                                    }
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
