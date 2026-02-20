/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.utils

import android.os.Build
import com.topjohnwu.superuser.Shell // Pastikan LibSu sudah di-gradle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object KernelInfoUtils {

    fun getDeviceModel(): String = "${Build.MANUFACTURER.uppercase()} ${Build.MODEL}"
    fun getBuildNumber(): String = Build.DISPLAY
    fun getKernelVersion(): String = System.getProperty("os.version") ?: "Unknown"

    suspend fun getActiveSlot(): String = withContext(Dispatchers.IO) {
        val slotSuffix = getProp("ro.boot.slot_suffix")
        when {
            slotSuffix.contains("_a") -> "A"
            slotSuffix.contains("_b") -> "B"
            else -> "A (Non-AB)"
        }
    }

    // --- LOGIC ROOT UTAMA DI SINI ---
    suspend fun getBootInfo(slot: String): BootInfo = withContext(Dispatchers.IO) {
        // 1. Cek apakah Root Access tersedia?
        if (!Shell.getShell().isRoot) {
            return@withContext BootInfo(
                sha1 = "No Root",
                format = "Unknown",
                vendorFormat = "Unknown"
            )
        }

        val targetSuffix = "_${slot.lowercase()}" // _a atau _b
        // Path partisi standar Android modern (Qualcomm/Google/etc)
        val bootPath = "/dev/block/by-name/boot$targetSuffix"
        val vendorBootPath = "/dev/block/by-name/vendor_boot$targetSuffix"

        // 2. Ambil SHA1 Hash
        // Command: sha1sum /dev/block/by-name/boot_a
        val sha1Output = Shell.cmd("sha1sum $bootPath").exec().out.firstOrNull() ?: ""
        // Output biasanya: "a1b2c3...  /dev/..." -> Kita ambil kata pertamanya aja
        val sha1Clean = sha1Output.split(Regex("\\s+")).firstOrNull() ?: "Error"
        
        // Format SHA1 biar pendek (8 karakter awal) kayak tampilan fastboot
        val sha1Display = if (sha1Clean.length > 8) sha1Clean.substring(0, 8) else sha1Clean

        // 3. Cek Format Image (Baca Header)
        // Kita baca 8 byte pertama untuk cek Magic Number
        val bootFormat = checkHeaderFormat(bootPath)
        val vendorFormat = checkHeaderFormat(vendorBootPath)

        BootInfo(sha1Display, bootFormat, vendorFormat)
    }

    // Helper untuk cek format (Raw vs LZ4 vs Gzip)
    private fun checkHeaderFormat(path: String): String {
        // Cek dulu apakah partisi ada?
        if (!Shell.cmd("ls $path").exec().isSuccess) return "Not Found"

        // Baca 8 byte pertama dalam format HEX
        // xxd mungkin tidak ada di semua HP, tapi biasanya ada di busybox Magisk
        val hexHeader = Shell.cmd("dd if=$path bs=1 count=8 2>/dev/null | xxd -p").exec().out.joinToString("")

        return when {
            // Magic: ANDROID! (41 4E 44 52 4F 49 44 21) -> Format Raw AOSP standard
            hexHeader.contains("414e44524f494421", ignoreCase = true) -> "raw"
            // Magic: LZ4 Legacy (02 21 4C 18)
            hexHeader.startsWith("02214c18", ignoreCase = true) -> "lz4_legacy"
            // Magic: GZIP (1F 8B)
            hexHeader.startsWith("1f8b", ignoreCase = true) -> "gzip"
            // Kalau kosong atau gagal baca
            hexHeader.isEmpty() -> "Unknown"
            else -> "Custom/Unk"
        }
    }

    private fun getProp(key: String): String {
        return try {
            val process = Runtime.getRuntime().exec("getprop $key")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()?.trim() ?: ""
            process.destroy()
            result
        } catch (e: Exception) {
            ""
        }
    }
}

// Data Class penampung
data class BootInfo(
    val sha1: String,
    val format: String,
    val vendorFormat: String
)
