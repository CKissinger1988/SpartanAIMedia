package com.spartanai.spartanaimedia.data.remote

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File

class MediaDownloadManager(private val context: Context) {

    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    /**
     * @param isSecure If true, downloads to private app storage. If false, to public Movies folder.
     */
    fun downloadMedia(url: String, title: String, isSecure: Boolean): Long {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(title)
            .setDescription(if (isSecure) "Secure anonymous download" else "Standard download")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .addRequestHeader("User-Agent", "SpartanAI/1.0 (Anonymous)") // Secure/Anonymous header

        if (isSecure) {
            // Private storage - not visible to other apps or gallery
            val file = File(context.getExternalFilesDir(null), "secure_media/$title.mp4")
            file.parentFile?.mkdirs()
            request.setDestinationUri(Uri.fromFile(file))
        } else {
            // Standard storage
            request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_MOVIES, "$title.mp4")
        }

        return downloadManager.enqueue(request)
    }

    fun getFileForPath(localPath: String): File? {
        val secureFile = File(context.getExternalFilesDir(null), localPath)
        if (secureFile.exists()) return secureFile
        
        val publicFile = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), localPath.substringAfterLast("/"))
        return if (publicFile.exists()) publicFile else null
    }
}
