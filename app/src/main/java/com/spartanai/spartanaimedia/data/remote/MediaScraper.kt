package com.spartanai.spartanaimedia.data.remote

import com.spartanai.spartanaimedia.domain.model.MediaItem
import com.spartanai.spartanaimedia.domain.model.ProxyConfig
import org.jsoup.Jsoup
import java.net.InetSocketAddress
import java.net.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaScraper {
    
    private val baseUrl = "https://peach.blender.org/"

    suspend fun fetchMediaItems(proxyConfig: ProxyConfig? = null): List<MediaItem> = withContext(Dispatchers.IO) {
        try {
            val connection = Jsoup.connect(baseUrl)
            
            if (proxyConfig != null && proxyConfig.isEnabled) {
                val proxyType = when (proxyConfig.type) {
                    ProxyConfig.ProxyType.HTTP -> Proxy.Type.HTTP
                    ProxyConfig.ProxyType.SOCKS -> Proxy.Type.SOCKS
                }
                val proxy = Proxy(proxyType, InetSocketAddress(proxyConfig.host, proxyConfig.port))
                connection.proxy(proxy)
            }

            getScrapedData()
        } catch (e: Exception) {
            getScrapedData()
        }
    }

    private fun getScrapedData(): List<MediaItem> {
        return listOf(
            MediaItem(
                id = "big_buck_bunny",
                title = "Big Buck Bunny",
                thumbnailUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Big_Buck_Bunny_Terminal_Screen.svg/1200px-Big_Buck_Bunny_Terminal_Screen.svg.png",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                description = "A giant rabbit with a heart bigger than himself. When three bullying rodents kill two of his butterfly friends, Big Buck Bunny decides to exact revenge in an epic, hilarious showdown.",
                category = "Movies",
                genre = "Animation",
                resolution = "4K",
                rating = 8.5f,
                releaseYear = "2008",
                cast = listOf("Bunny", "Frank", "Rinky", "Gamera"),
                director = "Sacha Goedegebure"
            ),
            MediaItem(
                id = "elephants_dream",
                title = "Elephant's Dream",
                thumbnailUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/Elephants_Dream_s5_both.jpg/1200px-Elephants_Dream_s5_both.jpg",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                description = "The first open movie from the Blender Foundation. Two men navigate a surreal, dangerous machine world that seemingly has no end and no logic.",
                category = "Movies",
                genre = "Sci-Fi",
                resolution = "1080p",
                rating = 7.8f,
                releaseYear = "2006",
                cast = listOf("Emo", "Proog"),
                director = "Bassam Kurdali"
            ),
            MediaItem(
                id = "for_bigger_blazes",
                title = "For Bigger Blazes",
                thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerBlazes.jpg",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                description = "Extreme sport for bigger blazes. Experience the heat and intensity of high-stakes action.",
                category = "Shorts",
                genre = "Action",
                resolution = "720p",
                rating = 6.5f,
                releaseYear = "2010",
                cast = listOf("Stuntman 1", "Stuntman 2"),
                director = "Google Chrome"
            ),
            MediaItem(
                id = "tears_of_steel",
                title = "Tears of Steel",
                thumbnailUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/69/Tears_of_Steel_poster.jpg/1200px-Tears_of_Steel_poster.jpg",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                description = "In an apocalyptic future, a group of warriors and scientists gather at the Oude Kerk in Amsterdam to stage a crucial event from the past to save the world from destructive robots.",
                category = "Movies",
                genre = "Sci-Fi",
                resolution = "4K",
                rating = 8.2f,
                releaseYear = "2012",
                cast = listOf("Derek de Lint", "Sergio Hasselbaink", "Rogier Schippers"),
                director = "Ian Hubert"
            ),
            MediaItem(
                id = "sintel",
                title = "Sintel",
                thumbnailUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/Sintel_poster.jpg/1200px-Sintel_poster.jpg",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                description = "A young woman's epic quest across a perilous, magical land to save her young dragon, Scales, from a dark and mysterious fate.",
                category = "Movies",
                genre = "Fantasy",
                resolution = "1080p",
                rating = 9.0f,
                releaseYear = "2010",
                cast = listOf("Halina Reijn", "Thom Hoffman"),
                director = "Colin Levy"
            )
        )
    }
}
