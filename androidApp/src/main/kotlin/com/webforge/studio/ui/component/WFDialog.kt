package com.webforge.studio.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * M3 AlertDialog wrapped in an [AnimatedVisibility] with a scale+fade enter
 * animation that follows M3 Expressive motion principles.
 *
 * @param visible         Whether the dialog is shown.
 * @param title           Dialog title.
 * @param message         Dialog body text.
 * @param confirmLabel    Label for the primary confirm button.
 * @param onConfirm       Called when the confirm button is tapped.
 * @param onDismissRequest Called when the user taps outside or the dismiss button.
 * @param dismissLabel    Label for the dismiss button (default "Cancel").
 */
@Composable
fun WFDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String = "Cancel",
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.85f),
        exit = fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.85f),
    ) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = onConfirm) { Text(confirmLabel) }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) { Text(dismissLabel) }
            },
            modifier = modifier,
        )
    }
}
