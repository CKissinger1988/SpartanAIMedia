package com.spartanai.spartanaimedia.data.remote

import android.net.Uri

object AdBlocker {
    private val AD_DOMAINS = listOf(
        "doubleclick.net",
        "admob.com",
        "googleadservices.com",
        "ads.youtube.com",
        "llvpn.com", // Tracking/Ads found in watchseries
        "sstatic1.histats.com", // Tracking
        "popads.net",
        "adsterra.com",
        "propellerads.com",
        "exoclick.com",
        "onclickads.net",
        "syndication.exdynsrv.com"
    )

    fun isAd(uri: Uri): Boolean {
        val host = uri.host ?: return false
        val path = uri.path ?: ""
        
        // Check domains
        if (AD_DOMAINS.any { host.contains(it) }) {
            return true
        }
        
        // Check common ad paths/keywords in URL
        if (path.contains("/ad/") || path.contains("/ads/") || path.contains("ad_video") || path.contains("banner")) {
            return true
        }
        
        return false
    }
}
