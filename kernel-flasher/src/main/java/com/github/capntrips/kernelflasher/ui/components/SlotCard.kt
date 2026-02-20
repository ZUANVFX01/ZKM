package com.github.capntrips.kernelflasher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.github.capntrips.kernelflasher.R
import com.github.capntrips.kernelflasher.ui.screens.slot.SlotViewModel

@ExperimentalMaterial3Api
@Composable
fun SlotCard(
    title: String,
    viewModel: SlotViewModel,
    navController: NavController,
    isSlotScreen: Boolean = false,
    showDlkm: Boolean = true,
) {
    DataCard (
        title = title,
        button = {
            if (!isSlotScreen && !viewModel.hasError) {
                AnimatedVisibility(!viewModel.isRefreshing) {
                    // ViewButton dipanggil dari file ViewButton.kt, tidak didefinisikan di sini
                    ViewButton {
                        navController.navigate("slot${viewModel.slotSuffix}")
                    }
                }
            }
        }
    ) {
        val cardWidth = remember { mutableIntStateOf(0) }
        if (!viewModel.hasError) {
            DataRow(
                label = stringResource(R.string.boot_sha1),
                value = viewModel.sha1.take(8),
                valueStyle = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                mutableMaxWidth = cardWidth
            )
            AnimatedVisibility(!viewModel.isRefreshing && viewModel.kernelVersion != null) {
                DataRow(
                    label = stringResource(R.string.kernel_version),
                    value = viewModel.kernelVersion ?: "",
                    mutableMaxWidth = cardWidth,
                    clickable = true
                )
            }
            if (showDlkm && viewModel.hasVendorDlkm) {
                var vendorDlkmValue = stringResource(R.string.not_found)
                if (viewModel.isVendorDlkmMapped) {
                    vendorDlkmValue = if (viewModel.isVendorDlkmMounted) {
                        "${stringResource(R.string.exists)}, ${stringResource(R.string.mounted)}"
                    } else {
                        "${stringResource(R.string.exists)}, ${stringResource(R.string.unmounted)}"
                    }
                }
                DataRow(stringResource(R.string.vendor_dlkm), vendorDlkmValue, mutableMaxWidth = cardWidth)
            }
        } else {
            Row {
                DataValue(
                    value = viewModel.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    clickable = true
                )
            }
        }
    }
}
