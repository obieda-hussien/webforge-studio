package com.webforge.studio.engine

import com.webforge.studio.model.BlockChain
import com.webforge.studio.model.BlockDescriptors
import com.webforge.studio.model.BlockNode
import com.webforge.studio.model.BlockParameterDefinition
import com.webforge.studio.model.BlockParameterType
import com.webforge.studio.model.BlockType
import kotlinx.serialization.json.Json

class BlockJavaScriptGenerator {

    private val json = Json { ignoreUnknownKeys = true }

    fun generateAppJs(chainsByElement: Map<String?, List<BlockChain>>): String {
        val functions = mutableListOf<String>()
        chainsByElement.toSortedMap(compareBy<String?> { it ?: "" }).forEach { (elementId, chains) ->
            chains.sortedBy { it.eventType.name }.forEach { chain ->
                functions += generateFunction(
                    functionName = functionNameFor(elementId, chain.eventType.name),
                    elementId = elementId,
                    chain = chain,
                )
            }
        }
        return functions.joinToString("\n\n")
    }

    private fun generateFunction(
        functionName: String,
        elementId: String?,
        chain: BlockChain,
    ): String {
        val bodyNodes = chain.blocks.filter { it.parentBlockId == null }.sortedBy { it.order }
        val eventDescriptor = BlockDescriptors.descriptorForEvent(chain.eventType)
        val variableMapping = buildVariableMapping(chain.blocks)
        val declarations = variableMapping.values.map { "let $it;" }
        val bodyCode = renderNodes(
            nodes = bodyNodes,
            allBlocks = chain.blocks,
            indent = 1,
            variableMapping = variableMapping,
        )

        val targetExpr = if (elementId.isNullOrBlank()) {
            "window"
        } else {
            "document.getElementById('${escapeJsString(elementId)}')"
        }
        val wrapperBody = eventDescriptor.codeTemplate
            .replace("\${target}", targetExpr)
            .replace(
                "\${body}",
                listOf(declarations.joinToString("\n"), bodyCode).filter { it.isNotBlank() }.joinToString("\n"),
            )
            .let(::fillUnresolvedPlaceholders)

        return buildString {
            appendLine("function $functionName() {")
            wrapperBody.lines().forEach { appendLine("  $it") }
            appendLine("}")
            appendLine("$functionName();")
        }
    }

    private fun renderNodes(
        nodes: List<BlockNode>,
        allBlocks: List<BlockNode>,
        indent: Int,
        variableMapping: Map<String, String>,
    ): String {
        return nodes.joinToString("\n") { node ->
            val descriptor = BlockDescriptors.all.getValue(node.type)
            val codeLine = descriptor.parameters.fold(descriptor.codeTemplate) { acc, param ->
                val rawValue = node.parameters[param.name]?.content ?: param.defaultValue
                val safeValue = safeParameterValue(node, param, rawValue, variableMapping)
                acc.replace("\${${param.name}}", safeValue)
            }

            val childNodes = allBlocks
                .filter { it.parentBlockId == node.id }
                .sortedBy { it.order }

            val renderedBody = if (descriptor.hasBodySlot) {
                val childCode = renderNodes(childNodes, allBlocks, indent + 1, variableMapping)
                codeLine.replace("\${body}", childCode)
            } else {
                codeLine
            }
            val rendered = fillUnresolvedPlaceholders(renderedBody)
            val withSafety = if (node.type in fetchBlocks) wrapFetchInTryCatch(rendered) else rendered
            withSafety.lines().joinToString("\n") { "${"  ".repeat(indent)}$it" }
        }
    }

    private fun buildVariableMapping(blocks: List<BlockNode>): Map<String, String> {
        val variableNames = mutableSetOf<String>()
        val mapping = linkedMapOf<String, String>()
        blocks
            .asSequence()
            .filter { it.type == BlockType.DECLARE_VARIABLE }
            .mapNotNull { it.parameters["name"]?.content?.trim() }
            .filter { it.isNotBlank() }
            .forEach { requested ->
                mapping.putIfAbsent(
                    requested,
                    uniqueName(safeIdentifier(requested, "var"), variableNames),
                )
            }
        return mapping
    }

