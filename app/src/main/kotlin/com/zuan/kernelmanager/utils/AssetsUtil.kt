/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream

object AssetsUtil {
    fun exportFiles(context: Context, src: String, out: String) {
        try {
            val fileNames = context.assets.list(src)
            if (!fileNames.isNullOrEmpty()) {
                val file = File(out)
                file.mkdirs()
                for (fileName in fileNames) {
                    exportFiles(context, "$src/$fileName", "$out/$fileName")
                }
            } else {
                val inputStream = context.assets.open(src)
                val fileOutputStream = FileOutputStream(File(out))
                val buffer = ByteArray(1024)
                var byteCount: Int
                while (inputStream.read(buffer).also { byteCount = it } != -1) {
                    fileOutputStream.write(buffer, 0, byteCount)
                }
                fileOutputStream.flush()
                inputStream.close()
                fileOutputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
