package com.spartanai.spartanaimedia.ui.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun OnboardingScreen(
    viewModel: MediaViewModel,
    onComplete: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Welcome to SpartanAI",
            description = "Experience premium, decentralized media streaming with AI-powered discovery and total privacy.",
            icon = Icons.Default.RocketLaunch,
            color = MaterialTheme.colorScheme.primary
        ),
        OnboardingPage(
            title = "Global Watch Parties",
            description = "Watch media together with friends across the world with real-time sync, chat, and reactions.",
            icon = Icons.Default.Groups,
            color = Color(0xFF4CAF50)
        ),
        OnboardingPage(
            title = "Pi Network Powered",
            description = "Support the ecosystem with our integrated light node and earn rewards while you stream.",
            icon = Icons.Default.Hub,
            color = Color(0xFFFFC107)
        ),
        OnboardingPage(
            title = "Secure & Anonymous",
            description = "Enjoy private discovery and encrypted local storage for your media collection.",
            icon = Icons.Default.Shield,
            color = Color(0xFFE91E63)
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size + 1 })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            if (page < pages.size) {
                OnboardingPageView(pages[page])
            } else {
                ProfileCreationPage(viewModel, onComplete)
            }
        }

        // Pager Indicator
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size + 1) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(10.dp)
                )
            }
        }

        // Action Button
        if (pagerState.currentPage < pages.size) {
            Button(
                onClick = { 
                    scope.launch { 
                        pagerState.animateScrollToPage(pagerState.currentPage + 1) 
                    } 
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("CONTINUE")
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
        }
    }
}

@Composable
fun OnboardingPageView(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = page.icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = page.color
        )
        Spacer(Modifier.height(48.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun ProfileCreationPage(
    viewModel: MediaViewModel,
    onComplete: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(32.dp))
        Text(
            text = "Create Your Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(24.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Profile Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isAnonymous,
                onCheckedChange = { isAnonymous = it }
            )
            Text("Enable Anonymous Mode", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = { 
                if (name.isNotBlank()) {
                    viewModel.createNewProfile(name, isAnonymous)
                    onComplete()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = name.isNotBlank()
        ) {
            Text("GET STARTED")
        }
    }
}
