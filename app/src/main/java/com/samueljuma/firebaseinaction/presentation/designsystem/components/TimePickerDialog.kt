package com.samueljuma.firebaseinaction.presentation.designsystem.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.samueljuma.firebaseinaction.presentation.designsystem.AppTheme

/**
 * Material 3 ships [androidx.compose.material3.DatePickerDialog] but, as of this Compose BOM,
 * no equivalent for [androidx.compose.material3.TimePicker]. This wraps one in a plain
 * [AlertDialog], per Google's own recommended pattern for the built-in TimePicker composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancel") }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("OK") }
        },
        text = { content() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun TimePickerDialogPreview() {
    AppTheme {
        TimePickerDialog(
            onDismissRequest = {},
            onConfirm = {}
        ) {
            TimePicker(state = rememberTimePickerState())
        }
    }
}
