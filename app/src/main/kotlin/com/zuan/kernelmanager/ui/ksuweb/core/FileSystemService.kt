/*
 * Original code from: 5ec1cff (KsuWebUIStandalone)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.ksuweb.core

import android.content.Intent
import android.os.IBinder
import com.topjohnwu.superuser.ipc.RootService
import com.topjohnwu.superuser.nio.FileSystemManager

class FileSystemService : RootService() {
    override fun onBind(intent: Intent): IBinder {
        return FileSystemManager.getService()
    }
}
