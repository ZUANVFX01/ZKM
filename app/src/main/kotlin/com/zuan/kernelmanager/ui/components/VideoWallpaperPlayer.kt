/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.components

import android.net.Uri
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

@OptIn(UnstableApi::class) 
@Composable
fun VideoWallpaperPlayer(
    uri: Uri,
    modifier: Modifier = Modifier,
    volume: Float = 0f
) {
    val context = LocalContext.current

    // 1. Setup ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE // Loop
            
            // [FIX] Gunakan 'this.volume' untuk merujuk ke property player
            // 'volume' merujuk ke parameter fungsi (val)
            this.volume = volume 
            
            // [PENTING] Scaling Mode untuk TextureView agar Full Screen (Crop)
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            playWhenReady = true
        }
    }

    // 2. Load Media
    LaunchedEffect(uri) {
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    // 3. Cleanup
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // 4. Render menggunakan TextureView (Bukan PlayerView)
    // TextureView wajib digunakan agar library Haze/Blur bisa "melihat" videonya.
    AndroidView(
        factory = { ctx ->
            TextureView(ctx).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                // Attach Player ke TextureView ini
                exoPlayer.setVideoTextureView(this)
            }
        },
        modifier = modifier
    )
}
