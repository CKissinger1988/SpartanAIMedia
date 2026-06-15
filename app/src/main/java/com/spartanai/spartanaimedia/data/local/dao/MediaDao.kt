package com.spartanai.spartanaimedia.data.local.dao

import androidx.room.*
import com.spartanai.spartanaimedia.data.local.entity.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items")
    fun getAllMediaItems(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchMediaItems(query: String): Flow<List<MediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMediaItems(items: List<MediaEntity>): List<Long>

    @Query("DELETE FROM media_items")
    fun deleteAllMediaItems(): Int

    @Query("SELECT * FROM media_items WHERE id = :id")
    fun getMediaItemById(id: String): MediaEntity?

    @Query("DELETE FROM media_items WHERE id = :id")
    fun deleteMediaItemById(id: String)

    @Update
    fun updateMediaItem(item: MediaEntity)
}
