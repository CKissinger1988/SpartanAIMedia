package com.spartanai.spartanaimedia.data.remote

import android.content.Context
import com.spartanai.spartanaimedia.domain.model.MediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class PreloadManager(private val context: Context) {
    private val client = OkHttpClient()
    private val cacheDir = File(context.cacheDir, "media_preload")
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        if (!cacheDir.exists()) cacheDir.mkdirs()
    }

    fun preloadItems(items: List<MediaItem>) {
        items.take(3).forEach { item ->
            preloadItem(item)
        }
    }

    private fun preloadItem(item: MediaItem) {
        val file = File(cacheDir, "${item.id}.chunk")
        if (file.exists()) return

        scope.launch {
            try {
                // Fetch first 2MB for instant start
                val request = Request.Builder()
                    .url(item.mediaUrl)
                    .addHeader("Range", "bytes=0-2097152")
                    .build()
                
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.byteStream()?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            } catch (e: Exception) {
                // Silent fail for background optimization
            }
        }
    }
}
