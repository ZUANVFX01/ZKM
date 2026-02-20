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
import android.util.Log
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader
import com.topjohnwu.superuser.nio.FileSystemManager
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.zip.GZIPInputStream

class RemoteFsPathHandler(
    context: Context,
    directory: File,
    private val fs: FileSystemManager
) : WebViewAssetLoader.PathHandler {

    private val mDirectory: File
    private val FORBIDDEN_DATA_DIRS = arrayOf("/data/data", "/data/system")

    init {
        try {
            mDirectory = File(getCanonicalDirPath(directory))
            if (!isAllowedInternalStorageDir(context)) {
                throw IllegalArgumentException("Directory not allowed: $directory")
            }
        } catch (e: IOException) {
            throw IllegalArgumentException("Failed to resolve path: ${directory.path}", e)
        }
    }

    private fun isAllowedInternalStorageDir(context: Context): Boolean {
        val dir = getCanonicalDirPath(mDirectory)
        for (forbiddenPath in FORBIDDEN_DATA_DIRS) {
            if (dir.startsWith(forbiddenPath)) return false
        }
        return true
    }

    override fun handle(path: String): WebResourceResponse {
        try {
            val file = getCanonicalFileIfChild(mDirectory, path)
            if (file != null) {
                val `is` = openFile(file, fs)
                val mimeType = MimeUtil.getMimeFromFileName(path) ?: "text/plain"
                return WebResourceResponse(mimeType, null, `is`)
            } else {
                Log.e("RemoteFs", "File outside directory: $path")
            }
        } catch (e: IOException) {
            Log.e("RemoteFs", "Error opening: $path", e)
        }
        return WebResourceResponse(null, null, null)
    }

    @Throws(IOException::class)
    private fun getCanonicalDirPath(file: File): String {
        var canonicalPath = file.canonicalPath
        if (!canonicalPath.endsWith("/")) canonicalPath += "/"
        return canonicalPath
    }

    @Throws(IOException::class)
    private fun getCanonicalFileIfChild(parent: File, child: String): File? {
        val parentPath = getCanonicalDirPath(parent)
        val childPath = File(parent, child).canonicalPath
        return if (childPath.startsWith(parentPath)) File(childPath) else null
    }

    @Throws(IOException::class)
    private fun openFile(file: File, fs: FileSystemManager): InputStream {
        val stream = fs.getFile(file.absolutePath).newInputStream()
        return if (file.path.endsWith(".svgz")) GZIPInputStream(stream) else stream
    }
}
