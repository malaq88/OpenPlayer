package com.example.openplayer.ui.playlist

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.openplayer.R
import com.example.openplayer.data.model.Library
import com.example.openplayer.ui.components.SelectableSongListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistEditScreen(
    playlistId: Long,
    library: Library?,
    playlistViewModel: PlaylistViewModel,
    onBack: () -> Unit,
) {
    LaunchedEffect(playlistId, library) {
        playlistViewModel.load(playlistId, library)
    }

    val playlist by playlistViewModel.playlist.collectAsStateWithLifecycle()
    val selectedIds by playlistViewModel.selectedSongIds.collectAsStateWithLifecycle()
    val allSongs = library?.songs.orEmpty()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = playlist?.name ?: stringResource(R.string.playlist),
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(
                        onClick = playlistViewModel::playSelectedSongs,
                        enabled = selectedIds.isNotEmpty(),
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.play_all))
                    }
                    IconButton(
                        onClick = playlistViewModel::playSelectedSongsShuffled,
                        enabled = selectedIds.isNotEmpty(),
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = stringResource(R.string.shuffle_all))
                    }
                    IconButton(
                        onClick = { playlistViewModel.saveSelection(onBack) },
                    ) {
                        Icon(Icons.Default.Save, contentDescription = stringResource(R.string.save))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 8.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.select_playlist_songs, selectedIds.size),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
            itemsIndexed(allSongs, key = { _, song -> song.id }) { index, song ->
                SelectableSongListItem(
                    song = song,
                    selected = song.id in selectedIds,
                    onToggle = { playlistViewModel.toggleSongSelection(song.id) },
                    rowIndex = index,
                )
            }
        }
    }
}
