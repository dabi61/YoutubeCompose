package com.alex.yang.youtubecompose.core.orientation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.OrientationEventListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Utilities for device orientation and fullscreen behavior.
 */

// ========================================
// 1. Device orientation detection
// ========================================

/**
 * Remembers the current physical device orientation using [OrientationEventListener].
 *
 * @return The current device orientation.
 */
@Composable
fun rememberDeviceOrientation(): DeviceOrientation {
    val context = LocalContext.current

    var currentOrientation by remember {
        mutableStateOf(DeviceOrientation.PORTRAIT)
    }

    DisposableEffect(context) {
        val listener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return

                // Convert the raw degree value to a semantic orientation.
                val newOrientation = when (orientation) {
                    // 0 degrees +/- 45 degrees -> portrait.
                    in 315..360, in 0..45 -> DeviceOrientation.PORTRAIT
                    // 270 degrees +/- 45 degrees -> landscape left.
                    in 225..315 -> DeviceOrientation.LANDSCAPE_LEFT
                    // 90 degrees +/- 45 degrees -> landscape right.
                    in 45..135 -> DeviceOrientation.LANDSCAPE_RIGHT
                    // 180 degrees +/- 45 degrees -> reverse portrait.
                    in 135..225 -> DeviceOrientation.PORTRAIT_REVERSE
                    else -> return
                }

                // Update state only when the orientation actually changes.
                if (newOrientation != currentOrientation) {
                    currentOrientation = newOrientation
                }
            }
        }

        // Enable detection only on supported devices.
        if (listener.canDetectOrientation()) {
            listener.enable()
        }

        onDispose { listener.disable() }
    }

    return currentOrientation
}

// ========================================
// 2. Combined orientation + fullscreen handling
// ========================================

/**
 * Applies the correct activity orientation and system bar visibility based on
 * the current device orientation.
 *
 * Landscape mode:
 * 1. Forces the activity into landscape sensor mode.
 * 2. Hides the status bar and navigation bar.
 * 3. Allows transient system bars via swipe.
 *
 * Portrait mode:
 * 1. Forces the activity into portrait sensor mode.
 * 2. Shows the system bars.
 *
 * On disposal:
 * - Restores the activity to unspecified orientation.
 * - Shows the system bars again.
 */
@Composable
fun AdaptiveScreenMode(orientation: DeviceOrientation) {
    val context = LocalContext.current
    val isLandscape = orientation.isLandscape

    DisposableEffect(orientation) {
        val activity = context.findActivity()

        if (activity != null) {
            val insetsController =
                WindowInsetsControllerCompat(activity.window, activity.window.decorView)

            if (isLandscape) {
                // 1. Force the activity into landscape sensor mode.
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

                // 2. Hide the system bars for fullscreen playback.
                insetsController.apply {
                    hide(WindowInsetsCompat.Type.systemBars())

                    systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                // 1. Force the activity back into portrait sensor mode.
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

                // 2. Show the system bars again.
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }

        onDispose {
            // Restore automatic rotation and visible system bars.
            activity?.let { act ->
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

                WindowCompat.getInsetsController(act.window, act.window.decorView)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}

/**
 * Finds the hosting [Activity] from a [Context].
 */
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

/**
 * Forces the activity into portrait mode.
 *
 * Used when the user exits fullscreen manually and the screen should return
 * to the portrait layout immediately.
 */
fun Context.forcePortraitOrientation() {
    findActivity()?.let { activity ->
        val insetsController = WindowInsetsControllerCompat(activity.window, activity.window.decorView)

        // 1. Force the activity back into portrait sensor mode.
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        // 2. Show the system bars again.
        insetsController.show(WindowInsetsCompat.Type.systemBars())
    }
}

/**
 * Returns `true` when the orientation is landscape.
 */
val DeviceOrientation.isLandscape: Boolean
    get() = this == DeviceOrientation.LANDSCAPE_LEFT ||
            this == DeviceOrientation.LANDSCAPE_RIGHT

/**
 * Returns `true` when the orientation is portrait.
 */
val DeviceOrientation.isPortrait: Boolean
    get() = this == DeviceOrientation.PORTRAIT ||
            this == DeviceOrientation.PORTRAIT_REVERSE

/**
 * Physical device orientations used by the app.
 */
enum class DeviceOrientation {
    PORTRAIT,        // Upright portrait.
    LANDSCAPE_LEFT,  // Landscape rotated to the left.
    LANDSCAPE_RIGHT, // Landscape rotated to the right.
    PORTRAIT_REVERSE // Upside-down portrait.
}
