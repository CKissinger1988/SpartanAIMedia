package com.spartanai.spartanaimedia.data.remote

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.ServerSocket
import java.net.Socket
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.concurrent.thread

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
    
    // In a real app, this should be exchanged via DHKE. For demo, we use a static pre-shared key.
    private val secretKey = SecretKeySpec("SpartanAI_Secure_Key_2026_AESGCM".toByteArray().copyOf(32), "AES")
    private val iv = "SpartanIV12".toByteArray() // 12 bytes for GCM

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
        override fun onDiscoveryStarted(regType: String) {}
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
        startListening()
    }
    
    private fun startListening() {
        thread {
            while (serverSocket?.isClosed == false) {
                try {
                    val client = serverSocket?.accept()
                    client?.let { handleIncomingConnection(it) }
                } catch (e: Exception) {
                    break
                }
            }
        }
    }
    
    private fun handleIncomingConnection(socket: Socket) {
        thread {
            try {
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val spec = GCMParameterSpec(128, iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
                
                val cipherInputStream = CipherInputStream(socket.getInputStream(), cipher)
                val outputFile = File(context.filesDir, "received_p2p_media_${System.currentTimeMillis()}.mp4")
                
                FileOutputStream(outputFile).use { fos ->
                    cipherInputStream.copyTo(fos)
                }
                
                Log.d("P2P", "File received successfully: ${outputFile.absolutePath}")
            } catch (e: Exception) {
                Log.e("P2P", "Error receiving file", e)
            } finally {
                socket.close()
            }
        }
    }
    
    suspend fun sendEncryptedFile(
        peer: PeerDevice, 
        file: File, 
        onProgress: ((Float) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val socket = Socket(peer.host, peer.port)
            
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
            
            val cipherOutputStream = CipherOutputStream(socket.getOutputStream(), cipher)
            
            val totalBytes = file.length().toFloat()
            var bytesCopied = 0L
            val buffer = ByteArray(8 * 1024)
            var bytes = 0
            
            FileInputStream(file).use { fis ->
                while (fis.read(buffer).also { bytes = it } >= 0) {
                    cipherOutputStream.write(buffer, 0, bytes)
                    bytesCopied += bytes
                    onProgress?.invoke(bytesCopied / totalBytes)
                }
            }
            
            cipherOutputStream.close()
            socket.close()
            return@withContext true
        } catch (e: Exception) {
            Log.e("P2P", "Error sending file", e)
            return@withContext false
        }
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
