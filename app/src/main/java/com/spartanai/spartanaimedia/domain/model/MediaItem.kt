package com.spartanai.spartanaimedia.domain.model

data class MediaItem(
    val id: String,
    val title: String,
    val description: String,
    val mediaUrl: String,
    val thumbnailUrl: String,
    val category: String,
    val genre: String = "Action",
    val resolution: String = "1080p",
    val downloadPath: String? = null,
    val isDownloaded: Boolean = false,
    val isFavorite: Boolean = false,
    val lastPlaybackPosition: Long = 0L,
    val totalDuration: Long = 0L,
    val rating: Float = 0.0f,
    val releaseYear: String = "2026",
    val cast: List<String> = emptyList(),
    val director: String = "Spartan Director"
) {
    val progress: Float
        get() = if (totalDuration > 0) lastPlaybackPosition.toFloat() / totalDuration else 0f
}

