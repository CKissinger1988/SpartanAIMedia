package com.spartanai.spartanaimedia.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spartanai.spartanaimedia.domain.model.MediaItem

@Entity(tableName = "media_items")
data class MediaEntity(
    @PrimaryKey val id: String,
    val title: String,
    val thumbnailUrl: String,
    val mediaUrl: String,
    val description: String,
    val category: String,
    val genre: String,
    val resolution: String,
    val downloadPath: String?,
    val isDownloaded: Boolean,
    val isFavorite: Boolean = false,
    val lastPlaybackPosition: Long = 0L,
    val totalDuration: Long = 0L,
    val rating: Float = 0.0f,
    val releaseYear: String = "2026",
    val director: String = "Spartan Director"
)

fun MediaEntity.toDomainModel(): MediaItem {
    return MediaItem(
        id = id,
        title = title,
        thumbnailUrl = thumbnailUrl,
        mediaUrl = mediaUrl,
        description = description,
        category = category,
        genre = genre,
        resolution = resolution,
        downloadPath = downloadPath,
        isDownloaded = isDownloaded,
        isFavorite = isFavorite,
        lastPlaybackPosition = lastPlaybackPosition,
        totalDuration = totalDuration,
        rating = rating,
        releaseYear = releaseYear,
        director = director
    )
}

fun MediaItem.toEntity(): MediaEntity {
    return MediaEntity(
        id = id,
        title = title,
        thumbnailUrl = thumbnailUrl,
        mediaUrl = mediaUrl,
        description = description,
        category = category,
        genre = genre,
        resolution = resolution,
        downloadPath = downloadPath,
        isDownloaded = isDownloaded,
        isFavorite = isFavorite,
        lastPlaybackPosition = lastPlaybackPosition,
        totalDuration = totalDuration,
        rating = rating,
        releaseYear = releaseYear,
        director = director
    )
}
