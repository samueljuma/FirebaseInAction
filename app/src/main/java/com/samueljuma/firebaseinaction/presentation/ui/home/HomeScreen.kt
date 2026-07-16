@file:OptIn(ExperimentalMaterial3Api::class)

package com.samueljuma.firebaseinaction.presentation.ui.home

import HomeAction
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samueljuma.firebaseinaction.core.notifications.NotificationPermissionRequester
import com.samueljuma.firebaseinaction.core.utils.ObserveAsEvents
import com.samueljuma.core.presentation.ui.utils.toRelativeTimeString
import com.samueljuma.firebaseinaction.domain.notes.model.Note
import com.samueljuma.core.presentation.designsystem.AppTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
@Composable
fun HomeScreenRoot(
    viewModel: HomeViewModel = koinViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToCreateNote: () -> Unit,
    onNavigateToNoteDetail: (String) -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    // Request Notifications Permission if need be
    NotificationPermissionRequester()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            HomeEvent.NavigateToLogin -> onNavigateToLogin()
            HomeEvent.NavigateToCreateNote -> onNavigateToCreateNote()
            is HomeEvent.NavigateToNoteDetail ->
                onNavigateToNoteDetail(event.noteId)
            is HomeEvent.ShowSnackbar -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        event.message.asString(context)
                    )
                }
            }
            HomeEvent.NavigateToNotifications -> onNavigateToNotifications()
        }
    }

    HomeScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction
    )
}

@Composable
private fun HomeScreen(
    state: HomeState,
    snackbarHostState: SnackbarHostState,
    onAction: (HomeAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "My Notes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        state.loggedInUser?.email?.let { email ->
                            Text(
                                text = email,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (state.unreadCount > 0) {
                                Badge {
                                    Text(
                                        text = if (state.unreadCount > 99) "99+"
                                               else state.unreadCount.toString()
                                    )
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = { onAction(HomeAction.OnNotificationsClicked) }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    }
                    IconButton(
                        onClick = { onAction(HomeAction.OnSignOutClicked) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.Logout,
                            contentDescription = "Sign out"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAction(HomeAction.OnCreateNoteClicked) }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create note"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.welcomeMessage.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = state.welcomeMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    state.notes.isEmpty() -> {
                        EmptyNotesContent(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        NotesList(
                            notes = state.notes,
                            onAction = onAction
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyNotesContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.NoteAdd,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Text(
            text = "No notes yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Tap + to create your first note",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NotesList(
    notes: List<Note>,
    onAction: (HomeAction) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            items = notes,
            key = { note -> note.id }  // stable keys — prevents unnecessary recomposition
        ) { note ->
            NoteCard(
                note = note,
                onNoteClicked = { onAction(HomeAction.OnNoteClicked(note.id)) },
                onDeleteClicked = { onAction(HomeAction.OnDeleteNote(note)) },
                onPinClicked = { onAction(HomeAction.OnPinNote(note)) }
            )
        }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onNoteClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onPinClicked: () -> Unit
) {
    Card(
        onClick = onNoteClicked,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (note.pinned) 4.dp else 1.dp
        ),
        border = if (note.pinned)
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = note.title.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    // Sync indicator
                    if (!note.synced) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Not synced",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                .copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    IconButton(
                        onClick = onPinClicked,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (note.pinned)
                                Icons.Default.PushPin
                            else
                                Icons.Outlined.PushPin,
                            contentDescription = if (note.pinned)
                                "Unpin note"
                            else
                                "Pin note",
                            modifier = Modifier.size(16.dp),
                            tint = if (note.pinned)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDeleteClicked,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete note",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Timestamp + sync status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.updatedAt.toRelativeTimeString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                        .copy(alpha = 0.7f)
                )
                if (note.pinned) {
                    Text(
                        text = "Pinned",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenPreview() {
    AppTheme {
        HomeScreen(
            state = HomeState(
                isLoading = false,
                welcomeMessage = "Welcome to Notey!",
                notes = listOf(
                    Note(
                        id = "1",
                        userId = "user1",
                        title = "Meeting Notes",
                        content = "Discuss Q3 roadmap with the team",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        pinned = true,
                        synced = true
                    ),
                    Note(
                        id = "2",
                        userId = "user1",
                        title = "Shopping List",
                        content = "Milk, eggs, bread",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        pinned = false,
                        synced = false
                    )
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {}
        )
    }
}