# Android YouTube Player Compose

## Docs

- Architecture and flow: [docs/youtube-player-architecture.vi.md](docs/youtube-player-architecture.vi.md)
- Smooth startup preload gate: see section `6.3 Smoother Startup Preload` in the architecture doc

## Project Overview

This project is an **Android YouTube playback app** built with the **PierfrancescoSoffritti YouTube Player Library**. It demonstrates:

> **Android + Jetpack Compose + MVVM + Clean Code**  
> **Adaptive video playback between portrait and landscape layouts with UI driven by StateFlow**

---

## Demo

| Screenshot |
|---------|------------|
| ![](docs/demo_01.png) |
| ![](docs/demo_02.png) |

### Core Features

- Play YouTube videos with the PierfrancescoSoffritti player library
- Automatically switch between portrait, landscape, and fullscreen playback
- Manage playback state: play, pause, buffering, ended
- Show playback progress and current time
- Seek backward and forward by 10 seconds
- Auto-hide the control overlay
- Handle lifecycle cleanup correctly
- Use a startup preload gate to reduce immediate rebuffering when a video first starts

---

## Highlights

- A single `PlayerController` manages all playback state
- `StateFlow` keeps the UI reactive and synchronized
- Fullscreen is triggered automatically when the device rotates to landscape
- The player UI is fully customized with Material 3
- Playback startup is smoother thanks to a small preload gate
- Errors and lifecycle transitions are handled explicitly
- Physical orientation is detected through `OrientationEventListener`
- Screen orientation can still be forced even if system auto-rotate is locked

---

## Tech Stack

### Core

- Kotlin
- Jetpack Compose (Material 3)
- Hilt Dependency Injection
- Coroutines / Flow / StateFlow
- Navigation Compose
- Kotlin Serialization

### Player

- PierfrancescoSoffritti YouTube Player `13.0.0`
- `AndroidView` for Compose interop
- Lifecycle-aware components

### UI / UX

- `AnimatedVisibility` with fade in / fade out
- Custom slider with shadow styling
- Gesture-based show/hide controls
- Adaptive portrait / landscape screen mode

```kotlin
// --- YouTube Player ---
implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:13.0.0")
```

---

## Project Structure

```text
com.alex.yang.youtubecompose
|
|-- player
|   |-- PlaybackState.kt              # Playback state enum
|   |-- PlaybackPreloadConfig.kt      # Startup preload tuning
|   |-- PlayerController.kt           # Player state controller
|   `-- PlayerFactory.kt              # YouTubePlayerView factory
|
|-- presentation
|   |-- component
|   |   |-- FullscreenPlayerPanel.kt  # Fullscreen control overlay
|   |   |-- PlayerSlider.kt           # Playback progress slider
|   |   `-- PlayerButtons.kt          # Playback control buttons
|   |
|   |-- VideoScreen.kt                # Main video screen
|   `-- VideoViewModel.kt             # Video screen ViewModel
|
|-- core
|   `-- orientation
|       `-- DeviceUtils.kt            # Device orientation utilities
|
|-- domain
|   `-- model
|       `-- Video.kt                  # Video data model
|
|-- ui
|   `-- theme
|       `-- AlexYoutubeComposeTheme.kt
|
`-- MainActivity.kt
```

---

## Architecture Principles

### 1. Single Responsibility

- **PlayerController** only manages player state and commands
- **VideoScreen** handles layout composition and orientation-aware rendering
- **FullscreenPlayerPanel** handles interactions in fullscreen mode
- **DeviceUtils** encapsulates physical orientation detection and screen forcing

### 2. Reactive UI

- `StateFlow` holds the observable playback state
- Compose UI updates automatically with `collectAsStateWithLifecycle()`
- The UI never reads directly from the player internals

### 3. Lifecycle Awareness

- `PlayerController` implements `DefaultLifecycleObserver`
- Player-related state is cleaned up in `onDestroy`
- `YouTubePlayerView` is registered with the lifecycle owner

### 4. Clear Separation of Concerns

- UI never manipulates `YouTubePlayer` directly
- All player commands flow through `PlayerController`
- Orientation logic and UI logic remain separated

---

## Core Flow

### 1. Player initialization flow

```kotlin
VideoScreen(video, initSecond)
    ->
remember { PlayerController() }  // Create controller
    ->
remember { createYouTubePlayerView(...) }  // Create player view
    ->
YouTubePlayer.onReady()
    ->
controller.initialize(youTubePlayer)  // Attach player to controller
    ->
controller.loadVideo(videoId, initSecond)  // Start preload / load flow
```

### 2. Playback state update flow

```kotlin
YouTubePlayer.onStateChange(state)
    ->
controller.updatePlaybackState(state)
    ->
_playbackState.value = PlaybackState.PLAYING  // Update StateFlow
    ->
UI collectAsStateWithLifecycle()  // Compose recomposes automatically
    ->
Display the matching play / pause UI state
```

