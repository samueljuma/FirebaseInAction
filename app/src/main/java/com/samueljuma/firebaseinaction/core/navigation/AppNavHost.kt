package com.samueljuma.firebaseinaction.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.samueljuma.firebaseinaction.presentation.ui.auth.AuthViewModel
import com.samueljuma.firebaseinaction.presentation.ui.auth.LoginScreenRot
import com.samueljuma.firebaseinaction.presentation.ui.auth.SignUpScreenRoot
import com.samueljuma.firebaseinaction.presentation.ui.home.HomeScreenRoot
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Composable
fun AppNavHost(
    isLoggedIn: Boolean = false
){
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = koinViewModel()

    val startDestination = if (isLoggedIn) {
        AppScreens.HomeScreen.route
    } else {
        AppScreens.LoginScreen.route
    }

    // AppNavHost observes isLoggedIn state reactively
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            navController.navigate(AppScreens.LoginScreen.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        startDestination = startDestination,
        navController = navController
    ) { 
        composable(route = AppScreens.LoginScreen.route) {
            LoginScreenRot(
                viewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(AppScreens.HomeScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(route = AppScreens.SignUpScreen.route){
                        popUpTo(AppScreens.LoginScreen.route){ inclusive = true }
                    }
                }
            )
        }
        composable(route = AppScreens.SignUpScreen.route) {
            SignUpScreenRoot(
                viewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(AppScreens.HomeScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(route = AppScreens.LoginScreen.route){
                        popUpTo(AppScreens.SignUpScreen.route){ inclusive = true }
                    }
                }
            )
        }

        composable(route = AppScreens.HomeScreen.route) {
            HomeScreenRoot(
                onNavigateToLogin = {
                    navController.navigate(AppScreens.LoginScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToCreateNote = {
                    navController.navigate(AppScreens.CreateNoteScreen.route)
                },
                onNavigateToNoteDetail = { noteId ->
                    navController.navigate(
                        AppScreens.NoteDetailScreen.route.replace("{noteId}", noteId)
                    )
                }
            )
        }
    }
}