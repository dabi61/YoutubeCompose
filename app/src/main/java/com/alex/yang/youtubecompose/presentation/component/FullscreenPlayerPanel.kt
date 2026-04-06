package com.alex.yang.youtubecompose.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alex.yang.youtubecompose.R
import com.alex.yang.youtubecompose.domain.model.Video
import com.alex.yang.youtubecompose.domain.model.mockVideo
import com.alex.yang.youtubecompose.player.PlaybackState
import com.alex.yang.youtubecompose.player.PlayerController
import com.alex.yang.youtubecompose.ui.theme.AlexYoutubeComposeTheme

/**
 * Fullscreen overlay controls shown above the video player.
 */
@Composable
fun FullscreenPlayerPanel(
    video: Video,
    controller: PlayerController,
    onExitFullscreen: () -> Unit = {}
) {
    val playbackState by controller.playbackState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {

        // Background layer used only for dimming the player.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        // Top control area.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Video title.
            Text(
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                maxLines = 2,
                text = video.title
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Exit fullscreen button.
            IconButton(
                modifier = Modifier.background(Color.Black.copy(alpha = 0.2f), CircleShape),
                onClick = onExitFullscreen,
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        }

        // Center playback controls.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rewind 10 seconds.
            IconButton(
                modifier = Modifier.size(84.dp),
                onClick = { controller.seekBackward() }
            ) {
                Icon(
                    modifier = Modifier.size(84.dp),
                    painter = painterResource(R.drawable.ic_player_backward_10s),
                    contentDescription = null,
                    tint = Color(0XFFFBC92B),
                )
            }

            // Play/pause/replay button.
            IconButton(
                modifier = Modifier
                    .size(84.dp)
                    .background(Color(0XFFFBC92B), CircleShape),
                onClick = {
                    when (playbackState) {
                        PlaybackState.ENDED -> controller.replay()
                        PlaybackState.PLAYING -> controller.pause()
                        else -> controller.play()
                    }
                }
            ) {
                Icon(
                    modifier = Modifier.size(52.dp),
                    imageVector = when (playbackState) {
                        PlaybackState.ENDED -> Icons.Default.Replay
                        PlaybackState.PLAYING, PlaybackState.BUFFERING -> Icons.Default.Pause
                        else -> Icons.Default.PlayArrow
                    },
                    contentDescription = null,
                    tint = Color.Black
                )
            }

            // Forward 10 seconds.
            IconButton(
                modifier = Modifier.size(84.dp),
                onClick = { controller.seekForward() },
            ) {
                Icon(
                    modifier = Modifier.size(84.dp),
                    painter = painterResource(R.drawable.ic_player_forward_10s),
                    contentDescription = null,
                    tint = Color(0XFFFBC92B),
                )
            }
        }

        // Bottom progress and actions area.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(horizontal = 8.dp),
        ) {
            // Playback progress bar.
            PlayerSlider(controller = controller)

            // Bottom action buttons.
            Box(modifier = Modifier.fillMaxWidth()) {
//                Row(
//                    modifier = Modifier.align(Alignment.Center),
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // Favorite button.
//                    IconButton(
//                        onClick = {
//                            // TODO: Favorite action.
//                        }
//                    ) {
//                        Icon(
//                            modifier = Modifier.size(36.dp),
//                            painter = painterResource(R.drawable.ic_player_collect),
//                            contentDescription = null,
//                            tint = Color.White,
//                        )
//                    }
//
//                    // Share button.
//                    IconButton(
//                        onClick = {
//                            // TODO: Share action.
//                        }
//                    ) {
//                        Icon(
//                            modifier = Modifier.size(36.dp),
//                            painter = painterResource(R.drawable.ic_player_share),
//                            contentDescription = null,
//                            tint = Color.White,
//                        )
//                    }
//                }

                // Fullscreen toggle button.
                IconButton(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = onExitFullscreen,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_player_exit_fullscreen),
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = false,
    device = "spec:width=915dp,height=412dp,dpi=420,orientation=landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode",
)
@Composable
fun FullscreenPlayerPanelPreview() {
    AlexYoutubeComposeTheme {
        FullscreenPlayerPanel(
            video = mockVideo,
            controller = PlayerController()
        )
    }
}
