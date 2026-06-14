package com.spartanai.spartanaimedia.domain.model

data class UserProfile(
    val userId: String,
    val username: String,
    val avatarUrl: String,
    val isAnonymous: Boolean = false,
    val proxyConfig: ProxyConfig? = null,
    val piUsername: String? = null,
    val piWalletAddress: String? = null,
    val isPiNodeActive: Boolean = false
)
