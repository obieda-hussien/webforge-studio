package com.webforge.studio.engine

import com.webforge.studio.model.ElementNode
import com.webforge.studio.model.ElementType
import com.webforge.studio.model.Page
import com.webforge.studio.model.ProjectModel
import com.webforge.studio.model.SEOConfig
import com.webforge.studio.model.TargetPlatform
import kotlinx.serialization.json.Json

interface CodeGenerator {
    fun generate(project: ProjectModel, rootElement: ElementNode, page: Page? = null): GeneratedCode
}

data class GeneratedCode(
    val files: Map<String, String>,
)

class PlatformCodeGenerator(
    private val htmlCodeGenerator: HtmlCodeGenerator = HtmlCodeGenerator(),
    private val reactCodeGenerator: ReactCodeGenerator = ReactCodeGenerator(),
) : CodeGenerator {
    override fun generate(project: ProjectModel, rootElement: ElementNode, page: Page?): GeneratedCode =
        when (project.targetPlatform) {
            TargetPlatform.REACT, TargetPlatform.REACT_TS -> reactCodeGenerator.generate(project, rootElement, page)
            else -> htmlCodeGenerator.generate(project, rootElement, page)
        }
}

class HtmlCodeGenerator : CodeGenerator {
    override fun generate(project: ProjectModel, rootElement: ElementNode, page: Page?): GeneratedCode {
        val body = renderHtmlElement(rootElement, 2)
        val seo = page?.seoConfig ?: SEOConfig(title = project.name)
        val html = buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang=\"en\">")
            appendLine("<head>")
            appendLine("  <meta charset=\"UTF-8\" />")
            appendLine("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />")
            append(renderSeoTags(seo, project.name, indent = "  "))
            appendLine("  <link rel=\"stylesheet\" href=\"theme.css\" />")
            appendLine("</head>")
            appendLine("<body>")
            append(body)
            appendLine("  <script src=\"app.js\"></script>")
            appendLine("</body>")
            appendLine("</html>")
        }
        return GeneratedCode(
            files = mapOf(
                "index.html" to html,
                "theme.css" to ThemeCssGenerator.toCss(project.themeConfig),
            ),
        )
    }
}

class ReactCodeGenerator : CodeGenerator {
    override fun generate(project: ProjectModel, rootElement: ElementNode, page: Page?): GeneratedCode {
        val seo = page?.seoConfig ?: SEOConfig(title = project.name)
        val app = buildString {
            appendLine("import React from \"react\";")
            appendLine("import \"./theme.css\";")
            appendLine()
            appendLine("export default function App() {")
            appendLine("  return (")
            append(renderReactElement(rootElement, 2))
            appendLine("  );")
            appendLine("}")
        }
        val indexHtml = buildString {
            appendLine("<!doctype html>")
            appendLine("<html lang=\"en\">")
            appendLine("<head>")
            appendLine("  <meta charset=\"UTF-8\" />")
            appendLine("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />")
            append(renderSeoTags(seo, project.name, indent = "  "))
            appendLine("</head>")
            appendLine("<body>")
            appendLine("  <div id=\"root\"></div>")
            appendLine("</body>")
            appendLine("</html>")
        }
        return GeneratedCode(
            files = mapOf(
                "App.jsx" to app,
                "index.html" to indexHtml,
                "theme.css" to ThemeCssGenerator.toCss(project.themeConfig),
            ),
        )
    }
}

private fun renderSeoTags(seo: SEOConfig, fallbackTitle: String, indent: String): String = buildString {
    appendLine("$indent<title>${escapeHtml(seo.title.ifBlank { fallbackTitle })}</title>")
    if (seo.metaDescription.isNotBlank()) {
        appendLine("$indent<meta name=\"description\" content=\"${escapeHtml(seo.metaDescription)}\" />")
    }
    if (seo.metaKeywords.isNotEmpty()) {
        appendLine("$indent<meta name=\"keywords\" content=\"${escapeHtml(seo.metaKeywords.joinToString(","))}\" />")
    }
    if (seo.canonicalUrl.isNotBlank()) {
        appendLine("$indent<link rel=\"canonical\" href=\"${escapeHtml(seo.canonicalUrl)}\" />")
    }
    appendLine("$indent<meta name=\"robots\" content=\"${seo.robotsDirective.value}\" />")
    if (seo.ogTitle.isNotBlank()) appendLine("$indent<meta property=\"og:title\" content=\"${escapeHtml(seo.ogTitle)}\" />")
    if (seo.ogDescription.isNotBlank()) appendLine("$indent<meta property=\"og:description\" content=\"${escapeHtml(seo.ogDescription)}\" />")
    if (seo.ogImage.isNotBlank()) appendLine("$indent<meta property=\"og:image\" content=\"${escapeHtml(seo.ogImage)}\" />")
    if (seo.ogType.isNotBlank()) appendLine("$indent<meta property=\"og:type\" content=\"${escapeHtml(seo.ogType)}\" />")
    if (seo.twitterTitle.isNotBlank()) appendLine("$indent<meta name=\"twitter:title\" content=\"${escapeHtml(seo.twitterTitle)}\" />")
    if (seo.twitterDescription.isNotBlank()) appendLine("$indent<meta name=\"twitter:description\" content=\"${escapeHtml(seo.twitterDescription)}\" />")
    if (seo.twitterImage.isNotBlank()) appendLine("$indent<meta name=\"twitter:image\" content=\"${escapeHtml(seo.twitterImage)}\" />")
    appendLine("$indent<meta name=\"twitter:card\" content=\"${seo.twitterCard.value}\" />")
    val safeJsonLd = sanitizeJsonLd(seo.structuredDataJson)
    if (!safeJsonLd.isNullOrBlank()) {
        appendLine("$indent<script type=\"application/ld+json\">")
        appendLine(safeJsonLd)
        appendLine("$indent</script>")
    }
}

