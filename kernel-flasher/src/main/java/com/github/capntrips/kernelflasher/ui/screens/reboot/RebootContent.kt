package com.github.capntrips.kernelflasher.ui.screens.reboot

import android.os.Build
import android.os.PowerManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.capntrips.kernelflasher.R

@Suppress("UnusedReceiverParameter")
@Composable
fun ColumnScope.RebootContent(
    viewModel: RebootViewModel,
    @Suppress("UNUSED_PARAMETER") ignoredNavController: NavController
) {
    val context = LocalContext.current
    
    Text(
        text = "Power Menu",
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
            // 1. Reboot System
            RebootActionItem(
                icon = Icons.Default.PowerSettingsNew,
                title = stringResource(R.string.reboot),
                subtitle = "Restart to Android system",
                onClick = { viewModel.rebootSystem() }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))

            // 2. Reboot Userspace (Android 11+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && context.getSystemService(PowerManager::class.java)?.isRebootingUserspaceSupported == true) {
                RebootActionItem(
                    icon = Icons.Default.Refresh,
                    title = stringResource(R.string.reboot_userspace),
                    subtitle = "Soft reboot (Userspace only)",
                    onClick = { viewModel.rebootUserspace() }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))
            }

            // 3. Recovery
            RebootActionItem(
                icon = Icons.Default.Build, // atau MedicalServices
                title = stringResource(R.string.reboot_recovery),
                subtitle = "Reboot to Recovery mode",
                onClick = { viewModel.rebootRecovery() }
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))

            // 4. Bootloader
            RebootActionItem(
                icon = Icons.Default.Adb,
                title = stringResource(R.string.reboot_bootloader),
                subtitle = "Reboot to Fastboot mode",
                onClick = { viewModel.rebootBootloader() }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))

            // 5. Download Mode
            RebootActionItem(
                icon = Icons.Default.Download,
                title = stringResource(R.string.reboot_download),
                subtitle = "Reboot to Download mode",
                onClick = { viewModel.rebootDownload() }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))

            // 6. EDL Mode
            RebootActionItem(
                icon = Icons.Default.Warning,
                title = stringResource(R.string.reboot_edl),
                subtitle = "Reboot to Emergency Download mode",
                onClick = { viewModel.rebootEdl() },
                isDangerous = true
            )
        }
    }
}

@Composable
fun RebootActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDangerous: Boolean = false
) {
    val iconColor = if (isDangerous) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) },
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
