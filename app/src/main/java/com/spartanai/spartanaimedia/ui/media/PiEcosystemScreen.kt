package com.spartanai.spartanaimedia.ui.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PiEcosystemScreen(
    viewModel: MediaViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.selectedProfile

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pi Network Ecosystem") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Pi Identity Section
            item {
                Text("Pi Network Identity", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (profile?.piUsername != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFFFFD700))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(profile.piUsername, style = MaterialTheme.typography.titleLarge)
                                    Text(profile.piWalletAddress ?: "", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            }
                        } else {
                            Text("Not connected to Pi Network")
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { viewModel.loginWithPi() }, modifier = Modifier.fillMaxWidth()) {
                                Text("Login with Pi SDK")
                            }
                        }
                    }
                }
            }

            // 2. Pi Node Section
            item {
                Text("Pi Mobile Node Status", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Memory, contentDescription = null)
                                Spacer(Modifier.width(12.dp))
                                Text("Node Activation")
                            }
                            Switch(
                                checked = profile?.isPiNodeActive ?: false,
                                onCheckedChange = { viewModel.togglePiNode(it) }
                            )
                        }

                        if (profile?.isPiNodeActive == true && uiState.nodeStatus != null) {
                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                            val status = uiState.nodeStatus!!
                            
                            NodeStatRow("Blockchain Sync", "${status.syncedBlock} / ${status.totalBlocks}")
                            LinearProgressIndicator(
                                progress = { status.syncedBlock.toFloat() / status.totalBlocks },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            )
                            NodeStatRow("Connected Peers", status.connectedPeers.toString())
                            NodeStatRow("Consensus Type", "Stellar-SCP (Light)")
                        } else {
                            Text(
                                "Activate your mobile node to help secure the SpartanAI media distribution network.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            // 3. Rewards / Economy
            item {
                Text("Pi Economy", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Available Pi: 3.14159 π", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(onClick = { }, label = { Text("Donate π") })
                            AssistChip(onClick = { }, label = { Text("Upgrade to 4K") })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NodeStatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}
