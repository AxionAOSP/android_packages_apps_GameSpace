/*
 * Copyright (C) 2025-2026 AxionOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.chaldeaprjkt.gamespace.gamebar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import androidx.compose.runtime.mutableStateOf

class MusicController(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val sessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
    private val handler = Handler(Looper.getMainLooper())

    val trackTitle = mutableStateOf("No Track Playing")
    val artistName = mutableStateOf("Unknown Artist")
    val isPlaying = mutableStateOf(false)
    val hasActiveMedia = mutableStateOf(false)
    val albumArt = mutableStateOf<Bitmap?>(null)
    val activeApp = mutableStateOf<String?>(null)
    val activeAppIcon = mutableStateOf<Drawable?>(null)

    private var activeController: MediaController? = null

    private val sessionCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            handler.post {
                if (metadata != null) {
                    trackTitle.value = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "No Title"
                    artistName.value = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Unknown Artist"
                    albumArt.value = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                        ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
                    hasActiveMedia.value = true
                }
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            handler.post {
                if (state != null) {
                    isPlaying.value = state.state == PlaybackState.STATE_PLAYING
                }
            }
        }
    }

    private val sessionsChangedListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        handler.post {
            updateActiveController(controllers)
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            val action = intent.action ?: return

            if (action.endsWith("metadatachanged") || action.endsWith("metachanged")) {
                val track = intent.getStringExtra("track") ?: intent.getStringExtra("id")
                val artist = intent.getStringExtra("artist")
                if (track != null) {
                    trackTitle.value = track
                    artistName.value = artist ?: "Unknown Artist"
                    albumArt.value = null
                    
                    val pkg = action.substringBeforeLast(".")
                    activeAppIcon.value = try {
                        this@MusicController.context.packageManager.getApplicationIcon(pkg)
                    } catch (_: Exception) {
                        null
                    }
                    activeApp.value = try {
                        val pm = this@MusicController.context.packageManager
                        val appInfo = pm.getApplicationInfo(pkg, 0)
                        pm.getApplicationLabel(appInfo).toString()
                    } catch (_: Exception) {
                        "Music Player"
                    }
                    hasActiveMedia.value = true
                }
            }

            if (intent.hasExtra("playing") || intent.hasExtra("playstate")) {
                isPlaying.value = intent.getBooleanExtra("playing", false) || 
                                  intent.getIntExtra("playstate", 0) == 1
            }
        }
    }

    private fun updateActiveController(controllers: List<MediaController>?) {
        activeController?.unregisterCallback(sessionCallback)
        activeController = null

        val controller = controllers?.firstOrNull()
        if (controller != null) {
            activeController = controller
            controller.registerCallback(sessionCallback)
            
            val metadata = controller.metadata
            if (metadata != null) {
                trackTitle.value = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "No Title"
                artistName.value = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Unknown Artist"
                albumArt.value = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                    ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
                hasActiveMedia.value = true
            }
            
            val pbState = controller.playbackState
            if (pbState != null) {
                isPlaying.value = pbState.state == PlaybackState.STATE_PLAYING
            }

            val pkg = controller.packageName
            activeAppIcon.value = try {
                context.packageManager.getApplicationIcon(pkg)
            } catch (_: Exception) {
                null
            }

            activeApp.value = if (pkg != null) {
                try {
                    val pm = context.packageManager
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (_: Exception) {
                    "Music Player"
                }
            } else {
                null
            }
        } else {
            hasActiveMedia.value = false
            activeApp.value = null
            activeAppIcon.value = null
        }
    }

    fun register() {
        try {
            sessionManager.addOnActiveSessionsChangedListener(sessionsChangedListener, null, handler)
            val controllers = sessionManager.getActiveSessions(null)
            updateActiveController(controllers)
        } catch (e: Exception) {
            e.printStackTrace()
            registerBroadcastFallback()
        }
    }

    fun unregister() {
        try {
            sessionManager.removeOnActiveSessionsChangedListener(sessionsChangedListener)
        } catch (_: Exception) {}
        activeController?.unregisterCallback(sessionCallback)
        activeController = null
        unregisterBroadcastFallback()
    }

    private fun registerBroadcastFallback() {
        val filter = IntentFilter().apply {
            addAction("com.android.music.metachanged")
            addAction("com.android.music.playstatechanged")
            addAction("com.spotify.music.metadatachanged")
            addAction("com.spotify.music.playbackstatechanged")
        }
        context.registerReceiver(broadcastReceiver, filter, Context.RECEIVER_EXPORTED)
    }

    private fun unregisterBroadcastFallback() {
        try {
            context.unregisterReceiver(broadcastReceiver)
        } catch (_: Exception) {}
    }

    fun togglePlayPause() {
        val controller = activeController
        if (controller != null) {
            val pbState = controller.playbackState
            if (pbState != null) {
                if (pbState.state == PlaybackState.STATE_PLAYING) {
                    controller.transportControls.pause()
                } else {
                    controller.transportControls.play()
                }
            } else {
                sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            }
        } else {
            sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        }
    }

    fun skipToNext() {
        val controller = activeController
        if (controller != null) {
            controller.transportControls.skipToNext()
        } else {
            sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
        }
    }

    fun skipToPrevious() {
        val controller = activeController
        if (controller != null) {
            controller.transportControls.skipToPrevious()
        } else {
            sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        }
    }

    private fun sendMediaKey(keyCode: Int) {
        val eventTime = SystemClock.uptimeMillis()
        audioManager.dispatchMediaKeyEvent(
            KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0)
        )
        audioManager.dispatchMediaKeyEvent(
            KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0)
        )
    }
}
