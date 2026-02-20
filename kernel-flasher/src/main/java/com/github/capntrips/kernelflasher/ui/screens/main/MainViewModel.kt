package com.github.capntrips.kernelflasher.ui.screens.main

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.github.capntrips.kernelflasher.common.PartitionUtil
import com.github.capntrips.kernelflasher.common.types.backups.Backup
import com.github.capntrips.kernelflasher.ui.screens.backups.BackupsViewModel
import com.github.capntrips.kernelflasher.ui.screens.reboot.RebootViewModel
import com.github.capntrips.kernelflasher.ui.screens.slot.SlotViewModel
import com.github.capntrips.kernelflasher.ui.screens.updates.UpdatesViewModel
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.nio.FileSystemManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ExperimentalSerializationApi
class MainViewModel(
    context: Context,
    fileSystemManager: FileSystemManager,
    private val navController: NavController
) : ViewModel() {
    companion object {
        const val TAG: String = "KernelFlasher/MainViewModel"
    }

    var slotSuffix: String = ""
    var kernelVersion: String = "Unknown"
    var isAb: Boolean = false
    
    var slotA: SlotViewModel? = null
    var slotB: SlotViewModel? = null
    
    lateinit var backups: BackupsViewModel
    lateinit var updates: UpdatesViewModel
    lateinit var reboot: RebootViewModel
    
    var hasRamoops: Boolean = false

    private val _isRefreshing: MutableState<Boolean> = mutableStateOf(true)
    private var _error: String? = null
    private var _backups: MutableMap<String, Backup> = mutableMapOf()

    val isRefreshing: Boolean
        get() = _isRefreshing.value
    val hasError: Boolean
        get() = _error != null
    val error: String
        get() = _error ?: "Unknown Error"

    init {
        try {
            // 1. Init PartitionUtil (HTTOOLS) - Bungkus try-catch agar tidak crash kalau gagal baca fstab
            try {
                PartitionUtil.init(context, fileSystemManager)
            } catch (e: Exception) {
                Log.w(TAG, "PartitionUtil warning: ${e.message}. Switching to manual mode.")
            }

            // 2. Info Dasar
            val kernelCmd = Shell.cmd("echo $(uname -r) $(uname -v)").exec()
            kernelVersion = kernelCmd.out.getOrNull(0) ?: "Unknown Kernel"

            val suffixCmd = Shell.cmd("getprop ro.boot.slot_suffix").exec()
            slotSuffix = suffixCmd.out.getOrNull(0) ?: ""

            // 3. Init VM Lain
            backups = BackupsViewModel(context, fileSystemManager, navController, _isRefreshing, _backups)
            updates = UpdatesViewModel(context, fileSystemManager, navController, _isRefreshing)
            reboot = RebootViewModel(context, fileSystemManager, navController, _isRefreshing)

            isAb = slotSuffix.isNotEmpty()

            // 4. LOGIKA PENCARIAN PARTISI (VERSI FIX ROOT CHECK)
            if (isAb) {
                val bootA = safeFindPartition(context, "boot", "_a")
                    ?: throw Exception("FATAL: Could not find partition boot_a manually!")
                val bootB = safeFindPartition(context, "boot", "_b")
                    ?: throw Exception("FATAL: Could not find partition boot_b manually!")
                
                val initBootA = safeFindPartition(context, "init_boot", "_a")
                val initBootB = safeFindPartition(context, "init_boot", "_b")

                slotA = SlotViewModel(context, fileSystemManager, navController, _isRefreshing, slotSuffix == "_a", "_a", bootA, initBootA, _backups)
                if (slotA?.hasError == true && slotSuffix == "_a") _error = slotA?.error
                
                slotB = SlotViewModel(context, fileSystemManager, navController, _isRefreshing, slotSuffix == "_b", "_b", bootB, initBootB, _backups)
                if (slotB?.hasError == true && slotSuffix == "_b") _error = slotB?.error

            } else {
                val boot = safeFindPartition(context, "boot", "")
                     ?: throw Exception("FATAL: Could not find partition boot manually!")
                val initBoot = safeFindPartition(context, "init_boot", "")
                
                slotA = SlotViewModel(context, fileSystemManager, navController, _isRefreshing, true, "", boot, initBoot, _backups)
                if (slotA?.hasError == true) _error = slotA?.error
                
                slotB = null
            }

            // 5. Ramoops
            hasRamoops = fileSystemManager.getFile("/sys/fs/pstore/console-ramoops-0").exists()
            
        } catch (e: Exception) {
            Log.e(TAG, "Init Failed", e)
            _error = "Init Failed: ${e.message}"
        } finally {
            _isRefreshing.value = false
        }
    }

    /**
     * Fungsi Pencari Partisi Tahan Banting (Bulletproof)
     * Menggunakan Root Shell untuk mengecek keberadaan file, bukan Java File.exists()
     */
    private fun safeFindPartition(context: Context, name: String, suffix: String): File? {
        // A. Coba cara Library (PartitionUtil)
        try {
            val file = PartitionUtil.findPartitionBlockDevice(context, name, suffix)
            // Cek eksistensi via Shell, karena Java mungkin permission denied
            if (file != null && Shell.cmd("test -e ${file.absolutePath}").exec().isSuccess) {
                return file
            }
        } catch (e: Exception) { }

        // B. Cara Manual (Hardcoded Paths)
        val targetName = "$name$suffix"
        val possiblePaths = listOf(
            "/dev/block/by-name/$targetName",
            "/dev/block/bootdevice/by-name/$targetName",
            "/dev/block/platform/bootdevice/by-name/$targetName",
            "/dev/block/mapper/$targetName"
        )

        for (path in possiblePaths) {
            // PENTING: Gunakan 'test -e' di Shell Root. 
            // File.exists() Java akan return false karena permission denied!
            if (Shell.cmd("test -e $path").exec().isSuccess) {
                // Resolve symlink biar dapat path asli (/dev/block/sde32 misalnya)
                val resolved = Shell.cmd("readlink -f $path").exec().out.firstOrNull() ?: path
                Log.d(TAG, "Found partition $targetName at $resolved")
                return File(resolved)
            }
        }
        
        // C. Cara Terakhir: Cari pakai perintah find (agak lambat tapi pasti)
        val findCmd = Shell.cmd("find /dev/block -name $targetName").exec()
        if (findCmd.isSuccess) {
            val foundPath = findCmd.out.firstOrNull()
            if (!foundPath.isNullOrEmpty()) {
                val resolved = Shell.cmd("readlink -f $foundPath").exec().out.firstOrNull() ?: foundPath
                return File(resolved)
            }
        }

        return null
    }

    fun refresh(context: Context) {
        launch {
            slotA?.refresh(context)
            if (isAb) slotB?.refresh(context)
            if (::backups.isInitialized) backups.refresh(context)
        }
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelScope.launch(Dispatchers.Main) { _isRefreshing.value = true }
            try { block() } 
            catch (e: Exception) {
                withContext (Dispatchers.Main) {
                    Log.e(TAG, e.message, e)
                    navController.navigate("error/${e.message}") { popUpTo("main") }
                }
            }
            viewModelScope.launch(Dispatchers.Main) { _isRefreshing.value = false }
        }
    }

    @Suppress("SameParameterValue")
    private fun log(context: Context, message: String, shouldThrow: Boolean = false) {
        Log.d(TAG, message)
        if (!shouldThrow) {
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e(TAG, message)
        }
    }

    fun saveRamoops(context: Context) {
        launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd--HH-mm"))
            @SuppressLint("SdCardPath")
            val ramoops = File("/sdcard/Download/console-ramoops--$now.log")
            Shell.cmd("cp /sys/fs/pstore/console-ramoops-0 $ramoops").exec()
            if (ramoops.exists()) log(context, "Saved ramoops to $ramoops")
            else log(context, "Failed to save $ramoops")
        }
    }

    fun saveDmesg(context: Context) {
        launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd--HH-mm"))
            @SuppressLint("SdCardPath")
            val dmesg = File("/sdcard/Download/dmesg--$now.log")
            Shell.cmd("dmesg > $dmesg").exec()
            if (dmesg.exists()) log(context, "Saved dmesg to $dmesg")
            else log(context, "Failed to save $dmesg")
        }
    }

    fun saveLogcat(context: Context) {
        launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd--HH-mm"))
            @SuppressLint("SdCardPath")
            val logcat = File("/sdcard/Download/logcat--$now.log")
            Shell.cmd("logcat -d > $logcat").exec()
            if (logcat.exists()) log(context, "Saved logcat to $logcat")
            else log(context, "Failed to save $logcat")
        }
    }
}
