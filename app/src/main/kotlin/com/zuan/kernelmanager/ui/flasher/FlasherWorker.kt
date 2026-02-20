/*
 * Original code from: libxzr (HorizonKernelFlasher)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.flasher

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.zuan.kernelmanager.utils.AssetsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*

class FlasherWorker(
    private val context: Context,
    private val uri: Uri,
    private val onLog: (String) -> Unit
) {
    private val filesDir = context.filesDir.absolutePath
    private var filePath: String = ""
    private var binaryPath: String = ""
    
    // Status flag
    private val DEBUG = false

    suspend fun startFlashing(): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Setup Path
            val documentFile = DocumentFile.fromSingleUri(context, uri)
            val fileName = documentFile?.name ?: "kernel.zip"
            filePath = "$filesDir/$fileName"
            binaryPath = "$filesDir/META-INF/com/google/android/update-binary"

            onLog("--- Starting Flash Process ---")
            
            // 2. Cleanup Old Files
            onLog("Cleaning up...")
            cleanup()

            // 3. Cek Root
            if (!rootAvailable()) {
                onLog("ERROR: No Root Access detected!")
                return@withContext false
            }

            // 4. Copy File dari URI ke Internal Storage
            onLog("Copying file to: $filePath")
            copyFile()
            if (!File(filePath).exists()) {
                onLog("ERROR: Failed to copy file.")
                return@withContext false
            }

            // 5. Extract Binary (update-binary)
            onLog("Extracting update-binary...")
            getBinary()
            
            // 6. Patch Binary (Inject mkbootfs)
            onLog("Patching binary...")
            patch()

            // 7. FLASHING (The Main Event)
            onLog(">>> EXECUTE FLASHING...")
            flash()

            onLog("\n--- FLASHING COMPLETE ---")
            onLog("You can reboot now.")
            return@withContext true

        } catch (e: Exception) {
            onLog("\nERROR: ${e.message}")
            e.printStackTrace()
            return@withContext false
        }
    }

    // --- ORIGINAL LOGIC FROM Worker.java ---

    private fun rootAvailable(): Boolean {
        return try {
            val ret = runWithNewProcessReturn(true, "id")
            ret.contains("root")
        } catch (e: IOException) {
            false
        }
    }

    private fun copyFile() {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(File(filePath)).use { output ->
                val buffer = ByteArray(1024)
                var count: Int
                while (input.read(buffer).also { count = it } != -1) {
                    output.write(buffer, 0, count)
                }
                output.flush()
            }
        }
    }

    private fun getBinary() {
        // Unzip logic from original code
        runWithNewProcessNoReturn(false, "unzip \"$filePath\" \"*/update-binary\" -d $filesDir")
        if (!File(binaryPath).exists()) throw IOException("update-binary not found inside zip")
    }

    private fun patch() {
        val mkbootfsPath = "$filesDir/mkbootfs"
        AssetsUtil.exportFiles(context, "mkbootfs", mkbootfsPath)
        
        // Command SED asli dari Worker.java
        val cmd = "sed -i '/\$BB chmod -R 755 tools bin;/i cp -f $mkbootfsPath \$AKHOME/tools;' $binaryPath"
        runWithNewProcessNoReturn(false, cmd)
    }

    private fun flash() {
        val process = ProcessBuilder("su").redirectErrorStream(true).start()
        val writer = OutputStreamWriter(process.outputStream)
        val reader = BufferedReader(InputStreamReader(process.inputStream))

        // Command original logic
        writer.write("export POSTINSTALL=$filesDir\n")
        val debugFlag = if (DEBUG) "-x " else ""
        writer.write("sh $debugFlag$binaryPath 3 1 \"$filePath\" && touch $filesDir/done\nexit\n")
        writer.flush()

        var line: String?
        while (reader.readLine().also { line = it } != null) {
            line?.let { rawLog ->
                // Filter log logic from MainActivity.java
                if (DEBUG) {
                    onLog(rawLog)
                } else if (rawLog.startsWith("ui_print")) {
                    onLog(rawLog.replace("ui_print", "").trim())
                }
            }
        }

        reader.close()
        writer.close()
        process.destroy()

        if (!File("$filesDir/done").exists()) {
            throw IOException("Flashing script did not finish successfully (no 'done' file).")
        }
    }

    private fun cleanup() {
        runWithNewProcessNoReturn(false, "rm -rf $filesDir/*")
    }

    // --- SHELL HELPER ---

    private fun runWithNewProcessNoReturn(su: Boolean, cmd: String) {
        runWithNewProcessReturn(su, cmd)
    }

    private fun runWithNewProcessReturn(su: Boolean, cmd: String): String {
        val process = ProcessBuilder(if (su) "su" else "sh").redirectErrorStream(true).start()
        val writer = OutputStreamWriter(process.outputStream)
        val reader = BufferedReader(InputStreamReader(process.inputStream))

        writer.write("$cmd\n")
        writer.write("exit\n")
        writer.flush()

        val ret = StringBuilder()
        var tmp: String?
        while (reader.readLine().also { tmp = it } != null) {
            ret.append(tmp).append("\n")
        }
        writer.close()
        reader.close()
        process.destroy()
        return ret.toString()
    }
    
    // Helper reboot static access
    companion object {
        fun rebootDevice() {
             try {
                 val p = ProcessBuilder("su").start()
                 val w = OutputStreamWriter(p.outputStream)
                 w.write("svc power reboot\nexit\n")
                 w.flush()
                 w.close()
             } catch (e: Exception) { e.printStackTrace() }
        }
    }
}
