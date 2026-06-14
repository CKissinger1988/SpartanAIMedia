package com.spartanai.spartanaimedia.data.remote

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.ServerSocket

data class PeerDevice(
    val name: String,
    val host: String,
    val port: Int
)

class P2PManager(private val context: Context) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val serviceType = "_spartanai._tcp"
    private var serviceName = "SpartanNode-${java.util.UUID.randomUUID().toString().take(4)}"
    
    private val _peers = MutableStateFlow<List<PeerDevice>>(emptyList())
    val peers = _peers.asStateFlow()

    private var serverSocket: ServerSocket? = null

    private val registrationListener = object : NsdManager.RegistrationListener {
        override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
            serviceName = NsdServiceInfo.serviceName
            Log.d("P2P", "Service registered: $serviceName")
        }
        override fun onRegistrationFailed(arg0: NsdServiceInfo, arg1: Int) {}
        override fun onServiceUnregistered(arg0: NsdServiceInfo) {}
        override fun onUnregistrationFailed(arg0: NsdServiceInfo, arg1: Int) {}
    }

    private val discoveryListener = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(regType: String) {
            Log.d("P2P", "Discovery started")
        }
        override fun onServiceFound(service: NsdServiceInfo) {
            if (service.serviceType == serviceType && service.serviceName != serviceName) {
                nsdManager.resolveService(service, resolveListener)
            }
        }
        override fun onServiceLost(service: NsdServiceInfo) {
            _peers.value = _peers.value.filter { it.name != service.serviceName }
        }
        override fun onDiscoveryStopped(serviceType: String) {}
        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            nsdManager.stopServiceDiscovery(this)
        }
        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            nsdManager.stopServiceDiscovery(this)
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            val peer = PeerDevice(
                name = serviceInfo.serviceName,
                host = serviceInfo.host.hostAddress ?: "",
                port = serviceInfo.port
            )
            val currentList = _peers.value.toMutableList()
            if (currentList.none { it.name == peer.name }) {
                currentList.add(peer)
                _peers.value = currentList
            }
        }
    }

    fun startP2P() {
        // Initialize Server Socket
        serverSocket = ServerSocket(0)
        val port = serverSocket?.localPort ?: 0

        registerService(port)
        discoverServices()
    }

    private fun registerService(port: Int) {
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = this@P2PManager.serviceName
            serviceType = this@P2PManager.serviceType
            setPort(port)
        }
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    private fun discoverServices() {
        nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun stopP2P() {
        try {
            nsdManager.unregisterService(registrationListener)
            nsdManager.stopServiceDiscovery(discoveryListener)
            serverSocket?.close()
        } catch (e: Exception) {}
    }
}
