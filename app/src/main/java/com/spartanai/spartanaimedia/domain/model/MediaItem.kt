package com.spartanai.spartanaimedia.domain.model

data class MediaItem(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val mediaUrl: String,
    val description: String = "",
    val category: String = "General",
    val genre: String = "Unknown",
    val resolution: String = "1080p",
    val downloadPath: String? = null,
    val isDownloaded: Boolean = false,
    val lastPlaybackPosition: Long = 0L,
    val totalDuration: Long = 0L
) {
    val progress: Float
        get() = if (totalDuration > 0) lastPlaybackPosition.toFloat() / totalDuration else 0f
}
