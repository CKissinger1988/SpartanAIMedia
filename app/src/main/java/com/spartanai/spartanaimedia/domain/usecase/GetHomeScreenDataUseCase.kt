package com.spartanai.spartanaimedia.domain.usecase

import com.spartanai.spartanaimedia.domain.model.MediaItem
import com.spartanai.spartanaimedia.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetHomeScreenDataUseCase(
    private val repository: MediaRepository
) {
    data class HomeData(
        val continueWatching: List<MediaItem>,
        val recommendations: List<MediaItem>,
        val trending: List<MediaItem>,
        val movies: List<MediaItem>,
        val series: List<MediaItem>
    )

    operator fun invoke(userId: String): Flow<HomeData> {
        return combine(
            repository.getMediaItems(),
            repository.getWatchlist(userId)
        ) { allItems, watchlist ->
            HomeData(
                continueWatching = allItems.filter { it.progress > 0f && it.progress < 0.95f }
                    .sortedByDescending { it.lastPlaybackPosition },
                recommendations = allItems.filter { it.progress == 0f }
                    .sortedByDescending { it.rating }
                    .take(10),
                trending = allItems.sortedByDescending { it.rating }.take(5),
                movies = allItems.filter { it.category == "Movies" },
                series = allItems.filter { it.category == "Series" }
            )
        }
    }
}
