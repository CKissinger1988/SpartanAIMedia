package com.spartanai.spartanaimedia.ui.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.spartanai.spartanaimedia.data.remote.PeerDevice
import com.spartanai.spartanaimedia.domain.model.MediaItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    viewModel: MediaViewModel,
    onMediaClick: (MediaItem) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showShareDialog by remember { mutableStateOf<MediaItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Downloads") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.downloadedMedia.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No downloads yet", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.downloadedMedia) { item ->
                    DownloadItemCard(
                        item = item,
                        onClick = onMediaClick,
                        onShare = { showShareDialog = item }
                    )
                }
            }
        }

        showShareDialog?.let { item ->
            AlertDialog(
                onDismissRequest = { showShareDialog = null },
                title = { Text("Share via P2P") },
                text = {
                    if (uiState.nearbyPeers.isEmpty()) {
                        Text("No nearby peers found on the local network.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.nearbyPeers) { peer ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        viewModel.shareMediaP2P(item, peer)
                                        showShareDialog = null
                                    },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Text(
                                        text = peer.name,
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showShareDialog = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun DownloadItemCard(
    item: MediaItem,
    onClick: (MediaItem) -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp, 60.dp)
                    .background(Color.DarkGray)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Resolution: ${item.resolution}", style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = "Share via P2P")
            }
            IconButton(onClick = { onClick(item) }) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play")
            }
        }
    }
}
