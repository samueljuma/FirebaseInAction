package com.samueljuma.firebaseinaction.presentation.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.samueljuma.firebaseinaction.presentation.designsystem.AppTheme
import com.samueljuma.firebaseinaction.presentation.designsystem.components.AppAlertDialog

@Composable
fun SessionExpiredDialog(
    onReLogin: () -> Unit,
    onDismiss: () -> Unit
) {
    AppAlertDialog(
        onDismissRequest = { /* intentionally empty — non-dismissable */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        SessionExpiredDialogContent(onReLogin = onReLogin, onDismiss = onDismiss)
    }
}

@Composable
private fun SessionExpiredDialogContent(
    onReLogin: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LockClock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )

        Text(
            text = "Session Expired",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Your session has expired for security reasons. Please sign in again to continue.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Dismiss")
            }

            Button(
                onClick = onReLogin,
                modifier = Modifier.weight(1f)
            ) {
                Text("Re-Login")
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SessionExpiredDialogPreview() {
    AppTheme {
        SessionExpiredDialogContent(onReLogin = {}, onDismiss = {})
    }
}
