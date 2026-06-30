package com.samueljuma.firebaseinaction.presentation.ui.notifications

import android.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samueljuma.firebaseinaction.core.utils.ObserveAsEvents
import com.samueljuma.firebaseinaction.core.utils.toFullTimestampString
import com.samueljuma.firebaseinaction.domain.notifications.model.AppNotification
import com.samueljuma.firebaseinaction.presentation.designsystem.AppTheme
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.days

private val tabs = NotificationTab.entries

@Composable
fun NotificationsScreenRoot(
    onNavigateBack: () -> Unit,
    viewModel: NotificationsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            NotificationsEvent.NavigateBack -> onNavigateBack()
        }
    }

    // Flush the tapped notifications to "read" once the user leaves the screen.
    DisposableEffect(Unit) {
        onDispose { viewModel.flushPendingReads() }
    }

    NotificationsScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsScreen(
    state: NotificationsState,
    onAction: (NotificationsAction) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = state.selectedTab.ordinal) { tabs.size }

    // Keep the TabRow and pager in sync in both directions.
    LaunchedEffect(state.selectedTab) {
        if (pagerState.currentPage != state.selectedTab.ordinal) {
            pagerState.animateScrollToPage(state.selectedTab.ordinal)
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        val tab = tabs[pagerState.currentPage]
        if (tab != state.selectedTab) {
            onAction(NotificationsAction.OnTabSelected(tab))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NotificationsHeader(
            selectedTab = state.selectedTab,
            onTabSelected = { onAction(NotificationsAction.OnTabSelected(it)) },
            onBack = { onAction(NotificationsAction.OnBackClicked) }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val tab = tabs[page]
            NotificationsPage(
                isLoading = state.isLoading,
                notifications = state.notificationsFor(tab),
                pendingReadIds = state.pendingReadIds,
                emptyMessage = tab.emptyMessage,
                onAction = onAction
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsHeader(
    selectedTab: NotificationTab,
    onTabSelected: (NotificationTab) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CircularIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBack
            )
            CircularIconButton(
                icon = Icons.Default.Close,
                contentDescription = "Close",
                onClick = onBack
            )
        }

        Text(
            text = "Notifications",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            modifier = Modifier.padding(start = 20.dp, top = 6.dp, bottom = 6.dp)
        )

        PrimaryTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            tabs.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Text(
                            text = tab.title,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun CircularIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.size(40.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun NotificationsPage(
    isLoading: Boolean,
    notifications: List<AppNotification>,
    pendingReadIds: Set<String>,
    emptyMessage: String,
    onAction: (NotificationsAction) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            notifications.isEmpty() -> {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = notifications,
                        key = { it.id }
                    ) { notification ->
                        NotificationCard(
                            notification = notification,
                            isRead = notification.read || notification.id in pendingReadIds,
                            onClick = {
                                onAction(NotificationsAction.OnNotificationClicked(notification.id))
                            },
                            onDelete = {
                                onAction(NotificationsAction.OnDeleteNotification(notification.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: AppNotification,
    isRead: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 20.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Dismiss (delete) button pinned to the top-end corner.
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp)
            ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete notification",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 48.dp, bottom = 16.dp)
            ) {
                Text(
                    text = notification.receivedAt.toFullTimestampString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isRead) FontWeight.Medium else FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun NotificationsScreenPreview() {
    AppTheme {
        NotificationsScreen(
            state = NotificationsState(
                isLoading = false,
                notifications = listOf(
                    AppNotification(
                        id = "1",
                        title = "Welcome to Notey",
                        body = "Hurray !!! You are officially onboarded on Notey. \n" +
                                "Have fun creating notes",
                        receivedAt = System.currentTimeMillis(),
                        read = false
                    ),
                    AppNotification(
                        id = "2",
                        title = "Inactivity",
                        body = "You have been inactive for a while. Any concerns we should know? ",
                        receivedAt = System.currentTimeMillis(),
                        read = true
                    )
                )
            ),
            onAction = {}
        )
    }
}
