package com.webforge.studio.ui.themeeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webforge.studio.R
import com.webforge.studio.model.BorderRadiusPreset
import com.webforge.studio.ui.component.WFFilledButton
import com.webforge.studio.ui.theme.Dimens

private val FONT_OPTIONS = listOf("Roboto", "Inter", "Poppins", "Lato", "Montserrat", "Open Sans")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeManagerScreen(
    onBack: () -> Unit,
    viewModel: ThemeManagerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.theme_manager_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.navigate_back))
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::onRandomize) { Text(stringResource(R.string.theme_randomize)) }
                    TextButton(onClick = viewModel::onExportThemeCss) { Text(stringResource(R.string.theme_export)) }
                },
            )
        },
    ) { inner ->
        when (val state = uiState) {
            ThemeManagerUiState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(inner),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) { CircularProgressIndicator() }
            }
            is ThemeManagerUiState.Ready -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner)
                        .verticalScroll(rememberScrollState())
                        .padding(Dimens.SpaceLg),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                ) {
                    Text(stringResource(R.string.theme_color_section), style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = state.config.primaryColor,
                        onValueChange = viewModel::onSeedChange,
                        label = { Text(stringResource(R.string.theme_seed_color)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    listOf("primary", "secondary", "background").forEach { role ->
                        OutlinedTextField(
                            value = state.config.customColors[role] ?: "",
                            onValueChange = { viewModel.onRoleColorChange(role, it) },
                            label = { Text("Role override: $role") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    OutlinedTextField(
                        value = state.coolorsUrl,
                        onValueChange = viewModel::onCoolorsUrlChange,
                        label = { Text(stringResource(R.string.theme_import_coolors)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextButton(onClick = viewModel::onImportCoolors) { Text(stringResource(R.string.theme_import_action)) }

                    Text(stringResource(R.string.theme_font_section), style = MaterialTheme.typography.titleMedium)
                    Text("Google Fonts")
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
                        FONT_OPTIONS.forEach { font ->
                            FilterChip(
                                selected = state.config.fontPrimary == font,
                                onClick = { viewModel.onFontPrimaryChange(font) },
                                label = { Text(font) },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = state.config.fontSecondary,
                        onValueChange = viewModel::onFontSecondaryChange,
                        label = { Text(stringResource(R.string.theme_font_secondary)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("Heading AaBbCc • Body AaBbCc", style = MaterialTheme.typography.bodyLarge)

                    Text(stringResource(R.string.theme_spacing_shape_section), style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                    ) {
                        FilterChip(
                            selected = state.config.baseSpacing == 4,
                            onClick = { viewModel.onBaseSpacingChange(4) },
                            label = { Text("4dp") },
                        )
                        FilterChip(
                            selected = state.config.baseSpacing == 8,
                            onClick = { viewModel.onBaseSpacingChange(8) },
                            label = { Text("8dp") },
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                        BorderRadiusPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = state.config.borderRadius == preset,
                                onClick = { viewModel.onRadiusChange(preset) },
                                label = { Text(preset.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(R.string.theme_dark_mode_default))
                        Switch(
                            checked = state.config.darkModeDefault,
                            onCheckedChange = viewModel::onDarkModeDefaultChange,
                        )
                    }

                    Text(stringResource(R.string.theme_custom_css_vars), style = MaterialTheme.typography.titleMedium)
                    state.config.cssVariables.entries.forEach { (name, value) ->
                        OutlinedTextField(
                            value = "$name:$value",
                            onValueChange = { raw ->
                                val key = raw.substringBefore(":").trim()
                                val newValue = raw.substringAfter(":", "").trim()
                                viewModel.onCssVariableChange(key, newValue)
                            },
                            label = { Text("Variable") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    TextButton(onClick = { viewModel.onCssVariableChange("custom-color", "#FFFFFF") }) {
                        Text(stringResource(R.string.theme_add_css_var))
                    }

                    if (state.exportCss.isNotBlank()) {
                        Text(stringResource(R.string.theme_export_preview), style = MaterialTheme.typography.titleSmall)
                        Text(state.exportCss, style = MaterialTheme.typography.bodySmall)
                    }

                    WFFilledButton(
                        onClick = { viewModel.onSave(onBack) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.dialog_confirm)) }
                }
            }
        }
    }
}
