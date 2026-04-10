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
 * @param pageId     Identifier of the page this element belongs to.
 * @param parentId   Identifier of the parent element, or null for root elements.
 * @param order      Z-order position among siblings.
 * @param x          Horizontal position in pixels on the canvas.
 * @param y          Vertical position in pixels on the canvas.
 * @param width      Element width in pixels.
 * @param height     Element height in pixels.
 */
@Serializable
data class ElementNode(
    val id: String,
    val type: ElementType,
    val label: String = "",
    val properties: Map<String, String> = emptyMap(),
    val children: List<ElementNode> = emptyList(),
    val pageId: String = "",
    val parentId: String? = null,
    val order: Int = 0,
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 120f,
    val height: Float = 60f,
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
