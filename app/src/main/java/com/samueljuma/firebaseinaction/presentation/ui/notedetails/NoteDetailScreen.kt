@file:OptIn(ExperimentalMaterial3Api::class)

package com.samueljuma.firebaseinaction.presentation.ui.notedetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samueljuma.firebaseinaction.core.utils.ObserveAsEvents
import com.samueljuma.firebaseinaction.presentation.designsystem.AppTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun NoteDetailScreenRoot(
    viewModel: NoteDetailViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

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
        }
    }

    NoteDetailScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction
    )
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
                        if (!state.isSynced) {
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
                            imageVector = if (state.isPinned)
                                Icons.Default.PushPin
                            else
                                Icons.Outlined.PushPin,
                            contentDescription = if (state.isPinned)
                                "Unpin" else "Pin",
                            tint = if (state.isPinned)
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
    ) {
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
                            color = MaterialTheme.colorScheme.onSurface
                                .copy(alpha = 0.3f),
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
            onValueChange = { onAction(NoteDetailAction.OnContentChanged(it)) },
            enabled = state.isEditing,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            decorationBox = { innerTextField ->
                Box {
                    if (state.content.isEmpty()) {
                        Text(
                            text = if (state.isEditing)
                                "Start writing..."
                            else
                                "No content",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                                .copy(alpha = 0.3f)
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