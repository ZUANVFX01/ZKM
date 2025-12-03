/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
 /*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.utils

import com.zuan.kernelmanager.BuildConfig

object BetaFeatures {
    val isBetaFeaturesEnabled: Boolean
        get() = BuildConfig.ENABLE_BETA_FEATURES
}
