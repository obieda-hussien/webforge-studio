package com.webforge.studio.model

/**
 * Represents a top-level project inside WebForge Studio.
 *
 * @param id           Unique identifier (UUID string).
 * @param name         Human-readable project name.
 * @param description  Optional project description.
 * @param createdAt    Unix epoch milliseconds when the project was created.
 * @param updatedAt    Unix epoch milliseconds of the last modification.
 * @param outputType   Target output format (React, HTML, PWA, etc.).
 * @param themeConfig  Active theme configuration for this project.
 */
data class ProjectModel(
    val id: String,
    val name: String,
    val description: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val outputType: OutputType = OutputType.HTML,
    val themeConfig: ThemeConfig = ThemeConfig(),
)

/** Supported code-generation output targets. */
enum class OutputType {
    HTML,
    REACT,
    PWA,
}
