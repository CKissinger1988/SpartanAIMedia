package com.spartanai.spartanaimedia.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val userId: String,
    val username: String,
    val avatarUrl: String,
    val isAnonymous: Boolean = false,
    val proxyHost: String? = null,
    val proxyPort: Int? = null,
    val proxyType: String? = null,
    val piUsername: String? = null,
    val piWalletAddress: String? = null,
    val isPiNodeActive: Boolean = false,
    val lastSyncTime: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "watchlist",
    primaryKeys = ["userId", "mediaId"]
)
data class WatchlistEntity(
    val userId: String,
    val mediaId: String,
    val addedAt: Long = System.currentTimeMillis()
)
