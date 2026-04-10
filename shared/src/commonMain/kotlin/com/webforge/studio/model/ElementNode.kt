package com.webforge.studio.model

import kotlinx.serialization.Serializable

/**
 * A node in the visual canvas element tree.
 *
 * Elements can be nested (e.g. a Column containing Text children).
 *
 * @param id         Unique identifier for this node (UUID string).
 * @param type       The visual element type (Container, Text, Image, …).
 * @param label      Display label shown in the layers panel.
 * @param properties Arbitrary key-value style/attribute map (CSS-like).
 * @param children   Ordered list of child nodes.
 */
@Serializable
data class ElementNode(
    val id: String,
    val type: ElementType,
    val label: String = "",
    val properties: Map<String, String> = emptyMap(),
    val children: List<ElementNode> = emptyList(),
)

/** Supported visual element types on the canvas. */
@Serializable
enum class ElementType {
    CONTAINER,
    TEXT,
    IMAGE,
    BUTTON,
    INPUT,
    LINK,
    DIVIDER,
    CUSTOM,
}
