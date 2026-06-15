package com.spartanai.spartanaimedia

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.spartanai.spartanaimedia.ui.media.MediaViewModel
import com.spartanai.spartanaimedia.ui.navigation.NavGraph
import com.spartanai.spartanaimedia.ui.theme.SpartanAIMediaTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : FragmentActivity() {
    
    private val viewModel: MediaViewModel by viewModel()
    private var isPiPActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpartanAIMediaTheme {
                val uiState by viewModel.uiState.collectAsState()
                val navController = rememberNavController()

                // Logic to determine start destination
                val startDestination = if (uiState.selectedProfile == null && uiState.profiles.isEmpty()) {
                    "onboarding"
                } else {
                    "home"
                }

                NavGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isPiPActive = isInPictureInPictureMode
    }

    fun enterPiP() {
        enterPictureInPictureMode(
            android.app.PictureInPictureParams.Builder().build()
        )
    }
}
