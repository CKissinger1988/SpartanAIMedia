package com.spartanai.spartanaimedia.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.spartanai.spartanaimedia.ui.media.*
import org.koin.androidx.compose.koinViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = "onboarding"
) {
    val viewModel: MediaViewModel = koinViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("onboarding") {
            OnboardingScreen(
                viewModel = viewModel,
                onComplete = {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onMediaClick = { item ->
                    navController.navigate("player/${item.mediaUrl}/${item.title}")
                },
                onDownloadsClick = {
                    navController.navigate("downloads")
                },
                onProfilesClick = {
                    navController.navigate("profiles")
                }
            )
        }

        composable("player/{url}/{title}") { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: ""
            val decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.toString())
            val decodedTitle = URLDecoder.decode(title, StandardCharsets.UTF_8.toString())
            
            PlayerScreen(
                viewModel = viewModel,
                mediaUrl = decodedUrl,
                title = decodedTitle,
                onBack = { navController.popBackStack() }
            )
        }

        composable("downloads") {
            DownloadsScreen(
                viewModel = viewModel,
                onMediaClick = { item ->
                    navController.navigate("player/${item.mediaUrl}/${item.title}")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("profiles") {
            ProfilesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
