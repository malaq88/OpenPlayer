package com.example.openplayer.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openplayer.player.PlaybackConnection
import com.example.openplayer.player.PlaybackUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playback: PlaybackConnection,
) : ViewModel() {

    val playbackState: StateFlow<PlaybackUiState> = playback.playbackState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlaybackUiState())

    init {
        viewModelScope.launch {
            playback.connect()
        }
    }

    fun togglePlayPause() = playback.togglePlayPause()
    fun skipToNext() = playback.skipToNext()
    fun skipToPrevious() = playback.skipToPrevious()
    fun seekTo(positionMs: Long) = playback.seekTo(positionMs)
}
