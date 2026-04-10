package com.webforge.studio.ui.component

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult

/**
 * Convenience wrapper that shows a snackbar with an optional action label and
 * returns the [SnackbarResult] for the caller to act on.
 *
 * Must be called from a coroutine scope (e.g. inside [androidx.compose.runtime.LaunchedEffect]).
 *
 * @param hostState    The [SnackbarHostState] to show the snackbar on.
 * @param message      The snackbar message text.
 * @param actionLabel  Optional action label; if null no action button is shown.
 * @param duration     How long the snackbar is visible.
 * @return [SnackbarResult.ActionPerformed] if the action was clicked, [SnackbarResult.Dismissed] otherwise.
 */
suspend fun showWFSnackbar(
    hostState: SnackbarHostState,
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
): SnackbarResult = hostState.showSnackbar(
    message = message,
    actionLabel = actionLabel,
    duration = duration,
    withDismissAction = true,
)
