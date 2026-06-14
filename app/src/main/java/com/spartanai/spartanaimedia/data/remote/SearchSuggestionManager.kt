package com.spartanai.spartanaimedia.data.remote

import com.spartanai.spartanaimedia.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchSuggestionManager {
    
    fun getSuggestions(query: String, allItems: List<MediaItem>): List<String> {
        if (query.isBlank()) return emptyList()
        
        val normalizedQuery = query.trim().lowercase()
        val suggestions = mutableSetOf<String>()

        // 1. Title matches
        allItems.filter { it.title.lowercase().contains(normalizedQuery) }
            .take(5)
            .forEach { suggestions.add(it.title) }

        // 2. Genre matches
        allItems.filter { it.genre.lowercase().contains(normalizedQuery) }
            .map { it.genre }
            .distinct()
            .take(3)
            .forEach { suggestions.add(it) }

        // 3. Category matches
        allItems.filter { it.category.lowercase().contains(normalizedQuery) }
            .map { it.category }
            .distinct()
            .take(2)
            .forEach { suggestions.add(it) }

        return suggestions.toList()
    }
}
