/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.terminal

import android.net.Uri
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
fun VideoBackground(
    videoUri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Inisialisasi ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE // Loop video
            volume = 0f // Mute audio
            // PENTING: Mode ini agar video crop to fill (seperti centerCrop)
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        }
    }

    // Setup Media & Lifecycle
    DisposableEffect(videoUri) {
        val mediaItem = MediaItem.fromUri(videoUri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        onDispose {
            // Bersihkan surface dan release player saat composable hancur
            exoPlayer.clearVideoTextureView(null)
            exoPlayer.release()
        }
    }

    // Render menggunakan TextureView (Wajib untuk efek Blur)
    AndroidView(
        factory = { ctx ->
            TextureView(ctx).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                // Set listener opsional jika butuh penyesuaian lain, 
                // tapi biasanya default TextureView sudah cukup.
            }
        },
        update = { view ->
            // Bind ExoPlayer ke TextureView ini
            exoPlayer.setVideoTextureView(view)
        },
        modifier = modifier
    )
}
