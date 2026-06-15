package com.spartanai.spartanaimedia.ui.media

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spartanai.spartanaimedia.data.remote.*
import com.spartanai.spartanaimedia.domain.model.MediaItem
import com.spartanai.spartanaimedia.domain.model.UserProfile
import com.spartanai.spartanaimedia.domain.repository.MediaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MediaUiState(
    val mediaByCategory: Map<String, Map<String, List<MediaItem>>> = emptyMap(),
    val allItems: List<MediaItem> = emptyList(),
    val continueWatching: List<MediaItem> = emptyList(),
    val downloadedMedia: List<MediaItem> = emptyList(),
    val watchlist: List<MediaItem> = emptyList(),
    val recommendations: List<MediaItem> = emptyList(),
    val profiles: List<UserProfile> = emptyList(),
    val selectedProfile: UserProfile? = null,
    val searchQuery: String = "",
    val searchSuggestions: List<String> = emptyList(),
    val selectedCategory: String = "All",
    val nodeStatus: PiNodeService.NodeStatus? = null,
    val updateInfo: UpdateInfo? = null,
    val nearbyPeers: List<PeerDevice> = emptyList(),
    val syncEvent: SyncEvent? = null,
    val chatMessages: List<SyncEvent.Message> = emptyList(),
    val isSyncConnected: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class MediaViewModel(
    private val repository: MediaRepository,
    private val downloadManager: MediaDownloadManager,
    private val piBlockchainManager: PiBlockchainManager,
    private val piNodeService: PiNodeService,
    private val updateManager: UpdateManager,
    private val preloadManager: PreloadManager,
    private val suggestionManager: SearchSuggestionManager,
    private val p2pManager: P2PManager,
    private val syncManager: MediaSyncManager,
    private val recommendationEngine: RecommendationEngine
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("All")
    private val _chatMessages = MutableStateFlow<List<SyncEvent.Message>>(emptyList())
    private val _currentMediaId = MutableStateFlow<String?>(null)

    private val _manualUpdateInfo = MutableStateFlow<UpdateInfo?>(null)

    // The main reactive pipeline for app data
    val uiState: StateFlow<MediaUiState> = combine(
        combine(
            repository.getAllProfiles(),
            repository.getSelectedProfile(),
            updateManager.checkForUpdates(),
            _manualUpdateInfo
        ) { profiles, selectedProfile, updateInfo, manualUpdate ->
            AppDataBundle(profiles, selectedProfile, manualUpdate ?: updateInfo)
        },
        combine(
            _searchQuery,
            _selectedCategory,
            piNodeService.monitorNodeStatus(),
            p2pManager.peers,
            _currentMediaId
        ) { query, category, nodeStatus, peers, currentMediaId ->
            UiContext(query, category, nodeStatus, peers, currentMediaId)
        },
        combine(
            syncManager.incomingEvents,
            syncManager.connectionStatus,
            _chatMessages
        ) { event, connected, messages ->
            Triple(event, connected, messages)
        }
    ) { data, context, sync ->
        try {
            val selectedProfile = data.selectedProfile
            if (selectedProfile == null) {
                flowOf(MediaUiState(
                    profiles = data.profiles,
                    updateInfo = data.updateInfo,
                    isLoading = false
                ))
            } else {
                combine(
                    repository.getMediaItems(context.query),
                    repository.getWatchlist(selectedProfile.userId),
                    repository.getMediaItems(null) 
                ) { filteredItems, watchlist, allItems ->
                    val filteredByTab = if (context.category == "All") filteredItems else filteredItems.filter { it.category == context.category }
                    val grouped = filteredByTab.groupBy { it.category }
                        .mapValues { entry -> entry.value.groupBy { it.genre } }
                    
                    val downloaded = filteredItems.filter { it.isDownloaded }
                    val continueWatching = filteredItems.filter { it.lastPlaybackPosition > 0 && it.progress < 0.95f }
                        .sortedByDescending { m -> m.lastPlaybackPosition }
                    
                    // AI Suggestions
                    val suggestions = try { suggestionManager.getSuggestions(context.query, allItems) } catch(e: Exception) { emptyList() }

                    // Recommendations
                    val recommendations = try {
                        context.currentMediaId?.let { id ->
                            allItems.find { it.id == id }?.let { current ->
                                recommendationEngine.getRelatedContent(current, allItems)
                            }
                        } ?: recommendationEngine.getPersonalizedRecommendations(allItems, continueWatching)
                    } catch(e: Exception) { emptyList() }

                    MediaUiState(
                        mediaByCategory = grouped,
                        allItems = filteredItems,
                        downloadedMedia = downloaded,
                        continueWatching = continueWatching,
                        watchlist = watchlist,
                        recommendations = recommendations,
                        profiles = data.profiles,
                        selectedProfile = selectedProfile,
                        searchQuery = context.query,
                        searchSuggestions = suggestions,
                        selectedCategory = context.category,
                        nodeStatus = context.nodeStatus,
                        updateInfo = data.updateInfo,
                        nearbyPeers = context.peers,
                        syncEvent = sync.first,
                        chatMessages = sync.third,
                        isSyncConnected = sync.second,
                        isLoading = false
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("SpartanAI_VM", "Error in UI state pipeline", e)
            flowOf(MediaUiState(error = e.message, isLoading = false))
        }
    }.flatMapLatest { it }
    .catch { e ->
        Log.e("SpartanAI_VM", "Fatal error in StateFlow", e)
        emit(MediaUiState(error = e.message, isLoading = false))
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MediaUiState()
    )

    private data class AppDataBundle(
        val profiles: List<UserProfile>,
        val selectedProfile: UserProfile?,
        val updateInfo: UpdateInfo?
    )

    private data class UiContext(
        val query: String,
        val category: String,
        val nodeStatus: PiNodeService.NodeStatus,
        val peers: List<PeerDevice>,
        val currentMediaId: String?
    )

    init {
        seedInitialData()
        observeSyncEvents()
        observePreloading()
    }

    override fun onCleared() {
        super.onCleared()
        p2pManager.stopP2P()
        syncManager.disconnect()
    }

    private fun observeSyncEvents() {
        syncManager.incomingEvents
            .onEach { event ->
                if (event is SyncEvent.Message) {
                    val currentMessages = _chatMessages.value
                    if (currentMessages.none { it.timestamp == event.timestamp && it.userId == event.userId }) {
                        _chatMessages.value = (currentMessages + event).takeLast(50)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observePreloading() {
        // Preload "Continue Watching" items automatically
        uiState.map { it.continueWatching }
            .distinctUntilChanged()
            .onEach { items ->
                if (items.isNotEmpty()) {
                    preloadManager.preloadItems(items)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun seedInitialData() {
        viewModelScope.launch {
            try {
                val profiles = kotlinx.coroutines.withTimeoutOrNull(2000) {
                    repository.getAllProfiles().first()
                } ?: emptyList()
                
                if (profiles.isEmpty()) {
                    repository.createProfile("Spartan Warrior", "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=400&q=80")
                    repository.createProfile("Anonymous Shadow", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&q=80", isAnonymous = true)
                }
            } catch (e: Exception) {
                Log.e("SpartanAI_VM", "Error during data seeding", e)
            }
            repository.refreshMediaItems()
        }
    }

    fun setCurrentlyPlaying(mediaId: String?) {
        _currentMediaId.value = mediaId
    }

    fun startSyncSession(roomId: String) {
        val userId = uiState.value.selectedProfile?.userId ?: return
        syncManager.connect(roomId, userId)
        _chatMessages.value = emptyList() // Clear old chat
    }

    fun sendSyncEvent(event: SyncEvent) {
        syncManager.sendEvent(event)
    }

    fun sendReaction(emoji: String) {
        val userId = uiState.value.selectedProfile?.userId ?: return
        syncManager.sendEvent(SyncEvent.Reaction(userId, emoji))
    }

    fun sendChatMessage(text: String) {
        val profile = uiState.value.selectedProfile ?: return
        val message = SyncEvent.Message(
            userId = profile.userId,
            username = profile.username,
            text = text
        )
        // Add to local list immediately for better responsiveness
        _chatMessages.value = (_chatMessages.value + message).takeLast(50)
        syncManager.sendEvent(message)
    }

    fun stopSync() {
        syncManager.disconnect()
        _chatMessages.value = emptyList()
    }

    fun loginWithPi() {
        // Feature disabled
    }

    fun togglePiNode(isActive: Boolean) {
        // Feature disabled
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun updatePlaybackProgress(mediaId: String, position: Long, duration: Long) {
        viewModelScope.launch {
            repository.updatePlaybackProgress(mediaId, position, duration)
        }
    }

    fun selectProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.selectProfile(profile.userId)
            p2pManager.startP2P()
        }
    }

    fun createNewProfile(name: String, isAnonymous: Boolean) {
        viewModelScope.launch {
            repository.createProfile(name, "https://api.dicebear.com/7.x/avataaars/svg?seed=$name", isAnonymous)
        }
    }

    fun downloadMedia(item: MediaItem) {
        val isSecure = uiState.value.selectedProfile?.isAnonymous ?: false
        val localPath = downloadManager.downloadMedia(item.mediaUrl, item.title, isSecure)
        viewModelScope.launch {
            repository.markAsDownloaded(item.id, localPath)
        }
    }

    fun forceUpdateCheck() {
        viewModelScope.launch {
            val update = updateManager.performManualCheck()
            if (update != null) {
                _manualUpdateInfo.value = update
            }
        }
    }

    fun shareMediaP2P(item: MediaItem, peer: PeerDevice, onProgress: ((Float) -> Unit)? = null) {
        viewModelScope.launch {
            val localPath = item.downloadPath ?: return@launch
            if (localPath.isBlank()) return@launch
            
            val file = downloadManager.getFileForPath(localPath)
            if (file != null && file.exists()) {
                p2pManager.sendEncryptedFile(peer, file, onProgress)
            }
        }
    }

    fun toggleWatchlist(item: MediaItem) {
        val userId = uiState.value.selectedProfile?.userId ?: return
        val isInWatchlist = uiState.value.watchlist.any { it.id == item.id }
        
        viewModelScope.launch {
            if (isInWatchlist) {
                repository.removeFromWatchlist(userId, item.id)
            } else {
                repository.addToWatchlist(userId, item.id)
            }
        }
    }

    fun updateProxyConfig(host: String, port: Int, type: String) {
        val userId = uiState.value.selectedProfile?.userId ?: return
        viewModelScope.launch {
            repository.updateProxyConfig(userId, host, port, type)
            repository.refreshMediaItems()
        }
    }
}
