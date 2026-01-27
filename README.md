# Android YouTube Player Compose

## 專案簡介

本專案是一個 **Android YouTube 播放器應用**，整合 **PierfrancescoSoffritti YouTube Player Library**，完整展示：

> **Android + Jetpack Compose + MVVM Architecture + Clean Code**  
> **以 StateFlow 驅動 UI 的橫豎屏自適應影片播放器實作方式**

---

## Demo

| Screenshot |
|---------|------------|
| ![](docs/demo_01.png) |
| ![](docs/demo_02.png) |

### 核心功能
* ✅ YouTube 影片播放（基於 PierfrancescoSoffritti）
* ✅ 橫豎屏自動切換與全屏模式
* ✅ 播放狀態管理（播放/暫停/緩衝/結束）
* ✅ 進度條拖曳與時間顯示
* ✅ 快進/快退 10 秒功能
* ✅ 控制面板自動隱藏機制
* ✅ 完整的生命週期管理

---

## 專案特色

* ✅ 單一 PlayerController 管理所有播放狀態
* ✅ 使用 StateFlow 實現響應式 UI 更新
* ✅ 支援橫屏自動進入全屏模式
* ✅ 自定義播放器 UI（Material 3 風格）
* ✅ 流暢的動畫效果（進度條、控制面板）
* ✅ 完整的錯誤處理機制
* ✅ 物理方向檢測（OrientationEventListener）
* ✅ 強制方向切換（不受系統鎖定影響）

---

## 使用技術

### 核心技術棧
* Kotlin
* Jetpack Compose（Material 3）
* Hilt Dependency Injection
* Coroutine / Flow / StateFlow
* Navigation Compose
* Kotlin Serialization

### 播放器相關
* PierfrancescoSoffritti YouTube Player 13.0.0
* AndroidView（Compose Interop）
* Lifecycle-aware Components

### UI/UX 技術
* AnimatedVisibility (Fade In/Out)
* Custom Slider with Shadow Effect
* Gesture Detection (Tap to show/hide controls)
* Adaptive Screen Mode (Portrait/Landscape)

```kotlin
// --- YouTube Player ---
implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:13.0.0")

```

---

## 專案架構

```text
com.alex.yang.youtubecompose
│
├── player
│   ├── PlaybackState.kt              # 播放狀態枚舉
│   ├── PlayerController.kt           # 播放器控制器
│   └── PlayerFactory.kt              # YouTubePlayerView 工廠
│
├── presentation
│   ├── component
│   │   ├── FullscreenPlayerPanel.kt  # 全屏控制面板
│   │   ├── PlayerSlider.kt           # 播放進度條
│   │   └── PlayerButtons.kt          # 播放控制按鈕
│   │
│   ├── VideoScreen.kt                # 影片播放主畫面
│   └── VideoViewModel.kt             # 影片 ViewModel
│
├── core
│   └── orientation
│       └── DeviceUtils.kt            # 設備方向檢測工具
│
├── domain
│   └── model
│       └── Video.kt                  # 影片數據模型
│
├── features_compose
│   └── home
│       └── presentation
│           └── HomeScreen.kt         # 新聞主頁（分類列表）
│
├── ui
│   └── theme
│       └── AlexYoutubeComposeTheme.kt
│
└── MainActivity.kt
```

---

## 架構設計原則

### 1. 單一職責原則
* **PlayerController** - 專注於播放器狀態管理
* **VideoScreen** - 負責 UI 佈局與方向檢測
* **FullscreenPlayerPanel** - 處理全屏模式的交互
* **DeviceUtils** - 封裝所有方向檢測邏輯

### 2. 響應式編程
* 使用 **StateFlow** 管理所有可觀察狀態
* UI 透過 `collectAsStateWithLifecycle()` 自動更新
* 避免手動 UI 更新，減少狀態不一致

### 3. 生命週期感知
* PlayerController 實現 `DefaultLifecycleObserver`
* 自動處理 Activity/Fragment 生命週期
* 防止內存洩漏（onDestroy 時清理資源）

### 4. 關注點分離
* UI 不直接操作 YouTubePlayer
* 所有播放操作透過 PlayerController
* 方向檢測與 UI 邏輯完全分離

---

## 核心流程說明

### 1. 播放器初始化流程

```kotlin
VideoScreen(video, initSecond) 
    ↓
remember { PlayerController() }  // 創建控制器
    ↓
remember { createYouTubePlayerView(...) }  // 創建播放器
    ↓
YouTubePlayer.onReady()
    ↓
controller.initialize(youTubePlayer)  // 綁定播放器
    ↓
controller.loadVideo(videoId, initSecond)  // 載入影片
```

### 2. 播放狀態更新流程

```kotlin
YouTubePlayer.onStateChange(state)
    ↓
controller.updatePlaybackState(state)
    ↓
_playbackState.value = PlaybackState.PLAYING  // 更新 StateFlow
    ↓
UI collectAsStateWithLifecycle()  // Compose 自動重組
    ↓
顯示對應的播放/暫停圖示
```

