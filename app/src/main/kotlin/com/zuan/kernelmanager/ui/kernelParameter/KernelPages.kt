package com.zuan.kernelmanager.ui.kernelParameter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zuan.kernelmanager.utils.KernelUtils

// --- SCHEDULER PAGE ---
@Composable
fun SchedulerPage(viewModel: KernelParameterViewModel, colors: ZkmColors) {
    val sched by viewModel.sched.collectAsState()
    val bore by viewModel.boreScheduler.collectAsState()
    val uclamp by viewModel.uclamp.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(bottom = 100.dp)) {
        item {
            // BORE & AUTO GROUP
            if (bore.hasBore || sched.hasSchedAutogroup) {
                SectionTitle("Scheduler Features", colors)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (bore.hasBore) {
                        BentoGridToggle(Modifier.weight(1f), "BORE", if (bore.bore == 1) "Active" else "Disabled", Icons.Rounded.Speed, bore.bore == 1, Color(0xFF3B82F6), colors) { viewModel.updateGeneric(KernelUtils.BORE, if(bore.bore==1) "0" else "1", "bore") }
                    }
                    if (sched.hasSchedAutogroup) {
                        BentoGridToggle(Modifier.weight(1f), "Auto Group", if (sched.schedAutogroup == "1") "On" else "Off", Icons.Rounded.GroupWork, sched.schedAutogroup == "1", Color(0xFF3B82F6), colors) { viewModel.updateGeneric(KernelUtils.SCHED_AUTO_GROUP, if(sched.schedAutogroup=="1") "0" else "1", "sched_autogroup") }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ORIGAMI SCHEDULER TUNABLES
            SectionTitle("Kernel Tunables", colors)
            if (sched.hasChildRunsFirst) BentoGridToggle(Modifier.fillMaxWidth(), "Child Runs First", if (sched.childRunsFirst == "1") "Enabled" else "Disabled", Icons.Rounded.ChildCare, sched.childRunsFirst == "1", Color(0xFF3B82F6), colors) { viewModel.updateGeneric(KernelUtils.SCHED_CHILD_RUNS_FIRST, if(sched.childRunsFirst=="1") "0" else "1", "sched_child_runs_first") }
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                 if (sched.hasCstateAware) BentoGridToggle(Modifier.weight(1f), "C-State Aware", if (sched.cstateAware == "1") "On" else "Off", Icons.Rounded.BatteryStd, sched.cstateAware == "1", Color(0xFF3B82F6), colors) { viewModel.updateGeneric(KernelUtils.SCHED_CSTATE_AWARE, if(sched.cstateAware=="1") "0" else "1", "sched_cstate_aware") }
                 if (sched.hasSchedStats) BentoGridToggle(Modifier.weight(1f), "Sched Stats", if (sched.schedStats == "1") "On" else "Off", Icons.Rounded.QueryStats, sched.schedStats == "1", Color(0xFF3B82F6), colors) { viewModel.updateGeneric(KernelUtils.SCHED_SCHEDSTATS, if(sched.schedStats=="1") "0" else "1", "sched_schedstats") }
            }

            // UCLAMP
            if (uclamp.hasUclampMax || uclamp.hasUclampMin) {
                Spacer(modifier = Modifier.height(20.dp))
                SectionTitle("UClamp Settings", colors)
                BentoWideValueCard("UClamp Max", uclamp.uclampMax, false, Color(0xFF3B82F6), colors) {}
                Spacer(modifier = Modifier.height(8.dp))
                BentoWideValueCard("UClamp Min", uclamp.uclampMin, false, Color(0xFF3B82F6), colors) {}
            }
        }
    }
}

