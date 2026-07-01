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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.samueljuma.firebaseinaction.core.navigation.AppNavHost
import com.samueljuma.firebaseinaction.core.navigation.AppScreens
import com.samueljuma.firebaseinaction.presentation.designsystem.AppTheme
import com.samueljuma.firebaseinaction.presentation.designsystem.components.InAppNotificationBanner
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
                                        navController.navigate(
                                            AppScreens.NotificationsScreen.createRoute(notification.id)
                                        )
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