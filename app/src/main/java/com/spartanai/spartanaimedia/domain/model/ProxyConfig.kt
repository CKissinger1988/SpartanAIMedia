package com.spartanai.spartanaimedia.domain.model

data class ProxyConfig(
    val host: String,
    val port: Int,
    val type: ProxyType = ProxyConfig.ProxyType.HTTP,
    val username: String? = null,
    val password: String? = null,
    val isEnabled: Boolean = false
) {
    enum class ProxyType {
        HTTP, SOCKS
    }
}
