/*
 * Original code from: 5ec1cff (KsuWebUIStandalone)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.ksuweb.core

import java.net.URLConnection
import java.util.Locale

object MimeUtil {
    fun getMimeFromFileName(fileName: String?): String? {
        if (fileName == null) return null
        val mimeType = URLConnection.guessContentTypeFromName(fileName)
        return mimeType ?: guessHardcodedMime(fileName)
    }

    private fun guessHardcodedMime(fileName: String): String? {
        val finalFullStop = fileName.lastIndexOf('.')
        if (finalFullStop == -1) return null
        val extension = fileName.substring(finalFullStop + 1).lowercase(Locale.getDefault())

        return when (extension) {
            // Web Core
            "html", "htm", "shtml", "shtm", "ehtml" -> "text/html"
            "css" -> "text/css"
            "js", "mjs", "cjs" -> "application/javascript"
            "json" -> "application/json"
            "xml" -> "text/xml"
            
            // Fonts (Penting untuk UI biar icon muncul)
            "woff" -> "font/woff"
            "woff2" -> "font/woff2"
            "ttf" -> "font/ttf"
            "otf" -> "font/otf"

            // WebAssembly (Penting untuk WebUI modern performa tinggi)
            "wasm" -> "application/wasm"

            // Images
            "png" -> "image/png"
            "jpg", "jpeg", "jfif", "pjpeg", "pjp" -> "image/jpeg"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "svg", "svgz" -> "image/svg+xml"
            "ico" -> "image/x-icon"
            "bmp" -> "image/bmp"

            // Audio/Video
            "mp3" -> "audio/mpeg"
            "mp4", "m4v" -> "video/mp4"
            "webm" -> "video/webm"
            "ogg", "oga", "opus" -> "audio/ogg"
            "wav" -> "audio/wav"
            
            // Archives/Docs
            "pdf" -> "application/pdf"
            "zip" -> "application/zip"
            "gz", "tgz" -> "application/gzip"
            
            else -> null // Biarkan null agar WebView mencoba menebak sendiri sebagai fallback
        }
    }
}
