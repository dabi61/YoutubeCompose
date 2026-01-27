package com.alex.yang.youtubecompose.domain.model

/**
 * Created by AlexYang on 2026/1/26.
 *
 *
 */
data class Video(
    val videoId: String,
    val title: String,
    val description: String,
    val thumbnail: String,
)

val mockVideo = Video(
    videoId = "cTQMmU-UVfI",
    title = "冬季風暴橫掃全美17州與華府釀百萬戶停電1.7萬架航班取消",
    description = "大規模強烈冬季風暴近日橫掃美國，重創電力、交通系統，超過100萬戶家庭停電，逾1.7萬架次航班被取消，華府及17個州進入緊急狀態。\\nA massive and intense winter storm recently swept across the United States, severely impacting power and transportation systems. Over 1 million households experienced power outages, more than 17,000 flights were canceled, and Washington D.C. along with 17 states entered a state of emergency. (此翻譯來源：ChatGPT)\\n\\n# Detailed Report\\n詳細報導:https://www.nownews.com/news/6779736\\n\\n─【NOWNEWS今日新聞】─\\n\uD83D\uDD36訂閱〈NOW娛樂 YT〉▶\uFE0F 爆紅影音輕鬆滑 https://lihi.cc/xoTgU\\n\uD83D\uDD36觀看〈NOWNEWS〉官方網站多元新聞秒接軌 https://www.nownews.com/\\n\uD83D\uDD36立刻下載(更新)「NOWNEWS APP」！精彩內容、超值好康等你來拿！\\nIOS\uD83D\uDC49 https://reurl.cc/xEWa3z \\nAndroid\uD83D\uDC49 https://reurl.cc/AR9p5Z\\n\\n最值得信賴的網路新聞媒體：NOWNEWS今日新聞──真實、公正、多元的AI時代資訊守護者。根據comScore數據，2025年1月NOWNEWS今日新聞在「新聞資訊類」網站訪客數排名全台第5，深受使用者肯定。\\n\\n在人工智慧資訊爆炸的時代，您需要一個具公信力的新聞來源。我們致力於成為最值得信賴的網路新聞平台，以真實報導、專業內容及多元觀點為核心，提供準確、可靠且具深度的新聞資訊。\\n我們堅持透明揭露資訊來源，並積極回應讀者需求，持續創新以守護新聞公信力。我們不僅是您的最佳資訊來源，更是您AI時代的智慧夥伴。\\n\\n冬季風暴襲美釀全國範圍大停電與航班取消\\n00:03 強烈冬季風暴襲美暴雨雪\\n00:07 超過百萬戶斷電東部受創\\n00:15 紐約芝加哥等地停課閉館\\n00:19 週日取消航班數達五年新高\\n00:22 總計1.7萬航班取消\\n00:27 川普稱風暴歷史性事件\\n00:30 多州進入聯邦緊急狀態\\n00:34 哥倫比亞特區等十七州進入緊急狀態\\n00:38 各州啟動災害應變機制",
    thumbnail = "https://i.ytimg.com/vi/cTQMmU-UVfI/hqdefault.jpg",
)
