package com.example.openplayer.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.openplayer.ui.detail.AlbumDetailScreen
import com.example.openplayer.ui.detail.ArtistDetailScreen
import com.example.openplayer.ui.detail.FolderDetailScreen
import com.example.openplayer.ui.detail.GenreDetailScreen
import com.example.openplayer.ui.library.LibraryScreen
import com.example.openplayer.ui.player.MiniPlayerBar
import com.example.openplayer.ui.player.NowPlayingScreen
import com.example.openplayer.ui.player.PlayerViewModel
import com.example.openplayer.ui.playlist.PlaylistEditScreen
import com.example.openplayer.ui.rememberLibraryViewModel
import com.example.openplayer.ui.rememberPlayerViewModel
import com.example.openplayer.ui.rememberPlaylistViewModel

@Composable
fun OpenPlayerNavHost(
    playerViewModel: PlayerViewModel = rememberPlayerViewModel(),
) {
    val navController = rememberNavController()
    val libraryViewModel = rememberLibraryViewModel()
    val playlistViewModel = rememberPlaylistViewModel()
    val playbackState by playerViewModel.playbackState.collectAsStateWithLifecycle()
    val libraryState by libraryViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            MiniPlayerBar(
                state = playbackState,
                onClick = { navController.navigate(Routes.NOW_PLAYING) },
                onPlayPause = playerViewModel::togglePlayPause,
            )
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LIBRARY,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            composable(Routes.LIBRARY) {
                LibraryScreen(
                    viewModel = libraryViewModel,
                    onNavigateToArtist = { navController.navigate(Routes.artist(it)) },
                    onNavigateToAlbum = { navController.navigate(Routes.album(it)) },
                    onNavigateToGenre = { navController.navigate(Routes.genre(it)) },
                    onNavigateToFolder = { navController.navigate(Routes.folder(it)) },
                    onNavigateToPlaylist = { navController.navigate(Routes.playlist(it)) },
                )
            }
            composable(Routes.NOW_PLAYING) {
                NowPlayingScreen(
                    onBack = navController::popBackStack,
                    viewModel = playerViewModel,
                )
            }
            composable(
                route = Routes.ARTIST,
                arguments = listOf(navArgument("artistName") { type = NavType.StringType }),
            ) { entry ->
                ArtistDetailScreen(
                    artistName = entry.arguments?.getString("artistName").orEmpty(),
                    library = libraryState.library,
                    libraryViewModel = libraryViewModel,
                    playlistViewModel = playlistViewModel,
                    onBack = navController::popBackStack,
                )
            }
            composable(
                route = Routes.ALBUM,
                arguments = listOf(navArgument("albumId") { type = NavType.LongType }),
            ) { entry ->
                AlbumDetailScreen(
                    albumId = entry.arguments?.getLong("albumId") ?: 0L,
                    library = libraryState.library,
                    libraryViewModel = libraryViewModel,
                    playlistViewModel = playlistViewModel,
                    onBack = navController::popBackStack,
                )
            }
            composable(
                route = Routes.GENRE,
                arguments = listOf(navArgument("genreName") { type = NavType.StringType }),
            ) { entry ->
                GenreDetailScreen(
                    genreName = entry.arguments?.getString("genreName").orEmpty(),
                    library = libraryState.library,
                    libraryViewModel = libraryViewModel,
                    onBack = navController::popBackStack,
                )
            }
            composable(
                route = Routes.FOLDER,
                arguments = listOf(navArgument("folderPath") { type = NavType.StringType }),
            ) { entry ->
                FolderDetailScreen(
                    folderPath = entry.arguments?.getString("folderPath").orEmpty(),
                    library = libraryState.library,
                    libraryViewModel = libraryViewModel,
                    onBack = navController::popBackStack,
                )
            }
            composable(
                route = Routes.PLAYLIST,
                arguments = listOf(navArgument("playlistId") { type = NavType.LongType }),
            ) { entry ->
                PlaylistEditScreen(
                    playlistId = entry.arguments?.getLong("playlistId") ?: 0L,
                    library = libraryState.library,
                    playlistViewModel = playlistViewModel,
                    onBack = navController::popBackStack,
                )
            }
        }
    }
}
