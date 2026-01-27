package com.alex.yang.youtubecompose.player

/**
 * Created by AlexYang on 2026/1/26.
 *
 * 播放狀態枚舉
 *
 * 定義播放器可能處於的所有狀態
 */
enum class PlaybackState {
    /**
     * 閒置狀態
     * - 播放器尚未初始化
     * - 或已經被釋放
     */
    IDLE,

    /**
     * 準備就緒
     * - 播放器已初始化
     * - 可以開始播放
     */
    READY,

    /**
     * 緩衝中
     * - 正在載入影片數據
     * - 播放器可能正在播放，但在等待數據
     */
    BUFFERING,

    /**
     * 播放中
     * - 影片正在播放
     * - 這是主要的播放狀態
     */
    PLAYING,

    /**
     * 暫停
     * - 用戶主動暫停
     * - 或播放器因其他原因暫停
     */
    PAUSED,

    /**
     * 結束
     * - 影片播放完畢
     * - 播放進度到達結尾
     */
    ENDED
}