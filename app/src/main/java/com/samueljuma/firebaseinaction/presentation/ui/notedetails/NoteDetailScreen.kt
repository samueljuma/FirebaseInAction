@file:OptIn(ExperimentalMaterial3Api::class)

package com.samueljuma.firebaseinaction.presentation.ui.notedetails

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.samueljuma.firebaseinaction.R
import com.samueljuma.firebaseinaction.core.utils.ObserveAsEvents
import com.samueljuma.firebaseinaction.presentation.designsystem.AppTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

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
        // ── Image Section ────────────────────────────────────
        NoteImageSection(
            imageUrl = state.imageUrl,
            isEditing = state.isEditing,
            isUploadingImage = state.isUploadingImage,
            uploadProgress = state.uploadProgress,
            onAddImageClicked = { onAction(NoteDetailAction.OnImageClicked) },
            onRemoveImageClicked = { onAction(NoteDetailAction.OnRemoveImageClicked) }
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

@Composable
private fun NoteImageSection(
    imageUrl: String?,
    isEditing: Boolean,
    isUploadingImage: Boolean,
    uploadProgress: Int?,
    onAddImageClicked: () -> Unit,
    onRemoveImageClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        when {
            // Uploading — show progress
            isUploadingImage -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { (uploadProgress ?: 0) / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Uploading ${uploadProgress ?: 0}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Image exists — show it
            imageUrl != null -> {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Note image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .diskCachePolicy(CachePolicy.ENABLED)   // ← key for offline
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        placeholder = painterResource(R.drawable.ic_image_placeholder),
                        error = painterResource(R.drawable.ic_image_error),
                        contentDescription = "Note image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    // Remove button — only visible in edit mode
                    if (isEditing) {
                        IconButton(
                            onClick = onRemoveImageClicked,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface
                                        .copy(alpha = 0.7f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove image",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // No image — show add button only in edit mode
            isEditing -> {
                OutlinedButton(
                    onClick = onAddImageClicked,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add cover image")
                }
            }
        }
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