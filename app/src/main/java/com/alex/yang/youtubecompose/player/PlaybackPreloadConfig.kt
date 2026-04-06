package com.alex.yang.youtubecompose.player

/**
 * Startup preload gate for YouTube IFrame playback.
 *
 * YouTube's IFrame player does not expose a real "buffer N seconds ahead" API,
 * so these values are used as a heuristic before autoplay begins.
 */
data class PlaybackPreloadConfig(
    val minBufferedSeconds: Float = 8f,
    val minBufferedFraction: Float = 0.03f,
    val maxPreloadWaitMs: Long = 2_500L,
)
