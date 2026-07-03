package com.samueljuma.firebaseinaction.core.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    class StringResource(
        @StringRes val id: Int,
        vararg val args: Any
    ) : UiText {
        override fun equals(other: Any?): Boolean =
            other is StringResource && id == other.id && args.contentEquals(other.args)
        override fun hashCode(): Int = 31 * id.hashCode() + args.contentHashCode()
    }

    @Composable
    fun asString(): String = when (this) {
        is DynamicString  -> value
        is StringResource -> stringResource(id, *args)
    }

    fun asString(context: Context): String = when (this) {
        is DynamicString  -> value
        is StringResource -> context.getString(id, *args)
    }
}