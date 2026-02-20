/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.logsview

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

enum class LogLevel(val letter: String, val color: Color, val displayName: String = "") {
    VERBOSE("V", Color(0xFF9AA0A6), "Verbose"),
    DEBUG("D", Color(0xFF4CC9F0), "Debug"),
    INFO("I", Color(0xFF00C853), "Info"),
    WARN("W", Color(0xFFFFB300), "Warning"),
    ERROR("E", Color(0xFFFF1744), "Error"),
    ASSERT("A", Color(0xFFD500F9), "Assert");

    companion object {
        fun fromLetter(letter: String): LogLevel {
            return entries.find { it.letter == letter.uppercase() } ?: VERBOSE
        }
    }
}

data class LogDisplayOptions(
    val showDate: Boolean = true,
    val showTime: Boolean = true,
    val showTag: Boolean = true,
    val showPid: Boolean = false,
    val showTid: Boolean = false,
    val showPackageName: Boolean = false,
    val isCompact: Boolean = false
)

data class LogEntry(
    val id: Long,
    val timestamp: String,
    val date: String,
    val pid: String,
    val tid: String,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val fullRaw: String
)

object LogcatReader {
    private val LOG_PATTERN = Pattern.compile(
        """^(\d{2}-\d{2})\s+(\d{2}:\d{2}:\d{2}\.\d{3})\s+(\d+)\s+(\d+)\s+([A-Z])\s+(.*?):\s+(.*)$"""
    )
    
    private val DATE_FORMAT = SimpleDateFormat("MM-dd HH:mm:ss", Locale.US)

    fun parseLine(line: String, id: Long): LogEntry? {
        val matcher = LOG_PATTERN.matcher(line)
        
        return if (matcher.matches()) {
            val date = matcher.group(1) ?: ""
            val time = matcher.group(2) ?: ""
            val pid = matcher.group(3) ?: "0"
            val tid = matcher.group(4) ?: "0"
            val levelStr = matcher.group(5) ?: "V"
            val tag = matcher.group(6)?.trim() ?: "Unknown"
            val msg = matcher.group(7) ?: ""
            
            LogEntry(
                id = id,
                timestamp = time,
                date = date,
                pid = pid,
                tid = tid,
                level = LogLevel.fromLetter(levelStr),
                tag = tag,
                message = msg,
                fullRaw = line
            )
        } else {
            if (line.isBlank() || line.startsWith("---------")) return null
            
            val now = DATE_FORMAT.format(Date()).split(" ")
            LogEntry(
                id = id,
                timestamp = if (now.size > 1) now[1] else "",
                date = if (now.isNotEmpty()) now[0] else "",
                pid = "?",
                tid = "?",
                level = LogLevel.VERBOSE,
                tag = "System",
                message = line,
                fullRaw = line
            )
        }
    }
    
    fun clearLogcat() {
        try {
            val process = Runtime.getRuntime().exec("logcat -c")
            process.waitFor(2, TimeUnit.SECONDS)
            process.destroy()
        } catch (e: Exception) { 
            e.printStackTrace() 
        }
    }
}
