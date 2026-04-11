package com.webforge.studio.ui.seo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webforge.studio.domain.usecase.GetPagesByProjectUseCase
import com.webforge.studio.domain.usecase.UpsertPageUseCase
import com.webforge.studio.model.Page
import com.webforge.studio.model.RobotsDirective
import com.webforge.studio.model.SEOConfig
import com.webforge.studio.model.StructuredDataType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeoScoreItem(
    val label: String,
    val score: Int,
    val recommendation: String,
    val passed: Boolean,
)

data class SeoManagerUiState(
    val page: Page? = null,
    val seo: SEOConfig = SEOConfig(),
    val score: Int = 0,
    val items: List<SeoScoreItem> = emptyList(),
)

@HiltViewModel
class SeoManagerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getPagesByProject: GetPagesByProjectUseCase,
    private val upsertPage: UpsertPageUseCase,
) : ViewModel() {
    private val projectId: String = requireNotNull(savedStateHandle["projectId"])
    private val pageId: String = requireNotNull(savedStateHandle["pageId"])
    private val _uiState = MutableStateFlow(SeoManagerUiState())
    val uiState: StateFlow<SeoManagerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getPagesByProject(projectId).collect { pages ->
                val page = pages.firstOrNull { it.id == pageId } ?: return@collect
                _uiState.value = SeoManagerUiState(page = page, seo = page.seoConfig)
                recalculate()
            }
        }
    }

    fun onTitleChange(v: String) = updateSeo { it.copy(title = v) }
    fun onDescriptionChange(v: String) = updateSeo { it.copy(metaDescription = v) }
    fun onKeywordsChange(v: String) = updateSeo { it.copy(metaKeywords = v.split(",").map { it.trim() }.filter { it.isNotBlank() }) }
    fun onCanonicalChange(v: String) = updateSeo { it.copy(canonicalUrl = v) }
    fun onRobotsChange(v: RobotsDirective) = updateSeo { it.copy(robotsDirective = v) }
    fun onOgTitleChange(v: String) = updateSeo { it.copy(ogTitle = v) }
    fun onOgDescriptionChange(v: String) = updateSeo { it.copy(ogDescription = v) }
    fun onOgImageChange(v: String) = updateSeo { it.copy(ogImage = v) }
    fun onTwitterTitleChange(v: String) = updateSeo { it.copy(twitterTitle = v) }
    fun onTwitterDescriptionChange(v: String) = updateSeo { it.copy(twitterDescription = v) }
    fun onTwitterImageChange(v: String) = updateSeo { it.copy(twitterImage = v) }
    fun onStructuredTypeChange(v: StructuredDataType) = updateSeo { it.copy(structuredDataType = v) }
    fun onJsonLdChange(v: String) = updateSeo { it.copy(structuredDataJson = v) }

    fun onQuickFix(label: String) {
        val seo = _uiState.value.seo
        when (label) {
            "Title length" -> onTitleChange(seo.title.ifBlank { "Professional Web Page" }.take(60))
            "Description length" -> onDescriptionChange(seo.metaDescription.ifBlank { "Build high quality pages with WebForge Studio." }.take(160))
            "Canonical URL set" -> onCanonicalChange(seo.canonicalUrl.ifBlank { "https://example.com/" })
            "OG tags complete" -> {
                onOgTitleChange(seo.ogTitle.ifBlank { seo.title.ifBlank { "Professional Web Page" } })
                onOgDescriptionChange(seo.ogDescription.ifBlank { seo.metaDescription.ifBlank { "Professional page built with WebForge Studio." } })
            }
            "Schema present" -> onStructuredTypeChange(StructuredDataType.ARTICLE)
        }
    }

    fun onSave(onSaved: () -> Unit) {
        val state = _uiState.value
        val page = state.page ?: return
        viewModelScope.launch {
            upsertPage(page.copy(seoConfig = state.seo))
            onSaved()
        }
    }

    private fun updateSeo(transform: (SEOConfig) -> SEOConfig) {
        _uiState.update { it.copy(seo = transform(it.seo)) }
        recalculate()
    }

    private fun recalculate() {
        val seo = _uiState.value.seo
        val titleScore = when (seo.title.length) {
            in 50..60 -> 20
            in 30..70 -> 10
            else -> 0
        }
        val descScore = when (seo.metaDescription.length) {
            in 150..160 -> 20
            in 100..200 -> 10
            else -> 0
        }
        val keywordScore = if (seo.metaKeywords.isNotEmpty()) 10 else 0
        val canonicalScore = if (seo.canonicalUrl.isNotBlank()) 10 else 0
        val ogScore = if (seo.ogTitle.isNotBlank() && seo.ogDescription.isNotBlank() && seo.ogImage.isNotBlank()) 20 else 0
        val schemaScore = if (seo.structuredDataType != StructuredDataType.NONE || seo.structuredDataJson.isNotBlank()) 20 else 0
        val total = titleScore + descScore + keywordScore + canonicalScore + ogScore + schemaScore
        _uiState.update {
            it.copy(
                score = total.coerceIn(0, 100),
                items = listOf(
                    SeoScoreItem("Title length", titleScore, "Keep title between 50 and 60 characters.", titleScore == 20),
                    SeoScoreItem("Description length", descScore, "Keep description between 150 and 160 characters.", descScore == 20),
                    SeoScoreItem("Keyword density", keywordScore, "Add relevant keywords.", keywordScore > 0),
                    SeoScoreItem("Canonical URL set", canonicalScore, "Set canonical URL to avoid duplicates.", canonicalScore > 0),
                    SeoScoreItem("OG tags complete", ogScore, "Set OG title, description, and image.", ogScore == 20),
                    SeoScoreItem("Schema present", schemaScore, "Add structured data for rich results.", schemaScore == 20),
                ),
            )
        }
    }
}
