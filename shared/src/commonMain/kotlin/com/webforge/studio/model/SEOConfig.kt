package com.webforge.studio.model

import kotlinx.serialization.Serializable

@Serializable
data class SEOConfig(
    val title: String = "",
    val metaDescription: String = "",
    val metaKeywords: List<String> = emptyList(),
    val canonicalUrl: String = "",
    val robotsDirective: RobotsDirective = RobotsDirective.INDEX_FOLLOW,
    val ogTitle: String = "",
    val ogDescription: String = "",
    val ogImage: String = "",
    val ogType: String = "website",
    val twitterCard: TwitterCard = TwitterCard.SUMMARY,
    val twitterTitle: String = "",
    val twitterDescription: String = "",
    val twitterImage: String = "",
    val structuredDataType: StructuredDataType = StructuredDataType.NONE,
    val structuredDataJson: String = "",
)

@Serializable
enum class RobotsDirective(val value: String) {
    INDEX_FOLLOW("index,follow"),
    NOINDEX_FOLLOW("noindex,follow"),
    INDEX_NOFOLLOW("index,nofollow"),
    NOINDEX_NOFOLLOW("noindex,nofollow"),
}

@Serializable
enum class TwitterCard(val value: String) {
    SUMMARY("summary"),
    SUMMARY_LARGE_IMAGE("summary_large_image"),
}

@Serializable
enum class StructuredDataType {
    NONE,
    ARTICLE,
    PRODUCT,
    FAQ,
    BREADCRUMB_LIST,
}
