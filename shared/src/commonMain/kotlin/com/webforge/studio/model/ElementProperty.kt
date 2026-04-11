package com.webforge.studio.model

import kotlinx.serialization.Serializable

/**
 * A single style or attribute property attached to an [ElementNode].
 *
 * @param id        Unique identifier (UUID string).
 * @param elementId Owning element identifier.
 * @param key       CSS property name or HTML attribute name (e.g. "color", "data-id").
 * @param value     Property value string (e.g. "#FF0000", "16px").
 * @param unit      Optional CSS unit when [type] is [PropertyType.UNIT] (e.g. "px", "rem").
 * @param type      Semantic type used to render the appropriate property editor.
 */
@Serializable
data class ElementProperty(
    val id: String,
    val elementId: String,
    val key: String,
    val value: String,
    val unit: String? = null,
    val type: PropertyType = PropertyType.STRING,
)

/** Semantic category of an [ElementProperty] value — drives the property-editor UI. */
@Serializable
enum class PropertyType {
    STRING,
    NUMBER,
    COLOR,
    UNIT,
    BOOLEAN,
}