// --- MEMORY PAGE ---
@Composable
fun MemoryPage(viewModel: KernelParameterViewModel, colors: ZkmColors) {
    val mem by viewModel.mem.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(bottom = 100.dp)) {
        item {
            // MAIN CONTROLS
            SectionTitle("Management", colors)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { viewModel.dropCaches("3") },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cardActive, contentColor = colors.textMain),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Drop Caches") }
                
                if (mem.hasLaptopMode) {
                    BentoGridToggle(Modifier.weight(1f), "Laptop Mode", if(mem.laptopMode!="0") "On" else "Off", Icons.Rounded.Laptop, mem.laptopMode!="0", Color(0xFFFACC15), colors) { viewModel.updateGeneric(KernelUtils.LAPTOP_MODE, if(mem.laptopMode=="0") "1" else "0", "laptop_mode") }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // VIRTUAL MEMORY TUNABLES
            SectionTitle("Virtual Memory", colors)
            if (mem.hasSwappiness) BentoWideValueCard("Swappiness", mem.swappiness, true, Color(0xFFFACC15), colors, onSliderChange = { viewModel.updateGeneric(KernelUtils.SWAPPINESS, it.toInt().toString(), "swappiness") })
            if (mem.hasVfsCachePressure) { Spacer(modifier=Modifier.height(8.dp)); BentoWideValueCard("VFS Cache Pressure", mem.vfsCachePressure, true, Color(0xFFFACC15), colors, onSliderChange = { viewModel.updateGeneric(KernelUtils.VFS_CACHE_PRESSURE, it.toInt().toString(), "vfs_cache_pressure") }) }
            if (mem.hasDirtyRatio) { Spacer(modifier=Modifier.height(8.dp)); BentoWideValueCard("Dirty Ratio", mem.dirtyRatio, true, Color(0xFFFACC15), colors, onSliderChange = { viewModel.updateGeneric(KernelUtils.DIRTY_RATIO, it.toInt().toString(), "dirty_ratio") }) }
            if (mem.hasDirtyBackgroundRatio) { Spacer(modifier=Modifier.height(8.dp)); BentoWideValueCard("Dirty Bg Ratio", mem.dirtyBackgroundRatio, true, Color(0xFFFACC15), colors, onSliderChange = { viewModel.updateGeneric(KernelUtils.DIRTY_BACKGROUND_RATIO, it.toInt().toString(), "dirty_background_ratio") }) }
            
            Spacer(modifier = Modifier.height(12.dp))
            if (mem.hasMinFreeKbytes) BentoWideValueCard("Min Free Kbytes", mem.minFreeKbytes, false, Color(0xFFFACC15), colors) {}
            
            if (mem.hasExtraFreeKbytes) { 
                Spacer(modifier=Modifier.height(8.dp))
                
                // Convert KB (Kernel) -> MB (UI)
                val currentKb = mem.extraFreeKbytes.replace(Regex("[^0-9]"), "").toFloatOrNull() ?: 0f
                val currentMb = currentKb / 1024f
                
                BentoWideValueCard(
                    title = "Extra Free Kbytes", 
                    value = "${currentMb.toInt()} MB", 
                    isSlider = true, 
                    accentColor = Color(0xFFFACC15), 
                    colors = colors,
                    customMaxRange = 512f, 
                    onSliderChange = { mbValue -> 
                        val kbValue = (mbValue * 1024).toInt()
                        viewModel.updateGeneric(KernelUtils.EXTRA_FREE_KBYTES, kbValue.toString(), "extra_free_kbytes") 
                    }
                ) 
            }
        }
    }
}

// --- NETWORK PAGE ---
@Composable
fun NetworkPage(viewModel: KernelParameterViewModel, colors: ZkmColors) {
    val net by viewModel.net.collectAsState()
    val printk by viewModel.printk.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(bottom = 100.dp)) {
        item {
            SectionTitle("TCP Congestion", colors)
            BentoChipGroup(net.availableTcp, net.tcpCongestion, Color(0xFF10B981), colors) { viewModel.updateTcpCongestionAlgorithm(it) }
            
            Spacer(modifier = Modifier.height(20.dp))
            SectionTitle("TCP Parameters", colors)
            
            if (net.hasSyncookies) BentoGridToggle(Modifier.fillMaxWidth(), "SYN Cookies", if(net.syncookies=="1") "Enabled" else "Disabled", Icons.Rounded.Cookie, net.syncookies=="1", Color(0xFF10B981), colors) { viewModel.updateGeneric(KernelUtils.TCP_SYNCOOKIES, if(net.syncookies=="1") "0" else "1", "tcp_syncookies") }
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if(net.hasReuse) BentoGridToggle(Modifier.weight(1f), "TCP Reuse", if(net.reuse=="1") "On" else "Off", Icons.Rounded.Replay, net.reuse=="1", Color(0xFF10B981), colors) { viewModel.updateGeneric(KernelUtils.TCP_REUSE, if(net.reuse=="1") "0" else "1", "tcp_reuse") }
                if(net.hasFastOpen) BentoGridToggle(Modifier.weight(1f), "Fast Open", if(net.fastOpen!="0") "On" else "Off", Icons.Rounded.FlashOn, net.fastOpen!="0", Color(0xFF10B981), colors) { viewModel.updateGeneric(KernelUtils.TCP_FASTOPEN, if(net.fastOpen=="0") "3" else "0", "tcp_fastopen") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            if(net.hasSack) BentoGridToggle(Modifier.fillMaxWidth(), "TCP SACK", if(net.sack=="1") "Enabled" else "Disabled", Icons.Rounded.DoneAll, net.sack=="1", Color(0xFF10B981), colors) { viewModel.updateGeneric(KernelUtils.TCP_SACK, if(net.sack=="1") "0" else "1", "tcp_sack") }
            Spacer(modifier = Modifier.height(8.dp))
            
            if(net.hasEcn) BentoWideValueCard("ECN", net.ecn, false, Color(0xFF10B981), colors) {}
            if(net.hasMaxSynBacklog) { Spacer(modifier=Modifier.height(8.dp)); BentoWideValueCard("Max SYN Backlog", net.maxSynBacklog, false, Color(0xFF10B981), colors) {} }

            Spacer(modifier = Modifier.height(20.dp))
            SectionTitle("Kernel Log", colors)
            BentoWideValueCard("Printk", printk, false, Color(0xFF10B981), colors) {}
        }
    }
}

