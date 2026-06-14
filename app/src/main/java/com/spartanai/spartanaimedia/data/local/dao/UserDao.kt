package com.spartanai.spartanaimedia.data.local.dao

import androidx.room.*
import com.spartanai.spartanaimedia.data.local.entity.UserProfileEntity
import com.spartanai.spartanaimedia.data.local.entity.WatchlistEntity
import com.spartanai.spartanaimedia.data.local.entity.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profiles")
    fun getAllProfiles(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    fun getProfileById(userId: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProfile(profile: UserProfileEntity)

    @Query("SELECT m.* FROM media_items m INNER JOIN watchlist w ON m.id = w.mediaId WHERE w.userId = :userId")
    fun getWatchlistForUser(userId: String): Flow<List<MediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addToWatchlist(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE userId = :userId AND mediaId = :mediaId")
    fun removeFromWatchlist(userId: String, mediaId: String)
}