### 3. 橫豎屏切換流程

```kotlin
rememberDeviceOrientation()  // 監聽物理方向
    ↓
OrientationEventListener.onOrientationChanged(orientation)
    ↓
判斷角度 → DeviceOrientation.LANDSCAPE_LEFT/RIGHT
    ↓
isLandscape = true
    ↓
顯示 LandscapeLayout（全屏模式）
    ↓
AdaptiveScreenMode(orientation)  // 調整系統欄
    ↓
強制 Activity 進入橫屏 + 隱藏系統欄
```

### 4. 進度條拖曳流程

```kotlin
Slider.onValueChange { newPosition }
    ↓
isSeeking = true  // 標記拖曳中
seekPosition = newPosition  // 更新臨時位置
    ↓
Slider.onValueChangeFinished
    ↓
controller.seekTo(seekPosition)  // 執行跳轉
    ↓
player.seekTo(time)  // YouTube API 調用
    ↓
isSeeking = false  // 結束拖曳
```

---

## 播放器狀態管理

### PlaybackState 枚舉

```kotlin
enum class PlaybackState {
    IDLE,        // 閒置（未初始化/已釋放）
    READY,       // 準備就緒（可開始播放）
    BUFFERING,   // 緩衝中（載入數據）
    PLAYING,     // 播放中
    PAUSED,      // 暫停
    ENDED        // 播放結束
}
```

### PlayerController 核心方法

| 方法 | 說明 |
|-----|------|
| `initialize(youTubePlayer)` | 初始化播放器實例 |
| `loadVideo(videoId, startTime)` | 載入指定影片 |
| `play()` | 開始播放 |
| `pause()` | 暫停播放 |
| `replay()` | 重新播放（從頭開始）|
| `seekTo(time)` | 跳轉到指定時間 |
| `seekBackward(seconds)` | 快退（預設 10 秒）|
| `seekForward(seconds)` | 快進（預設 10 秒）|
| `updateCurrentSecond(second)` | 更新當前時間（回調）|
| `updateDuration(duration)` | 更新總時長（回調）|
| `updatePlaybackState(state)` | 更新播放狀態（回調）|

---

## 方向檢測與全屏控制

### 1. 物理方向檢測

使用 `OrientationEventListener` 監聽設備角度：

```kotlin
0° ± 45°    → PORTRAIT          (直屏)
90° ± 45°   → LANDSCAPE_RIGHT   (橫屏右)
180° ± 45°  → PORTRAIT_REVERSE  (倒置)
270° ± 45°  → LANDSCAPE_LEFT    (橫屏左)
```

### 2. 自適應螢幕模式

```kotlin
AdaptiveScreenMode(orientation)
```

**橫屏時：**
1. 強制 Activity 進入橫屏模式
2. 隱藏狀態欄和導航欄（全屏）
3. 允許邊緣滑動暫時顯示系統欄

**直屏時：**
1. 強制 Activity 進入直屏模式
2. 顯示狀態欄和導航欄（正常）

### 3. 手動退出全屏

```kotlin
context.forcePortraitOrientation()
```

用於點擊「退出全屏」按鈕時強制回到直屏。

---

## YouTube Player 配置

### IFramePlayerOptions

```kotlin
IFramePlayerOptions.Builder(context)
    .controls(0)       // 隱藏原生控制
    .rel(0)            // 播放結束不顯示相關影片
    .ccLoadPolicy(0)   // 不自動載入字幕
    .build()
```

### 回調監聽

```kotlin
AbstractYouTubePlayerListener()
    .onReady()              // 播放器準備完成
    .onCurrentSecond()      // 每秒更新當前時間
    .onVideoDuration()      // 取得影片總時長
    .onStateChange()        // 播放狀態變化
    .onError()              // 播放錯誤
```

---

## 未來擴充方向

### 功能擴充
* 🔹 播放速度調整（0.25x ~ 2x）
* 🔹 畫質選擇（Auto / 720p / 1080p）
* 🔹 字幕開關與語言選擇
* 🔹 影片收藏功能
* 🔹 投放到電視（Cast）

### 技術優化
* 🔹 預載入下一部影片
* 🔹 播放器實例池管理
* 🔹 網路狀態監測（自動調整畫質）
* 🔹 錯誤重試策略

### UI/UX 改進
* 🔹 手勢控制（滑動調整音量/亮度）
* 🔹 雙擊快進/快退
* 🔹 長按倍速播放

---

## 授權與引用

### YouTube Player Library
本專案使用 [PierfrancescoSoffritti/android-youtube-player](https://github.com/PierfrancescoSoffritti/android-youtube-player)

許可證：MIT License

### YouTube API 使用限制
- 必須遵守 [YouTube Terms of Service](https://www.youtube.com/t/terms)
- 不得下載影片
- 不得移除廣告
- 需保留 YouTube 品牌標識

---

## Author

**Alex Yang**  
Senior Android Engineer  
GitHub: https://github.com/m9939418

---

## ⭐ 如果這個專案對你有幫助，歡迎給個 Star