    private fun safeParameterValue(
        node: BlockNode,
        parameter: BlockParameterDefinition,
        rawValue: String,
        variableMapping: Map<String, String>,
    ): String {
        if (node.type == BlockType.DECLARE_VARIABLE && parameter.name == "name") {
            return variableMapping[rawValue.trim()] ?: safeIdentifier(rawValue, "var")
        }
        if (parameter.acceptsVariableReference) {
            variableMapping[rawValue.trim()]?.let { return it }
        }
        return when (parameter.type) {
            BlockParameterType.TEXT,
            BlockParameterType.MULTILINE_TEXT,
            BlockParameterType.URL,
            BlockParameterType.ELEMENT_SELECTOR,
            BlockParameterType.CSS_PROPERTY_NAME,
            BlockParameterType.CSS_UNIT_VALUE,
            BlockParameterType.COLOR,
            BlockParameterType.EVENT_TYPE,
            -> quoteJs(rawValue)

            BlockParameterType.NUMBER -> rawValue.toDoubleOrNull()?.toString()
                ?: parameter.defaultValue.toDoubleOrNull()?.toString()
                ?: "0"

            BlockParameterType.BOOLEAN -> rawValue.toBooleanStrictOrNull()?.toString()
                ?: parameter.defaultValue.toBooleanStrictOrNull()?.toString()
                ?: "false"

            BlockParameterType.JSON_OBJECT -> safeJsonLiteral(rawValue)
            BlockParameterType.VARIABLE_REF -> variableMapping[rawValue.trim()] ?: "undefined"
            BlockParameterType.EXPRESSION -> safeExpression(rawValue, variableMapping)
        }
    }

    private fun safeExpression(raw: String, variableMapping: Map<String, String>): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return "undefined"
        if (!expressionRegex.matches(trimmed)) return "undefined"
        var expression = trimmed
        variableMapping.forEach { (requested, unique) ->
            val token = Regex("\\b${Regex.escape(requested)}\\b")
            expression = expression.replace(token, unique)
        }
        return expression
    }

    private fun safeJsonLiteral(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return "{}"
        return runCatching { json.parseToJsonElement(trimmed).toString() }.getOrDefault("{}")
    }

    private fun fillUnresolvedPlaceholders(template: String): String {
        var resolved = template
        resolved = resolved.replace("\${responseVar}", "response")
        resolved = resolved.replace("\${resultVar}", "result")
        resolved = resolved.replace("\${intervalVar}", "intervalId")
        return placeholderRegex.replace(resolved) { "undefined" }
    }

    private fun wrapFetchInTryCatch(statement: String): String = buildString {
        appendLine("try {")
        statement.lines().forEach { appendLine("  $it") }
        appendLine("} catch (error) {")
        appendLine("  console.error(error);")
        append("}")
    }

    private fun functionNameFor(elementId: String?, eventType: String): String {
        val element = (elementId ?: "page").replace(Regex("[^a-zA-Z0-9_]"), "_")
        val event = eventType.lowercase().replace(Regex("[^a-z0-9_]"), "_")
        return "handle_${element}_$event"
    }

    private fun uniqueName(base: String, existing: MutableSet<String>): String {
        var candidate = base
        var i = 1
        while (!existing.add(candidate)) {
            candidate = "${base}_$i"
            i++
        }
        return candidate
    }

    private fun safeIdentifier(raw: String, fallback: String): String {
        val normalized = raw.trim().replace(Regex("[^a-zA-Z0-9_]"), "_")
        val prefixed = if (normalized.firstOrNull()?.isDigit() == true) "_$normalized" else normalized
        return prefixed.ifBlank { fallback }
    }

    private fun quoteJs(raw: String): String = "'${escapeJsString(raw)}'"

    private fun escapeJsString(value: String): String = buildString {
        value.forEach { ch ->
            when (ch) {
                '\\' -> append("\\\\")
                '\'' -> append("\\'")
                '\"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(ch)
            }
        }
    }

    private companion object {
        val placeholderRegex = Regex("\\$\\{[^}]+}")
        val expressionRegex = Regex("[a-zA-Z0-9_$.\\s+\\-*/%<>=!&|?:(),\\[\\]'\"`]+")
        val fetchBlocks = setOf(
            BlockType.FETCH_GET,
            BlockType.FETCH_POST,
            BlockType.FETCH_PUT,
            BlockType.FETCH_DELETE,
        )
    }
}
