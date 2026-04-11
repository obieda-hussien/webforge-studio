package com.webforge.studio.ui.themeeditor

import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeColorTransformerTest {

    @Test
    fun `shiftColor darkens with factor below 1`() {
        assertEquals("#7F7F7F", ThemeColorTransformer.shiftColor("#AAAAAA", 0.75f))
    }

    @Test
    fun `shiftColor lightens and clamps with factor above 1`() {
        assertEquals("#FFFFFF", ThemeColorTransformer.shiftColor("#A0A0A0", 1.9f))
    }

    @Test
    fun `shiftColor preserves black and white edge cases`() {
        assertEquals("#000000", ThemeColorTransformer.shiftColor("#000000", 1.9f))
        assertEquals("#BFBFBF", ThemeColorTransformer.shiftColor("#FFFFFF", 0.75f))
    }

    @Test
    fun `normalizeHex supports shorthand input`() {
        assertEquals("#AABBCC", ThemeColorTransformer.normalizeHex("#ABC"))
    }
}
