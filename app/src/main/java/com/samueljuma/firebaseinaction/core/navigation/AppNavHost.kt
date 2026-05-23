package com.samueljuma.firebaseinaction.core.navigation

import androidx.compose.runtime.Composable
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
fun AppNavHost(){
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = koinViewModel()

    NavHost(
        startDestination = AppScreens.LoginScreen.route,
        navController = navController
    ) { 
        composable(route = AppScreens.LoginScreen.route) {
            LoginScreenRot(
                viewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(AppScreens.HomeScreen.route){
                        popUpTo(AppScreens.LoginScreen.route){ inclusive = true }
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
                    Timber.tag("JAYYY").i("We are here")
                    navController.navigate(AppScreens.HomeScreen.route){
                        popUpTo(AppScreens.SignUpScreen.route){ inclusive = true }
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
                onLogout = {
                    navController.navigate(AppScreens.LoginScreen.route){
                        popUpTo(AppScreens.HomeScreen.route){ inclusive = true }
                    }
                }
            )
        }
    }
}