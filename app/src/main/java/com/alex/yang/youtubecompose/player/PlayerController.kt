package com.alex.yang.youtubecompose.player

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.alex.yang.youtubecompose.TAG
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Keeps player state stable for Compose and adds a small preload gate before
 * autoplay starts. This reduces "play immediately then buffer again" behavior.
 */
class PlayerController(
    private val preloadConfig: PlaybackPreloadConfig = PlaybackPreloadConfig()
) : DefaultLifecycleObserver {

    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var preloadTimeoutJob: Job? = null
    private var isWaitingForInitialBuffer = false
    private var shouldAutoResume = false
    private var lastLoadStartedAtMs = 0L

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState = _playbackState.asStateFlow()

    val isPlaying: Boolean
        get() = _playbackState.value in listOf(
            PlaybackState.PLAYING,
            PlaybackState.BUFFERING
        )

    private val _currentSecond = MutableStateFlow(0f)
    val currentSecond = _currentSecond.asStateFlow()

    private val _duration = MutableStateFlow(0f)
    val duration = _duration.asStateFlow()

    private val _loadedFraction = MutableStateFlow(0f)
    val loadedFraction = _loadedFraction.asStateFlow()

    private val _playbackQuality =
        MutableStateFlow(PlayerConstants.PlaybackQuality.UNKNOWN)
    val playbackQuality = _playbackQuality.asStateFlow()

    var player: YouTubePlayer? = null

    fun initialize(youTubePlayer: YouTubePlayer) {
        player = youTubePlayer
        _playbackState.value = PlaybackState.READY
    }

    fun loadVideo(videoId: String, startTime: Float = 0f) {
        resetPlaybackMetrics(startTime)
        isWaitingForInitialBuffer = true
        shouldAutoResume = true
        lastLoadStartedAtMs = SystemClock.elapsedRealtime()
        _playbackState.value = PlaybackState.BUFFERING
        schedulePreloadTimeout()
        player?.cueVideo(videoId, startTime)
    }

    fun play() {
        shouldAutoResume = true
        if (isWaitingForInitialBuffer) {
            _playbackState.value = PlaybackState.BUFFERING
            tryStartPlayback()
            return
        }
        player?.play()
    }

    fun pause() {
        shouldAutoResume = false
        if (isWaitingForInitialBuffer) {
            cancelPreloadGate(promoteToReady = true)
            return
        }
        player?.pause()
    }

    fun replay() {
        shouldAutoResume = true
        seekTo(0f)
        play()
    }

    fun seekBackward(seconds: Float = 10f) {
        seekTo(maxOf(0f, _currentSecond.value - seconds))
    }

    fun seekForward(seconds: Float = 10f) {
        val maxTime = if (_duration.value > 0f) _duration.value else Float.MAX_VALUE
        seekTo(minOf(maxTime, _currentSecond.value + seconds))
    }

    fun seekTo(time: Float) {
        player?.seekTo(time)
        _currentSecond.value = time
    }

    fun updateCurrentSecond(second: Float) {
        _currentSecond.value = second
    }

    fun updateDuration(duration: Float) {
        _duration.value = duration
        tryStartPlayback()
    }

    fun updateLoadedFraction(loadedFraction: Float) {
        _loadedFraction.value = loadedFraction.coerceIn(0f, 1f)
        tryStartPlayback()
    }

    fun updatePlaybackQuality(playbackQuality: PlayerConstants.PlaybackQuality) {
        if (_playbackQuality.value == playbackQuality) return

        Log.d(
            TAG,
            "Playback quality changed: ${_playbackQuality.value} -> $playbackQuality"
        )
        _playbackQuality.value = playbackQuality
    }

    fun updatePlaybackState(youtubeState: PlayerConstants.PlayerState) {
        _playbackState.value = when (youtubeState) {
            PlayerConstants.PlayerState.UNSTARTED -> preloadAwareReadyState()
            PlayerConstants.PlayerState.PLAYING -> {
                cancelPreloadGate(promoteToReady = false)
                PlaybackState.PLAYING
            }
            PlayerConstants.PlayerState.PAUSED -> {
                if (isWaitingForInitialBuffer && shouldAutoResume) {
                    PlaybackState.BUFFERING
                } else {
                    PlaybackState.PAUSED
                }
            }
            PlayerConstants.PlayerState.ENDED -> {
                cancelPreloadGate(promoteToReady = false)
                PlaybackState.ENDED
            }
            PlayerConstants.PlayerState.BUFFERING -> PlaybackState.BUFFERING
            PlayerConstants.PlayerState.VIDEO_CUED -> {
                tryStartPlayback()
                preloadAwareReadyState()
            }
            else -> PlaybackState.IDLE
        }
    }

    private fun preloadAwareReadyState(): PlaybackState {
        return if (isWaitingForInitialBuffer && shouldAutoResume) {
            PlaybackState.BUFFERING
        } else {
            PlaybackState.READY
        }
    }

    private fun tryStartPlayback(force: Boolean = false) {
        if (!isWaitingForInitialBuffer || !shouldAutoResume) return

        val timedOut = force || hasExceededPreloadWait()
        if (!timedOut && !hasEnoughBufferedData()) return

        cancelPreloadTimeout()
        isWaitingForInitialBuffer = false
        player?.play()
    }

    private fun hasEnoughBufferedData(): Boolean {
        val duration = _duration.value
        val loadedFraction = _loadedFraction.value

        if (duration <= 0f) {
            return loadedFraction >= preloadConfig.minBufferedFraction
        }

        val requiredBufferedSeconds = minOf(duration, preloadConfig.minBufferedSeconds)
        val estimatedBufferedSeconds = duration * loadedFraction
        return estimatedBufferedSeconds >= requiredBufferedSeconds
    }

    private fun hasExceededPreloadWait(): Boolean {
        return SystemClock.elapsedRealtime() - lastLoadStartedAtMs >= preloadConfig.maxPreloadWaitMs
    }

    private fun schedulePreloadTimeout() {
        cancelPreloadTimeout()
        preloadTimeoutJob = controllerScope.launch {
            delay(preloadConfig.maxPreloadWaitMs)
            tryStartPlayback(force = true)
        }
    }

    private fun cancelPreloadTimeout() {
        preloadTimeoutJob?.cancel()
        preloadTimeoutJob = null
    }

    private fun cancelPreloadGate(promoteToReady: Boolean) {
        cancelPreloadTimeout()
        isWaitingForInitialBuffer = false
        if (promoteToReady) {
            _playbackState.value = PlaybackState.READY
        }
    }

    private fun resetPlaybackMetrics(startTime: Float) {
        cancelPreloadTimeout()
        _currentSecond.value = startTime
        _duration.value = 0f
        _loadedFraction.value = 0f
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        cancelPreloadTimeout()
        controllerScope.cancel()
        isWaitingForInitialBuffer = false
        shouldAutoResume = false
        player = null
        _playbackState.value = PlaybackState.IDLE
        _currentSecond.value = 0f
        _duration.value = 0f
        _loadedFraction.value = 0f
        _playbackQuality.value = PlayerConstants.PlaybackQuality.UNKNOWN
    }
}
