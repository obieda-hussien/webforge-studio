package com.webforge.studio.ui.newproject

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webforge.studio.R
import com.webforge.studio.model.TargetPlatform
import com.webforge.studio.ui.component.WFCircularLoading
import com.webforge.studio.ui.component.WFFilledButton
import com.webforge.studio.ui.component.WFTextField
import com.webforge.studio.ui.component.WFTonalButton
import com.webforge.studio.ui.theme.Dimens

private val FONT_PAIRS = listOf("Inter / Roboto", "Poppins / Noto Sans", "Playfair Display / Lato")
private val COLOR_SEEDS = listOf(
    0xFF6750A4L to "Purple",
    0xFF006E1CL to "Green",
    0xFF00629EL to "Blue",
    0xFFBA1A1AL to "Red",
    0xFF7E5700L to "Gold",
    0xFF00658AL to "Teal",
)

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

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is NewProjectUiState.Saved -> onProjectCreated(state.projectId)
            is NewProjectUiState.Error -> {
                snackbarHostState.showSnackbar(state.message.asString(context))
                viewModel.onErrorDismissed()
            }
            else -> Unit
        }
    }

    val currentStep = (uiState as? NewProjectUiState.Editing)?.step ?: 1

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_project_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 1) viewModel.onBack() else onBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
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
            // Step progress bar
            LinearProgressIndicator(
                progress = { currentStep / 3f },
                modifier = Modifier.fillMaxWidth(),
            )

            // Step indicator dots
            StepIndicator(
                currentStep = currentStep,
                totalSteps = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.SpaceLg),
            )

            when (val state = uiState) {
                is NewProjectUiState.Saving, is NewProjectUiState.Saved -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        WFCircularLoading()
                    }
                }

                is NewProjectUiState.Editing, is NewProjectUiState.Error -> {
                    val form = (state as? NewProjectUiState.Editing)?.form
                        ?: NewProjectFormState()

                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                            } else {
                                slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                            }
                        },
                        label = "wizard_step",
                    ) { step ->
                        when (step) {
                            1 -> StepIdentity(
                                form = form,
                                onNameChange = viewModel::onNameChange,
                                onDescriptionChange = viewModel::onDescriptionChange,
                                onNext = viewModel::onNext,
                            )
                            2 -> StepOutputFormat(
                                form = form,
                                onPlatformChange = viewModel::onTargetPlatformChange,
                                onNext = viewModel::onNext,
                            )
                            else -> StepVisualTheme(
                                form = form,
                                onColorSeedChange = viewModel::onColorSeedChange,
                                onFontPairChange = viewModel::onFontPairChange,
                                onDarkModeToggle = viewModel::onDarkModeToggle,
                                onCreate = viewModel::onNext,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Step indicator
// ---------------------------------------------------------------------------

@Composable
private fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { idx ->
            val isActive = idx + 1 == currentStep
            val isDone = idx + 1 < currentStep
            Box(
                modifier = Modifier
                    .padding(horizontal = Dimens.SpaceXs)
                    .size(if (isActive) Dimens.SpaceMd else Dimens.SpaceSm)
                    .clip(CircleShape)
                    .background(
                        when {
                            isActive -> MaterialTheme.colorScheme.primary
                            isDone -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.outlineVariant
                        },
                    ),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Step 1 — Identity
// ---------------------------------------------------------------------------

@Composable
private fun StepIdentity(
    form: NewProjectFormState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.SpaceLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
    ) {
        Spacer(modifier = Modifier.height(Dimens.SpaceSm))

        Text(
            text = stringResource(R.string.new_project_step1_title),
            style = MaterialTheme.typography.headlineSmall,
        )

        WFTextField(
            value = form.name,
            onValueChange = onNameChange,
            label = stringResource(R.string.new_project_name_label),
            validationError = form.nameError?.asString(),
            singleLine = true,
        )

        if (form.slug.isNotBlank()) {
            Text(
                text = stringResource(R.string.new_project_slug_preview, form.slug),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        WFTextField(
            value = form.description,
            onValueChange = onDescriptionChange,
            label = stringResource(R.string.new_project_description_label),
            singleLine = false,
            minLines = 3,
            maxLines = 5,
        )

        WFFilledButton(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.wizard_next))
        }
    }
}

// ---------------------------------------------------------------------------
// Step 2 — Output Format
// ---------------------------------------------------------------------------

private data class PlatformOption(
    val platform: TargetPlatform,
    val icon: String,
    val description: String,
)

@Composable
private fun StepOutputFormat(
    form: NewProjectFormState,
    onPlatformChange: (TargetPlatform) -> Unit,
    onNext: () -> Unit,
) {
    val options = listOf(
        PlatformOption(TargetPlatform.HTML, "🌐", "Static HTML5 + CSS + vanilla JS"),
        PlatformOption(TargetPlatform.REACT, "⚛️", "React JSX — JavaScript component tree"),
        PlatformOption(TargetPlatform.REACT_TS, "🔷", "React + TypeScript — type-safe components"),
        PlatformOption(TargetPlatform.PWA, "📱", "Progressive Web App with offline support"),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.SpaceLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
    ) {
        Spacer(modifier = Modifier.height(Dimens.SpaceSm))

        Text(
            text = stringResource(R.string.new_project_step2_title),
            style = MaterialTheme.typography.headlineSmall,
        )

        options.forEach { option ->
            val isSelected = form.targetPlatform == option.platform
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlatformChange(option.platform) }
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                width = Dimens.CanvasElementBorderSelected,
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.medium,
                            )
                        } else Modifier,
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface,
                        )
                        .padding(Dimens.SpaceLg),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
                ) {
                    Text(text = option.icon, style = MaterialTheme.typography.headlineMedium)
                    Column {
                        Text(
                            text = option.platform.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = option.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpaceSm))

        WFFilledButton(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.wizard_next))
        }
    }
}

