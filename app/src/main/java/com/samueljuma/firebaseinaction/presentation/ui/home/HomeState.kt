package com.samueljuma.firebaseinaction.presentation.ui.home

import com.samueljuma.firebaseinaction.domain.auth.models.User
import com.samueljuma.firebaseinaction.domain.notes.model.Note

data class HomeState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val loggedInUser: User? = null,
    val unreadCount: Int = 0,
    val welcomeMessage: String = ""
)
