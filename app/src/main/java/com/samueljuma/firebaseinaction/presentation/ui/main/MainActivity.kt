package com.samueljuma.firebaseinaction.presentation.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.samueljuma.firebaseinaction.core.navigation.AppNavHost
import com.samueljuma.firebaseinaction.presentation.designsystem.AppTheme
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
                // Don't render anything until auth check completes
                // This prevents a flash of the login screen for logged-in users
                if (!viewModel.state.isCheckingAuth) {
                    AppNavHost(isLoggedIn = viewModel.state.isLoggedIn)
                }
            }
        }
    }
}