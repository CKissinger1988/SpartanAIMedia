package com.spartanai.spartanaimedia.data.remote

import com.spartanai.spartanaimedia.domain.model.MediaItem

class RecommendationEngine {
    
    fun getRelatedContent(currentItem: MediaItem, allItems: List<MediaItem>): List<MediaItem> {
        return allItems
            .filter { it.id != currentItem.id } // Don't recommend itself
            .map { item ->
                var score = 0
                
                // 1. Genre Match (High weight)
                if (item.genre == currentItem.genre) score += 50
                
                // 2. Category Match (Medium weight)
                if (item.category == currentItem.category) score += 30
                
                // 3. Title Overlap (Keyword analysis)
                val currentKeywords = currentItem.title.lowercase().split(" ").toSet()
                val itemKeywords = item.title.lowercase().split(" ").toSet()
                val intersection = currentKeywords.intersect(itemKeywords)
                if (intersection.isNotEmpty()) score += 20
                
                item to score
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(6)
    }
}
