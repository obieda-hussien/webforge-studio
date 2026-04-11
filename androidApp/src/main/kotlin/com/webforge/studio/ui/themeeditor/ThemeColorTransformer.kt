package com.webforge.studio.ui.themeeditor

object ThemeColorTransformer {
    fun shiftColor(color: String, factor: Float): String {
        val clean = normalizeHex(color).removePrefix("#")
        val r = clean.substring(0, 2).toInt(16)
        val g = clean.substring(2, 4).toInt(16)
        val b = clean.substring(4, 6).toInt(16)
        fun shift(channel: Int): Int = ((channel * factor).toInt()).coerceIn(0, 255)
        return "#%02X%02X%02X".format(shift(r), shift(g), shift(b))
    }

    fun normalizeHex(value: String): String {
        val cleaned = value.trim().removePrefix("#")
        val normalized = when {
            cleaned.length == 3 && cleaned.all { it.isHexChar() } ->
                cleaned.map { "$it$it" }.joinToString("")
            cleaned.length >= 6 -> cleaned.take(6)
            else -> cleaned.padEnd(6, '0')
        }
        return "#${normalized.uppercase()}"
    }
}

private fun Char.isHexChar(): Boolean =
    this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'
