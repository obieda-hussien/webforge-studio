package com.webforge.studio.domain.usecase

import com.webforge.studio.model.OutputType
import com.webforge.studio.model.ProjectModel
import com.webforge.studio.model.TargetPlatform
import com.webforge.studio.model.ThemeConfig
import com.webforge.studio.repository.ProjectRepository
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Creates a new project after validating the name, generating a slug,
 * and persisting the record via [ProjectRepository].
 *
 * @throws IllegalArgumentException if [name] is blank, shorter than 3 chars,
 *         longer than 50 chars, or contains characters other than letters,
 *         digits, spaces, and hyphens.
 */
class CreateProjectUseCase @Inject constructor(
    private val repository: ProjectRepository,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        name: String,
        description: String = "",
        targetPlatform: TargetPlatform = TargetPlatform.HTML,
        colorSeed: Long = 0xFF6750A4,
        fontPair: String = "Inter / Roboto",
        isDarkMode: Boolean = false,
    ): ProjectModel {
        val trimmed = name.trim()
        require(trimmed.length >= 3) { "Project name must be at least 3 characters." }
        require(trimmed.length <= 50) { "Project name must be at most 50 characters." }
        require(trimmed.matches(Regex("[\\w\\s\\-]+"))) {
            "Project name may only contain letters, digits, spaces, and hyphens."
        }

        val slug = trimmed.lowercase()
            .replace(Regex("\\s+"), "-")
            .replace(Regex("[^a-z0-9\\-]"), "")

        val now = System.currentTimeMillis()
        val project = ProjectModel(
            id = Uuid.random().toString(),
            name = trimmed,
            slug = slug,
            description = description.trim(),
            createdAt = now,
            updatedAt = now,
            outputType = OutputType.HTML,
            targetPlatform = targetPlatform,
            colorSeed = colorSeed,
            fontPair = fontPair,
            themeConfig = ThemeConfig(
                isDarkMode = isDarkMode,
                darkModeDefault = isDarkMode,
                colorSeed = toRgbSeed(colorSeed),
                fontPrimary = fontPair.substringBefore("/").trim().ifBlank { "Roboto" },
                fontSecondary = fontPair.substringAfter("/", "Inter").trim(),
            ),
        )
        repository.upsertProject(project)
        return project
    }

    private fun toRgbSeed(colorValue: Long): Int {
        // Theme seed operates on RGB only; keep lower 24 bits and discard alpha channel.
        return (colorValue and 0x00FFFFFF).toInt()
    }
}
