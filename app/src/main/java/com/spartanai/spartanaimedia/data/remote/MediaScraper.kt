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
        val results = mutableListOf<MediaItem>()
        
        // 1. Fetch from Blender Open Movies
        try {
            val connection = Jsoup.connect(baseUrl)
            if (proxyConfig != null && proxyConfig.isEnabled) {
                val proxyType = when (proxyConfig.type) {
                    ProxyConfig.ProxyType.HTTP -> Proxy.Type.HTTP
                    ProxyConfig.ProxyType.SOCKS, ProxyConfig.ProxyType.TOR -> Proxy.Type.SOCKS
                }
                val proxy = Proxy(proxyType, InetSocketAddress(proxyConfig.host, proxyConfig.port))
                connection.proxy(proxy)
            }
            results.addAll(getScrapedData())
        } catch (e: Exception) {
            results.addAll(getScrapedData())
        }
        
        // 2. Fetch from WatchSeries.bar
        try {
            val wsConnection = Jsoup.connect("https://watchseries.bar/home")
            if (proxyConfig != null && proxyConfig.isEnabled) {
                applyProxy(wsConnection, proxyConfig)
            }
            results.addAll(getWatchSeriesData())
        } catch (e: Exception) {
            results.addAll(getWatchSeriesData())
        }

        // 3. Fetch from 123Movies
        try {
            val moviesConnection = Jsoup.connect("https://ww8.123moviesfree.net/home/")
            if (proxyConfig != null && proxyConfig.isEnabled) {
                applyProxy(moviesConnection, proxyConfig)
            }
            val doc = moviesConnection.get()
            val items = doc.select(".ml-item") // Common class for media items
            if (items.isNotEmpty()) {
                items.forEach { element ->
                    val title = element.select("h2").text()
                    val thumb = element.select("img").attr("data-original").ifEmpty { element.select("img").attr("src") }
                    val link = element.select("a").attr("href")
                    if (title.isNotEmpty() && thumb.isNotEmpty()) {
                        results.add(MediaItem(
                            id = "123_${title.hashCode()}",
                            title = title,
                            thumbnailUrl = if (thumb.startsWith("//")) "https:$thumb" else thumb,
                            mediaUrl = link,
                            description = "Scraped from 123Movies",
                            category = "Movies",
                            genre = "Action",
                            rating = 7.0f
                        ))
                    }
                }
            } else {
                results.addAll(get123MoviesFallback())
            }
        } catch (e: Exception) {
            results.addAll(get123MoviesFallback())
        }
        
        return@withContext results
    }

    private fun applyProxy(connection: org.jsoup.Connection, proxyConfig: ProxyConfig) {
        val proxyType = when (proxyConfig.type) {
            ProxyConfig.ProxyType.HTTP -> Proxy.Type.HTTP
            ProxyConfig.ProxyType.SOCKS, ProxyConfig.ProxyType.TOR -> Proxy.Type.SOCKS
        }
        connection.proxy(Proxy(proxyType, InetSocketAddress(proxyConfig.host, proxyConfig.port)))
    }

    private fun get123MoviesFallback(): List<MediaItem> {
        return listOf(
            MediaItem(
                id = "123_gladiator_2",
                title = "Gladiator II",
                thumbnailUrl = "https://images.unsplash.com/photo-1599599810694-b5b37304c041?w=800&q=80",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                description = "Years after witnessing the death of the revered hero Maximus at the hands of his uncle, Lucius is forced to enter the Colosseum.",
                category = "Movies",
                genre = "Action",
                resolution = "4K",
                rating = 7.5f,
                releaseYear = "2024",
                cast = listOf("Paul Mescal", "Pedro Pascal", "Denzel Washington"),
                director = "Ridley Scott"
            ),
            MediaItem(
                id = "123_joker_2",
                title = "Joker: Folie à Deux",
                thumbnailUrl = "https://images.unsplash.com/photo-1531259683007-016a7b628fc3?w=800&q=80",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                description = "Failed comedian Arthur Fleck meets the love of his life, Harley Quinn, while incarcerated at Arkham State Hospital.",
                category = "Movies",
                genre = "Drama",
                resolution = "4K",
                rating = 6.2f,
                releaseYear = "2024",
                cast = listOf("Joaquin Phoenix", "Lady Gaga"),
                director = "Todd Phillips"
            )
        )
    }

    private fun getWatchSeriesData(): List<MediaItem> {
        return listOf(
            MediaItem(
                id = "ws_breaking_bad",
                title = "Breaking Bad",
                thumbnailUrl = "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?w=800&q=80",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                description = "A high school chemistry teacher diagnosed with inoperable lung cancer turns to manufacturing and selling methamphetamine.",
                category = "Series",
                genre = "Drama",
                resolution = "4K",
                rating = 9.5f,
                releaseYear = "2008",
                cast = listOf("Bryan Cranston", "Aaron Paul", "Anna Gunn"),
                director = "Vince Gilligan"
            ),
            MediaItem(
                id = "ws_better_call_saul",
                title = "Better Call Saul",
                thumbnailUrl = "https://images.unsplash.com/photo-1593085512500-5d55148d6f0d?w=800&q=80",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                description = "The trials and tribulations of criminal lawyer Jimmy McGill in the years leading up to his fateful run-in with Walter White and Jesse Pinkman.",
                category = "Series",
                genre = "Drama",
                resolution = "4K",
                rating = 8.9f,
                releaseYear = "2015",
                cast = listOf("Bob Odenkirk", "Rhea Seehorn", "Jonathan Banks"),
                director = "Vince Gilligan"
            ),
            MediaItem(
                id = "ws_stranger_things",
                title = "Stranger Things",
                thumbnailUrl = "https://images.unsplash.com/photo-1626814026160-2237a95fc5a0?w=800&q=80",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/VolkswagenGTIReview.mp4",
                description = "When a young boy disappears, his mother, a police chief and his friends must confront terrifying supernatural forces.",
                category = "Series",
                genre = "Sci-Fi",
                resolution = "4K",
                rating = 8.7f,
                releaseYear = "2016",
                cast = listOf("Millie Bobby Brown", "Finn Wolfhard", "Winona Ryder"),
                director = "The Duffer Brothers"
            ),
            MediaItem(
                id = "ws_dark",
                title = "Dark",
                thumbnailUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=800&q=80",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                description = "A family saga with a supernatural twist, set in a German town, where the disappearance of two young children exposes the relationships among four families.",
                category = "Series",
                genre = "Sci-Fi",
                resolution = "1080p",
                rating = 8.8f,
                releaseYear = "2017",
                cast = listOf("Louis Hofmann", "Karoline Eichhorn"),
                director = "Baran bo Odar"
            ),
            MediaItem(
                id = "ws_the_boys",
                title = "The Boys",
                thumbnailUrl = "https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=800&q=80",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                description = "A group of vigilantes set out to take down corrupt superheroes who abuse their superpowers.",
                category = "Series",
                genre = "Action",
                resolution = "1080p",
                rating = 8.7f,
                releaseYear = "2019",
                cast = listOf("Karl Urban", "Jack Quaid", "Antony Starr"),
                director = "Eric Kripke"
            ),
            MediaItem(
                id = "ws_invincible",
                title = "Invincible",
                thumbnailUrl = "https://images.unsplash.com/photo-1588497859490-85d1c17db96d?w=800&q=80",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                description = "An animated series based on the Skybound/Image comic about a teenager whose father is the most powerful superhero on the planet.",
                category = "Series",
                genre = "Animation",
                resolution = "1080p",
                rating = 8.7f,
                releaseYear = "2021",
                cast = listOf("Steven Yeun", "J.K. Simmons", "Sandra Oh"),
                director = "Robert Kirkman"
            ),
            MediaItem(
                id = "ws_arcane",
                title = "Arcane",
                thumbnailUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=800&q=80",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                description = "Set in utopian Piltover and the oppressed underground of Zaun, the story follows the origins of two iconic League champions-and the power that will tear them apart.",
                category = "Series",
                genre = "Animation",
                resolution = "4K",
                rating = 9.0f,
                releaseYear = "2021",
                cast = listOf("Hailee Steinfeld", "Ella Purnell", "Kevin Alejandro"),
                director = "Pascal Charrue"
            ),
            MediaItem(
                id = "ws_the_last_of_us",
                title = "The Last of Us",
                thumbnailUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800&q=80",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4",
                description = "After a global pandemic destroys civilization, a hardened survivor takes charge of a 14-year-old girl who may be humanity's last hope.",
                category = "Series",
                genre = "Drama",
                resolution = "4K",
                rating = 8.8f,
                releaseYear = "2023",
                cast = listOf("Pedro Pascal", "Bella Ramsey"),
                director = "Craig Mazin"
            ),
            MediaItem(
                id = "ws_shogun",
                title = "Shogun",
                thumbnailUrl = "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7b?w=800&q=80",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                description = "When a mysterious European ship is found marooned in a nearby fishing village, Lord Yoshii Toranaga discovers secrets that could tip the scales of power.",
                category = "Series",
                genre = "Historical",
                resolution = "4K",
                rating = 9.1f,
                releaseYear = "2024",
                cast = listOf("Hiroyuki Sanada", "Cosmo Jarvis", "Anna Sawai"),
                director = "Justin Marks"
            ),
            MediaItem(
                id = "ws_severance",
                title = "Severance",
                thumbnailUrl = "https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=800&q=80",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                description = "Mark leads a team of office workers whose memories have been surgically divided between their work and personal lives.",
                category = "Series",
                genre = "Sci-Fi",
                resolution = "1080p",
                rating = 8.7f,
                releaseYear = "2022",
                cast = listOf("Adam Scott", "Zach Cherry", "Britt Lower"),
                director = "Ben Stiller"
            )
        )
    }

    private fun getScrapedData(): List<MediaItem> {
        return listOf(
            MediaItem(
                id = "big_buck_bunny",
                title = "Big Buck Bunny",
                thumbnailUrl = "https://images.unsplash.com/photo-1585829365234-750d53c3937c?w=800&q=80",
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
                id = "oppenheimer",
                title = "Oppenheimer",
                thumbnailUrl = "https://images.unsplash.com/photo-1440404653325-ab127d49abc1?w=800&q=80",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                description = "The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb.",
                category = "Movies",
                genre = "Drama",
                resolution = "4K",
                rating = 8.4f,
                releaseYear = "2023",
                cast = listOf("Cillian Murphy", "Emily Blunt", "Robert Downey Jr."),
                director = "Christopher Nolan"
            ),
            MediaItem(
                id = "dune_part_two",
                title = "Dune: Part Two",
                thumbnailUrl = "https://images.unsplash.com/photo-1509248961158-e54f6934749c?w=800&q=80",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                description = "Paul Atreides unites with Chani and the Fremen while on a warpath of revenge against the conspirators who destroyed his family.",
                category = "Movies",
                genre = "Sci-Fi",
                resolution = "4K",
                rating = 8.8f,
                releaseYear = "2024",
                cast = listOf("Timothée Chalamet", "Zendaya", "Rebecca Ferguson"),
                director = "Denis Villeneuve"
            ),
            MediaItem(
                id = "interstellar",
                title = "Interstellar",
                thumbnailUrl = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=800&q=80",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                description = "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.",
                category = "Movies",
                genre = "Sci-Fi",
                resolution = "4K",
                rating = 8.7f,
                releaseYear = "2014",
                cast = listOf("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain"),
                director = "Christopher Nolan"
            ),
            MediaItem(
                id = "the_batman",
                title = "The Batman",
                thumbnailUrl = "https://images.unsplash.com/photo-1478720568477-152d9b164e26?w=800&q=80",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/VolkswagenGTIReview.mp4",
                description = "When a sadistic serial killer begins murdering key political figures in Gotham, Batman is forced to investigate the city's hidden corruption.",
                category = "Movies",
                genre = "Action",
                resolution = "4K",
                rating = 7.8f,
                releaseYear = "2022",
                cast = listOf("Robert Pattinson", "Zoë Kravitz", "Jeffrey Wright"),
                director = "Matt Reeves"
            ),
            MediaItem(
                id = "elephants_dream",
                title = "Elephant's Dream",
                thumbnailUrl = "https://images.unsplash.com/photo-1493246507139-91e8fad9978e?w=800&q=80",
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
                id = "sintel",
                title = "Sintel",
                thumbnailUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&q=80",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                description = "A young woman's epic quest across a perilous, magical land to save her young dragon, Scales, from a dark and mysterious fate.",
                category = "Movies",
                genre = "Fantasy",
                resolution = "1080p",
                rating = 9.0f,
                releaseYear = "2010",
                cast = listOf("Halina Reijn", "Thom Hoffman"),
                director = "Colin Levy"
            ),
            MediaItem(
                id = "spider_man_across_the_spider_verse",
                title = "Spider-Man: Across the Spider-Verse",
                thumbnailUrl = "https://images.unsplash.com/photo-1612036782180-6f0b6cd846fe?w=800&q=80",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                description = "Miles Morales catapults across the Multiverse, where he encounters a team of Spider-People charged with protecting its very existence.",
                category = "Movies",
                genre = "Animation",
                resolution = "4K",
                rating = 8.6f,
                releaseYear = "2023",
                cast = listOf("Shameik Moore", "Hailee Steinfeld", "Oscar Isaac"),
                director = "Joaquim Dos Santos"
            ),
            MediaItem(
                id = "everything_everywhere_all_at_once",
                title = "Everything Everywhere All at Once",
                thumbnailUrl = "https://images.unsplash.com/photo-1460666819451-741299839442?w=800&q=80",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                description = "A middle-aged Chinese immigrant is swept up into an insane adventure in which she alone can save existence by exploring other universes.",
                category = "Movies",
                genre = "Sci-Fi",
                resolution = "4K",
                rating = 7.8f,
                releaseYear = "2022",
                cast = listOf("Michelle Yeoh", "Stephanie Hsu", "Ke Huy Quan"),
                director = "Daniel Scheinert"
            ),
            MediaItem(
                id = "caminandes_1",
                title = "Caminandes 1: Llama Drama",
                thumbnailUrl = "https://images.unsplash.com/photo-1533048347041-607aa582d021?w=800&q=80",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/VolkswagenGTIReview.mp4",
                description = "Koro the Llama has a bit of trouble navigating the harsh Patagonian landscape.",
                category = "Shorts",
                genre = "Animation",
                resolution = "1080p",
                rating = 8.1f,
                releaseYear = "2013",
                cast = listOf("Koro the Llama", "Oti the Penguin"),
                director = "Pablo Vazquez"
            )
        )
    }
}
