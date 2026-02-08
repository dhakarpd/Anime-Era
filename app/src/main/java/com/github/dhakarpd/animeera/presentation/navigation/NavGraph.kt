package com.github.dhakarpd.animeera.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.github.dhakarpd.animeera.presentation.animeDetail.AnimeDetailScreen
import com.github.dhakarpd.animeera.presentation.animeList.AnimeListScreen

fun NavGraphBuilder.animeNavGraph(
    navController: NavController
) {
    composable(Screen.AnimeList.route) {
        AnimeListScreen(
            onAnimeClick = { animeId ->
                navController.navigate(
                    Screen.AnimeDetail.createRoute(animeId)
                )
            }
        )
    }

    composable(
        route = Screen.AnimeDetail.route,
        arguments = listOf(
            navArgument("animeId") {
                type = NavType.IntType
                defaultValue = -1
            }
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "https://piyush.dhakar.com/{animeId}"
                action = android.content.Intent.ACTION_VIEW
            }
        )
    ) { backStackEntry ->
        val animeId = backStackEntry.arguments?.getInt("animeId") ?: -1
        AnimeDetailScreen(animeId)
    }
}
