package com.spartanai.spartanaimedia.ui.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalUriHandler
import coil3.compose.AsyncImage
import com.spartanai.spartanaimedia.domain.model.MediaItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MediaViewModel,
    onMediaClick: (MediaItem) -> Unit,
    onDownloadsClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    onProfilesClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    // Forced Update Dialog
    uiState.updateInfo?.let { info ->
        if (info.isForceUpdate) {
            AlertDialog(
                onDismissRequest = { /* Non-dismissible */ },
                title = { Text("Update Required") },
                text = { Text("A new version of SpartanAI Media (v${info.latestVersion}) is available. You must update to continue using the application.") },
                confirmButton = {
                    Button(
                        onClick = { uriHandler.openUri(info.downloadUrl) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("UPDATE NOW")
                    }
                },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            )
        }
    }

    Scaffold(
        topBar = {
            if (!isSearchActive) {
                Column {
                    TopAppBar(
                        title = { Text("SpartanAI Media", fontWeight = FontWeight.Bold) },
                        actions = {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                            IconButton(onClick = onWatchlistClick) {
                                Icon(Icons.Default.Bookmarks, contentDescription = "Watchlist")
                            }
                            IconButton(onClick = onDownloadsClick) {
                                Icon(Icons.Default.FileDownload, contentDescription = "Downloads")
                            }
                            uiState.selectedProfile?.let { profile ->
                                AsyncImage(
                                    model = profile.avatarUrl,
                                    contentDescription = "Profile",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .clickable(onClick = onProfilesClick)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        }
                    )
                    CategoryTabs(
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = viewModel::onCategorySelected
                    )
                }
            } else {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    onSearch = { isSearchActive = false },
                    active = true,
                    onActiveChange = { isSearchActive = it },
                    placeholder = { Text("Search titles, genres...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = { 
                        IconButton(onClick = { 
                            viewModel.onSearchQueryChanged("")
                            isSearchActive = false 
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.searchSuggestions) { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onSearchQueryChanged(suggestion) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                                Spacer(Modifier.width(16.dp))
                                Text(suggestion, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val uriHandler = LocalUriHandler.current
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Update Notification
                uiState.updateInfo?.let { info ->
                    if (info.updateAvailable) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxWidth().clickable { uriHandler.openUri(info.downloadUrl) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Update, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Update Available: v${info.latestVersion}", fontWeight = FontWeight.Bold)
                                        Text("Download the latest SpartanAI features.", style = MaterialTheme.typography.bodySmall)
                                    }
                                    TextButton(onClick = { uriHandler.openUri(info.downloadUrl) }) {
                                        Text("DOWNLOAD")
                                    }
                                }
                            }
                        }
                    }
                }

                // Featured / Continue Watching (only on "All" tab)
                if (uiState.selectedCategory == "All" && uiState.continueWatching.isNotEmpty() && uiState.searchQuery.isEmpty()) {
                    item {
                        SectionHeader("Continue Watching")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.continueWatching) { item ->
                                ContinueWatchingCard(item, onMediaClick)
                            }
                        }
                    }
                }

                // Nearby Spartans (P2P Discovery)
                if (uiState.selectedCategory == "All" && uiState.nearbyPeers.isNotEmpty() && uiState.searchQuery.isEmpty()) {
                    item {
                        SectionHeader("Nearby Spartans")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.nearbyPeers) { peer ->
                                Card(
                                    modifier = Modifier.width(180.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.WifiTethering, null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(peer.name, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                                            Text("Ready to share", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Dynamic Shelves by Category and Genre
                uiState.mediaByCategory.forEach { (category, genres) ->
                    // Category Header (if in "All" view)
                    if (uiState.selectedCategory == "All") {
                        item {
                            Text(
                                text = category.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(start = 16.dp, top = 32.dp, bottom = 4.dp),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    genres.forEach { (genre, items) ->
                        item {
                            SectionHeader(genre)
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(items) { item ->
                                    GenreMediaCard(item, onMediaClick)
                                }
                            }
                        }
                    }
                }

                if (uiState.mediaByCategory.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(64.dp), contentAlignment = Alignment.Center) {
                            Text("No content in this category yet.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryTabs(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("All", "Movies", "Shorts", "Series")
    ScrollableTabRow(
        selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        divider = {},
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[categories.indexOf(selectedCategory).coerceAtLeast(0)]),
                color = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        categories.forEach { category ->
            Tab(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                text = { Text(category) }
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun GenreMediaCard(
    item: MediaItem,
    onClick: (MediaItem) -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick(item) },
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            Box {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = CircleShape
                ) {
                    Text(
                        text = item.resolution,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                if (item.progress > 0) {
                    LinearProgressIndicator(
                        progress = { item.progress },
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).height(3.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(8.dp),
                maxLines = 1
            )
        }
    }
}

@Composable
fun ContinueWatchingCard(
    item: MediaItem,
    onClick: (MediaItem) -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable { onClick(item) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    contentScale = ContentScale.Crop
                )
                LinearProgressIndicator(
                    progress = { item.progress },
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(8.dp),
                maxLines = 1
            )
        }
    }
}

@Composable
fun WatchlistItemCard(
    item: MediaItem,
    onClick: (MediaItem) -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick(item) }
    ) {
        AsyncImage(
            model = item.thumbnailUrl,
            contentDescription = item.title,
            modifier = Modifier.fillMaxWidth().height(180.dp),
            contentScale = ContentScale.Crop
        )
    }
}
