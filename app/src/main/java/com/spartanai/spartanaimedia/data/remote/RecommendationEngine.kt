package com.spartanai.spartanaimedia.data.remote

import com.spartanai.spartanaimedia.domain.model.MediaItem

class RecommendationEngine {
    
    /**
     * Generates personalized recommendations based on the user's watch history.
     */
    fun getPersonalizedRecommendations(
        allItems: List<MediaItem>,
        history: List<MediaItem> // Items the user has interacted with (progress > 0)
    ): List<MediaItem> {
        
        // If there's no history, just return top-rated items
        if (history.isEmpty()) {
            return allItems.sortedByDescending { it.rating }.take(10)
        }

        // 1. Build a profile of the user's favorite genres
        val genreWeights = mutableMapOf<String, Float>()
        history.forEach { item ->
            // Give more weight to items they watched further into
            val weight = if (item.progress > 0.8f) 2.0f else 1.0f 
            genreWeights[item.genre] = (genreWeights[item.genre] ?: 0f) + weight
        }

        // 2. Score un-watched items against this profile
        val unwatchedItems = allItems.filter { it.progress == 0f }
        
        return unwatchedItems
            .map { item ->
                var score = 0f
                
                // Match against favorite genres
                score += (genreWeights[item.genre] ?: 0f) * 10f
                
                // Boost by base rating
                score += item.rating
                
                item to score
            }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(10)
    }

    /**
     * Legacy support for "More Like This" when viewing a specific item.
     */
    fun getRelatedContent(currentItem: MediaItem, allItems: List<MediaItem>): List<MediaItem> {
        return allItems
            .filter { it.id != currentItem.id } // Don't recommend itself
            .map { item ->
                var score = 0f
                
                // 1. Genre Match (High weight)
                if (item.genre == currentItem.genre) score += 50f
                
                // 2. Director Match
                if (item.director == currentItem.director && item.director.isNotBlank()) score += 30f
                
                // 3. Category Match (Medium weight)
                if (item.category == currentItem.category) score += 20f
                
                // 4. Cast Overlap
                val intersection = currentItem.cast.intersect(item.cast.toSet())
                score += (intersection.size * 10f)
                
                item to score
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(6)
    }
}
