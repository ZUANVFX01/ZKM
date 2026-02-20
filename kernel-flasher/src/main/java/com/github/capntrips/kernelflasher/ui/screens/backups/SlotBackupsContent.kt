@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.capntrips.kernelflasher.ui.screens.backups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.github.capntrips.kernelflasher.R
import com.github.capntrips.kernelflasher.common.PartitionUtil
import com.github.capntrips.kernelflasher.ui.components.FlashList
import com.github.capntrips.kernelflasher.ui.components.SlotCard
import com.github.capntrips.kernelflasher.ui.screens.slot.SlotViewModel

@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@ExperimentalUnitApi
@Composable
fun ColumnScope.SlotBackupsContent(
    slotViewModel: SlotViewModel,
    backupsViewModel: BackupsViewModel,
    slotSuffix: String,
    navController: NavController
) {
    val context = LocalContext.current
    
    // Cek route saat ini untuk menentukan tampilan
    val currentRoute = navController.currentDestination?.route ?: ""

    // =========================================================
    // CASE 1 & 2: LIST BACKUP atau DETAIL BACKUP
    // =========================================================
    if (!currentRoute.contains("/backups/{backupId}/restore")) {
        
        // Header Slot selalu muncul
        SlotCard(
            title = stringResource(if (slotSuffix == "_a") R.string.slot_a else if (slotSuffix == "_b") R.string.slot_b else R.string.slot),
            viewModel = slotViewModel,
            navController = navController,
            isSlotScreen = true,
            showDlkm = false,
        )
        
        Spacer(Modifier.height(16.dp))

        // --- SUB-CASE: DETAIL BACKUP (Jika ada ID backup terpilih) ---
        if (backupsViewModel.currentBackup != null && backupsViewModel.backups.get(backupsViewModel.currentBackup!!) != null) {
            val currentBackup = backupsViewModel.backups.getValue(backupsViewModel.currentBackup!!)
            
            // Header Title
            Text(
                text = "Backup Details",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    SlotBackupInfoItem(Icons.Default.Label, stringResource(R.string.backup_type), currentBackup.type)
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))
                    SlotBackupInfoItem(Icons.Default.Memory, stringResource(R.string.kernel_version), currentBackup.kernelVersion)
                    
                    if (currentBackup.type == "raw") {
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))
                        SlotBackupInfoItem(
                            icon = Icons.Default.Fingerprint,
                            title = stringResource(R.string.boot_sha1),
                            value = currentBackup.bootSha1?.take(8) ?: "Unknown",
                            isMonospace = true
                        )
                    }
                }
            }

            // Hashes List (Jika RAW)
            if (currentBackup.type == "raw" && currentBackup.hashes != null) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Partition Hashes",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        val partitions = PartitionUtil.PartitionNames.filter { currentBackup.hashes!!.get(it) != null }
                        partitions.forEachIndexed { index, partitionName ->
                            val hash = currentBackup.hashes!!.get(partitionName)!!
                            SlotBackupInfoItem(
                                icon = Icons.Outlined.Storage,
                                title = partitionName,
                                value = hash.take(8),
                                isMonospace = true
                            )
                            if (index < partitions.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Action Buttons
            AnimatedVisibility(!slotViewModel.isRefreshing) {
                Column {
                    Text(
                        text = "Actions",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            if (slotViewModel.isActive) {
                                if (currentBackup.type == "raw") {
                                    SlotBackupActionItem(
                                        icon = Icons.Default.Restore,
                                        title = stringResource(R.string.restore),
                                        subtitle = "Select partitions to restore",
                                        onClick = {
                                            navController.navigate("slot$slotSuffix/backups/${backupsViewModel.currentBackup!!}/restore")
                                        }
                                    )
                                } else if (currentBackup.type == "ak3") {
                                    SlotBackupActionItem(
                                        icon = Icons.Default.SystemUpdate,
                                        title = stringResource(R.string.flash),
                                        subtitle = "Flash AK3 Zip",
                                        onClick = {
                                            slotViewModel.flashAk3(context, backupsViewModel.currentBackup!!, currentBackup.filename!!)
                                            navController.navigate("slot$slotSuffix/backups/${backupsViewModel.currentBackup!!}/flash/ak3") {
                                                popUpTo("slot$slotSuffix")
                                            }
                                        }
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))
                            }
                            
                            SlotBackupActionItem(
                                icon = Icons.Default.Delete,
                                title = stringResource(R.string.delete),
                                subtitle = "Delete this backup permanently",
                                onClick = { backupsViewModel.delete(context) { navController.popBackStack() } },
                                isDestructive = true
                            )
                        }
                    }
                }
            }

        } else {
            // --- SUB-CASE: LIST BACKUP (Jika tidak ada ID terpilih) ---
            val backups = backupsViewModel.backups.filter { it.value.bootSha1.equals(slotViewModel.sha1) || it.value.type == "ak3" }
            
            if (backups.isNotEmpty()) {
                Text(
                    text = "Compatible Backups",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (id in backups.keys.sortedByDescending { it }) {
                        val bk = backups[id]!!
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                            onClick = {
                                if (!slotViewModel.isRefreshing) {
                                    navController.navigate("slot$slotSuffix/backups/$id")
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = if (bk.type == "ak3") Icons.Default.Archive else Icons.Default.Backup
                                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = id, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = bk.kernelVersion,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f))
                            }
                        }
                    }
                }
            } else {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Backup, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(R.string.no_backups_found), color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    
    // =========================================================
    // CASE 3: RESTORE SELECTION SCREEN (Ini yang kamu cari!)
    // =========================================================
    } else if (currentRoute.endsWith("/backups/{backupId}/restore")) {
        
        Text(
            text = "Select Partitions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
        )

        val currentBackup = backupsViewModel.backups.getValue(backupsViewModel.currentBackup!!)
        
        if (currentBackup.hashes != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column {
                    for (partitionName in PartitionUtil.PartitionNames) {
                        val hash = currentBackup.hashes.get(partitionName)
                        if (hash != null) {
                            val isChecked = backupsViewModel.backupPartitions[partitionName] == true
                            // DISINI PERUBAHANNYA: ListItem MODERN + Checkbox
                            ListItem(
                                modifier = Modifier.clickable {
                                    backupsViewModel.backupPartitions[partitionName] = !isChecked
                                },
                                headlineContent = { Text(partitionName, fontWeight = FontWeight.Medium) },
                                trailingContent = {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { 
                                            backupsViewModel.backupPartitions[partitionName] = it 
                                        }
                                    )
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.1f))
                        }
                    }
                }
            }
        } else {
            Text(
                stringResource(R.string.partition_selection_unavailable),
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                textAlign = TextAlign.Center,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(24.dp))
        
        // Restore Button (Floating atau Bottom)
        val isEnabled = currentBackup.hashes == null || (PartitionUtil.PartitionNames.none { currentBackup.hashes.get(it) != null && backupsViewModel.backupPartitions[it] == null } && backupsViewModel.backupPartitions.filter { it.value }.isNotEmpty())
        
        Button(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            onClick = {
                backupsViewModel.restore(context, slotSuffix)
                navController.navigate("slot$slotSuffix/backups/${backupsViewModel.currentBackup!!}/restore/restore") {
                    popUpTo("slot$slotSuffix")
                }
            },
            enabled = isEnabled,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Restore, null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.restore), fontSize = 16.sp)
        }

    // =========================================================
    // CASE 4: LOG OUTPUT (Saat proses restore berjalan)
    // =========================================================
    } else {
        FlashList(
            stringResource(R.string.restore),
            backupsViewModel.restoreOutput
        ) {
            AnimatedVisibility(!backupsViewModel.isRefreshing && backupsViewModel.wasRestored != null) {
                Column {
                    if (backupsViewModel.wasRestored != false) {
                        Button(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            onClick = { navController.navigate("reboot") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.reboot))
                        }
                    }
                }
            }
        }
    }
}

// --- HELPER COMPONENTS ---

@Composable
fun SlotBackupInfoItem(icon: ImageVector, title: String, value: String, isMonospace: Boolean = false) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium) },
        supportingContent = { 
            Text(
                value, 
                style = if (isMonospace) MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace) else MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                maxLines = 1, 
                overflow = TextOverflow.Ellipsis
            ) 
        },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun SlotBackupActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val iconColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(title, fontWeight = FontWeight.SemiBold, color = color) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.padding(8.dp)
            )
        },
        trailingContent = {
             Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f), modifier = Modifier.size(16.dp))
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
