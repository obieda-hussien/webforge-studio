package com.webforge.studio.engine

import com.webforge.studio.model.ElementNode
import com.webforge.studio.model.ProjectModel

/**
 * Contract for code-generation back-ends.
 *
 * Each implementation converts the canvas element tree of a [ProjectModel]
 * into a target language / framework (HTML, React, PWA, …).
 */
interface CodeGenerator {

    /**
     * Generates the full source code for the given [project] and its [rootElement] tree.
     *
     * @param project     The project metadata (name, theme, output type, …).
     * @param rootElement The root of the canvas element tree to serialise.
     * @return            A [GeneratedCode] bundle containing the file(s) to write.
     */
    fun generate(project: ProjectModel, rootElement: ElementNode): GeneratedCode
}

/**
 * The result produced by a [CodeGenerator].
 *
 * @param files Map of relative file paths to their string content,
 *              e.g. `"index.html" -> "<html>…</html>"`.
 */
data class GeneratedCode(
    val files: Map<String, String>,
)

// ---------------------------------------------------------------------------
// HTML implementation
// ---------------------------------------------------------------------------

/**
 * Plain HTML5 code generator.
 *
 * Recursively converts an [ElementNode] tree into semantic HTML with
 * inline CSS derived from the project [com.webforge.studio.model.ThemeConfig].
 */
class HtmlCodeGenerator : CodeGenerator {

    override fun generate(project: ProjectModel, rootElement: ElementNode): GeneratedCode {
        val theme = project.themeConfig
        val body = renderElement(rootElement, indentLevel = 2)
        val html = buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang=\"en\">")
            appendLine("<head>")
            appendLine("  <meta charset=\"UTF-8\" />")
            appendLine("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />")
            appendLine("  <title>${project.name}</title>")
            appendLine("  <style>")
            appendLine("    :root {")
            appendLine("      --color-primary: ${theme.primaryColor};")
            appendLine("      --color-secondary: ${theme.secondaryColor};")
            appendLine("      --color-background: ${theme.backgroundColor};")
            appendLine("      --font-family: '${theme.fontFamily}', sans-serif;")
            appendLine("      --font-size-base: ${theme.baseFontSizeSp}px;")
            appendLine("    }")
            appendLine("    * { box-sizing: border-box; margin: 0; padding: 0; }")
            appendLine("    body {")
            appendLine("      font-family: var(--font-family);")
            appendLine("      font-size: var(--font-size-base);")
            appendLine("      background-color: var(--color-background);")
            appendLine("    }")
            appendLine("  </style>")
            appendLine("</head>")
            appendLine("<body>")
            append(body)
            appendLine("</body>")
            appendLine("</html>")
        }
        return GeneratedCode(files = mapOf("index.html" to html))
    }

    private fun renderElement(node: ElementNode, indentLevel: Int): String {
        val indent = "  ".repeat(indentLevel)
        val styleAttr = if (node.properties.isNotEmpty()) {
            val css = node.properties.entries.joinToString("; ") { (k, v) -> "$k: $v" }
            " style=\"$css\""
        } else {
            ""
        }

        return buildString {
            when (node.type) {
                com.webforge.studio.model.ElementType.TEXT -> {
                    appendLine("$indent<p$styleAttr>${node.label}</p>")
                }
                com.webforge.studio.model.ElementType.BUTTON -> {
                    appendLine("$indent<button$styleAttr>${node.label}</button>")
                }
                com.webforge.studio.model.ElementType.IMAGE -> {
                    val src = node.properties["src"] ?: ""
                    val alt = node.label.ifBlank { "image" }
                    appendLine("$indent<img src=\"$src\" alt=\"$alt\"$styleAttr />")
                }
                com.webforge.studio.model.ElementType.INPUT -> {
                    val placeholder = node.label.ifBlank { "" }
                    appendLine("$indent<input placeholder=\"$placeholder\"$styleAttr />")
                }
                com.webforge.studio.model.ElementType.LINK -> {
                    val href = node.properties["href"] ?: "#"
                    appendLine("$indent<a href=\"$href\"$styleAttr>${node.label}</a>")
                }
                com.webforge.studio.model.ElementType.DIVIDER -> {
                    appendLine("$indent<hr$styleAttr />")
                }
                else -> {
                    // CONTAINER, CUSTOM, and any future types → <div>
                    appendLine("$indent<div$styleAttr>")
                    node.children.forEach { child ->
                        append(renderElement(child, indentLevel + 1))
                    }
                    appendLine("$indent</div>")
                }
            }
        }
    }
}
