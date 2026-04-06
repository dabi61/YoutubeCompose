package com.alex.yang.youtubecompose.presentation

import android.content.res.Configuration
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.ui.input.pointer.pointerInteropFilter
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

private fun Modifier.consumePlayerTouches(onTap: (() -> Unit)? = null): Modifier =
    pointerInteropFilter { event ->
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            onTap?.invoke()
        }
        true
    }

/**
 * Main video playback screen.
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

    // Keep a single controller and player instance for this screen.
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

    // Observe playback state from the controller.
    val playbackState by controller.playbackState.collectAsStateWithLifecycle()

    // Register the controller with the lifecycle.
    DisposableEffect(lifecycle) {
        lifecycle.lifecycle.addObserver(controller)
        onDispose { lifecycle.lifecycle.removeObserver(controller) }
    }

    // Track the physical device orientation.
    val deviceOrientation = rememberDeviceOrientation()
    val isLandscape = deviceOrientation.isLandscape
    var blockLandscapeOnce by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isLandscape) {
        if (!isLandscape && blockLandscapeOnce) blockLandscapeOnce = false
    }

    // Switch between portrait and landscape layouts.
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
 * Portrait screen layout.
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
        // Player area.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        ) {
            // Embedded YouTube player view.
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { playerView }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .consumePlayerTouches()
            )

            // Loading indicator.
            if (playbackState == PlaybackState.BUFFERING) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFFFA500)
                )
            }
        }

        // Video title.
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

        // Video description.
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

        // Playback progress.
        PlayerSlider(controller = controller)

        PlayerButtons(controller = controller)
    }
}

/**
 * Landscape fullscreen layout.
 *
 * Behavior:
 * - Tap to show or hide the controls.
 * - Auto-hide the controls after a short delay.
 * - Use fade animations for the overlay.
 */
@Composable
private fun LandscapeLayout(
    video: Video,
    playerView: YouTubePlayerView,
    controller: PlayerController,
    playbackState: PlaybackState,
    onExitFullscreen: () -> Unit = {}
) {
    // Whether the overlay controls are currently visible.
    var showControls by remember { mutableStateOf(true) }

    // Auto-hide the controls after a short delay.
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
        // Embedded YouTube player view.
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { playerView },
            update = { view ->
                // Force fullscreen layout params.
                view.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )

                // Request another layout pass from the parent to avoid stale sizing.
                (view.parent as? View)?.requestLayout()
            }
        )

        // Loading indicator.
        if (playbackState == PlaybackState.BUFFERING) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFFFA500)
            )
        }

        // Touch layer used to toggle the controls.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .consumePlayerTouches { showControls = !showControls }
        )

        // Top-most control panel.
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
