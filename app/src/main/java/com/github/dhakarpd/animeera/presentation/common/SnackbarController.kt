package com.github.dhakarpd.animeera.presentation.common

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

data class SnackbarEvent(
    val message: String,
    val action: SnackbarAction? = null
)

data class SnackbarAction(
    val name: String,
    val action: suspend () -> Unit
)

/**
 * Below class helps create a global snackbar system that allows to show snackbar from anywhere
 * be it composable or viewmodel(recommended to call from presentation layer). It allows snackbar
 * to be there even when navigation is happening
 * **/
object SnackbarController {
    private val _events = Channel<SnackbarEvent>()
    val events = _events.receiveAsFlow()

    suspend fun sendEvent(event: SnackbarEvent) {
        // This will suspend until a receiver is not there. This is the by-default rendezvous (a
        // meeting at an agreed time and place) behaviour of channel
        _events.send(event)
    }
}