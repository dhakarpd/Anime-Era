package com.github.dhakarpd.animeera.presentation.common

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * .
 * UiText Utility: Created a UiText sealed class that allows ViewModels to return either a
 * hardcoded string or a resource ID, ensuring clean separation of concerns.
 *
 * This utility will allow for passing either literal strings or string resource IDs with
 * optional arguments, and provide a asString() extension for easy resolution in Composables.
 * **/
sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ) : UiText()

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
        }
    }

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }
}
