package com.webforge.studio.ui.seo

import com.webforge.studio.model.SEOConfig
import com.webforge.studio.model.StructuredDataType

data class SeoScoreBreakdown(
    val total: Int,
    val items: List<SeoScoreItem>,
)

object SeoScoreCalculator {
    fun calculate(seo: SEOConfig): SeoScoreBreakdown {
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
        val total = (titleScore + descScore + keywordScore + canonicalScore + ogScore + schemaScore).coerceIn(0, 100)
        return SeoScoreBreakdown(
            total = total,
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
