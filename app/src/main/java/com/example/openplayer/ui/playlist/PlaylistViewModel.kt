package com.example.openplayer.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openplayer.data.model.Library
import com.example.openplayer.data.model.Playlist
import com.example.openplayer.data.model.Song
import com.example.openplayer.data.repository.MusicRepository
import com.example.openplayer.player.PlaybackConnection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistViewModel(
    private val repository: MusicRepository,
    private val playback: PlaybackConnection,
) : ViewModel() {

    private val _library = MutableStateFlow<Library?>(null)
    private val _playlistId = MutableStateFlow<Long?>(null)
    private val _selectedSongIds = MutableStateFlow<Set<Long>>(emptySet())

    val selectedSongIds: StateFlow<Set<Long>> = _selectedSongIds.asStateFlow()

    val playlist: StateFlow<Playlist?> = _playlistId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.observePlaylistDetails(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val songs: StateFlow<List<Song>> = _playlistId
        .flatMapLatest { id ->
            val library = _library.value
            if (id == null || library == null) {
                flowOf(emptyList())
            } else {
                repository.observePlaylistSongs(id, library)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun load(playlistId: Long, library: Library?) {
        _playlistId.value = playlistId
        _library.value = library
        viewModelScope.launch {
            val resolvedLibrary = library ?: repository.getLibrary()
            _library.value = resolvedLibrary
            _selectedSongIds.value = repository.getPlaylistSongIds(playlistId).toSet()
        }
    }

    fun toggleSongSelection(songId: Long) {
        _selectedSongIds.update { current ->
            if (songId in current) current - songId else current + songId
        }
    }

    fun saveSelection(onSaved: () -> Unit) {
        val playlistId = _playlistId.value ?: return
        val library = _library.value ?: return
        val orderedIds = library.songs
            .map { it.id }
            .filter { it in _selectedSongIds.value }
        viewModelScope.launch {
            repository.setPlaylistSongs(playlistId, orderedIds)
            onSaved()
        }
    }

    fun playSelectedSongs() {
        val library = _library.value ?: return
        val selected = library.songs.filter { it.id in _selectedSongIds.value }
        if (selected.isNotEmpty()) playback.playSongs(selected)
    }

    fun playSelectedSongsShuffled() {
        val library = _library.value ?: return
        val selected = library.songs.filter { it.id in _selectedSongIds.value }
        if (selected.isNotEmpty()) playback.playSongsShuffled(selected)
    }

    fun playPlaylist() {
        val currentSongs = songs.value
        if (currentSongs.isNotEmpty()) playback.playSongs(currentSongs)
    }

    fun playPlaylistShuffled() {
        val currentSongs = songs.value
        if (currentSongs.isNotEmpty()) playback.playSongsShuffled(currentSongs)
    }

    fun playSong(song: Song, queue: List<Song>) {
        val index = queue.indexOfFirst { it.id == song.id }.takeIf { it >= 0 } ?: 0
        playback.playSongs(queue, index)
    }

    fun addSongs(playlistId: Long, songIds: List<Long>) {
        viewModelScope.launch {
            repository.addSongsToPlaylist(playlistId, songIds)
        }
    }

    fun addAlbum(playlistId: Long, albumId: Long) {
        viewModelScope.launch {
            val library = _library.value ?: repository.getLibrary()
            repository.addAlbumToPlaylist(playlistId, albumId, library)
        }
    }
}
