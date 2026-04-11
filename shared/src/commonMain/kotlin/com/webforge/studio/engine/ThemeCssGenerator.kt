package com.webforge.studio.engine

import com.webforge.studio.model.BorderRadiusPreset
import com.webforge.studio.model.ThemeConfig

object ThemeCssGenerator {

    fun toCss(theme: ThemeConfig): String = buildString {
        appendLine(":root {")
        appendLine("  --wf-color-primary: ${theme.customColors["primary"] ?: theme.primaryColor};")
        appendLine("  --wf-color-secondary: ${theme.customColors["secondary"] ?: theme.secondaryColor};")
        appendLine("  --wf-color-background: ${theme.customColors["background"] ?: theme.backgroundColor};")
        appendLine("  --wf-font-primary: '${theme.fontPrimary.ifBlank { theme.fontFamily }}', sans-serif;")
        appendLine("  --wf-font-secondary: '${theme.fontSecondary}', sans-serif;")
        appendLine("  --wf-font-size-base: ${theme.baseFontSizeSp}px;")
        appendLine("  --wf-spacing-base: ${theme.baseSpacing}px;")
        appendLine("  --wf-radius: ${radiusValue(theme.borderRadius)};")
        appendLine("  --wf-radius-sharp: 4px;")
        appendLine("  --wf-radius-rounded: 12px;")
        appendLine("  --wf-radius-pill: 999px;")
        theme.cssVariables.forEach { (name, value) ->
            val cleanName = name.trim().removePrefix("--")
            if (cleanName.isNotBlank() && value.isNotBlank()) {
                appendLine("  --$cleanName: $value;")
            }
        }
        appendLine("}")
        appendLine()
        appendLine("body {")
        appendLine("  font-family: var(--wf-font-primary);")
        appendLine("  font-size: var(--wf-font-size-base);")
        appendLine("  background: var(--wf-color-background);")
        appendLine("  color: var(--wf-color-primary);")
        appendLine("}")
    }

    private fun radiusValue(preset: BorderRadiusPreset): String = when (preset) {
        BorderRadiusPreset.SHARP -> "var(--wf-radius-sharp)"
        BorderRadiusPreset.ROUNDED -> "var(--wf-radius-rounded)"
        BorderRadiusPreset.PILL -> "var(--wf-radius-pill)"
    }
}
