@file:OptIn(ExperimentalMaterial3Api::class)

package com.samueljuma.firebaseinaction.presentation.ui.createnotes

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samueljuma.firebaseinaction.core.utils.ObserveAsEvents
import com.samueljuma.core.presentation.designsystem.AppTheme
import com.samueljuma.feature.notes.presentation.components.NoteImageSection
import com.samueljuma.firebaseinaction.presentation.ui.common.CancelUploadDialog
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateNoteScreenRoot(
    viewModel: CreateNoteViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onAction(CreateNoteAction.OnImageSelected(it)) }
    }

    BackHandler {
        viewModel.onAction(CreateNoteAction.OnBackClicked)
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            CreateNoteEvent.NavigateBack -> onNavigateBack()
            is CreateNoteEvent.ShowSnackbar -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(event.message.asString(context))
                }
            }
            CreateNoteEvent.LaunchImagePicker -> {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        }
    }

    if (state.showCancelUploadDialog) {
        CancelUploadDialog(
            onConfirm = { viewModel.onAction(CreateNoteAction.OnCancelUploadConfirmed) },
            onDismiss = { viewModel.onAction(CreateNoteAction.OnCancelUploadDismissed) }
        )
    }

    CreateNoteScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction
    )
}

@Composable
private fun CreateNoteScreen(
    state: CreateNoteState,
    snackbarHostState: SnackbarHostState,
    onAction: (CreateNoteAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Note") },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(CreateNoteAction.OnBackClicked) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        TextButton(
                            onClick = { onAction(CreateNoteAction.OnSaveClicked) },
                            enabled = state.title.isNotBlank() && !state.isUploadingImage
                        ) {
                            Text("Save")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            NoteImageSection(
                imageUrl = state.imageUrl,
                isEditing = true,
                isUploadingImage = state.isUploadingImage,
                uploadProgress = state.uploadProgress,
                onAddImageClicked = { onAction(CreateNoteAction.OnImageClicked) },
                onRemoveImageClicked = { onAction(CreateNoteAction.OnRemoveImageClicked) }
            )

            BasicTextField(
                value = state.title,
                onValueChange = { onAction(CreateNoteAction.OnTitleChanged(it)) },
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
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

            BasicTextField(
                value = state.content,
                onValueChange = { onAction(CreateNoteAction.OnContentChanged(it)) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (state.content.isEmpty()) {
                            Text(
                                text = "Start writing...",
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
}

@PreviewLightDark
@Composable
private fun CreateNoteScreenPreview() {
    AppTheme {
        CreateNoteScreen(
            state = CreateNoteState(),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {}
        )
    }
}
