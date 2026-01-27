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

/**
 * Created by AlexYang on 2026/1/26.
 *
 *
 */
fun createYouTubePlayerView(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    controller: PlayerController,
    videoId: String,
    initSecond: Float = 0f
): YouTubePlayerView {
    Log.d(TAG, "🏗️ create YouTubePlayerView")
    Log.d(TAG, "videoId: $videoId, initSecond: ${initSecond}s")

    return YouTubePlayerView(context).apply {
        lifecycleOwner.lifecycle.addObserver(this)

        // 禁用自動初始化，手動控制
        enableAutomaticInitialization = false

        // 初始化 YouTubePlayer
        initialize(
            object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    Log.d(TAG, "✅ YouTubePlayer onReady!")

                    controller.initialize(youTubePlayer)
                    controller.loadVideo(videoId, initSecond)
                }

                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    controller.updateCurrentSecond(second)
                }

                override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                    controller.updateDuration(duration)
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
                    Log.e(TAG, "❌ 播放器錯誤: $error")
                }
            },
            false,
            IFramePlayerOptions.Builder(context)
                .controls(0)
                .rel(0)
                .ccLoadPolicy(0)
                .build()
        )
    }
}
