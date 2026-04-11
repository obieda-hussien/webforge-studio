package com.webforge.studio.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Outlined text field that surfaces a [validationError] as the Material 3
 * supporting-text slot when non-null.
 *
 * Meets the 48 dp touch-target minimum via [OutlinedTextField]'s default
 * measurement policy.
 *
 * @param value           Current text value.
 * @param onValueChange   Called on every character change.
 * @param label           Floating label text.
 * @param modifier        Applied to the text field.
 * @param placeholder     Optional placeholder hint.
 * @param validationError Error message shown below the field when non-null.
 * @param singleLine      When true, disables line breaks.
 * @param minLines        Minimum visible lines (relevant when [singleLine] is false).
 * @param maxLines        Maximum visible lines.
 */
@Composable
fun WFTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    validationError: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        isError = validationError != null,
        supportingText = validationError?.let { err -> { Text(err) } },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        modifier = modifier.fillMaxWidth(),
    )
}
