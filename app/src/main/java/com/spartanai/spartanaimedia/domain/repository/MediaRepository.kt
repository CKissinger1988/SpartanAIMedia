package com.spartanai.spartanaimedia.domain.repository

import com.spartanai.spartanaimedia.domain.model.MediaItem
import com.spartanai.spartanaimedia.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getMediaItems(query: String? = null): Flow<List<MediaItem>>
    suspend fun updatePlaybackProgress(mediaId: String, position: Long, duration: Long)
    suspend fun refreshMediaItems()
    
    // User & Watchlist
    fun getAllProfiles(): Flow<List<UserProfile>>
    suspend fun createProfile(username: String, avatarUrl: String, isAnonymous: Boolean = false)
    suspend fun selectProfile(userId: String)
    fun getSelectedProfile(): Flow<UserProfile?>
    fun getWatchlist(userId: String): Flow<List<MediaItem>>
    suspend fun addToWatchlist(userId: String, mediaId: String)
    suspend fun removeFromWatchlist(userId: String, mediaId: String)
    
    // Downloads
    suspend fun markAsDownloaded(mediaId: String, localPath: String)
    
    // Pi Network
    suspend fun updatePiData(userId: String, username: String, wallet: String)
    suspend fun setPiNodeActive(userId: String, isActive: Boolean)
}
