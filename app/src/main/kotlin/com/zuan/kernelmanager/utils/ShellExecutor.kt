package com.zuan.kernelmanager.utils

import android.content.Context
import android.util.Log
import com.topjohnwu.superuser.Shell
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShellExecutor {

    enum class AccessType { ROOT, SHIZUKU, NONE }
    var preferredAccessType = AccessType.NONE

    // Flag biar gak spam logcat kalau user gak punya akses sama sekali
    private var hasLoggedError = false

    fun init(context: Context) {
        refreshAccess()
    }

    // Fungsi ini bakal dipanggil otomatis kalau akses masih NONE atau saat Init
    private fun refreshAccess() {
        // 1. Cek Root (Prioritas Utama)
        try {
            if (Shell.getShell().isRoot) {
                preferredAccessType = AccessType.ROOT
                return
            }
        } catch (e: Exception) { }

        // 2. Cek Shizuku (Prioritas Kedua)
        try {
            if (isShizukuAvailable()) {
                if (Shizuku.checkSelfPermission() == 0) {
                    preferredAccessType = AccessType.SHIZUKU
                    return
                } else {
                    // Ada Shizuku tapi belum diizinkan
                    Log.w("ZKM_Shell", "Shizuku detected but NO PERMISSION")
                }
            }
        } catch (e: Exception) { }

        preferredAccessType = AccessType.NONE
    }

    fun executeWithResult(command: String): String {
        // AUTO-RETRY: Kalau belum punya akses, coba cari akses lagi!
        if (preferredAccessType == AccessType.NONE) {
            refreshAccess()
        }

        val result = when (preferredAccessType) {
            AccessType.ROOT -> {
                try {
                    // Exec via LibSU
                    Shell.cmd(command).exec().out.joinToString("\n")
                } catch(e: Exception){ "" }
            }
            AccessType.SHIZUKU -> executeShizuku(command)
            else -> {
                if (!hasLoggedError) {
                    Log.e("ZKM_Shell", "GAGAL EKSEKUSI: Tidak ada akses Root atau Shizuku!")
                    hasLoggedError = true 
                }
                ""
            }
        }
        
        // PENTING: Trim spasi/newline di awal & akhir agar parsing angka tidak error
        return result.trim()
    }

    private fun executeShizuku(command: String): String {
        return try {
            // Menggunakan Reflection untuk akses hidden method newProcess biar bisa run command kompleks
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess", 
                Array<String>::class.java, 
                Array<String>::class.java, 
                String::class.java
            )
            method.isAccessible = true
            
            // PENTING: Pake "sh", "-c" biar command ribet (pake pipe/grep) bisa jalan
            val process = method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
            
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val sb = StringBuilder()
            var line: String?
            
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
            
            process.waitFor()
            sb.toString()
        } catch (e: Exception) {
            Log.e("ZKM_Shizuku", "Error exec: ${e.message}")
            ""
        }
    }

    // --- HELPER FUNCTIONS ---
    
    fun isShizukuAvailable(): Boolean = try { 
        Shizuku.pingBinder() 
    } catch (e: Exception) { 
        false 
    }
    
    fun isShizukuPermissionGranted(): Boolean = try { 
        isShizukuAvailable() && Shizuku.checkSelfPermission() == 0 
    } catch (e: Exception) { 
        false 
    }
    
    fun requestShizukuPermission() { 
        try { 
            if (isShizukuAvailable() && Shizuku.checkSelfPermission() != 0) {
                Shizuku.requestPermission(1001) 
            }
        } catch (e: Exception) {
            Log.e("ZKM_Shizuku", "Request permission failed: ${e.message}")
        } 
    }
}
