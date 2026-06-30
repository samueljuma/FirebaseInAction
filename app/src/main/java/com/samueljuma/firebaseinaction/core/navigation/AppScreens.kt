package com.samueljuma.firebaseinaction.core.navigation

sealed class AppScreens(val route: String) {
    object LoginScreen : AppScreens("login_screen")
    object SignUpScreen : AppScreens("signup_screen")
    object HomeScreen : AppScreens("home_screen")
    object CreateNoteScreen : AppScreens("create_note_screen")
    object NoteDetailScreen : AppScreens("note_detail_screen/{noteId}") {
        fun createRoute(noteId: String) = "note_detail_screen/$noteId"
    }
    object EmailVerificationScreen : AppScreens("email_verification_screen")
    object NotificationsScreen : AppScreens("notifications_screen?notificationId={notificationId}") {
        fun createRoute(notificationId: String) = "notifications_screen?notificationId=$notificationId"
    }
}