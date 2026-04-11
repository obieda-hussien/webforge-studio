package com.webforge.studio.ui.seo

import com.webforge.studio.model.SEOConfig
import com.webforge.studio.model.StructuredDataType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeoScoreCalculatorTest {

    @Test
    fun `calculate returns 100 for fully optimized config`() {
        val seo = SEOConfig(
            title = "A".repeat(55),
            metaDescription = "B".repeat(155),
            metaKeywords = listOf("web", "forge"),
            canonicalUrl = "https://example.com/",
            ogTitle = "OG Title",
            ogDescription = "OG Description",
            ogImage = "https://example.com/image.png",
            structuredDataType = StructuredDataType.ARTICLE,
        )

        val result = SeoScoreCalculator.calculate(seo)

        assertEquals(100, result.total)
        assertTrue(result.items.all { it.passed })
    }

    @Test
    fun `calculate returns 0 for empty config`() {
        val result = SeoScoreCalculator.calculate(SEOConfig())
        assertEquals(0, result.total)
        assertTrue(result.items.none { it.passed })
    }

    @Test
    fun `calculate uses partial buckets for title and description`() {
        val seo = SEOConfig(
            title = "T".repeat(35),
            metaDescription = "D".repeat(120),
        )

        val result = SeoScoreCalculator.calculate(seo)

        assertEquals(20, result.total)
        assertEquals(10, result.items.first { it.label == "Title length" }.score)
        assertEquals(10, result.items.first { it.label == "Description length" }.score)
    }
}