private fun renderHtmlElement(node: ElementNode, indentLevel: Int): String {
    val indent = "  ".repeat(indentLevel)
    val styleAttr = styleAttribute(node.properties)
    return buildString {
        when (node.type) {
            ElementType.TEXT -> appendLine("$indent<p$styleAttr>${escapeHtml(node.label)}</p>")
            ElementType.BUTTON -> appendLine("$indent<button$styleAttr>${escapeHtml(node.label)}</button>")
            ElementType.IMAGE -> appendLine("$indent<img src=\"${escapeHtml(node.properties["src"] ?: "")}\" alt=\"${escapeHtml(node.label.ifBlank { "image" })}\"$styleAttr />")
            ElementType.INPUT -> appendLine("$indent<input placeholder=\"${escapeHtml(node.label)}\"$styleAttr />")
            ElementType.LINK -> appendLine("$indent<a href=\"${escapeHtml(node.properties["href"] ?: "#")}\"$styleAttr>${escapeHtml(node.label)}</a>")
            ElementType.DIVIDER -> appendLine("$indent<hr$styleAttr />")
            else -> {
                appendLine("$indent<div$styleAttr>")
                node.children.forEach { append(renderHtmlElement(it, indentLevel + 1)) }
                appendLine("$indent</div>")
            }
        }
    }
}

private fun renderReactElement(node: ElementNode, indentLevel: Int): String {
    val indent = "  ".repeat(indentLevel)
    val style = node.properties.entries.joinToString(", ") {
        "\"${toReactStyleKey(it.key)}\": \"${escapeJsString(it.value)}\""
    }
    val styleAttr = if (style.isBlank()) "" else " style={{ $style }}"
    return buildString {
        when (node.type) {
            ElementType.TEXT -> appendLine("$indent<p$styleAttr>${escapeHtml(node.label)}</p>")
            ElementType.BUTTON -> appendLine("$indent<button$styleAttr>${escapeHtml(node.label)}</button>")
            ElementType.IMAGE -> appendLine("$indent<img src=\"${escapeHtml(node.properties["src"] ?: "")}\" alt=\"${escapeHtml(node.label.ifBlank { "image" })}\"$styleAttr />")
            ElementType.INPUT -> appendLine("$indent<input placeholder=\"${escapeHtml(node.label)}\"$styleAttr />")
            ElementType.LINK -> appendLine("$indent<a href=\"${escapeHtml(node.properties["href"] ?: "#")}\"$styleAttr>${escapeHtml(node.label)}</a>")
            ElementType.DIVIDER -> appendLine("$indent<hr$styleAttr />")
            else -> {
                appendLine("$indent<div$styleAttr>")
                node.children.forEach { append(renderReactElement(it, indentLevel + 1)) }
                appendLine("$indent</div>")
            }
        }
    }
}

private fun styleAttribute(properties: Map<String, String>): String {
    if (properties.isEmpty()) return ""
    val css = properties.entries.joinToString("; ") { (k, v) -> "$k: $v" }
    return " style=\"$css\""
}

private fun escapeHtml(value: String): String = value
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")

private fun escapeJsString(value: String): String = value
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")
    .replace("\r", "\\r")

private fun sanitizeJsonLd(value: String): String? = runCatching {
    val normalized = value.trim()
    Json.parseToJsonElement(normalized)
    normalized.replace("</script>", "<\\/script>")
}.getOrNull()

private fun toReactStyleKey(cssKey: String): String {
    val segments = cssKey.split("-").filter { it.isNotBlank() }
    if (segments.isEmpty()) return cssKey
    return buildString {
        append(segments.first())
        segments.drop(1).forEach { part ->
            append(part.replaceFirstChar { it.uppercase() })
        }
    }
}
