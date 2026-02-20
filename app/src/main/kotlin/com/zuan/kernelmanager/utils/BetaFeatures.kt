/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.utils

import com.zuan.kernelmanager.BuildConfig

object BetaFeatures {
    val isBetaFeaturesEnabled: Boolean
        get() = BuildConfig.ENABLE_BETA_FEATURES
}
