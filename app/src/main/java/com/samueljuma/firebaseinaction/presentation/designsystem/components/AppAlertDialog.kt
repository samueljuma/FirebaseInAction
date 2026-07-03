package com.samueljuma.firebaseinaction.presentation.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.samueljuma.firebaseinaction.presentation.designsystem.AppTheme

@Composable
fun AppAlertDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        AppAlertDialogContent(
            content = content
        )
    }
}

@Composable
private fun AppAlertDialogContent(
    content: @Composable () -> Unit
){
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        content()
    }
}

@PreviewLightDark
@Composable
private fun AppAlertDialogPreview() {
    AppTheme {
        AppAlertDialogContent {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ){
                Text(
                    text = "Dialog Title",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "This is where feature-specific dialog content goes.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

        }
    }
}