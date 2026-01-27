package com.alex.yang.youtubecompose.presentation

import android.content.res.Configuration
import android.view.View
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alex.yang.youtubecompose.core.orientation.AdaptiveScreenMode
import com.alex.yang.youtubecompose.core.orientation.forcePortraitOrientation
import com.alex.yang.youtubecompose.core.orientation.isLandscape
import com.alex.yang.youtubecompose.core.orientation.rememberDeviceOrientation
import com.alex.yang.youtubecompose.domain.model.Video
import com.alex.yang.youtubecompose.domain.model.mockVideo
import com.alex.yang.youtubecompose.player.PlaybackState
import com.alex.yang.youtubecompose.player.PlayerController
import com.alex.yang.youtubecompose.player.createYouTubePlayerView
import com.alex.yang.youtubecompose.presentation.component.FullscreenPlayerPanel
import com.alex.yang.youtubecompose.presentation.component.PlayerButtons
import com.alex.yang.youtubecompose.presentation.component.PlayerSlider
import com.alex.yang.youtubecompose.ui.theme.AlexYoutubeComposeTheme
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.delay

/**
 * Created by AlexYang on 2026/1/26.
 *
 *
 */
@Composable
fun VideoScreen(
    modifier: Modifier = Modifier,
    video: Video = mockVideo,
    initSecond: Float = 0f,
    onEvent: (VideoViewModel.UiEvent) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current

    // ========== 新增唯一的控制器和播放器 ==========
    val controller = remember { PlayerController() }
    val playView = remember {
        createYouTubePlayerView(
            context = context,
            lifecycleOwner = lifecycle,
            controller = controller,
            videoId = video.videoId,
            initSecond = initSecond,
        )
    }

    // ========== 訂閱狀態 ==========
    val playbackState by controller.playbackState.collectAsStateWithLifecycle()

    // ========== 生命週期管理 ==========
    DisposableEffect(lifecycle) {
        lifecycle.lifecycle.addObserver(controller)
        onDispose { lifecycle.lifecycle.removeObserver(controller) }
    }

    // ========== 檢測螢幕方向 ==========
    val deviceOrientation = rememberDeviceOrientation()
    val isLandscape = deviceOrientation.isLandscape
    var blockLandscapeOnce by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isLandscape) {
        if (!isLandscape && blockLandscapeOnce) blockLandscapeOnce = false
    }

    // ========== 根據方向顯示不同佈局 ==========
    if (isLandscape && !blockLandscapeOnce) {
        LandscapeLayout(
            video = video,
            playerView = playView,
            controller = controller,
            playbackState = playbackState,
            onExitFullscreen = {
                context.forcePortraitOrientation()
                blockLandscapeOnce = true
            }
        )
    } else {
        PortraitLayout(
            video = video,
            playerView = playView,
            controller = controller,
            playbackState = playbackState
        )
    }

    AdaptiveScreenMode(deviceOrientation)
}

/**
 * 直屏佈局
 */
@Composable
private fun PortraitLayout(
    video: Video,
    playerView: YouTubePlayerView,
    controller: PlayerController,
    playbackState: PlaybackState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 播放器區域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        ) {
            // Youtube Player
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { playerView }
            )

            // Loading
            if (playbackState == PlaybackState.BUFFERING) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFFFA500)
                )
            }
        }

        // 標題
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            text = video.title
        )

        // 描述
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            text = video.description
        )

        // 播放進度條
        PlayerSlider(controller = controller)

        PlayerButtons(controller = controller)
    }
}

/**
 * 橫屏佈局（全螢幕）
 *
 * 特點：
 * - 點擊顯示/隱藏控制面板
 * - 3 秒後自動隱藏
 * - 淡入淡出動畫
 */
@Composable
private fun LandscapeLayout(
    video: Video,
    playerView: YouTubePlayerView,
    controller: PlayerController,
    playbackState: PlaybackState,
    onExitFullscreen: () -> Unit = {}
) {
    // ========== 控制面板顯示狀態 ==========
    var showControls by remember { mutableStateOf(true) }

    // ========== 自動隱藏（3 秒後）==========
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(2500L)
            showControls = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Youtube Player
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { playerView },
            update = { view ->
                // 強制 Fullscreen LayoutParams
                view.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )

                // 通知 parent 重新 layout（避免一半機率不生效）
                (view.parent as? View)?.requestLayout()
            }
        )

        // ========== Loading 指示器 ==========
        if (playbackState == PlaybackState.BUFFERING) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFFFA500)
            )
        }

        // ========== 點擊檢測層（中層）==========
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { showControls = !showControls })
                }
        )

        // ========== 控制面板（頂層）==========
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            FullscreenPlayerPanel(
                video = video,
                controller = controller,
                onExitFullscreen = onExitFullscreen
            )
        }
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
fun VideoScreenPreview() {
    AlexYoutubeComposeTheme {
        VideoScreen(
            video = mockVideo
        )
    }
}