### 3. Portrait / landscape switching flow

```kotlin
rememberDeviceOrientation()  // Track physical device orientation
    ->
OrientationEventListener.onOrientationChanged(orientation)
    ->
Resolve angle -> DeviceOrientation.LANDSCAPE_LEFT / RIGHT
    ->
isLandscape = true
    ->
Show LandscapeLayout (fullscreen mode)
    ->
AdaptiveScreenMode(orientation)  // Update system bars and orientation
    ->
Force landscape + hide system bars
```

### 4. Slider seek flow

```kotlin
Slider.onValueChange { newPosition }
    ->
isSeeking = true  // User is dragging
seekPosition = newPosition  // Update temporary position
    ->
Slider.onValueChangeFinished
    ->
controller.seekTo(seekPosition)  // Seek to the new position
    ->
player.seekTo(time)  // Call the YouTube player API
    ->
isSeeking = false  // Drag finished
```

---

## Playback State Management

### `PlaybackState`

```kotlin
enum class PlaybackState {
    IDLE,        // Not initialized or already released
    READY,       // Ready for playback
    BUFFERING,   // Waiting for media data
    PLAYING,     // Actively playing
    PAUSED,      // Temporarily paused
    ENDED        // Playback finished
}
```

### Main `PlayerController` APIs

| Function | Description |
| --- | --- |
| `initialize(youTubePlayer)` | Stores the player instance |
| `loadVideo(videoId, startTime)` | Starts the startup preload / cue flow |
| `play()` | Resumes playback |
| `pause()` | Pauses playback |
| `replay()` | Seeks back to the beginning and plays |
| `seekTo(time)` | Jumps to a specific time |
| `seekBackward(seconds)` | Rewinds, default 10 seconds |
| `seekForward(seconds)` | Fast-forwards, default 10 seconds |
| `updateCurrentSecond(second)` | Syncs current playback time |
| `updateDuration(duration)` | Syncs total duration |
| `updateLoadedFraction(loadedFraction)` | Syncs how much content is buffered |
| `updatePlaybackState(state)` | Maps YouTube state to app state |

---

## Orientation and Fullscreen Handling

### 1. Physical orientation detection

`OrientationEventListener` is used to resolve the device angle into semantic orientation:

```kotlin
0° ± 45°    -> PORTRAIT
90° ± 45°   -> LANDSCAPE_RIGHT
180° ± 45°  -> PORTRAIT_REVERSE
270° ± 45°  -> LANDSCAPE_LEFT
```

### 2. Adaptive screen mode

```kotlin
AdaptiveScreenMode(orientation)
```

In landscape:

1. Force the activity into landscape
2. Hide the status and navigation bars
3. Allow transient system bars with edge swipe

In portrait:

1. Force the activity into portrait
2. Show system bars normally

### 3. Manual exit from fullscreen

```kotlin
context.forcePortraitOrientation()
```

Used by the fullscreen exit button to return the app to portrait mode.

---

## YouTube Player Configuration

### `IFramePlayerOptions`

```kotlin
IFramePlayerOptions.Builder(context)
    .controls(0)       // Hide the default web controls
    .rel(0)            // Do not show unrelated videos after playback
    .ccLoadPolicy(0)   // Do not auto-load captions
    .build()
```

### Main callbacks

```kotlin
AbstractYouTubePlayerListener()
    .onReady()              // Player is ready
    .onCurrentSecond()      // Current playback time callback
    .onVideoDuration()      // Total video duration callback
    .onStateChange()        // Playback state changed
    .onError()              // Playback error callback
```

---

## Future Improvements

### Feature ideas

- Playback speed control (`0.25x` to `2x`)
- Manual quality selection (`Auto / 720p / 1080p`)
- Caption toggle and language selection
- Favorite / save video functionality
- TV casting

### Technical improvements

- Preload the next video
- Manage a pool of player instances
- Monitor network state and adapt quality heuristics
- Add retry strategies for playback errors

### UI / UX improvements

- Gesture controls for volume and brightness
- Double-tap to rewind / fast-forward
- Press-and-hold for temporary speed boost

---

## License and References

### YouTube Player Library

This project uses [PierfrancescoSoffritti/android-youtube-player](https://github.com/PierfrancescoSoffritti/android-youtube-player)

License: MIT

### YouTube API limitations

- Must comply with the [YouTube Terms of Service](https://www.youtube.com/t/terms)
- Video download is not allowed
- Ad removal is not allowed
- YouTube branding requirements must be respected

---

## Author

**Alex Yang**  
Senior Android Engineer  
GitHub: [https://github.com/m9939418](https://github.com/m9939418)

---

## Support

If this project is useful to you, consider giving it a star.
