package com.webforge.studio.ui.seo

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webforge.studio.R
import com.webforge.studio.model.RobotsDirective
import com.webforge.studio.model.StructuredDataType
import com.webforge.studio.ui.component.WFFilledButton
import com.webforge.studio.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeoManagerScreen(
    onBack: () -> Unit,
    viewModel: SeoManagerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Basic", "Social", "Structured Data", "Analysis")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.seo_manager_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.navigate_back))
                    }
                },
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.SpaceLg),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
        ) {
            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = tabIndex == index, onClick = { tabIndex = index }, text = { Text(title) })
                }
            }

            when (tabIndex) {
                0 -> {
                    val titleLen = state.seo.title.length
                    val descLen = state.seo.metaDescription.length
                    OutlinedTextField(
                        value = state.seo.title,
                        onValueChange = viewModel::onTitleChange,
                        label = { Text("${stringResource(R.string.seo_title_label)} ($titleLen)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    LinearProgressIndicator(progress = { (titleLen / 60f).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = state.seo.metaDescription,
                        onValueChange = viewModel::onDescriptionChange,
                        label = { Text("${stringResource(R.string.seo_description_label)} ($descLen)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    LinearProgressIndicator(progress = { (descLen / 160f).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = state.seo.metaKeywords.joinToString(", "),
                        onValueChange = viewModel::onKeywordsChange,
                        label = { Text(stringResource(R.string.seo_keywords_label)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.seo.canonicalUrl,
                        onValueChange = viewModel::onCanonicalChange,
                        label = { Text(stringResource(R.string.seo_canonical_label)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
                        RobotsDirective.entries.forEach { directive ->
                            FilterChip(
                                selected = state.seo.robotsDirective == directive,
                                onClick = { viewModel.onRobotsChange(directive) },
                                label = { Text(directive.value) },
                            )
                        }
                    }
                }
                1 -> {
                    OutlinedTextField(value = state.seo.ogTitle, onValueChange = viewModel::onOgTitleChange, label = { Text("OG Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = state.seo.ogDescription, onValueChange = viewModel::onOgDescriptionChange, label = { Text("OG Description") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = state.seo.ogImage, onValueChange = viewModel::onOgImageChange, label = { Text("OG Image") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = state.seo.twitterTitle, onValueChange = viewModel::onTwitterTitleChange, label = { Text("Twitter Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = state.seo.twitterDescription, onValueChange = viewModel::onTwitterDescriptionChange, label = { Text("Twitter Description") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = state.seo.twitterImage, onValueChange = viewModel::onTwitterImageChange, label = { Text("Twitter Image") }, modifier = Modifier.fillMaxWidth())
                    Text("OG Preview: ${state.seo.ogTitle.ifBlank { state.seo.title }}")
                    Text("Twitter Preview: ${state.seo.twitterTitle.ifBlank { state.seo.title }}")
                }
                2 -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
                        StructuredDataType.entries.forEach { type ->
                            FilterChip(
                                selected = state.seo.structuredDataType == type,
                                onClick = { viewModel.onStructuredTypeChange(type) },
                                label = { Text(type.name) },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = state.seo.structuredDataJson,
                        onValueChange = viewModel::onJsonLdChange,
                        label = { Text("JSON-LD") },
                        minLines = 8,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("Live JSON-LD Preview", style = MaterialTheme.typography.titleSmall)
                    Text(state.seo.structuredDataJson.ifBlank { "{ }" }, style = MaterialTheme.typography.bodySmall)
                }
                else -> {
                    Text("SEO Score: ${state.score}/100", style = MaterialTheme.typography.titleLarge)
                    LinearProgressIndicator(progress = { state.score / 100f }, modifier = Modifier.fillMaxWidth())
                    state.items.forEach { item ->
                        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
                            Text("${if (item.passed) "✅" else "⚠️"} ${item.label} (${item.score})")
                            Text(item.recommendation, style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = { viewModel.onQuickFix(item.label) }) {
                                Text(stringResource(R.string.seo_quick_fix))
                            }
                        }
                    }
                }
            }

            WFFilledButton(
                onClick = { viewModel.onSave(onBack) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.dialog_confirm)) }
        }
    }
}
