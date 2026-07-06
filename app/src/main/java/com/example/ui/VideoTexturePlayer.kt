package com.example.ui

import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

@Composable
fun VideoTexturePlayer(
    videoUri: Uri,
    isPlaying: Boolean,
    seekPosition: Long?,
    contrast: Float,
    saturation: Float,
    onSeekConsumed: () -> Unit,
    onProgressUpdate: (position: Long, duration: Long) -> Unit,
    modifier: Modifier = Modifier,
    onVideoLoaded: (width: Int, height: Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var videoAspectRatio by remember { mutableStateOf(16f / 9f) }
    var activeSurface by remember { mutableStateOf<Surface?>(null) }

    // Handle MediaPlayer instantiation and clean up
    DisposableEffect(videoUri) {
        val player = MediaPlayer().apply {
            try {
                setDataSource(context, videoUri)
                isLooping = true // loop standard video for projector continuous play
                prepareAsync()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        mediaPlayer = player

        player.setOnPreparedListener { mp ->
            val width = mp.videoWidth
            val height = mp.videoHeight
            if (width > 0 && height > 0) {
                videoAspectRatio = width.toFloat() / height.toFloat()
                onVideoLoaded(width, height)
            }
            if (isPlaying) {
                mp.start()
            }
            onProgressUpdate(0L, mp.duration.toLong())
        }

        player.setOnVideoSizeChangedListener { _, width, height ->
            if (width > 0 && height > 0) {
                videoAspectRatio = width.toFloat() / height.toFloat()
                onVideoLoaded(width, height)
            }
        }

        onDispose {
            try {
                player.stop()
            } catch (e: Exception) {}
            player.release()
            mediaPlayer = null
        }
    }

    // React to play/pause state changes
    LaunchedEffect(isPlaying) {
        mediaPlayer?.let { player ->
            try {
                if (isPlaying && !player.isPlaying) {
                    player.start()
                } else if (!isPlaying && player.isPlaying) {
                    player.pause()
                }
            } catch (e: Exception) {}
        }
    }

    // React to manual seek commands
    LaunchedEffect(seekPosition) {
        if (seekPosition != null) {
            try {
                mediaPlayer?.seekTo(seekPosition.toInt())
            } catch (e: Exception) {}
            onSeekConsumed()
        }
    }

    // Periodic progress reporting
    LaunchedEffect(videoUri, isPlaying) {
        while (true) {
            mediaPlayer?.let { player ->
                try {
                    if (player.isPlaying) {
                        onProgressUpdate(player.currentPosition.toLong(), player.duration.toLong())
                    }
                } catch (e: Exception) {}
            }
            delay(500)
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(st: android.graphics.SurfaceTexture, width: Int, height: Int) {
                            val s = Surface(st)
                            activeSurface = s
                            mediaPlayer?.setSurface(s)
                        }

                        override fun onSurfaceTextureSizeChanged(st: android.graphics.SurfaceTexture, width: Int, height: Int) {}

                        override fun onSurfaceTextureDestroyed(st: android.graphics.SurfaceTexture): Boolean {
                            activeSurface?.release()
                            activeSurface = null
                            mediaPlayer?.setSurface(null)
                            return true
                        }

                        override fun onSurfaceTextureUpdated(st: android.graphics.SurfaceTexture) {}
                    }
                }
            },
            modifier = Modifier
                .aspectRatio(videoAspectRatio)
                .fillMaxSize(),
            update = { textureView ->
                activeSurface?.let { s ->
                    try {
                        mediaPlayer?.setSurface(s)
                    } catch (e: Exception) {}
                }

                // Apply dynamic hardware-accelerated contrast and saturation paint directly to the view!
                try {
                    val c = contrast
                    val s = saturation
                    val t = 0.5f * (1f - c)
                    val lr = 0.213f
                    val lg = 0.715f
                    val lb = 0.072f
                    val ms = 1.0f - s

                    val m0 = (lr * ms + s) * c
                    val m1 = (lg * ms) * c
                    val m2 = (lb * ms) * c

                    val m5 = (lr * ms) * c
                    val m6 = (lg * ms + s) * c
                    val m7 = (lb * ms) * c

                    val m10 = (lr * ms) * c
                    val m11 = (lg * ms) * c
                    val m12 = (lb * ms + s) * c

                    val nativeMatrix = android.graphics.ColorMatrix(
                        floatArrayOf(
                            m0, m1, m2, 0f, t * 255f,
                            m5, m6, m7, 0f, t * 255f,
                            m10, m11, m12, 0f, t * 255f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    val paint = android.graphics.Paint().apply {
                        colorFilter = android.graphics.ColorMatrixColorFilter(nativeMatrix)
                    }
                    textureView.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, paint)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }
}