// ---------------------------------------------------------------------------
// Step 3 — Visual Theme
// ---------------------------------------------------------------------------

@Composable
private fun StepVisualTheme(
    form: NewProjectFormState,
    onColorSeedChange: (Long) -> Unit,
    onFontPairChange: (String) -> Unit,
    onDarkModeToggle: (Boolean) -> Unit,
    onCreate: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.SpaceLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
    ) {
        Spacer(modifier = Modifier.height(Dimens.SpaceSm))

        Text(
            text = stringResource(R.string.new_project_step3_title),
            style = MaterialTheme.typography.headlineSmall,
        )

        // Color seed swatches
        Text(
            text = stringResource(R.string.new_project_color_label),
            style = MaterialTheme.typography.labelLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
            COLOR_SEEDS.forEach { (seed, name) ->
                val isSelected = form.colorSeed == seed
                Box(
                    modifier = Modifier
                        .size(Dimens.Space3xl)
                        .clip(CircleShape)
                        .background(Color(seed))
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    Dimens.CanvasElementBorderSelected,
                                    MaterialTheme.colorScheme.onSurface,
                                    CircleShape,
                                )
                            } else Modifier,
                        )
                        .clickable { onColorSeedChange(seed) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Text("✓", color = Color.White)
                    }
                }
            }
        }

        // Font pair picker
        Text(
            text = stringResource(R.string.new_project_font_label),
            style = MaterialTheme.typography.labelLarge,
        )
        FONT_PAIRS.forEach { pair ->
            val isSelected = form.fontPair == pair
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFontPairChange(pair) }
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                Dimens.CanvasElementBorderSelected,
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.shapes.medium,
                            )
                        } else Modifier,
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface,
                        )
                        .padding(Dimens.SpaceMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = pair, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "Aa",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Dark mode toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.new_project_dark_mode_label),
                style = MaterialTheme.typography.bodyLarge,
            )
            Switch(
                checked = form.isDarkMode,
                onCheckedChange = onDarkModeToggle,
            )
        }

        Spacer(modifier = Modifier.height(Dimens.SpaceSm))

        WFFilledButton(
            onClick = onCreate,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.new_project_create_button))
        }
    }
}
