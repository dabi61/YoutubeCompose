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
 * Created by AlexYang on 2026/1/26.
 *
 * Device Orientation 工具類
 */


// ========================================
// 1. 設備方向檢測
// ========================================

/**
 * 記住設備方向（使用 OrientationEventListener）
 *
 * @return 當前設備方向
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

                // 將角度轉換為方向
                val newOrientation = when (orientation) {
                    // 0° ± 45° → 直屏
                    in 315..360, in 0..45 -> DeviceOrientation.PORTRAIT
                    // 270° ± 45° → 橫屏（左）
                    in 225..315 -> DeviceOrientation.LANDSCAPE_LEFT
                    // 90° ± 45° → 橫屏（右）
                    in 45..135 -> DeviceOrientation.LANDSCAPE_RIGHT
                    // 180° ± 45° → 倒置
                    in 135..225 -> DeviceOrientation.PORTRAIT_REVERSE
                    else -> return
                }

                // 只在方向真正改變時才更新
                if (newOrientation != currentOrientation) {
                    currentOrientation = newOrientation
                }
            }
        }

        // 檢查設備是否支持方向檢測
        if (listener.canDetectOrientation()) {
            listener.enable()
        }

        onDispose { listener.disable() }
    }

    return currentOrientation
}

// ========================================
// 2. 整合：強制方向 + 全螢幕控制
// ========================================

/**
 * 自適應螢幕模式
 *
 * 根據設備物理方向自動調整 Activity 螢幕方向和系統欄顯示狀態。
 * 即使用戶鎖定了螢幕旋轉，依然能夠強制 Activity 進入指定方向。
 *
 * 功能說明：
 *
 * **橫屏時：**
 * 1. 強制 Activity 進入橫屏模式 ([ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE])
 * 2. 隱藏狀態欄和導航欄（全螢幕）
 * 3. 允許用戶從邊緣滑動暫時顯示系統欄
 *
 * **直屏時：**
 * 1. 強制 Activity 進入直屏模式 ([ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT])
 * 2. 顯示狀態欄和導航欄（正常模式）
 *
 * **退出時：**
 * - 自動恢復為未指定方向 ([ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED])
 * - 自動顯示系統欄
 */
@Composable
fun AdaptiveScreenMode(orientation: DeviceOrientation) {
    val context = LocalContext.current
    val isLandscape = orientation.isLandscape

    DisposableEffect(orientation) {
        val activity = context.findActivity()

        if (activity != null) {
            val insetsController = WindowInsetsControllerCompat(activity.window, activity.window.decorView)

            if (isLandscape) {
                // 1. 強制 Activity 進入橫屏（跟隨傳感器，允許左右橫屏）
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

                // 2. 隱藏系統欄（狀態欄 + 導航欄）
                insetsController.apply {
                    hide(WindowInsetsCompat.Type.systemBars())

                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                // 1. 強制 Activity 進入直屏（跟隨傳感器，允許正常和倒置）
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

                // 2. 顯示系統欄
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }

        onDispose {
            // 恢復為自動旋轉（由系統決定）
            activity?.let { act ->
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

                WindowCompat.getInsetsController(act.window, act.window.decorView)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}

/**
 * 從 Context 中找到 Activity
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
 * 強制進入直屏模式
 *
 * 用於手動退出橫屏全螢幕時，強制 Activity 回到直屏模式。
 * 通常在用戶點擊「退出全螢幕」按鈕時調用。
 *
 * 工作原理：
 * 1. 強制 Activity 進入直屏模式
 * 2. [autoFullscreenLandscape] 檢測到直屏
 * 3. 自動切換到 PortraitLayout
 *
 * 注意事項：
 * - 會觸發 Activity 的 Configuration Change
 * - 使用 [androidx.compose.runtime.saveable.rememberSaveable] 保存重要狀態
 */
fun Context.forcePortraitOrientation() {
    findActivity()?.let { activity ->
        val insetsController = WindowInsetsControllerCompat(activity.window, activity.window.decorView)

        // 1. 強制 Activity 進入直屏（跟隨傳感器，允許正常和倒置）
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        // 2. 顯示系統欄
        insetsController.show(WindowInsetsCompat.Type.systemBars())
    }
}

/**
 * 判斷是否為橫屏
 */
val DeviceOrientation.isLandscape: Boolean
    get() = this == DeviceOrientation.LANDSCAPE_LEFT ||
            this == DeviceOrientation.LANDSCAPE_RIGHT

/**
 * 判斷是否為直屏
 */
val DeviceOrientation.isPortrait: Boolean
    get() = this == DeviceOrientation.PORTRAIT ||
            this == DeviceOrientation.PORTRAIT_REVERSE

/**
 * 設備方向
 */
enum class DeviceOrientation {
    PORTRAIT,        // 直屏
    LANDSCAPE_LEFT,  // 橫屏（左）
    LANDSCAPE_RIGHT, // 橫屏（右）
    PORTRAIT_REVERSE // 倒置
}