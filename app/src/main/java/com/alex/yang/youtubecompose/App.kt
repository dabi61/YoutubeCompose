package com.alex.yang.youtubecompose

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point used to initialize Hilt.
 */
@HiltAndroidApp
class App : Application()

const val TAG = "[DEBUG]"
