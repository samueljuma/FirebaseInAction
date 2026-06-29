package com.samueljuma.firebaseinaction.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.samueljuma.firebaseinaction.presentation.ui.auth.signin.SignInScreenRoot
import com.samueljuma.firebaseinaction.presentation.ui.auth.signup.SignUpScreenRoot
import com.samueljuma.firebaseinaction.presentation.ui.auth.emailverification.EmailVerificationScreenRoot
import com.samueljuma.firebaseinaction.presentation.ui.createnotes.CreateNoteScreenRoot
import com.samueljuma.firebaseinaction.presentation.ui.home.HomeScreenRoot
import com.samueljuma.firebaseinaction.presentation.ui.notedetails.NoteDetailScreenRoot
import com.samueljuma.firebaseinaction.presentation.ui.notifications.NotificationsScreenRoot
import timber.log.Timber

@Composable
fun AppNavHost(
    isLoggedIn: Boolean,
    isEmailVerified: Boolean
){
    val navController = rememberNavController()

    val startDestination = when {
        !isLoggedIn -> AppScreens.LoginScreen.route
        !isEmailVerified -> AppScreens.EmailVerificationScreen.route
        else -> AppScreens.HomeScreen.route
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
            SignInScreenRoot(
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
        composable(AppScreens.SignUpScreen.route) {
            SignUpScreenRoot(
                onNavigateToHome = {
                    navController.navigate(AppScreens.EmailVerificationScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(AppScreens.LoginScreen.route) {
                        popUpTo(route = AppScreens.SignUpScreen.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = AppScreens.EmailVerificationScreen.route) {
            EmailVerificationScreenRoot(
                onNavigateToHome = {
                    navController.navigate(AppScreens.HomeScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(AppScreens.LoginScreen.route) {
                        popUpTo(0) { inclusive = true }
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
                },
                onNavigateToNotifications = {
                    navController.navigate(AppScreens.NotificationsScreen.route)
                }
            )
        }

        composable(route = AppScreens.NotificationsScreen.route) {
            NotificationsScreenRoot(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(route = AppScreens.CreateNoteScreen.route) {
            CreateNoteScreenRoot(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(
            route = AppScreens.NoteDetailScreen.route,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) {
            NoteDetailScreenRoot(
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}