package com.alex.yang.youtubecompose.player

/**
 * Playback states exposed to the UI layer.
 */
enum class PlaybackState {
    /**
     * The player has not been initialized yet or has already been released.
     */
    IDLE,

    /**
     * The player is initialized and ready to accept playback commands.
     */
    READY,

    /**
     * The player is currently waiting for more media data.
     */
    BUFFERING,

    /**
     * The video is actively playing.
     */
    PLAYING,

    /**
     * Playback is paused by the user or by the player.
     */
    PAUSED,

    /**
     * Playback reached the end of the video.
     */
    ENDED
}
