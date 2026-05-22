package com.samueljuma.firebaseinaction.core.utils

import android.content.Context
import androidx.annotation.StringRes

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    data class StringResource(
        @StringRes val resId: Int,
        val args: Array<Any> = emptyArray()
    ) : UiText

    fun asString(context: Context): String = when (this) {
        is DynamicString -> value
        is StringResource -> context.getString(resId, *args)
    }
}