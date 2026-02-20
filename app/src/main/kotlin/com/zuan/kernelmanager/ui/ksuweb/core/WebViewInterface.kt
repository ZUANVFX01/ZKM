/*
 * Original code from: 5ec1cff (KsuWebUIStandalone)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.ksuweb.core

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.internal.UiThreadHandler
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.CompletableFuture

class WebViewInterface(
    val context: Context,
    private val webView: WebView,
    private val modDir: String
) {

    @JavascriptInterface
    fun exec(cmd: String): String {
        // Simple exec sync
        return Shell.cmd(cmd).exec().out.joinToString("\n")
    }

    @JavascriptInterface
    fun exec(cmd: String, callbackFunc: String) {
        exec(cmd, null, callbackFunc)
    }

    private fun processOptions(sb: StringBuilder, options: String?) {
        val opts = if (options == null) JSONObject() else JSONObject(options)
        val cwd = opts.optString("cwd")
        if (cwd.isNotEmpty()) {
            sb.append("cd $cwd;")
        }
        opts.optJSONObject("env")?.let { env ->
            env.keys().forEach { key ->
                sb.append("export $key=${env.getString(key)};")
            }
        }
    }

    @JavascriptInterface
    fun exec(cmd: String, options: String?, callbackFunc: String) {
        val finalCommand = StringBuilder()
        processOptions(finalCommand, options)
        finalCommand.append(cmd)

        val result = Shell.cmd(finalCommand.toString()).exec()
        val stdout = result.out.joinToString("\n")
        val stderr = result.err.joinToString("\n")

        val jsCode = "(function() { try { ${callbackFunc}(${result.code}, ${JSONObject.quote(stdout)}, ${JSONObject.quote(stderr)}); } catch(e) { console.error(e); } })();"
        
        postToWeb { webView.evaluateJavascript(jsCode, null) }
    }

    @JavascriptInterface
    fun spawn(command: String, args: String, options: String?, callbackFunc: String) {
        val finalCommand = StringBuilder()
        processOptions(finalCommand, options)

        if (args.isNotEmpty()) {
            finalCommand.append(command).append(" ")
            JSONArray(args).let { argsArray ->
                for (i in 0 until argsArray.length()) {
                    finalCommand.append(argsArray.getString(i)).append(" ")
                }
            }
        } else {
            finalCommand.append(command)
        }

        val emitData = { name: String, data: String ->
            val js = "(function() { try { ${callbackFunc}.${name}.emit('data', ${JSONObject.quote(data)}); } catch(e) { console.error('emitData', e); } })();"
            postToWeb { webView.evaluateJavascript(js, null) }
        }

        val stdout = object : CallbackList<String>(UiThreadHandler::runAndWait) {
            override fun onAddElement(s: String) = emitData("stdout", s)
        }
        val stderr = object : CallbackList<String>(UiThreadHandler::runAndWait) {
            override fun onAddElement(s: String) = emitData("stderr", s)
        }

        // Jalankan async
        CompletableFuture.supplyAsync {
            Shell.cmd(finalCommand.toString()).to(stdout, stderr).exec()
        }.thenAccept { result ->
            val emitExit = "(function() { try { ${callbackFunc}.emit('exit', ${result.code}); } catch(e) { console.error(`emitExit error: \${e}`); } })();"
            postToWeb { webView.evaluateJavascript(emitExit, null) }
        }
    }

    @JavascriptInterface
    fun toast(msg: String) {
        postToWeb { Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
    }

    @JavascriptInterface
    fun moduleInfo(): String {
        val info = JSONObject()
        info.put("moduleDir", modDir)
        info.put("id", java.io.File(modDir).name)
        return info.toString()
    }

    private fun postToWeb(block: () -> Unit) {
        Handler(Looper.getMainLooper()).post(block)
    }
}
