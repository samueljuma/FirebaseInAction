package com.samueljuma.firebaseinaction.presentation.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.samueljuma.firebaseinaction.core.navigation.AppNavHost
import com.samueljuma.firebaseinaction.core.navigation.AppScreens
import com.samueljuma.firebaseinaction.core.notifications.NotificationDeepLinks
import com.samueljuma.core.domain.notifications.FcmPayloadKind
import com.samueljuma.core.presentation.designsystem.AppTheme
import com.samueljuma.core.presentation.ui.components.InAppNotificationBanner
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.state.isCheckingAuth
            }
        }
        enableEdgeToEdge()
        setContent {
            val state = viewModel.state
            AppTheme {
                if (!state.isCheckingAuth) {
                    val navController = rememberNavController()
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppNavHost(
                            isLoggedIn = state.isLoggedIn,
                            isEmailVerified = state.isEmailVerified,
                            navController = navController
                        )

                        AnimatedVisibility(
                            visible = state.activeNotification != null,
                            enter = slideInVertically { -it } + fadeIn(),
                            exit = slideOutVertically { -it } + fadeOut(),
                            modifier = Modifier.align(Alignment.TopCenter)
                        ) {
                            state.activeNotification?.let { notification ->
                                InAppNotificationBanner(
                                    notification = notification,
                                    onClick = {
                                        viewModel.onDismissNotification()
                                        if (notification.kind == FcmPayloadKind.DISPLAY) {
                                            // A DISPLAY notification (e.g. a Console campaign) was
                                            // never persisted to the inbox, so the notifications-
                                            // screen fallback below would try to mark a
                                            // never-created Firestore doc as read and fail. Land on
                                            // Home instead — a real destination, no phantom write.
                                            navController.navigate(AppScreens.HomeScreen.route)
                                        } else {
                                            // Resolve against the nav graph's registered navDeepLinks
                                            // — the same mechanism a tapped system notification uses.
                                            // Reminders carry their own deepLink (opens the note);
                                            // anything else falls back to the notifications inbox.
                                            val uri = notification.deepLink
                                                ?: NotificationDeepLinks.uri(notification.id)
                                            navController.navigate(uri.toUri())
                                        }
                                    },
                                    onDismiss = { viewModel.onDismissNotification() }
                                )
                            }
                        }

                        if (state.sessionExpired) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)
                                    )
                            )
                            SessionExpiredDialog(
                                onReLogin = { viewModel.onReLoginClicked() },
                                onDismiss = { viewModel.onSessionExpiredDismissed() }
                            )
                        }
                    }
                }
            }
        }
    }
}