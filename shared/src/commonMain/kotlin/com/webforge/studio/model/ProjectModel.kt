package com.webforge.studio.model

import kotlinx.serialization.Serializable

/**
 * Represents a top-level project inside WebForge Studio.
 *
 * @param id              Unique identifier (UUID string).
 * @param name            Human-readable project name.
 * @param slug            URL-friendly identifier derived from [name].
 * @param description     Optional project description.
 * @param createdAt       Unix epoch milliseconds when the project was created.
 * @param updatedAt       Unix epoch milliseconds of the last modification.
 * @param outputType      Legacy output-format enum kept for backward compatibility.
 * @param targetPlatform  Primary target platform for code generation.
 * @param themeConfig     Active theme configuration for this project.
 * @param thumbnailPath   Optional path to a cached thumbnail image.
 * @param colorSeed       Seed color (ARGB long) used to derive the M3 tonal palette.
 * @param fontPair        Display/body font pairing identifier (e.g. "Inter / Roboto").
 */
@Serializable
data class ProjectModel(
    val id: String,
    val name: String,
    val slug: String = "",
    val description: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val outputType: OutputType = OutputType.HTML,
    val targetPlatform: TargetPlatform = TargetPlatform.HTML,
    val themeConfig: ThemeConfig = ThemeConfig(),
    val thumbnailPath: String? = null,
    val colorSeed: Long = 0xFF6750A4,
    val fontPair: String = "Inter / Roboto",
)

/** Legacy supported code-generation output targets (kept for backward compatibility). */
@Serializable
enum class OutputType {
    HTML,
    REACT,
    PWA,
}

/** Primary platform target — drives code-generation strategy. */
@Serializable
enum class TargetPlatform {
    /** Static HTML5 + CSS + vanilla JS. */
    HTML,
    /** React JSX (JavaScript). */
    REACT,
    /** React + TypeScript. */
    REACT_TS,
    /** Progressive Web App (HTML + service worker + manifest). */
    PWA,
}
