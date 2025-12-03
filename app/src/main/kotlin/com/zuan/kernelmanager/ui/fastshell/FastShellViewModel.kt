package com.zuan.kernelmanager.ui.fastshell

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell
import com.zuan.kernelmanager.utils.FastShellUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Collections
import java.util.UUID

data class LogItem(
    val id: String = UUID.randomUUID().toString(),
    val content: AnnotatedString
)

class FastShellViewModel : ViewModel() {

    private val _logs = mutableStateListOf<LogItem>()
    val logs: List<LogItem> get() = _logs

    var commandInput by mutableStateOf("")
    var shouldScrollToBottom by mutableStateOf(false)
    
    // State: Apakah command sedang berjalan?
    var isRunning by mutableStateOf(false)

    // Private Shell Instance (agar bisa di-close/kill)
    private var currentShell: Shell? = null

    // Buffer & Config
    private val logBuffer = Collections.synchronizedList(mutableListOf<String>())
    private var flushJob: Job? = null
    private val MAX_LOG_HISTORY = 300 
    private val LINES_PER_CHUNK = 100  

    init {
        addLogSafe("Zuan Shell v2.3 (Stable Fixed)")
        checkRoot()
        startLogFlusher()
    }

    private fun startLogFlusher() {
        flushJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                if (logBuffer.isNotEmpty()) {
                    val rawData = synchronized(logBuffer) {
                        val temp = ArrayList(logBuffer)
                        logBuffer.clear()
                        temp
                    }

                    if (rawData.isNotEmpty()) {
                        val newItems = ArrayList<LogItem>()
                        val chunks = rawData.chunked(LINES_PER_CHUNK)

                        for (chunk in chunks) {
                            val textBlock = chunk.joinToString("\n")
                            val styledText = FastShellUtils.parseAnsi(textBlock)
                            newItems.add(LogItem(content = styledText))
                        }

                        withContext(Dispatchers.Main) {
                            _logs.addAll(newItems)
                            if (_logs.size > MAX_LOG_HISTORY) {
                                val removeCount = _logs.size - MAX_LOG_HISTORY
                                _logs.removeRange(0, removeCount)
                            }
                            shouldScrollToBottom = true
                        }
                    }
                }
                delay(150) 
            }
        }
    }

    private fun checkRoot() {
        viewModelScope.launch(Dispatchers.IO) {
            val isRoot = Shell.rootAccess()
            val status = if(isRoot) "\u001B[32mGRANTED\u001B[0m" else "\u001B[31mDENIED\u001B[0m"
            logBuffer.add("Root Access: $status")
            logBuffer.add("Type 'help' or any shell command.")
        }
    }

    private fun addLogSafe(text: String) {
        logBuffer.add(text)
    }

    fun runCommand() {
        val cmd = commandInput.trim()
        if (cmd.isEmpty()) return

        if (cmd == "clear") {
            _logs.clear()
            commandInput = ""
            return
        }

        commandInput = "" 
        isRunning = true
        addLogSafe("\n\u001B[32m$ \u001B[0m$cmd")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Pastikan kita punya Shell instance aktif
                if (currentShell == null || currentShell?.isAlive == false) {
                    // Membuat instance Shell baru (bukan global)
                    currentShell = Shell.Builder.create().build()
                }

                val callback = object : CallbackList<String>() {
                    override fun onAddElement(s: String) {
                        logBuffer.add(s)
                    }
                }
                
                // 2. Gunakan shell instance tersebut untuk menjalankan job
                // .submit {} mengembalikan Unit, jadi kita tidak assign ke variabel
                currentShell?.newJob()?.add(cmd)?.to(callback)?.submit { result ->
                    // Callback saat selesai
                    viewModelScope.launch(Dispatchers.Main) {
                        isRunning = false
                    }
                }
            } catch (e: Exception) {
                logBuffer.add("\u001B[31mError: ${e.message}\u001B[0m")
                withContext(Dispatchers.Main) {
                    isRunning = false
                }
            }
        }
    }

    // Fungsi STOP COMMAND (Fix: Kill Shell Instance)
    fun stopCommand() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Menutup Shell akan mematikan semua proses child (seperti ping/logcat)
                currentShell?.close()
                currentShell = null // Reset agar command berikutnya bikin shell baru
                
                logBuffer.add("\u001B[31m^C (Interrupted)\u001B[0m")
            } catch (e: IOException) {
                logBuffer.add("Failed to stop: ${e.message}")
            }
            
            withContext(Dispatchers.Main) {
                isRunning = false
            }
        }
    }
    
    fun updateInput(newInput: String) {
        commandInput = newInput
    }

    fun onScrolledToBottom() {
        shouldScrollToBottom = false
    }
    
    override fun onCleared() {
        super.onCleared()
        flushJob?.cancel()
        try {
            currentShell?.close()
        } catch (e: Exception) { /* ignore */ }
    }
}
