package com.webforge.studio.ui.newproject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webforge.studio.R
import com.webforge.studio.model.OutputType
import com.webforge.studio.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProjectScreen(
    onProjectCreated: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: NewProjectViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Navigate to canvas when the project is saved
    LaunchedEffect(uiState) {
        if (uiState is NewProjectUiState.Saved) {
            onProjectCreated((uiState as NewProjectUiState.Saved).projectId)
        }
    }

    // Show errors in a Snackbar then dismiss so the form re-appears
    LaunchedEffect(uiState) {
        if (uiState is NewProjectUiState.Error) {
            snackbarHostState.showSnackbar((uiState as NewProjectUiState.Error).message.asString(context))
            viewModel.onErrorDismissed()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_project_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val state = uiState) {
            is NewProjectUiState.Saving -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is NewProjectUiState.Saved -> {
                // Navigation is triggered via LaunchedEffect above; show loading while it fires.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is NewProjectUiState.Editing, is NewProjectUiState.Error -> {
                val form = (state as? NewProjectUiState.Editing)?.form
                    ?: NewProjectFormState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = Dimens.SpaceLg)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
                ) {
                    Spacer(modifier = Modifier.height(Dimens.SpaceSm))

                    OutlinedTextField(
                        value = form.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text(stringResource(R.string.new_project_name_label)) },
                        isError = form.nameError != null,
                        supportingText = form.nameError?.let { uiText -> { Text(uiText.asString()) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = form.description,
                        onValueChange = viewModel::onDescriptionChange,
                        label = { Text(stringResource(R.string.new_project_description_label)) },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        text = stringResource(R.string.new_project_output_type_label),
                        style = MaterialTheme.typography.labelLarge,
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                        OutputType.entries.forEach { type ->
                            FilterChip(
                                selected = form.outputType == type,
                                onClick = { viewModel.onOutputTypeChange(type) },
                                label = { Text(type.name) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.SpaceSm))

                    Button(
                        onClick = viewModel::onSaveProject,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.new_project_create_button))
                    }
                }
            }
        }
    }
}
