package com.example.openplayer.ui.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.openplayer.R
import com.example.openplayer.data.model.Library
import com.example.openplayer.ui.components.AlbumListItem
import com.example.openplayer.ui.components.AppBarLogoTitle
import com.example.openplayer.ui.components.SimpleListItem
import com.example.openplayer.ui.components.SongListItem
import com.example.openplayer.ui.playlist.CreatePlaylistDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onNavigateToArtist: (String) -> Unit,
    onNavigateToAlbum: (Long) -> Unit,
    onNavigateToGenre: (String) -> Unit,
    onNavigateToFolder: (String) -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    var showCreatePlaylist by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { AppBarLogoTitle() },
                actions = {
                    IconButton(onClick = viewModel::loadLibrary) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
                    }
                },
            )
        },
        floatingActionButton = {
            if (uiState.selectedTab == LibraryTab.PLAYLISTS) {
                FloatingActionButton(onClick = { showCreatePlaylist = true }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.create_playlist))
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LibraryTabRow(
                selectedTab = uiState.selectedTab,
                onTabSelected = viewModel::selectTab,
            )

            when {
                uiState.permissionDenied -> PermissionDeniedState(onRetry = viewModel::loadLibrary)
                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
                uiState.errorMessage != null -> ErrorState(
                    message = uiState.errorMessage.orEmpty(),
                    onRetry = viewModel::loadLibrary,
                )
                uiState.library == null -> EmptyLibraryState()
                else -> LibraryContent(
                    tab = uiState.selectedTab,
                    library = uiState.library!!,
                    playlists = playlists,
                    songs = viewModel.filteredSongs(),
                    viewModel = viewModel,
                    onNavigateToArtist = onNavigateToArtist,
                    onNavigateToAlbum = onNavigateToAlbum,
                    onNavigateToGenre = onNavigateToGenre,
                    onNavigateToFolder = onNavigateToFolder,
                    onNavigateToPlaylist = onNavigateToPlaylist,
                )
            }
        }
    }

    if (showCreatePlaylist) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylist = false },
            onCreate = { name ->
                viewModel.createPlaylist(name) { result ->
                    result.onSuccess { id -> onNavigateToPlaylist(id) }
                    showCreatePlaylist = false
                }
            },
        )
    }
}

@Composable
private fun LibraryTabRow(
    selectedTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
) {
    val tabs = listOf(
        LibraryTab.SONGS to R.string.tab_songs,
        LibraryTab.ARTISTS to R.string.tab_artists,
        LibraryTab.ALBUMS to R.string.tab_albums,
        LibraryTab.GENRES to R.string.tab_genres,
        LibraryTab.FOLDERS to R.string.tab_folders,
        LibraryTab.PLAYLISTS to R.string.tab_playlists,
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(tabs, key = { _, tab -> tab.first }) { _, (tab, labelRes) ->
            FilterChip(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                label = { Text(stringResource(labelRes)) },
            )
        }
    }
}

@Composable
private fun LibraryContent(
    tab: LibraryTab,
    library: Library,
    playlists: List<com.example.openplayer.data.model.Playlist>,
    songs: List<com.example.openplayer.data.model.Song>,
    viewModel: LibraryViewModel,
    onNavigateToArtist: (String) -> Unit,
    onNavigateToAlbum: (Long) -> Unit,
    onNavigateToGenre: (String) -> Unit,
    onNavigateToFolder: (String) -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 8.dp),
    ) {
        when (tab) {
            LibraryTab.SONGS -> {
                itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                    SongListItem(
                        song = song,
                        rowIndex = index,
                        onClick = { viewModel.playSong(song, songs) },
                    )
                }
            }
            LibraryTab.ARTISTS -> {
                itemsIndexed(library.artists, key = { _, artist -> artist.name }) { index, artist ->
                    SimpleListItem(
                        title = artist.name,
                        subtitle = stringResource(R.string.albums_and_songs, artist.albumCount, artist.songCount),
                        rowIndex = index,
                        onClick = { onNavigateToArtist(artist.name) },
                    )
                }
            }
            LibraryTab.ALBUMS -> {
                itemsIndexed(library.albums, key = { _, album -> album.id }) { index, album ->
                    AlbumListItem(
                        title = album.title,
                        subtitle = "${album.artist} · ${album.songCount} faixas",
                        albumArtUri = album.albumArtUri,
                        rowIndex = index,
                        onClick = { onNavigateToAlbum(album.id) },
                    )
                }
            }
            LibraryTab.GENRES -> {
                itemsIndexed(library.genres, key = { _, genre -> genre.name }) { index, genre ->
                    SimpleListItem(
                        title = genre.name,
                        subtitle = stringResource(R.string.songs_count, genre.songCount),
                        rowIndex = index,
                        onClick = { onNavigateToGenre(genre.name) },
                    )
                }
            }
            LibraryTab.FOLDERS -> {
                itemsIndexed(library.folders, key = { _, folder -> folder.path }) { index, folder ->
                    SimpleListItem(
                        title = folder.name,
                        subtitle = stringResource(R.string.songs_count, folder.songCount),
                        rowIndex = index,
                        onClick = { onNavigateToFolder(folder.path) },
                    )
                }
            }
            LibraryTab.PLAYLISTS -> {
                if (playlists.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.no_playlists_yet),
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                } else {
                    itemsIndexed(playlists, key = { _, playlist -> playlist.id }) { index, playlist ->
                        SimpleListItem(
                            title = playlist.name,
                            subtitle = stringResource(R.string.songs_count, playlist.songCount),
                            rowIndex = index,
                            onClick = { onNavigateToPlaylist(playlist.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionDeniedState(onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.permission_required))
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message)
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun EmptyLibraryState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.no_music_found))
    }
}
