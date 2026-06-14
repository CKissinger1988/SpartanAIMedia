package com.spartanai.spartanaimedia.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.spartanai.spartanaimedia.ui.media.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Downloads : Screen("downloads")
    object Profiles : Screen("profiles")
    object Player : Screen("player/{mediaUrl}/{title}") {
        fun createRoute(mediaUrl: String, title: String): String {
            val encodedUrl = URLEncoder.encode(mediaUrl, StandardCharsets.UTF_8.toString())
            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
            return "player/$encodedUrl/$encodedTitle"
        }
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: MediaViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onMediaClick = { item ->
                    navController.navigate(Screen.Player.createRoute(item.mediaUrl, item.title))
                },
                onDownloadsClick = { navController.navigate(Screen.Downloads.route) },
                onProfilesClick = { navController.navigate(Screen.Profiles.route) }
            )
        }
        composable(Screen.Downloads.route) {
            DownloadsScreen(
                viewModel = viewModel,
                onMediaClick = { item ->
                    navController.navigate(Screen.Player.createRoute(item.mediaUrl, item.title))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Profiles.route) {
            ProfilesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument("mediaUrl") { type = androidx.navigation.NavType.StringType },
                navArgument("title") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val mediaUrl = backStackEntry.arguments?.getString("mediaUrl") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: ""
            PlayerScreen(
                mediaUrl = mediaUrl,
                title = title,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
