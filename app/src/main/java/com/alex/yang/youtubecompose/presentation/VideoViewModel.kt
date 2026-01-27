package com.alex.yang.youtubecompose.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Created by AlexYang on 2026/1/26.
 *
 *
 */
@HiltViewModel
class VideoViewModel @Inject constructor(

) : ViewModel() {

    fun onEvent(event: UiEvent) = when (event) {
        UiEvent.OnBackClick -> {

        }
    }

    sealed interface UiEvent {
        data object OnBackClick : UiEvent
    }
}