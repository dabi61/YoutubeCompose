package com.alex.yang.youtubecompose.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alex.yang.youtubecompose.R
import com.alex.yang.youtubecompose.player.PlaybackState
import com.alex.yang.youtubecompose.player.PlayerController
import com.alex.yang.youtubecompose.ui.theme.AlexYoutubeComposeTheme

/**
 * Playback controls shown below the player in portrait mode.
 */
@Composable
fun PlayerButtons(
    controller: PlayerController,
) {
    val playbackState by controller.playbackState.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rewind 10 seconds.
        IconButton(
            modifier = Modifier.size(64.dp),
            onClick = { controller.seekBackward() }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_player_backward_10s),
                contentDescription = null,
                tint = Color(0XFFFBC92B),
            )
        }

        // Large play/pause/replay button.
        IconButton(
            modifier = Modifier
                .size(72.dp)
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
                modifier = Modifier.size(40.dp),
                imageVector = when (playbackState) {
                    PlaybackState.ENDED -> Icons.Default.Replay
                    PlaybackState.PLAYING, PlaybackState.BUFFERING -> Icons.Default.Pause
                    else -> Icons.Default.PlayArrow
                },
                contentDescription = null,
                tint = Color.Black,
            )
        }

        // Forward 10 seconds.
        IconButton(
            modifier = Modifier.size(64.dp),
            onClick = { controller.seekForward() }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_player_forward_10s),
                contentDescription = null,
                tint = Color(0XFFFBC92B),
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
fun PlayerButtonsPreview() {
    AlexYoutubeComposeTheme {
        PlayerButtons(controller = PlayerController())
    }
}
