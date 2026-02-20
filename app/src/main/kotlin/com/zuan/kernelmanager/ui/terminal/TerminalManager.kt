/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.terminal

import android.content.Context
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import java.io.File

object TerminalManager {

    var uiUpdater: (() -> Unit)? = null

    fun createSession(context: Context): TerminalSession {
        // Setup direktori kerja ke folder files aplikasi
        val workDir = context.filesDir.absolutePath
        val shellPath = "/system/bin/sh"
        
        // Environment dasar agar command Linux (ls, cd, dll) berjalan
        val env = arrayOf(
            "HOME=$workDir",
            "PATH=/sbin:/system/sbin:/system/bin:/system/xbin:/odm/bin:/vendor/bin:/vendor/xbin"
        )
        val args = arrayOf<String>()

        val session = TerminalSession(
            shellPath,
            workDir,
            args,
            env,
            2000,
            object : TerminalSessionClient {
                override fun onTextChanged(changedSession: TerminalSession) {
                    uiUpdater?.invoke()
                }
                override fun onTitleChanged(changedSession: TerminalSession) {}
                override fun onSessionFinished(finishedSession: TerminalSession) {}
                override fun onBell(session: TerminalSession) {}
                override fun onColorsChanged(session: TerminalSession) {}
                override fun onCopyTextToClipboard(session: TerminalSession, text: String?) {}
                override fun onPasteTextFromClipboard(session: TerminalSession?) {}
                override fun onTerminalCursorStateChange(enabled: Boolean) {}
                override fun setTerminalShellPid(session: TerminalSession, pid: Int) {}
                override fun getTerminalCursorStyle(): Int = 0 // Block cursor
                override fun logError(tag: String?, message: String?) {}
                override fun logWarn(tag: String?, message: String?) {}
                override fun logInfo(tag: String?, message: String?) {}
                override fun logDebug(tag: String?, message: String?) {}
                override fun logVerbose(tag: String?, message: String?) {}
                override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {}
                override fun logStackTrace(tag: String?, e: Exception?) {}
            }
        )
        
        // Bersihkan layar terminal saat pertama kali dibuka biar rapi
        // (Opsional, karena di atasnya sudah ada Header Compose)
        // session.write("clear\r")

        return session
    }
}
