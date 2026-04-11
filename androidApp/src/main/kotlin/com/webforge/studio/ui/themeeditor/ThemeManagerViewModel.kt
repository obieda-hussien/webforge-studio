package com.webforge.studio.ui.themeeditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webforge.studio.domain.usecase.GetProjectByIdUseCase
import com.webforge.studio.domain.usecase.UpdateProjectUseCase
import com.webforge.studio.model.BorderRadiusPreset
import com.webforge.studio.model.ProjectModel
import com.webforge.studio.model.ThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

sealed interface ThemeManagerUiState {
    data object Loading : ThemeManagerUiState
    data class Ready(
        val project: ProjectModel,
        val config: ThemeConfig,
        val coolorsUrl: String = "",
        val exportCss: String = "",
    ) : ThemeManagerUiState
}

@HiltViewModel
class ThemeManagerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProjectById: GetProjectByIdUseCase,
    private val updateProject: UpdateProjectUseCase,
) : ViewModel() {
    private val projectId: String = requireNotNull(savedStateHandle["projectId"])
    private val _uiState = MutableStateFlow<ThemeManagerUiState>(ThemeManagerUiState.Loading)
    val uiState: StateFlow<ThemeManagerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val project = getProjectById(projectId) ?: return@launch
            _uiState.value = ThemeManagerUiState.Ready(
                project = project,
                config = project.themeConfig,
            )
        }
    }

    fun onSeedChange(hex: String) {
        val primary = normalizeHex(hex)
        val secondary = shiftColor(primary, 0.75f)
        val background = shiftColor(primary, 1.9f)
        updateConfig {
            it.copy(
                primaryColor = primary,
                secondaryColor = secondary,
                backgroundColor = background,
                colorSeed = parseSeed(primary),
                customColors = it.customColors + mapOf(
                    "primary" to primary,
                    "secondary" to secondary,
                    "background" to background,
                ),
            )
        }
    }

    fun onRoleColorChange(role: String, hex: String) {
        val normalized = normalizeHex(hex)
        updateConfig {
            val updatedConfig = when (role) {
                "primary" -> it.copy(primaryColor = normalized)
                "secondary" -> it.copy(secondaryColor = normalized)
                "background" -> it.copy(backgroundColor = normalized)
                else -> it
            }
            updatedConfig.copy(
                customColors = updatedConfig.customColors.toMutableMap().apply { put(role, normalized) },
            )
        }
    }

    fun onFontPrimaryChange(value: String) = updateConfig { it.copy(fontPrimary = value, fontFamily = value) }
    fun onFontSecondaryChange(value: String) = updateConfig { it.copy(fontSecondary = value) }
    fun onBaseSpacingChange(value: Int) = updateConfig { it.copy(baseSpacing = value) }
    fun onRadiusChange(value: BorderRadiusPreset) = updateConfig { it.copy(borderRadius = value) }
    fun onDarkModeDefaultChange(value: Boolean) = updateConfig { it.copy(darkModeDefault = value, isDarkMode = value) }

    fun onCoolorsUrlChange(value: String) {
        _uiState.update {
            val ready = it as? ThemeManagerUiState.Ready ?: return@update it
            ready.copy(coolorsUrl = value)
        }
    }

    fun onImportCoolors() {
        val ready = _uiState.value as? ThemeManagerUiState.Ready ?: return
        val colors = parseCoolorsPalette(ready.coolorsUrl)
        if (colors.isEmpty()) return
        updateConfig { config ->
            config.copy(
                primaryColor = colors.getOrElse(0) { config.primaryColor },
                secondaryColor = colors.getOrElse(1) { config.secondaryColor },
                backgroundColor = colors.getOrElse(2) { config.backgroundColor },
                customColors = config.customColors.toMutableMap().apply {
                    colors.getOrNull(0)?.let { put("primary", it) }
                    colors.getOrNull(1)?.let { put("secondary", it) }
                    colors.getOrNull(2)?.let { put("background", it) }
                },
            )
        }
    }

    fun onRandomize() {
        val random = Random(System.currentTimeMillis())
        fun randomColor() = "#%02X%02X%02X".format(
            random.nextInt(40, 230),
            random.nextInt(40, 230),
            random.nextInt(40, 230),
        )
        updateConfig {
            val primary = randomColor()
            val secondary = randomColor()
            val background = randomColor()
            it.copy(
                primaryColor = primary,
                secondaryColor = secondary,
                backgroundColor = background,
                customColors = it.customColors + mapOf(
                    "primary" to primary,
                    "secondary" to secondary,
                    "background" to background,
                ),
            )
        }
    }

    fun onCssVariableChange(name: String, value: String) {
        updateConfig {
            it.copy(
                cssVariables = it.cssVariables.toMutableMap().apply {
                    if (name.isBlank() || value.isBlank()) remove(name) else put(name, value)
                },
            )
        }
    }

    fun onExportThemeCss() {
        _uiState.update {
            val ready = it as? ThemeManagerUiState.Ready ?: return@update it
            ready.copy(exportCss = com.webforge.studio.engine.ThemeCssGenerator.toCss(ready.config))
        }
    }

    fun onSave(onSaved: () -> Unit) {
        val ready = _uiState.value as? ThemeManagerUiState.Ready ?: return
        viewModelScope.launch {
            updateProject(ready.project.copy(themeConfig = ready.config))
            onSaved()
        }
    }

    private fun updateConfig(transform: (ThemeConfig) -> ThemeConfig) {
        _uiState.update {
            val ready = it as? ThemeManagerUiState.Ready ?: return@update it
            ready.copy(config = transform(ready.config))
        }
    }

    private fun parseSeed(hex: String): Int = runCatching {
        normalizeHex(hex).removePrefix("#").toInt(16)
    }.getOrDefault(0x6750A4)

    private fun normalizeHex(value: String): String {
        val cleaned = value.trim().removePrefix("#")
        val normalized = when {
            cleaned.length == 3 && cleaned.all { it.isLetterOrDigit() } ->
                cleaned.map { "$it$it" }.joinToString("")
            cleaned.length >= 6 -> cleaned.take(6)
            else -> cleaned.padEnd(6, '0')
        }
        return "#${normalized.uppercase()}"
    }

    private fun parseCoolorsPalette(url: String): List<String> {
        val tail = url.substringAfterLast("/")
        return tail.split("-")
            .mapNotNull { token ->
                val hex = token.trim()
                if (hex.matches(Regex("^[0-9a-fA-F]{6}$"))) "#${hex.uppercase()}" else null
            }
    }

    private fun shiftColor(color: String, factor: Float): String {
        val clean = normalizeHex(color).removePrefix("#")
        val r = clean.substring(0, 2).toInt(16)
        val g = clean.substring(2, 4).toInt(16)
        val b = clean.substring(4, 6).toInt(16)
        fun shift(channel: Int): Int = ((channel * factor).toInt()).coerceIn(0, 255)
        return "#%02X%02X%02X".format(shift(r), shift(g), shift(b))
    }
}
