package com.webforge.studio.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webforge.studio.R
import com.webforge.studio.model.ProjectModel
import com.webforge.studio.ui.component.WFEmptyState
import com.webforge.studio.ui.component.WFCard
import com.webforge.studio.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNewProject: () -> Unit,
    onOpenProject: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var searchActive by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is HomeUiState.Error) {
            snackbarHostState.showSnackbar(
                (uiState as HomeUiState.Error).message.asString(context),
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!searchActive) {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    actions = {
                        IconButton(onClick = { searchActive = true }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = stringResource(R.string.home_search_hint),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            if (!searchActive) {
                LargeFloatingActionButton(onClick = onNewProject) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.home_fab_new_project),
                        modifier = Modifier.size(Dimens.Space3xl),
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (searchActive) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    onSearch = { searchActive = false },
                    active = true,
                    onActiveChange = { searchActive = it },
                    placeholder = { Text(stringResource(R.string.home_search_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // search suggestions could be shown here
                }
            }

            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is HomeUiState.Error -> {
                    WFEmptyState(
                        title = stringResource(R.string.home_empty_title),
                        message = stringResource(R.string.home_empty_hint),
                        actionLabel = stringResource(R.string.home_fab_new_project),
                        onAction = onNewProject,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is HomeUiState.Success -> {
                    val displayed = state.projects.filter { project ->
                        searchQuery.isBlank() ||
                            project.name.contains(searchQuery, ignoreCase = true) ||
                            project.description.contains(searchQuery, ignoreCase = true)
                    }

                    if (displayed.isEmpty()) {
                        WFEmptyState(
                            title = stringResource(R.string.home_empty_title),
                            message = stringResource(R.string.home_empty_hint),
                            actionLabel = stringResource(R.string.home_fab_new_project),
                            onAction = onNewProject,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                                slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)),
                        ) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 180.dp),
                                contentPadding = PaddingValues(Dimens.SpaceLg),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                items(
                                    items = displayed,
                                    key = { it.id },
                                ) { project ->
                                    ProjectGridCard(
                                        project = project,
                                        onOpen = { onOpenProject(project.id) },
                                        onDelete = { viewModel.onDeleteProject(project.id) },
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

@Composable
private fun ProjectGridCard(
    project: ProjectModel,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    // Use colorSeed to derive a tonal gradient for the thumbnail
    val seedColor = androidx.compose.ui.graphics.Color(project.colorSeed)
    val gradientColors = listOf(
        seedColor.copy(alpha = 0.8f),
        MaterialTheme.colorScheme.secondaryContainer,
    )

    WFCard(
        onClick = onOpen,
        modifier = modifier.fillMaxWidth(),
    ) {
        // Gradient thumbnail
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset.Zero,
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    ),
                ),
        ) {
            Text(
                text = project.name.take(2).uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Column(modifier = Modifier.padding(Dimens.SpaceMd)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(Dimens.Space3xl),
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.home_project_menu),
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.home_menu_open)) },
                            onClick = { menuExpanded = false; onOpen() },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.home_menu_delete)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            },
                            onClick = { menuExpanded = false; onDelete() },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceXs))

            AssistChip(
                onClick = {},
                label = {
                    Text(
                        text = project.targetPlatform.name,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceXs))

            Text(
                text = dateFormat.format(Date(project.updatedAt.coerceAtLeast(project.createdAt))),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
