@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.capntrips.kernelflasher.ui.screens.main

import android.os.Build
import android.widget.Toast // [BARU] Import Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.github.capntrips.kernelflasher.R
import com.github.capntrips.kernelflasher.ui.screens.slot.SlotViewModel
import kotlinx.serialization.ExperimentalSerializationApi

@ExperimentalMaterial3Api
@ExperimentalSerializationApi
@Composable
fun ColumnScope.MainContent(
    viewModel: MainViewModel,
    navController: NavController
) {
    val context = LocalContext.current

    // --- 1. DEVICE INFO SECTION ---
    Text(
        text = "Device Information",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            ModernListItem(
                icon = Icons.Default.PhoneAndroid,
                title = stringResource(R.string.model),
                value = "${Build.MODEL} (${Build.DEVICE})"
            )
            HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ModernListItem(
                icon = Icons.Default.Tag, 
                title = stringResource(R.string.build_number),
                value = Build.ID
            )
            HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ModernListItem(
                icon = Icons.Default.Memory,
                title = stringResource(R.string.kernel_version),
                value = viewModel.kernelVersion
            )
            if (viewModel.isAb) {
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ModernListItem(
                    icon = Icons.Default.Layers,
                    title = stringResource(R.string.slot_suffix),
                    value = viewModel.slotSuffix
                )
            }
        }
    }

    Spacer(Modifier.height(24.dp))

    // --- 2. SLOT STATUS SECTION ---
    Text(
        text = "Boot Slots",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Slot A
        if (viewModel.slotA != null) {
            Box(Modifier.weight(1f)) {
                ModernSlotCard(
                    title = "Slot A",
                    slotVM = viewModel.slotA!!,
                    isActive = viewModel.slotSuffix == "_a",
                    onClick = { navController.navigate("slot_a") }
                )
            }
        }

        // Slot B
        if (viewModel.isAb && viewModel.slotB != null) {
            Box(Modifier.weight(1f)) {
                ModernSlotCard(
                    title = "Slot B",
                    slotVM = viewModel.slotB!!,
                    isActive = viewModel.slotSuffix == "_b",
                    onClick = { navController.navigate("slot_b") }
                )
            }
        }
    }

    Spacer(Modifier.height(24.dp))

    // --- 3. TOOLS & ACTIONS SECTION ---
    Text(
        text = "Tools & Actions",
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
            // Backups
            ActionItem(
                icon = Icons.Default.Backup,
                title = stringResource(R.string.backups),
                subtitle = "Manage your kernel backups",
                onClick = { navController.navigate("backups") }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))
            
            // Updates (FROZEN / TOAST ONLY)
            ActionItem(
                icon = Icons.Default.Update,
                title = stringResource(R.string.updates),
                subtitle = "Check for updates",
                onClick = { 
                    // [MODIFIKASI] Tampilkan Toast alih-alih navigasi
                    Toast.makeText(context, "You are using the latest version", Toast.LENGTH_SHORT).show()
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))

            // Ramoops (Conditional)
            if (viewModel.hasRamoops) {
                ActionItem(
                    icon = Icons.Default.BugReport,
                    title = stringResource(R.string.save_ramoops),
                    subtitle = "Save console-ramoops log",
                    onClick = { viewModel.saveRamoops(context) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))
            }

            // Dmesg & Logcat
            ActionItem(
                icon = Icons.Default.Description,
                title = stringResource(R.string.save_dmesg),
                subtitle = "Save kernel buffer log",
                onClick = { viewModel.saveDmesg(context) }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))

            ActionItem(
                icon = Icons.Default.LogoDev,
                title = stringResource(R.string.save_logcat),
                subtitle = "Save android system log",
                onClick = { viewModel.saveLogcat(context) }
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))
            
            // Reboot
            ActionItem(
                icon = Icons.Default.PowerSettingsNew,
                title = stringResource(R.string.reboot),
                subtitle = "Reboot device system",
                onClick = { navController.navigate("reboot") },
                iconColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

// --- CUSTOM MODERN COMPONENTS ---

@Composable
fun ModernListItem(icon: ImageVector, title: String, value: String) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun ModernSlotCard(
    title: String,
    slotVM: SlotViewModel,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest
    val contentColor = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Icon & Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Outlined.SdStorage,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
                if (isActive) {
                    Text(
                        text = "ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Info (SHA1)
            Text(
                text = "SHA1: ${slotVM.sha1.take(8)}",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = contentColor.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // View Button (Small & Integrated)
            FilledTonalButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(36.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isActive) MaterialTheme.colorScheme.surface.copy(alpha=0.2f) else MaterialTheme.colorScheme.primary.copy(alpha=0.1f),
                    contentColor = contentColor
                )
            ) {
                Text("View Details", fontSize = 12.sp)
                Spacer(Modifier.width(4.dp))
                // [FIX] Menggunakan Ikon Default agar tidak error
                Icon(Icons.Default.KeyboardArrowRight, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun ActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.padding(8.dp)
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
