package com.samueljuma.firebaseinaction.presentation.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.samueljuma.firebaseinaction.core.utils.toFullTimestampString
import com.samueljuma.firebaseinaction.presentation.designsystem.AppTheme

/**
 * Reminder row for the note detail screen. Hidden entirely in read-only mode when no reminder
 * is set — a note without a reminder shouldn't advertise the feature to a viewer, only an editor.
 */
@Composable
fun ReminderSection(
    reminderAt: Long?,
    isEditing: Boolean,
    onSetReminderClicked: () -> Unit,
    onClearReminderClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (reminderAt == null && !isEditing) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .let { if (isEditing) it.clickable(onClick = onSetReminderClicked) else it }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (reminderAt != null) Icons.Default.Alarm else Icons.Default.AlarmAdd,
            contentDescription = null,
            tint = if (reminderAt != null)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = reminderAt?.toFullTimestampString() ?: "Add reminder",
            style = MaterialTheme.typography.bodyMedium,
            color = if (reminderAt != null)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        if (isEditing && reminderAt != null) {
            IconButton(
                onClick = onClearReminderClicked,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear reminder",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ReminderSectionEditingUnsetPreview() {
    AppTheme {
        ReminderSection(
            reminderAt = null,
            isEditing = true,
            onSetReminderClicked = {},
            onClearReminderClicked = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun ReminderSectionEditingSetPreview() {
    AppTheme {
        ReminderSection(
            reminderAt = System.currentTimeMillis() + 3_600_000,
            isEditing = true,
            onSetReminderClicked = {},
            onClearReminderClicked = {}
        )
    }
}