/* Update function DisplayPage */

@Composable
fun DisplayPage(viewModel: KernelParameterViewModel, colors: ZkmColors) {
    val disp by viewModel.displayState.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(bottom = 100.dp)) {
        item {
            // 1. INFO GRID
            SectionTitle("Screen Specifications", colors)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoCard(Modifier.weight(1f), "Resolution", disp.resolution, Icons.Rounded.AspectRatio, colors)
                    InfoCard(Modifier.weight(1f), "Technology", disp.tech, Icons.Rounded.Smartphone, colors)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoCard(Modifier.weight(1f), "Diagonal", disp.sizeInch, Icons.Rounded.Straighten, colors)
                    InfoCard(Modifier.weight(1f), "Density", disp.density, Icons.Rounded.BlurOn, colors)
                }
                
                // Detailed Stats in a single wide card
                BentoWideDetailCard(
                    items = listOf(
                        "HDR Support" to disp.hdrCaps,
                        "Refresh Rate" to "${disp.refreshRate.toInt()} Hz",
                        "xDPI" to disp.xdpi,
                        "yDPI" to disp.ydpi
                    ),
                    colors = colors
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- BAGIAN BRIGHTNESS SUDAH DIHAPUS ---

            // 3. SURFACEFLINGER SATURATION
            SectionTitle("Color Calibration", colors)
            
            // Penjelasan Status Real-time
            Text(
                text = "Note: System does not report current saturation.\nDefault value is usually 1.00.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSub,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            BentoWideValueCard(
                title = "Saturation",
                value = "%.2f".format(disp.sfSaturation), // Format 2 angka di belakang koma
                isSlider = true,
                accentColor = Color(0xFFEC4899),
                colors = colors,
                customMaxRange = 2.0f, // Range: 0.0 (BW) - 1.0 (Normal) - 2.0 (Vivid)
                onSliderChange = { viewModel.updateSurfaceFlingerSaturation(it) }
            )
            
            // Reset Button
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.updateSurfaceFlingerSaturation(1.0f) },
                modifier = Modifier.fillMaxWidth().height(45.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.cardActive, contentColor = colors.textMain),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Reset to Default (1.0)") }
        }
    }
}

// --- NEW COMPONENT: Info Card (Small Grid Item) ---
@Composable
fun InfoCard(modifier: Modifier, title: String, value: String, icon: ImageVector, colors: ZkmColors) {
    Column(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.cardBg)
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(icon, null, tint = colors.textSub, modifier = Modifier.size(20.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelSmall, color = colors.textSub)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textMain, maxLines = 1)
        }
    }
}

// --- NEW COMPONENT: Detailed List Card ---
@Composable
fun BentoWideDetailCard(items: List<Pair<String, String>>, colors: ZkmColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.cardBg)
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEachIndexed { index, (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.textSub, fontWeight = FontWeight.Medium)
                Text(value, style = MaterialTheme.typography.bodyMedium, color = colors.textMain, fontWeight = FontWeight.Bold)
            }
            if (index < items.size - 1) {
                Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(colors.border))
            }
        }
    }
}
