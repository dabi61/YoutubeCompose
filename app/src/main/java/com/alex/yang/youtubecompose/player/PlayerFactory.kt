package com.alex.yang.youtubecompose.player

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.alex.yang.youtubecompose.TAG
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

fun createYouTubePlayerView(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    controller: PlayerController,
    videoId: String,
    initSecond: Float = 0f
): YouTubePlayerView {
    Log.d(TAG, "create YouTubePlayerView")
    Log.d(TAG, "videoId: $videoId, initSecond: ${initSecond}s")

    return YouTubePlayerView(context).apply {
        lifecycleOwner.lifecycle.addObserver(this)
        enableAutomaticInitialization = false

        initialize(
            object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    Log.d(TAG, "YouTubePlayer onReady")
                    controller.initialize(youTubePlayer)
                    controller.loadVideo(videoId, initSecond)
                }

                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    controller.updateCurrentSecond(second)
                }

                override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                    controller.updateDuration(duration)
                }

                override fun onVideoLoadedFraction(
                    youTubePlayer: YouTubePlayer,
                    loadedFraction: Float
                ) {
                    controller.updateLoadedFraction(loadedFraction)
                }

                override fun onPlaybackQualityChange(
                    youTubePlayer: YouTubePlayer,
                    playbackQuality: PlayerConstants.PlaybackQuality
                ) {
                    controller.updatePlaybackQuality(playbackQuality)
                }

                override fun onStateChange(
                    youTubePlayer: YouTubePlayer,
                    state: PlayerConstants.PlayerState
                ) {
                    controller.updatePlaybackState(state)
                }

                override fun onError(
                    youTubePlayer: YouTubePlayer,
                    error: PlayerConstants.PlayerError
                ) {
                    Log.e(TAG, "Player error: $error")
                }
            },
            false,
            IFramePlayerOptions.Builder(context)
                .controls(0)
                .fullscreen(0)
                .ivLoadPolicy(3)
                .rel(0)
                .ccLoadPolicy(0)
                .build()
        )
    }
}
