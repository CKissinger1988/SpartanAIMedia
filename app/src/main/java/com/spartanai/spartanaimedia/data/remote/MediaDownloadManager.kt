package com.spartanai.spartanaimedia.data.remote

import android.content.Context
import com.spartanai.spartanaimedia.domain.model.ProxyConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.UUID

class MediaDownloadManager(private val context: Context) {

    /**
     * Downloads media using a stealth OkHttp client.
     * Bypasses the system DownloadManager completely to prevent intent broadcasting,
     * system notifications, and OS-level file tracking.
     * 
     * @param url The media URL.
     * @param title The media title.
     * @param isSecure Whether to use secure mode (always uses internal storage now).
     * @param proxyConfig Optional proxy configuration to mask the IP during download.
     */
    fun downloadMedia(url: String, title: String, isSecure: Boolean, proxyConfig: ProxyConfig? = null): String {
        val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9]"), "_")
        val fileName = if (isSecure) "${UUID.randomUUID()}.blob" else "$sanitizedTitle.mp4"
        val folder = if (isSecure) "secure_media" else "Movies"
        val localPath = "$folder/$fileName"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val clientBuilder = OkHttpClient.Builder()
                
                // Route download through proxy if provided
                if (proxyConfig != null && proxyConfig.isEnabled) {
                    val proxyType = when (proxyConfig.type) {
                        ProxyConfig.ProxyType.HTTP -> Proxy.Type.HTTP
                        ProxyConfig.ProxyType.SOCKS, ProxyConfig.ProxyType.TOR -> Proxy.Type.SOCKS
                    }
                    val proxy = Proxy(proxyType, InetSocketAddress(proxyConfig.host, proxyConfig.port))
                    clientBuilder.proxy(proxy)
                }

                val client = clientBuilder.build()
                val request = Request.Builder()
                    .url(url)
                    // Spoof User-Agent to prevent fingerprinting
                    .header("User-Agent", if (isSecure) "SpartanAI/1.0 (Anonymous)" else "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .build()

                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val body = response.body
                    if (body != null) {
                        val dir = File(context.filesDir, folder)
                        if (!dir.exists()) dir.mkdirs()
                        
                        val file = File(dir, fileName)
                        
                        // Stream write
                        val inputStream = body.byteStream()
                        val outputStream = FileOutputStream(file)
                        
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                        
                        outputStream.flush()
                        outputStream.close()
                        inputStream.close()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return localPath
    }

    /**
     * Resolves the obfuscated or standard file path securely.
     */
    fun getFileForPath(localPath: String): File? {
        // We now solely use internal storage
        val file = File(context.filesDir, localPath)
        return if (file.exists()) file else null
    }
}
