package com.alex.yang.youtubecompose.domain.model

/**
 * Model representing a playable YouTube video.
 */
data class Video(
    val videoId: String,
    val title: String,
    val description: String,
    val thumbnail: String,
    val landscapeContentAspectRatio: Float = 16f / 9f,
)

val mockVideo = Video(
    videoId = "TK5jHU-slo0",
    title = "",
    description = "",
    thumbnail = "https://i.ytimg.com/vi/TK5jHU-slo0/hqdefault.jpg",
    landscapeContentAspectRatio = 21f / 9f,
)
