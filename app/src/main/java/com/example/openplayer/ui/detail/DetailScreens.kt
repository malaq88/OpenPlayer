package com.example.openplayer.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.openplayer.R
import com.example.openplayer.data.model.Library
import com.example.openplayer.data.model.Playlist
import com.example.openplayer.data.model.Song
import com.example.openplayer.ui.components.SongListItem
import com.example.openplayer.ui.library.LibraryViewModel
import com.example.openplayer.ui.playlist.AddToPlaylistDialog
import com.example.openplayer.ui.playlist.CreatePlaylistDialog
import com.example.openplayer.ui.playlist.PlaylistViewModel
import java.net.URLDecoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artistName: String,
    library: Library?,
    libraryViewModel: LibraryViewModel,
    playlistViewModel: PlaylistViewModel,
    onBack: () -> Unit,
) {
    val decodedName = remember(artistName) { URLDecoder.decode(artistName, Charsets.UTF_8.name()) }
    val playlists by libraryViewModel.playlists.collectAsStateWithLifecycle()
    var pendingSongIds by remember { mutableStateOf<List<Long>?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val songs = remember(library, decodedName) {
        library?.let { libraryViewModel.songsForArtist(decodedName, it) }.orEmpty()
    }

    DetailScaffold(
        title = decodedName,
        subtitle = stringResource(R.string.songs_count, songs.size),
        onBack = onBack,
        onPlayAll = { libraryViewModel.playAll(songs) },
        onShuffleAll = { libraryViewModel.playAllShuffled(songs) },
    ) {
        songItems(
            songs = songs,
            libraryViewModel = libraryViewModel,
            onMoreClick = { song ->
                pendingSongIds = listOf(song.id)
                showAddDialog = true
            },
        )
    }

    PlaylistPickerDialogs(
        showAddDialog = showAddDialog,
        showCreateDialog = showCreateDialog,
        playlists = playlists,
        onDismissAdd = { showAddDialog = false },
        onDismissCreate = { showCreateDialog = false },
        onSelectPlaylist = { playlistId ->
            pendingSongIds?.let { playlistViewModel.addSongs(playlistId, it) }
            showAddDialog = false
        },
        onCreateNew = {
            showAddDialog = false
            showCreateDialog = true
        },
        onCreatePlaylist = { name ->
            libraryViewModel.createPlaylist(name) { result ->
                result.onSuccess { id ->
                    pendingSongIds?.let { playlistViewModel.addSongs(id, it) }
                }
                showCreateDialog = false
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumId: Long,
    library: Library?,
    libraryViewModel: LibraryViewModel,
    playlistViewModel: PlaylistViewModel,
    onBack: () -> Unit,
) {
    val playlists by libraryViewModel.playlists.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val album = remember(library, albumId) { library?.albums?.find { it.id == albumId } }
    val songs = remember(library, albumId) {
        library?.let { libraryViewModel.songsForAlbum(albumId, it) }.orEmpty()
    }

    DetailScaffold(
        title = album?.title ?: stringResource(R.string.album),
        subtitle = album?.artist.orEmpty(),
        onBack = onBack,
        onPlayAll = { libraryViewModel.playAll(songs) },
        onShuffleAll = { libraryViewModel.playAllShuffled(songs) },
        fab = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(
                    Icons.AutoMirrored.Filled.PlaylistAdd,
                    contentDescription = stringResource(R.string.add_album_to_playlist),
                )
            }
        },
    ) {
        songItems(songs, libraryViewModel)
    }

    PlaylistPickerDialogs(
        showAddDialog = showAddDialog,
        showCreateDialog = showCreateDialog,
        playlists = playlists,
        onDismissAdd = { showAddDialog = false },
        onDismissCreate = { showCreateDialog = false },
        onSelectPlaylist = { playlistId ->
            playlistViewModel.addAlbum(playlistId, albumId)
            showAddDialog = false
        },
        onCreateNew = {
            showAddDialog = false
            showCreateDialog = true
        },
        onCreatePlaylist = { name ->
            libraryViewModel.createPlaylist(name) { result ->
                result.onSuccess { id -> playlistViewModel.addAlbum(id, albumId) }
                showCreateDialog = false
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreDetailScreen(
    genreName: String,
    library: Library?,
    libraryViewModel: LibraryViewModel,
    onBack: () -> Unit,
) {
    val decodedName = remember(genreName) { URLDecoder.decode(genreName, Charsets.UTF_8.name()) }
    val songs = remember(library, decodedName) {
        library?.let { libraryViewModel.songsForGenre(decodedName, it) }.orEmpty()
    }

    DetailScaffold(
        title = decodedName,
        subtitle = stringResource(R.string.songs_count, songs.size),
        onBack = onBack,
        onPlayAll = { libraryViewModel.playAll(songs) },
        onShuffleAll = { libraryViewModel.playAllShuffled(songs) },
    ) {
        songItems(songs, libraryViewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(
    folderPath: String,
    library: Library?,
    libraryViewModel: LibraryViewModel,
    onBack: () -> Unit,
) {
    val decodedPath = remember(folderPath) { URLDecoder.decode(folderPath, Charsets.UTF_8.name()) }
    val songs = remember(library, decodedPath) {
        library?.let { libraryViewModel.songsForFolder(decodedPath, it) }.orEmpty()
    }
    val folderName = decodedPath.substringAfterLast('/')

    DetailScaffold(
        title = folderName.ifBlank { decodedPath },
        subtitle = stringResource(R.string.songs_count, songs.size),
        onBack = onBack,
        onPlayAll = { libraryViewModel.playAll(songs) },
        onShuffleAll = { libraryViewModel.playAllShuffled(songs) },
    ) {
        songItems(songs, libraryViewModel)
    }
}

private fun LazyListScope.songItems(
    songs: List<Song>,
    libraryViewModel: LibraryViewModel,
    onMoreClick: ((Song) -> Unit)? = null,
) {
    itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
        SongListItem(
            song = song,
            rowIndex = index,
            onClick = { libraryViewModel.playSong(song, songs) },
            onMoreClick = onMoreClick?.let { callback -> { callback(song) } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailScaffold(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    fab: @Composable () -> Unit = {},
    content: LazyListScope.() -> Unit,
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, maxLines = 1)
                        if (subtitle.isNotBlank()) {
                            Text(subtitle, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onPlayAll) {
                        Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.play_all))
                    }
                    if (onShuffleAll != null) {
                        IconButton(onClick = onShuffleAll) {
                            Icon(Icons.Default.Shuffle, contentDescription = stringResource(R.string.shuffle_all))
                        }
                    }
                },
            )
        },
        floatingActionButton = fab,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 8.dp),
            content = content,
        )
    }
}

@Composable
private fun PlaylistPickerDialogs(
    showAddDialog: Boolean,
    showCreateDialog: Boolean,
    playlists: List<Playlist>,
    onDismissAdd: () -> Unit,
    onDismissCreate: () -> Unit,
    onSelectPlaylist: (Long) -> Unit,
    onCreateNew: () -> Unit,
    onCreatePlaylist: (String) -> Unit,
) {
    if (showAddDialog) {
        AddToPlaylistDialog(
            playlists = playlists,
            onDismiss = onDismissAdd,
            onSelectPlaylist = onSelectPlaylist,
            onCreateNew = onCreateNew,
        )
    }
    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = onDismissCreate,
            onCreate = onCreatePlaylist,
        )
    }
}
