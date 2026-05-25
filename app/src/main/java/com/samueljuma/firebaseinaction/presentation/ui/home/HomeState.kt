package com.samueljuma.firebaseinaction.presentation.ui.home

import com.samueljuma.firebaseinaction.domain.auth.models.User

data class HomeState(
    val loggedInUser: User? = null
)
