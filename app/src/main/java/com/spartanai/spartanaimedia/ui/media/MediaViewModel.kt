package com.spartanai.spartanaimedia.ui.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spartanai.spartanaimedia.data.remote.MediaDownloadManager
import com.spartanai.spartanaimedia.data.remote.PiBlockchainManager
import com.spartanai.spartanaimedia.data.remote.PiNodeService
import com.spartanai.spartanaimedia.domain.model.MediaItem
import com.spartanai.spartanaimedia.domain.model.UserProfile
import com.spartanai.spartanaimedia.domain.repository.MediaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MediaUiState(
    val mediaByCategory: Map<String, Map<String, List<MediaItem>>> = emptyMap(),
    val allItems: List<MediaItem> = emptyList(),
    val continueWatching: List<MediaItem> = emptyList(),
    val downloadedMedia: List<MediaItem> = emptyList(),
    val watchlist: List<MediaItem> = emptyList(),
    val profiles: List<UserProfile> = emptyList(),
    val selectedProfile: UserProfile? = null,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val nodeStatus: PiNodeService.NodeStatus? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class MediaViewModel(
    private val repository: MediaRepository,
    private val downloadManager: MediaDownloadManager,
    private val piBlockchainManager: PiBlockchainManager,
    private val piNodeService: PiNodeService
) : ViewModel() {

    private val _uiState = MutableStateFlow(MediaUiState())
    val uiState: StateFlow<MediaUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("All")

    init {
        loadInitialData()
        observeProfiles()
        observeNodeStatus()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            repository.refreshMediaItems()
        }
    }

    private fun observeProfiles() {
        viewModelScope.launch {
            repository.getAllProfiles().collect { profiles ->
                if (profiles.isEmpty()) {
                    repository.createProfile("Spartan Warrior", "https://api.dicebear.com/7.x/avataaars/svg?seed=Spartan")
                    repository.createProfile("Anonymous Shadow", "https://api.dicebear.com/7.x/bottts/svg?seed=Shadow", isAnonymous = true)
                }
                _uiState.update { it.copy(profiles = profiles) }
            }
        }

        viewModelScope.launch {
            repository.getSelectedProfile().collectLatest { profile ->
                _uiState.update { it.copy(selectedProfile = profile) }
                if (profile != null) {
                    observeAppData(profile.userId)
                    
                    // Sync service state with profile
                    piNodeService.setNodeActive(profile.isPiNodeActive)

                    // Auto-activate Pi Node if not already active in DB
                    if (!profile.isPiNodeActive) {
                        togglePiNode(true)
                    }
                }
            }
        }
    }

    private fun observeAppData(userId: String) {
        viewModelScope.launch {
            combine(
                _searchQuery,
                _selectedCategory
            ) { query, category -> 
                Pair(query, category)
            }.flatMapLatest { (query, category) ->
                combine(
                    repository.getMediaItems(query),
                    repository.getWatchlist(userId)
                ) { items, watchlist ->
                    val filteredByTab = if (category == "All") items else items.filter { it.category == category }
                    val grouped = filteredByTab.groupBy { it.category }
                        .mapValues { entry -> entry.value.groupBy { it.genre } }
                    
                    val downloaded = items.filter { it.isDownloaded }
                    val continueWatching = items.filter { it.lastPlaybackPosition > 0 && it.progress < 0.95f }
                    
                    _uiState.update { 
                        it.copy(
                            mediaByCategory = grouped,
                            allItems = items,
                            downloadedMedia = downloaded,
                            continueWatching = continueWatching.sortedByDescending { m -> m.lastPlaybackPosition },
                            watchlist = watchlist,
                            searchQuery = query,
                            selectedCategory = category,
                            isLoading = false
                        )
                    }
                }
            }.collect()
        }
    }

    private fun observeNodeStatus() {
        viewModelScope.launch {
            piNodeService.monitorNodeStatus().collect { status ->
                _uiState.update { it.copy(nodeStatus = status) }
            }
        }
    }

    fun loginWithPi() {
        viewModelScope.launch {
            piBlockchainManager.authenticate()
            piBlockchainManager.piAuthState.collectLatest { result ->
                if (result is PiBlockchainManager.PiAuthResult.Success) {
                    val userId = _uiState.value.selectedProfile?.userId ?: return@collectLatest
                    repository.updatePiData(userId, result.username, result.walletAddress)
                }
            }
        }
    }

    fun togglePiNode(isActive: Boolean) {
        val userId = _uiState.value.selectedProfile?.userId ?: return
        piNodeService.setNodeActive(isActive)
        viewModelScope.launch {
            repository.setPiNodeActive(userId, isActive)
        }
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
        }
    }

    fun createNewProfile(name: String, isAnonymous: Boolean) {
        viewModelScope.launch {
            repository.createProfile(name, "https://api.dicebear.com/7.x/avataaars/svg?seed=$name", isAnonymous)
        }
    }

    fun downloadMedia(item: MediaItem) {
        val isSecure = _uiState.value.selectedProfile?.isAnonymous ?: false
        downloadManager.downloadMedia(item.mediaUrl, item.title, isSecure)
        viewModelScope.launch {
            val localPath = if (isSecure) "secure_media/${item.title}.mp4" else "Movies/${item.title}.mp4"
            repository.markAsDownloaded(item.id, localPath)
        }
    }

    fun toggleWatchlist(item: MediaItem) {
        val userId = _uiState.value.selectedProfile?.userId ?: return
        val isInWatchlist = _uiState.value.watchlist.any { it.id == item.id }
        
        viewModelScope.launch {
            if (isInWatchlist) {
                repository.removeFromWatchlist(userId, item.id)
            } else {
                repository.addToWatchlist(userId, item.id)
            }
        }
    }
}
