package com.webforge.studio.ui.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Abstraction that allows ViewModels to produce user-facing strings without
 * holding a reference to [Context].
 *
 * **Why this pattern?**
 * ViewModels must not reference Android [Context] directly — doing so creates
 * memory leaks and makes them untestable. [UiText] solves this by deferring
 * resolution to the UI layer (Composables or Activities that own a context).
 *
 * Usage in a ViewModel:
 * ```kotlin
 * _uiState.value = MyState.Error(UiText.StringResource(R.string.error_network))
 * ```
 *
 * Usage in a Composable:
 * ```kotlin
 * Text(text = state.error.asString())
 * ```
 */
sealed interface UiText {

    /** A string provided directly (e.g. from a remote API error message). */
    data class Raw(val value: String) : UiText

    /** A string resolved from Android string resources at render time. */
    data class StringResource(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList(),
    ) : UiText

    /** Resolves this [UiText] to a plain [String] inside a Composable. */
    @Composable
    fun asString(): String = when (this) {
        is Raw -> value
        is StringResource -> LocalContext.current.getString(resId, *args.toTypedArray())
    }

    /** Resolves this [UiText] to a plain [String] outside of Composition. */
    fun asString(context: Context): String = when (this) {
        is Raw -> value
        is StringResource -> context.getString(resId, *args.toTypedArray())
    }
}
