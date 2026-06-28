package com.example.openplayer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.openplayer.AppContainer
import com.example.openplayer.OpenPlayerApp
import com.example.openplayer.ui.library.LibraryViewModel
import com.example.openplayer.ui.player.PlayerViewModel
import com.example.openplayer.ui.playlist.PlaylistViewModel

@Composable
fun rememberAppContainer(): AppContainer {
    val context = LocalContext.current.applicationContext as OpenPlayerApp
    return context.container
}

@Composable
inline fun <reified VM : ViewModel> appViewModel(
    crossinline factory: (AppContainer) -> VM,
): VM {
    val container = rememberAppContainer()
    return viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                factory(container) as T
        },
    )
}

@Composable
fun rememberLibraryViewModel(): LibraryViewModel =
    appViewModel { LibraryViewModel(it.musicRepository, it.playbackConnection) }

@Composable
fun rememberPlayerViewModel(): PlayerViewModel =
    appViewModel { PlayerViewModel(it.playbackConnection) }

@Composable
fun rememberPlaylistViewModel(): PlaylistViewModel =
    appViewModel { PlaylistViewModel(it.musicRepository, it.playbackConnection) }
