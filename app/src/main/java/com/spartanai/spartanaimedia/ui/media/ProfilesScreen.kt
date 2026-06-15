package com.spartanai.spartanaimedia.ui.media

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.spartanai.spartanaimedia.domain.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(
    viewModel: MediaViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Profiles") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New Profile")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.profiles) { profile ->
                ProfileItem(
                    profile = profile,
                    isSelected = uiState.selectedProfile?.userId == profile.userId,
                    onSelect = { viewModel.selectProfile(profile) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Network Settings", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                var proxyHost by remember { mutableStateOf(uiState.selectedProfile?.proxyConfig?.host ?: "") }
                var proxyPort by remember { mutableStateOf(uiState.selectedProfile?.proxyConfig?.port?.toString() ?: "") }
                var proxyType by remember { mutableStateOf(uiState.selectedProfile?.proxyConfig?.type?.name ?: "HTTP") }

                OutlinedTextField(
                    value = proxyHost,
                    onValueChange = { proxyHost = it },
                    label = { Text("Proxy Host (e.g. 127.0.0.1)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = proxyPort,
                    onValueChange = { proxyPort = it },
                    label = { Text("Proxy Port (e.g. 9050 for TOR)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf("HTTP", "SOCKS", "TOR").forEach { type ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = (type == proxyType),
                                onClick = { proxyType = type }
                            )
                            Text(text = type)
                        }
                    }
                }
                
                Button(
                    onClick = { 
                        viewModel.updateProxyConfig(proxyHost, proxyPort.toIntOrNull() ?: 8080, proxyType) 
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Network Settings")
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("System Settings", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { viewModel.forceUpdateCheck() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Check for Updates")
                }
                
                uiState.updateInfo?.let { update ->
                    if (update.updateAvailable) {
                        Text(
                            text = "New version available: ${update.latestVersion}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else if (update.latestVersion.isEmpty()) {
                        Text(
                            text = "No updates found.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        if (showCreateDialog) {
            CreateProfileDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name, isAnon ->
                    viewModel.createNewProfile(name, isAnon)
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
fun ProfileItem(
    profile: UserProfile,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = profile.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = profile.username, style = MaterialTheme.typography.titleLarge)
                if (profile.isAnonymous) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Secure Anonymous Mode",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CreateProfileDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Profile Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isAnonymous, onCheckedChange = { isAnonymous = it })
                    Text("Secure Anonymous Mode")
                }
                if (isAnonymous) {
                    Text(
                        "Downloads will be stored in private app storage and tracking headers will be stripped.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onCreate(name, isAnonymous) }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
