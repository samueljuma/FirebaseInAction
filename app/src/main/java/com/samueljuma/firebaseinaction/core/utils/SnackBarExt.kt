package com.samueljuma.firebaseinaction.core.utils

import android.content.Context
import androidx.compose.material3.SnackbarHostState

suspend fun SnackbarHostState.showSnackbar(uiText: UiText, context: Context) {
    showSnackbar(uiText.asString(context))
}