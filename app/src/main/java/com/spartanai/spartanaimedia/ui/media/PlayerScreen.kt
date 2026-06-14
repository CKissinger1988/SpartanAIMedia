package com.spartanai.spartanaimedia.ui.media

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import com.spartanai.spartanaimedia.MainActivity
import com.spartanai.spartanaimedia.data.remote.SyncEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

data class ActiveReaction(
    val id: String = UUID.randomUUID().toString(),
    val emoji: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: MediaViewModel = koinViewModel(),
    mediaUrl: String,
    title: String,
    isInPiPMode: Boolean = false,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    val mediaItem = uiState.allItems.find { it.title == title }
    val mediaId = mediaItem?.id ?: ""
    val initialPosition = mediaItem?.lastPlaybackPosition ?: 0L

    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var showControls by remember { mutableStateOf(true) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var isChatVisible by remember { mutableStateOf(false) }
    var activeReactions by remember { mutableStateOf(listOf<ActiveReaction>()) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(mediaUrl))
            seekTo(initialPosition)
            prepare()
            playWhenReady = true
        }
    }

    LaunchedEffect(mediaId) {
        viewModel.setCurrentlyPlaying(mediaId)
    }

    // Handle Incoming Sync Events
    LaunchedEffect(uiState.syncEvent) {
        val event = uiState.syncEvent ?: return@LaunchedEffect
        when (event) {
            is SyncEvent.Play -> {
                if (Math.abs(exoPlayer.currentPosition - event.position) > 1000) {
                    exoPlayer.seekTo(event.position)
                }
                exoPlayer.play()
            }
            is SyncEvent.Pause -> {
                exoPlayer.pause()
            }
            is SyncEvent.Seek -> {
                exoPlayer.seekTo(event.position)
            }
            is SyncEvent.Reaction -> {
                activeReactions = (activeReactions + ActiveReaction(emoji = event.emoji)).takeLast(10)
            }
            else -> {}
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.updatePlaybackProgress(mediaId, exoPlayer.currentPosition, exoPlayer.duration)
            viewModel.setCurrentlyPlaying(null)
            exoPlayer.release()
            viewModel.stopSync()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize().clickable { showControls = !showControls },
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
        )

        // Floating Reactions Overlay
        Box(modifier = Modifier.fillMaxSize()) {
            activeReactions.forEach { reaction ->
                key(reaction.id) {
                    FloatingEmoji(emoji = reaction.emoji) {
                        activeReactions = activeReactions.filter { it.id != reaction.id }
                    }
                }
            }
        }

        // Custom Immersive Controls (Hidden in PiP)
        if (!isInPiPMode) {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text(
                            title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Reaction Shortcut (Fire)
                        if (uiState.isSyncConnected) {
                            IconButton(onClick = { viewModel.sendReaction("🔥") }) {
                                Text("🔥", fontSize = 24.sp)
                            }
                        }

                        // Chat Toggle
                        if (uiState.isSyncConnected) {
                            IconButton(onClick = { isChatVisible = !isChatVisible }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Chat, 
                                    contentDescription = "Chat", 
                                    tint = if (isChatVisible) MaterialTheme.colorScheme.primary else Color.White
                                )
                            }
                        }

                        // Sync Button
                        IconButton(onClick = { showSyncDialog = true }) {
                            Icon(
                                Icons.Default.Groups, 
                                contentDescription = "Sync", 
                                tint = if (uiState.isSyncConnected) MaterialTheme.colorScheme.primary else Color.White
                            )
                        }

                        // PiP Button
                        IconButton(onClick = {
                            (context as? MainActivity)?.enterPiP()
                        }) {
                            Icon(Icons.Default.PictureInPicture, contentDescription = "PiP", tint = Color.White)
                        }

                        // Speed Control
                        TextButton(onClick = {
                            playbackSpeed = if (playbackSpeed >= 2.0f) 0.5f else playbackSpeed + 0.25f
                            exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
                        }) {
                            Text("${playbackSpeed}x", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Center Controls
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { 
                            val pos = exoPlayer.currentPosition - 10000
                            exoPlayer.seekBack() 
                            if (uiState.isSyncConnected) viewModel.sendSyncEvent(SyncEvent.Seek(pos))
                        }, modifier = Modifier.size(64.dp)) {
                            Icon(Icons.Default.Replay10, null, tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                        
                        IconButton(
                            onClick = { 
                                if (exoPlayer.isPlaying) {
                                    exoPlayer.pause()
                                    if (uiState.isSyncConnected) viewModel.sendSyncEvent(SyncEvent.Pause(exoPlayer.currentPosition))
                                } else {
                                    exoPlayer.play()
                                    if (uiState.isSyncConnected) viewModel.sendSyncEvent(SyncEvent.Play(exoPlayer.currentPosition))
                                }
                            },
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(
                                if (exoPlayer.isPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(80.dp)
                            )
                        }

                        IconButton(onClick = { 
                            val pos = exoPlayer.currentPosition + 30000
                            exoPlayer.seekForward() 
                            if (uiState.isSyncConnected) viewModel.sendSyncEvent(SyncEvent.Seek(pos))
                        }, modifier = Modifier.size(64.dp)) {
                            Icon(Icons.Default.Forward30, null, tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                    }

                    // Bottom Progress, Reactions & Recommendations
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(16.dp)
                    ) {
                        // More Like This shelf (Animated)
                        if (uiState.recommendations.isNotEmpty() && !exoPlayer.isPlaying) {
                            Text(
                                "MORE LIKE THIS",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                items(uiState.recommendations) { item ->
                                    Card(
                                        modifier = Modifier.width(120.dp).height(70.dp).clickable { 
                                            // Handle cross-recommendation navigation
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        AsyncImage(
                                            model = item.thumbnailUrl,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }

                        if (uiState.isSyncConnected) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("😂", "😮", "😍", "👏", "😢").forEach { emoji ->
                                    Text(
                                        text = emoji,
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .clickable { viewModel.sendReaction(emoji) },
                                        fontSize = 24.sp
                                    )
                                }
                            }
                        }

                        Slider(
                            value = 0f, 
                            onValueChange = { },
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            if (uiState.isSyncConnected) {
                                Text("CONNECTED TO WATCH PARTY", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                            } else {
                                Text("HD", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                            }
                            Text("SpartanAI Secure Stream", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Chat Overlay
        AnimatedVisibility(
            visible = isChatVisible && uiState.isSyncConnected,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(300.dp)
        ) {
            ChatPanel(
                messages = uiState.chatMessages,
                currentUserId = uiState.selectedProfile?.userId ?: "",
                onSendMessage = viewModel::sendChatMessage,
                onClose = { isChatVisible = false }
            )
        }

        // Sync Dialog
        if (showSyncDialog) {
            var roomId by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showSyncDialog = false },
                title = { Text("Watch Together") },
                text = {
                    Column {
                        Text("Enter a Room ID to sync playback with friends.")
                        OutlinedTextField(
                            value = roomId,
                            onValueChange = { roomId = it },
                            label = { Text("Room ID") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.startSyncSession(roomId)
                        showSyncDialog = false
                    }) {
                        Text("JOIN ROOM")
                    }
                }
            )
        }
    }
}

@Composable
fun FloatingEmoji(emoji: String, onFinished: () -> Unit) {
    val duration = 3000
    
    val yOffset = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    val randomX = remember { (Math.random() * 200 - 100).toFloat() }

    LaunchedEffect(Unit) {
        launch {
            yOffset.animateTo(
                targetValue = -800f,
                animationSpec = tween(duration, easing = LinearEasing)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(duration, easing = FastOutSlowInEasing)
            )
        }
        delay(duration.toLong())
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = emoji,
            fontSize = 40.sp,
            modifier = Modifier
                .offset(y = yOffset.value.dp, x = randomX.dp)
                .alpha(alpha.value)
        )
    }
}

@Composable
fun ChatPanel(
    messages: List<SyncEvent.Message>,
    currentUserId: String,
    onSendMessage: (String) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        color = Color.Black.copy(alpha = 0.8f),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Party Chat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
            
            val listState = rememberLazyListState()
            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages) { msg ->
                    val isMine = msg.userId == currentUserId
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
                    ) {
                        Text(
                            msg.username, 
                            style = MaterialTheme.typography.labelSmall, 
                            color = if (isMine) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                        Surface(
                            color = if (isMine) MaterialTheme.colorScheme.primary else Color.DarkGray,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                msg.text,
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            var text by remember { mutableStateOf("") }
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Say something...") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 2
                )
                IconButton(onClick = {
                    if (text.isNotBlank()) {
                        onSendMessage(text)
                        text = ""
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
