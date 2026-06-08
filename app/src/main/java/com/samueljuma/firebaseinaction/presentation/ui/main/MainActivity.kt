package com.samueljuma.firebaseinaction.presentation.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.samueljuma.firebaseinaction.core.navigation.AppNavHost
import com.samueljuma.firebaseinaction.presentation.designsystem.AppTheme
import com.samueljuma.firebaseinaction.presentation.ui.main.SessionExpiredDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel by viewModel<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep splash screen visible while checking auth
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.state.isCheckingAuth
            }
        }
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val state = viewModel.state
                if(!state.isCheckingAuth){
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppNavHost(
                            isLoggedIn = state.isLoggedIn,
                            isEmailVerified = state.isEmailVerified
                        )

                        // Overlays on top of everything — non-dismissable
                        if (state.sessionExpired) {
                            // Scrim behind dialog
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f))
                            )

                            SessionExpiredDialog(
                                onReLogin = {
                                    viewModel.onReLoginClicked()
                                },
                                onDismiss = {
                                    viewModel.onSessionExpiredDismissed()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}