package com.spartanai.spartanaimedia

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

                val startDestination = remember(uiState.isLoading) {
                    if (uiState.isLoading) {
                        "loading"
                    } else if (uiState.selectedProfile == null && uiState.profiles.isEmpty()) {
                        "onboarding"
                    } else {
                        "home"
                    }
                }

                if (startDestination != "loading") {
                    NavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
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
