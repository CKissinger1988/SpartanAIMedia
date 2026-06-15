package com.spartanai.spartanaimedia.domain.usecase

import com.spartanai.spartanaimedia.domain.model.MediaItem
import com.spartanai.spartanaimedia.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetRecommendationsUseCase(
    private val repository: MediaRepository
) {
    operator fun invoke(userId: String): Flow<List<MediaItem>> {
        return repository.getMediaItems().map { allItems ->
            // In a real scenario, we'd use the RecommendationEngine logic here,
            // but for Clean Architecture, we keep the domain logic in the UseCase
            // and maybe delegate to a domain-level service if complex.
            // For now, we'll return items the user hasn't finished yet as "re-recommendations"
            // and top-rated items they haven't seen.
            allItems.filter { it.progress < 0.95f }
                .sortedByDescending { it.rating }
                .take(10)
        }
    }
}
