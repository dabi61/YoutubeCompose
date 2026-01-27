package com.alex.yang.youtubecompose.player

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Created by AlexYang on 2026/1/26.
 *
 *
 */
class PlayerController : DefaultLifecycleObserver {
    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState = _playbackState.asStateFlow()

    /**
     * 是否正在播放
     *
     * 包括 PLAYING 和 BUFFERING 狀態
     * 因為緩衝時影片仍在「嘗試播放」
     */
    val isPlaying: Boolean
        get() = _playbackState.value in listOf(
            PlaybackState.PLAYING,
            PlaybackState.BUFFERING
        )

    /**
     * 當前播放時間（秒）
     */
    private val _currentSecond = MutableStateFlow(0f)
    val currentSecond = _currentSecond.asStateFlow()

    /**
     * 影片總時長（秒）
     */
    private val _duration = MutableStateFlow(0f)
    val duration = _duration.asStateFlow()

    /**
     * YouTube 播放器實例
     */
    var player: YouTubePlayer? = null


    /**
     * 初始化播放器
     *
     * @param youTubePlayer YouTube 播放器實例
     */
    fun initialize(youTubePlayer: YouTubePlayer) {
        player = youTubePlayer
        _playbackState.value = PlaybackState.READY
    }

    /**
     * 載入影片
     *
     * @param videoId YouTube 影片 ID
     * @param startTime 起始時間（秒）
     */
    fun loadVideo(videoId: String, startTime: Float = 0f) {
        _playbackState.value = PlaybackState.BUFFERING
        player?.loadVideo(videoId, startTime)
    }

    /**
     * 播放
     */
    fun play() {
        player?.play()
    }

    /**
     * 暫停
     */
    fun pause() {
        player?.pause()
    }

    /**
     * 重播（從頭開始）
     */
    fun replay() {
        seekTo(0f)
        play()
    }

    /**
     * 快退
     *
     * @param seconds 快退秒數（預設 10 秒）
     */
    fun seekBackward(seconds: Float = 10f) {
        seekTo(maxOf(0f, _currentSecond.value - seconds))
    }

    /**
     * 快進
     *
     * @param seconds 快進秒數（預設 10 秒）
     */
    fun seekForward(seconds: Float = 10f) {
        val maxTime = if (_duration.value > 0f) _duration.value else Float.MAX_VALUE
        seekTo(minOf(maxTime, _currentSecond.value + seconds))
    }

    /**
     * 跳轉到指定時間
     *
     * @param time 目標時間（秒）
     */
    fun seekTo(time: Float) {
        player?.seekTo(time)
        _currentSecond.value = time
    }

    // ========================================
    // 狀態更新（由 YouTube 回調）
    // ========================================

    /**
     * 更新當前時間
     */
    fun updateCurrentSecond(second: Float) {
        _currentSecond.value = second
    }

    /**
     * 更新總時長
     */
    fun updateDuration(duration: Float) {
        _duration.value = duration
    }

    /**
     * 更新播放狀態
     */
    fun updatePlaybackState(youtubeState: PlayerConstants.PlayerState) {
        _playbackState.value = when (youtubeState) {
            PlayerConstants.PlayerState.UNSTARTED -> PlaybackState.READY
            PlayerConstants.PlayerState.PLAYING -> PlaybackState.PLAYING
            PlayerConstants.PlayerState.PAUSED -> PlaybackState.PAUSED
            PlayerConstants.PlayerState.ENDED -> PlaybackState.ENDED
            PlayerConstants.PlayerState.BUFFERING -> PlaybackState.BUFFERING
            PlayerConstants.PlayerState.VIDEO_CUED -> PlaybackState.READY
            else -> PlaybackState.IDLE
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        player = null
        _playbackState.value = PlaybackState.IDLE
        _currentSecond.value = 0f
        _duration.value = 0f
    }
}