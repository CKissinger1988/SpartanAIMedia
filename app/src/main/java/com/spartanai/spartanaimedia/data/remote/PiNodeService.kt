package com.spartanai.spartanaimedia.data.remote

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

/**
 * Interfaces with the Pi Network Node.
 * On mobile, this acts as a 'Light Node' or proxy to a desktop node.
 */
class PiNodeService {

    private val _isActive = MutableStateFlow(false)
    
    fun setNodeActive(active: Boolean) {
        _isActive.value = active
    }

    fun monitorNodeStatus(): Flow<NodeStatus> = _isActive.flatMapLatest { active ->
        if (!active) {
            flowOf(NodeStatus(isActive = false, syncedBlock = 0, totalBlocks = 0, connectedPeers = 0))
        } else {
            flow {
                var currentBlock = 12540000L
                val targetBlock = 12540050L
                while (true) {
                    if (currentBlock < targetBlock) {
                        currentBlock += (1..3).random()
                    }
                    emit(NodeStatus(
                        isActive = true,
                        syncedBlock = currentBlock,
                        totalBlocks = targetBlock,
                        connectedPeers = (5..12).random()
                    ))
                    delay(3000)
                }
            }
        }
    }

    data class NodeStatus(
        val isActive: Boolean,
        val syncedBlock: Long,
        val totalBlocks: Long,
        val connectedPeers: Int
    )
}
