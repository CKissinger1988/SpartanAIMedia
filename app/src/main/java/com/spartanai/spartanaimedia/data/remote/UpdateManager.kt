package com.spartanai.spartanaimedia.data.remote

import android.content.Context
import com.spartanai.spartanaimedia.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

@Serializable
data class GitHubRelease(
    val tag_name: String,
    val html_url: String,
    val assets: List<GitHubAsset>
)

@Serializable
data class GitHubAsset(
    val browser_download_url: String,
    val name: String
)

data class UpdateInfo(
    val latestVersion: String,
    val downloadUrl: String,
    val updateAvailable: Boolean,
    val isForceUpdate: Boolean = false
)

class UpdateManager(private val context: Context) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val repoUrl = "https://api.github.com/repos/CKissinger1988/SpartanAIMedia/releases/latest"

    fun checkForUpdates(): Flow<UpdateInfo?> = flow {
        emit(performManualCheck())
    }

    suspend fun performManualCheck(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(repoUrl).build()
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    val release = json.decodeFromString<GitHubRelease>(body)
                    val latestVersion = release.tag_name.removePrefix("v")
                    val currentVersion = BuildConfig.VERSION_NAME
                    
                    val updateAvailable = isVersionNewer(latestVersion, currentVersion)
                    val isForce = isMajorUpdate(latestVersion, currentVersion)
                    val downloadUrl = release.assets.find { it.name.endsWith(".apk") }?.browser_download_url ?: release.html_url
                    
                    return@withContext UpdateInfo(latestVersion, downloadUrl, updateAvailable, isForceUpdate = isForce)
                }
            }
            return@withContext null
        } catch (e: Exception) {
            return@withContext null
        }
    }

    private fun isVersionNewer(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        
        for (i in 0 until minOf(latestParts.size, currentParts.size)) {
            if (latestParts[i] > currentParts[i]) return true
            if (latestParts[i] < currentParts[i]) return false
        }
        return latestParts.size > currentParts.size
    }

    private fun isMajorUpdate(latest: String, current: String): Boolean {
        val latestMajor = latest.split(".").firstOrNull()?.toIntOrNull() ?: 0
        val currentMajor = current.split(".").firstOrNull()?.toIntOrNull() ?: 0
        return latestMajor > currentMajor
    }
}
