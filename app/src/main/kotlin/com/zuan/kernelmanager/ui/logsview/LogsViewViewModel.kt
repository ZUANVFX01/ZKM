/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.logsview

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class LogSettings(
    val pollInterval: Long = 250L,
    val selectedBuffers: Set<LogBuffer> = setOf(
        LogBuffer.MAIN, 
        LogBuffer.SYSTEM, 
        LogBuffer.CRASH, 
        LogBuffer.KERNEL
    ),
    val maxLogsInMemory: Int = 300_000,
    val saveLocation: SaveLocation = SaveLocation.INTERNAL
)

enum class LogBuffer(val command: String, val displayName: String) {
    MAIN("main", "Main"),
    SYSTEM("system", "System"),
    CRASH("crash", "Crash"),
    KERNEL("kernel", "Kernel"),
    EVENTS("events", "Events"),
    RADIO("radio", "Radio"),
    SECURITY("security", "Security")
}

enum class SaveLocation {
    INTERNAL, EXTERNAL
}

class LogsViewViewModel : ViewModel() {

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val _displayOptions = MutableStateFlow(LogDisplayOptions())
    val displayOptions: StateFlow<LogDisplayOptions> = _displayOptions.asStateFlow()
    
    // Settings State
    var settings by mutableStateOf(LogSettings())
        private set

    var searchQuery by mutableStateOf("")
        private set
    
    var isPaused by mutableStateOf(false)
        private set

    private var logJob: Job? = null
    private var process: Process? = null
    
    // High performance collections
    private val allLogsCache = Collections.synchronizedList(ArrayList<LogEntry>(10000))
    private val batchBuffer = ArrayList<LogEntry>(1000)
    private val batchLock = Any()
    
    init {
        startReadingLogs()
    }

    fun updateSettings(newSettings: LogSettings) {
        val needRestart = newSettings.selectedBuffers != settings.selectedBuffers
        settings = newSettings
        
        if (needRestart) {
            restartLogcat()
        }
    }

    fun startReadingLogs() {
        stopReadingLogs()
        
        LogcatReader.clearLogcat()
        allLogsCache.clear()
        synchronized(batchLock) { batchBuffer.clear() }
        _logs.value = emptyList()

        // Build logcat command dengan buffer selection
        val buffers = settings.selectedBuffers.joinToString(" ") { "-b ${it.command}" }
        val command = if (buffers.isNotEmpty()) "logcat -v threadtime $buffers -T 1000" 
                     else "logcat -v threadtime -T 1000"

        logJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val builder = ProcessBuilder(*command.split(" ").toTypedArray())
                builder.redirectErrorStream(true)
                process = builder.start()

                val reader = BufferedReader(InputStreamReader(process!!.inputStream), 16384) // 16KB buffer
                
                var counter = 0L
                var lastFlushTime = System.currentTimeMillis()
                val pollInterval = settings.pollInterval
                
                while (isActive) {
                    try {
                        val line = reader.readLine()
                        if (line == null) break 
                        
                        if (isPaused) {
                            delay(pollInterval)
                            continue
                        }

                        val entry = LogcatReader.parseLine(line, counter++)
                        if (entry != null) {
                            synchronized(batchLock) {
                                batchBuffer.add(entry)
                            }
                        }

                        // Flush berdasarkan waktu (poll interval) atau batch size
                        val currentTime = System.currentTimeMillis()
                        val batchSize = synchronized(batchLock) { batchBuffer.size }
                        val shouldFlush = batchSize >= 1000 || (currentTime - lastFlushTime > pollInterval)
                        
                        if (shouldFlush) {
                            flushBuffer()
                            lastFlushTime = currentTime
                        }
                        
                    } catch (e: Exception) { 
                        if (!isActive) break
                        yield()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // UI Update dengan poll interval
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(settings.pollInterval.coerceAtLeast(100))
                if (!isPaused) {
                    applyFilterFast()
                }
            }
        }
    }

    private fun stopReadingLogs() {
        logJob?.cancel()
        try { 
            process?.destroy() 
            process?.waitFor(500, TimeUnit.MILLISECONDS)
        } catch (e: Exception) { 
            e.printStackTrace()
        } finally {
            process = null
        }
    }

    private fun flushBuffer() {
        synchronized(batchLock) {
            if (batchBuffer.isEmpty()) return
            
            val batch = ArrayList(batchBuffer)
            batchBuffer.clear()
            
            synchronized(allLogsCache) {
                allLogsCache.addAll(batch)
                
                // Trim ke max logs setting
                val maxLogs = settings.maxLogsInMemory.coerceIn(1000, 500_000)
                if (allLogsCache.size > maxLogs) {
                    allLogsCache.subList(0, allLogsCache.size - maxLogs).clear()
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        applyFilterFast()
    }

    fun togglePause() {
        isPaused = !isPaused
    }

    fun clearLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            LogcatReader.clearLogcat()
            synchronized(allLogsCache) { allLogsCache.clear() }
            _logs.value = emptyList()
        }
    }
    
    fun restartLogcat() {
        startReadingLogs()
    }

    fun updateDisplayOptions(newOptions: LogDisplayOptions) {
        _displayOptions.value = newOptions
    }

    fun saveLogs(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fileName = "ZKM_Log_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.txt"
                
                val file = when (settings.saveLocation) {
                    SaveLocation.EXTERNAL -> {
                        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileName)
                    }
                    SaveLocation.INTERNAL -> {
                        File(context.getExternalFilesDir(null), fileName)
                    }
                }
                
                FileWriter(file).use { writer ->
                    val currentList = synchronized(allLogsCache) { ArrayList(allLogsCache) }
                    for (log in currentList) {
                        writer.append(log.fullRaw).append("\n")
                    }
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Saved ${allLogsCache.size} logs to ${file.absolutePath}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun applyFilterFast() {
        val query = searchQuery.lowercase()
        val maxDisplay = 2000
        
        // FIX: Menggunakan ArrayList copy dan reversed() biasa (bukan asReversed)
        // Ini mencegah ConcurrentModificationException saat background thread melakukan trimming
        val filtered = synchronized(allLogsCache) {
            if (query.isBlank()) {
                val startIndex = maxOf(0, allLogsCache.size - maxDisplay)
                // Membuat Copy list baru (ArrayList) dari sublist, lalu di-reverse.
                // Ini memutus hubungan referensi langsung ke allLogsCache yang sedang aktif di-update.
                ArrayList(allLogsCache.subList(startIndex, allLogsCache.size)).reversed()
            } else {
                // filter() secara default membuat ArrayList baru, jadi ini relatif aman
                // selama dilakukan di dalam blok synchronized
                allLogsCache.asReversed().filter { 
                    it.message.lowercase().contains(query) ||
                    it.tag.lowercase().contains(query)
                }.take(1000)
            }
        }
        
        _logs.value = filtered
    }

    override fun onCleared() {
        super.onCleared()
        stopReadingLogs()
    }
}
