package com.webforge.studio.engine

import com.webforge.studio.model.BlockChain
import com.webforge.studio.model.BlockDescriptors
import com.webforge.studio.model.BlockNode
import com.webforge.studio.model.BlockType
import com.webforge.studio.model.blockTypeForEvent

class BlockJavaScriptGenerator {

    fun generateAppJs(chainsByElement: Map<String?, List<BlockChain>>): String {
        val functions = mutableListOf<String>()
        chainsByElement.forEach { (elementId, chains) ->
            chains.forEach { chain ->
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
        val eventDescriptor = BlockDescriptors.all.getValue(blockTypeForEvent(chain.eventType))

        val variableNames = mutableSetOf<String>()
        val variableMapping = linkedMapOf<String, String>()
        val declarations = chain.blocks
            .filter { it.type == BlockType.DECLARE_VARIABLE }
            .mapNotNull { it.parameters["name"]?.content }
            .map { requested ->
                val unique = uniqueName(requested.ifBlank { "unnamedVar" }, variableNames)
                variableMapping.putIfAbsent(requested, unique)
                "let $unique;"
            }

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
            .replace("${'$'}{target}", targetExpr)
            .replace("${'$'}{body}", listOf(declarations.joinToString("\n"), bodyCode).filter { it.isNotBlank() }.joinToString("\n"))

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
                val resolvedValue = variableMapping[rawValue] ?: rawValue
                acc.replace("${'$'}{${param.name}}", resolvedValue)
            }

            val childNodes = allBlocks
                .filter { it.parentBlockId == node.id }
                .sortedBy { it.order }

            val rendered = if (descriptor.hasBodySlot) {
                val childCode = renderNodes(childNodes, allBlocks, indent + 1, variableMapping)
                codeLine.replace("${'$'}{body}", childCode)
            } else {
                codeLine
            }

            rendered.lines().joinToString("\n") { "${"  ".repeat(indent)}$it" }
        }
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
}
