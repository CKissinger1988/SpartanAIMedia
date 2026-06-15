package com.spartanai.spartanaimedia.data.repository

import com.spartanai.spartanaimedia.data.local.dao.MediaDao
import com.spartanai.spartanaimedia.data.local.dao.UserDao
import com.spartanai.spartanaimedia.data.local.entity.UserProfileEntity
import com.spartanai.spartanaimedia.data.local.entity.WatchlistEntity
import com.spartanai.spartanaimedia.data.local.entity.toDomainModel
import com.spartanai.spartanaimedia.data.local.entity.toEntity
import com.spartanai.spartanaimedia.data.remote.MediaScraper
import com.spartanai.spartanaimedia.domain.model.MediaItem
import com.spartanai.spartanaimedia.domain.model.ProxyConfig
import com.spartanai.spartanaimedia.domain.model.UserProfile
import com.spartanai.spartanaimedia.domain.repository.MediaRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class MediaRepositoryImpl(
    private val scraper: MediaScraper,
    private val mediaDao: MediaDao,
    private val userDao: UserDao
) : MediaRepository {
    
    private val selectedUserId = MutableStateFlow<String?>(null)

    init {
        // Simple initialization - in a real app, this might be from SharedPreferences
        // For SpartanAI, we'll try to pick the first available profile on startup
        CoroutineScope(Dispatchers.IO).launch {
            val profiles = userDao.getAllProfiles().first()
            if (profiles.isNotEmpty()) {
                selectedUserId.value = profiles.first().userId
            }
        }
    }

    override fun getMediaItems(query: String?): Flow<List<MediaItem>> {
        val flow = if (query.isNullOrBlank()) {
            mediaDao.getAllMediaItems()
        } else {
            mediaDao.searchMediaItems(query)
        }
        return flow.map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun updatePlaybackProgress(mediaId: String, position: Long, duration: Long) {
        withContext(Dispatchers.IO) {
            val entity = mediaDao.getMediaItemById(mediaId)
            if (entity != null) {
                mediaDao.updateMediaItem(entity.copy(
                    lastPlaybackPosition = position,
                    totalDuration = duration
                ))
            }
        }
    }

    override suspend fun refreshMediaItems() {
        withContext(Dispatchers.IO) {
            val user = selectedUserId.value?.let { userDao.getProfileById(it) }
            val proxy = user?.proxyHost?.let { host ->
                val pType = try {
                    ProxyConfig.ProxyType.valueOf(user.proxyType ?: "HTTP")
                } catch (e: Exception) {
                    ProxyConfig.ProxyType.HTTP
                }
                ProxyConfig(host, user.proxyPort ?: 8080, pType, isEnabled = true)
            }
            
            val remoteItems = scraper.fetchMediaItems(proxy)
            mediaDao.insertMediaItems(remoteItems.map { it.toEntity() })
        }
    }

    override fun getAllProfiles(): Flow<List<UserProfile>> {
        return userDao.getAllProfiles().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private fun UserProfileEntity.toDomain() = UserProfile(
        userId = userId,
        username = username,
        avatarUrl = avatarUrl,
        isAnonymous = isAnonymous,
        proxyConfig = proxyHost?.let { host -> 
            val pType = try {
                ProxyConfig.ProxyType.valueOf(proxyType ?: "HTTP")
            } catch (e: Exception) {
                ProxyConfig.ProxyType.HTTP
            }
            ProxyConfig(host, proxyPort ?: 8080, pType, isEnabled = true) 
        },
        piUsername = piUsername,
        piWalletAddress = piWalletAddress,
        isPiNodeActive = isPiNodeActive
    )

    override suspend fun createProfile(username: String, avatarUrl: String, isAnonymous: Boolean) {
        withContext(Dispatchers.IO) {
            val userId = UUID.randomUUID().toString()
            userDao.insertProfile(UserProfileEntity(userId, username, avatarUrl, isAnonymous))
            if (selectedUserId.value == null) {
                selectedUserId.value = userId
            }
        }
    }

    override suspend fun selectProfile(userId: String) {
        selectedUserId.value = userId
    }

    override fun getSelectedProfile(): Flow<UserProfile?> {
        return selectedUserId.flatMapLatest { id ->
            if (id == null) flowOf(null)
            else userDao.observeProfileById(id).map { it?.toDomain() }
        }
    }

    override fun getWatchlist(userId: String): Flow<List<MediaItem>> {
        return userDao.getWatchlistForUser(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun addToWatchlist(userId: String, mediaId: String) {
        withContext(Dispatchers.IO) {
            userDao.addToWatchlist(WatchlistEntity(userId, mediaId))
        }
    }

    override suspend fun removeFromWatchlist(userId: String, mediaId: String) {
        withContext(Dispatchers.IO) {
            userDao.removeFromWatchlist(userId, mediaId)
        }
    }

    override suspend fun markAsDownloaded(mediaId: String, localPath: String) {
        withContext(Dispatchers.IO) {
            val entity = mediaDao.getMediaItemById(mediaId)
            if (entity != null) {
                mediaDao.updateMediaItem(entity.copy(
                    isDownloaded = true,
                    downloadPath = localPath
                ))
            }
        }
    }

    override suspend fun deleteMediaItem(mediaId: String) {
        withContext(Dispatchers.IO) {
            mediaDao.deleteMediaItemById(mediaId)
        }
    }

    override suspend fun toggleFavorite(mediaId: String) {
        withContext(Dispatchers.IO) {
            val entity = mediaDao.getMediaItemById(mediaId)
            if (entity != null) {
                mediaDao.updateMediaItem(entity.copy(isFavorite = !entity.isFavorite))
            }
        }
    }

    override suspend fun updateProxyConfig(userId: String, proxyHost: String?, proxyPort: Int?, proxyType: String?) {
        withContext(Dispatchers.IO) {
            userDao.updateProxyConfig(userId, proxyHost, proxyPort, proxyType)
        }
    }

    override suspend fun updatePiData(userId: String, username: String, wallet: String) {
        withContext(Dispatchers.IO) {
            val entity = userDao.getProfileById(userId)
            if (entity != null) {
                userDao.insertProfile(entity.copy(piUsername = username, piWalletAddress = wallet))
            }
        }
    }

    override suspend fun setPiNodeActive(userId: String, isActive: Boolean) {
        withContext(Dispatchers.IO) {
            val entity = userDao.getProfileById(userId)
            if (entity != null) {
                userDao.insertProfile(entity.copy(isPiNodeActive = isActive))
            }
        }
    }
}
