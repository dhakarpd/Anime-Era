package com.github.dhakarpd.animeera.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.github.dhakarpd.animeera.presentation.common.ObserveAsEvents
import com.github.dhakarpd.animeera.presentation.common.SnackbarController
import com.github.dhakarpd.animeera.presentation.common.shimmer
import com.github.dhakarpd.animeera.presentation.navigation.Screen
import com.github.dhakarpd.animeera.presentation.navigation.animeNavGraph
import com.github.dhakarpd.animeera.ui.theme.AnimeEraTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Use a mutable state to hold the deep link URI.
    // This makes it survive recomposition and is the "Compose way" to handle events.
    private val deepLinkUri = mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle the initial intent that started the activity
        val initialIntent = intent
        if (initialIntent?.action == Intent.ACTION_VIEW) {
            deepLinkUri.value = initialIntent.data
        }

        setContent {
            val context = LocalContext.current
            val navController = rememberNavController()
            val snackbarHostState = remember {
                SnackbarHostState()
            }
            val scope = rememberCoroutineScope()
            ObserveAsEvents(
                flow = SnackbarController.events,
                snackbarHostState
            ) { event ->
                scope.launch {
                    // First dismiss any currently showing snackbar
                    snackbarHostState.currentSnackbarData?.dismiss()

                    val result = snackbarHostState.showSnackbar(
                        message = event.message.asString(context),
                        actionLabel = event.action?.name,
                        duration = SnackbarDuration.Short
                    )

                    if(result == SnackbarResult.ActionPerformed) {
                        event.action?.action?.invoke()
                    }
                }
            }

            AnimeEraTheme {
                // Base composable as scaffold because we need a snackbar system
                // which is able to show snackbar even when navigation is happening. To achieve that
                // if base composable was NavHost then to show a snackbar we would have required a
                // scaffold at each screen which would lead to no scaffold being there in hierarchy
                // when navigation is happening. Hence, we use base composable as scaffold.
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        startDestination = Screen.AnimeList.route
                    ) {
                        animeNavGraph(navController)
                    }
                }
            }
            // A side effect that triggers when deepLinkUri changes
            HandleDeepLink(navController = navController)
        }
    }
    /**
     * Command to trigger deep link
     * ./adb shell am start -a android.intent.action.VIEW -d "https://piyush.dhakar.com/anime/8" com.github.dhakarpd.animeera
     * **/
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // This will be called when the activity is singleTop and already running.
        // Update the state with the new URI.
        if (intent?.action == Intent.ACTION_VIEW) {
            deepLinkUri.value = intent.data
        }
    }
    @Composable
    private fun HandleDeepLink(navController: NavController) {
        // Observe the deepLinkUri state
        val uri by remember { deepLinkUri }

        // let block in LaunchedEffect will run when `uri` is not null.
        LaunchedEffect(uri) {
            uri?.let {
                // Example URI: https://piyush.dhakar.com/anime/8
                val pathSegments = it.pathSegments
                if (pathSegments.size == 2 && pathSegments[0] == "anime") {
                    try {
                        val animeId = pathSegments[1].toInt()
                        navController.navigate(Screen.AnimeDetail.createRoute(animeId))
                    } catch (_: Exception){
                        navController.navigate(Screen.AnimeList.route)
                    }
                }
                // Important: Consume the event by setting the URI back to null
                // to prevent re-navigation on configuration change.
                deepLinkUri.value = null
            }
        }
    }
}