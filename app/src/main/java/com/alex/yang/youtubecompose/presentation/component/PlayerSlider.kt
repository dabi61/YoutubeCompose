package com.alex.yang.youtubecompose.presentation.component

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alex.yang.youtubecompose.player.PlayerController
import com.alex.yang.youtubecompose.ui.theme.AlexYoutubeComposeTheme

/**
 * Created by AlexYang on 2026/1/26.
 *
 *
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSlider(
    controller: PlayerController,
) {
    // ========== 訂閱播放狀態 ==========
    val currentSecond by controller.currentSecond.collectAsStateWithLifecycle()
    val duration by controller.duration.collectAsStateWithLifecycle()

    // ========== Seeking 狀態 ==========
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }

    // ========== 計算顯示值 ==========
    val displayValue = if (isSeeking) seekPosition else currentSecond
    val maxValue = if (duration > 0f) duration else 100f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // 進度條
        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color(0XFFFBC92B),
                activeTrackColor = Color(0XFFFBC92B),
                inactiveTrackColor = Color.Gray,
                disabledThumbColor = Color(0XFFFBC92B),
                disabledActiveTrackColor = Color(0XFFFBC92B),
                disabledInactiveTrackColor = Color.Gray
            ),
            // 自訂 Thumb（立體感）
            thumb = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .shadow(4.dp, CircleShape, clip = false)
                        .background(
                            color = Color(0XFFFBC92B),
                            shape = CircleShape
                        ),
                )
            },
            track = {
                val targetProgress = if (duration > 0f) displayValue / duration else 0f
                val animatedProgress by animateFloatAsState(
                    targetValue = targetProgress.coerceIn(0f, 1f),
                    animationSpec = tween(
                        durationMillis = 120,
                        easing = LinearEasing
                    ),
                    label = "slider_progress"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(color = Color.Gray, shape = CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(8.dp)
                            .background(color = Color(0XFFFBC92B), shape = CircleShape)
                    )
                }
            },
            valueRange = 0f..maxValue,
            value = if (isSeeking) seekPosition else currentSecond,
            onValueChange = {
                isSeeking = true
                seekPosition = it
            },
            onValueChangeFinished = {
                controller.seekTo(seekPosition)
                isSeeking = false
            },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // 當前時間
            Text(
                color = Color.White,
                text = formatSecond(displayValue)
            )

            // 總時長
            Text(
                color = Color.White,
                text = formatSecond(duration)
            )
        }
    }
}

/**
 * 格式化時間
 *
 * @param seconds 秒數
 * @return 格式化後的時間字符串（mm:ss）
 *
 * 範例：
 * - 0    → "00:00"
 * - 30   → "00:30"
 * - 90   → "01:30"
 * - 3665 → "61:05"
 */
private fun formatSecond(seconds: Float): String {
    val s = seconds.toInt().coerceAtLeast(0)
    val hours = s / 3600
    val minute = (s % 3600) / 60
    val second = s % 60

    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minute, second)
    } else {
        "%02d:%02d".format(minute, second)
    }
}

@Preview(
    showBackground = true,
    showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode"
)
@Preview(
    showBackground = true,
    showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
fun PlayerSliderPreview() {
    AlexYoutubeComposeTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
        ) {
            PlayerSlider(
                controller = PlayerController()
            )
        }
    }
}
