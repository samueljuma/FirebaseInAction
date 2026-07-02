@file:OptIn(ExperimentalMaterial3Api::class)

package com.samueljuma.firebaseinaction.presentation.ui.notedetails

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samueljuma.firebaseinaction.core.utils.ObserveAsEvents
import com.samueljuma.firebaseinaction.presentation.designsystem.AppTheme
import com.samueljuma.firebaseinaction.presentation.designsystem.components.NoteImageSection
import com.samueljuma.firebaseinaction.presentation.designsystem.components.ReminderSection
import com.samueljuma.firebaseinaction.presentation.designsystem.components.TimePickerDialog
import com.samueljuma.firebaseinaction.presentation.ui.common.CancelUploadDialog
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber
import java.util.Calendar
import java.util.TimeZone

@Composable
fun NoteDetailScreenRoot(
    viewModel: NoteDetailViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()


    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            // Take persistent permission BEFORE passing URI anywhere
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Some URIs don't support persistable permissions
                // The URI is still valid for the current session
                Timber.tag("ImagePicker").w(e, "Could not take persistable URI permission")
            }
            viewModel.onAction(NoteDetailAction.OnImageSelected(it))
        }
    }

    BackHandler {
        viewModel.onAction(NoteDetailAction.OnBackClicked)
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            NoteDetailEvent.NavigateBack -> onNavigateBack()
            is NoteDetailEvent.ShowSnackbar -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        event.message.asString(context)
                    )
                }
            }

            NoteDetailEvent.LaunchImagePicker -> {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        }
    }

    if (state.showCancelUploadDialog) {
        CancelUploadDialog(
            onConfirm = { viewModel.onAction(NoteDetailAction.OnCancelUploadConfirmed) },
            onDismiss = { viewModel.onAction(NoteDetailAction.OnCancelUploadDismissed) }
        )
    }

    // Two-step reminder picker: date first, then time. Driven entirely by ViewModel state
    // (not composable-local `remember`) so it survives activity recreation — e.g. rotating
    // mid-pick doesn't silently dismiss the dialog. The UTC-midnight/local-time combination
    // math lives in the ViewModel; this Composable only reports what the user picked.
    if (state.showDatePicker) {
        ReminderDatePickerDialog(
            initialSelectedDateMillis = state.reminderAt ?: System.currentTimeMillis(),
            onDateSelected = { viewModel.onAction(NoteDetailAction.OnReminderDateSelected(it)) },
            onDismissRequest = { viewModel.onAction(NoteDetailAction.OnDatePickerDismissed) }
        )
    }

    if (state.showTimePicker) {
        val timePickerState = rememberTimePickerState()
        TimePickerDialog(
            onDismissRequest = { viewModel.onAction(NoteDetailAction.OnTimePickerDismissed) },
            onConfirm = {
                viewModel.onAction(
                    NoteDetailAction.OnReminderTimeSelected(
                        hour = timePickerState.hour,
                        minute = timePickerState.minute
                    )
                )
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    NoteDetailScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction
    )
}

@Composable
private fun ReminderDatePickerDialog(
    initialSelectedDateMillis: Long,
    onDateSelected: (utcDateMillis: Long) -> Unit,
    onDismissRequest: () -> Unit
) {
    val todayUtcMidnight = remember {
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis,
        selectableDates = object : SelectableDates by DatePickerDefaults.AllDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                utcTimeMillis >= todayUtcMidnight
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let(onDateSelected) ?: onDismissRequest()
            }) { Text("Next") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancel") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@PreviewLightDark
@Composable
private fun ReminderDatePickerDialogPreview() {
    AppTheme {
        ReminderDatePickerDialog(
            initialSelectedDateMillis = System.currentTimeMillis(),
            onDateSelected = {},
            onDismissRequest = {}
        )
    }
}

@Composable
private fun NoteDetailScreen(
    state: NoteDetailState,
    snackbarHostState: SnackbarHostState,
    onAction: (NoteDetailAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!state.synced) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Not synced",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    .copy(alpha = 0.5f)
                            )
                        }
                        Text(
                            text = if (state.isEditing) "Editing" else "Note",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(NoteDetailAction.OnBackClicked) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Pin button
                    IconButton(
                        onClick = { onAction(NoteDetailAction.OnPinClicked) }
                    ) {
                        Icon(
                            imageVector = if (state.pinned)
                                Icons.Default.PushPin
                            else
                                Icons.Outlined.PushPin,
                            contentDescription = if (state.pinned)
                                "Unpin" else "Pin",
                            tint = if (state.pinned)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = { onAction(NoteDetailAction.OnDeleteClicked) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    // Edit / Save button
                    if (state.isEditing) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            TextButton(
                                onClick = { onAction(NoteDetailAction.OnSaveClicked) }
                            ) {
                                Text("Save")
                            }
                        }
                    } else {
                        IconButton(
                            onClick = { onAction(NoteDetailAction.OnEditClicked) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                NoteDetailContent(
                    state = state,
                    onAction = onAction,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}
@Composable
private fun NoteDetailContent(
    state: NoteDetailState,
    onAction: (NoteDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Image Section ────────────────────────────────────
        NoteImageSection(
            imageUrl = state.imageUrl,
            isEditing = state.isEditing,
            isUploadingImage = state.isUploadingImage,
            uploadProgress = state.uploadProgress,
            onAddImageClicked = { onAction(NoteDetailAction.OnImageClicked) },
            onRemoveImageClicked = { onAction(NoteDetailAction.OnRemoveImageClicked) }
        )

        // ── Reminder ─────────────────────────────────────────
        ReminderSection(
            reminderAt = state.reminderAt,
            isEditing = state.isEditing,
            onSetReminderClicked = { onAction(NoteDetailAction.OnSetReminderClicked) },
            onClearReminderClicked = { onAction(NoteDetailAction.OnClearReminderClicked) }
        )

        // ── Title ────────────────────────────────────────────
        BasicTextField(
            value = state.title,
            onValueChange = { onAction(NoteDetailAction.OnTitleChanged(it)) },
            enabled = state.isEditing,
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            ),
            decorationBox = { innerTextField ->
                Box {
                    if (state.title.isEmpty()) {
                        Text(
                            text = "Title",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ── Content ──────────────────────────────────────────
        BasicTextField(
            value = state.content,
            onValueChange = { onAction(NoteDetailAction.OnContentChanged(it)) },
            enabled = state.isEditing,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            decorationBox = { innerTextField ->
                Box {
                    if (state.content.isEmpty()) {
                        Text(
                            text = if (state.isEditing) "Start writing..." else "No content",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun NoteDetailScreenPreview() {
    AppTheme {
        NoteDetailScreen(
            state = NoteDetailState(),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {}
        ) 
    }
    
}