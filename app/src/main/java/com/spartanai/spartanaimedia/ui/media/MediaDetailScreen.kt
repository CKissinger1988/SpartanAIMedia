package com.spartanai.spartanaimedia.ui.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.spartanai.spartanaimedia.domain.model.MediaItem
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    viewModel: MediaViewModel = koinViewModel(),
    mediaUrl: String,
    title: String,
    onBack: () -> Unit,
    onPlay: (MediaItem) -> Unit,
    onWatchTogether: (MediaItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val mediaItem = uiState.allItems.find { it.title == title } ?: return

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Hero Image with Gradient
        Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
            AsyncImage(
                model = mediaItem.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                            startY = 100f
                        )
                    )
            )
        }

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }

        // Content Scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 280.dp) // Start below the main part of the hero image
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Title and Meta
                Text(
                    text = mediaItem.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${mediaItem.rating} • ${mediaItem.releaseYear} • ${mediaItem.resolution}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = mediaItem.genre,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { onPlay(mediaItem) },
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (mediaItem.progress > 0) "RESUME" else "PLAY")
                    }
                    OutlinedButton(
                        onClick = { onWatchTogether(mediaItem) },
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("WATCH PARTY")
                    }
                    IconButton(
                        onClick = { viewModel.toggleWatchlist(mediaItem) },
                        modifier = Modifier.size(50.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        val inWatchlist = uiState.watchlist.any { it.id == mediaItem.id }
                        Icon(
                            if (inWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Watchlist",
                            tint = if (inWatchlist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Synopsis
                Text("Synopsis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mediaItem.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Cast & Crew
                Text("Cast & Crew", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Director: ${mediaItem.director}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(mediaItem.cast) { actor ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        ) {
                            Text(
                                text = actor,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // More Like This (If we want to show it here too)
                if (uiState.recommendations.isNotEmpty()) {
                    Text("More Like This", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.recommendations) { recItem ->
                            Card(
                                modifier = Modifier.width(140.dp).clickable { 
                                    // Normally we would navigate to another detail screen
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column {
                                    AsyncImage(
                                        model = recItem.thumbnailUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxWidth().height(200.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    Text(
                                        text = recItem.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(8.dp),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}
