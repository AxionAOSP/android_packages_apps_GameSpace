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

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import io.chaldeaprjkt.gamespace.R
import io.chaldeaprjkt.gamespace.data.AppSettings

class CrosshairController(
    private val context: Context,
    private val wm: WindowManager,
    private val appSettings: AppSettings
) {
    private val handler = Handler(Looper.getMainLooper())
    private var crosshairView: ImageView? = null
    private var isAdded = false

    fun updateState() {
        handler.post {
            if (appSettings.crosshairEnabled) {
                showCrosshair()
            } else {
                hideCrosshair()
            }
        }
    }

    private fun showCrosshair() {
        val view = crosshairView ?: ImageView(context).also {
            crosshairView = it
        }

        val styleRes = when (appSettings.crosshairStyle) {
            1 -> R.drawable.crosshair_style_1
            2 -> R.drawable.crosshair_style_2
            3 -> R.drawable.crosshair_style_3
            4 -> R.drawable.crosshair_style_4
            5 -> R.drawable.crosshair_style_5
            6 -> R.drawable.crosshair_style_6
            7 -> R.drawable.crosshair_style_7
            8 -> R.drawable.crosshair_style_8
            else -> R.drawable.crosshair_style_1
        }
        view.setImageResource(styleRes)

        view.setColorFilter(appSettings.crosshairColor, PorterDuff.Mode.SRC_IN)
        view.alpha = appSettings.crosshairOpacity

        val sizePx = (appSettings.crosshairSize * context.resources.displayMetrics.density).toInt()
        val params = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            format = PixelFormat.TRANSLUCENT
            width = sizePx
            height = sizePx
            gravity = Gravity.CENTER
            x = (appSettings.crosshairOffsetX * context.resources.displayMetrics.density).toInt()
            y = (appSettings.crosshairOffsetY * context.resources.displayMetrics.density).toInt()
        }

        try {
            if (!isAdded) {
                wm.addView(view, params)
                isAdded = true
            } else {
                wm.updateViewLayout(view, params)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideCrosshair() {
        handler.post {
            crosshairView?.let { view ->
                if (isAdded) {
                    try {
                        wm.removeViewImmediate(view)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    isAdded = false
                }
            }
            crosshairView = null
        }
    }
}
