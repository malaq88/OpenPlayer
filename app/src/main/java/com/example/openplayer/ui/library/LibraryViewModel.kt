package com.example.openplayer.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openplayer.data.model.Library
import com.example.openplayer.data.model.Playlist
import com.example.openplayer.data.model.Song
import com.example.openplayer.data.repository.MusicRepository
import com.example.openplayer.player.PlaybackConnection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LibraryTab {
    SONGS,
    ARTISTS,
    ALBUMS,
    GENRES,
    FOLDERS,
    PLAYLISTS,
}

data class LibraryUiState(
    val isLoading: Boolean = true,
    val library: Library? = null,
    val selectedTab: LibraryTab = LibraryTab.SONGS,
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val permissionDenied: Boolean = false,
)

class LibraryViewModel(
    private val repository: MusicRepository,
    private val playback: PlaybackConnection,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    val playlists: StateFlow<List<Playlist>> = repository.observePlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            loadLibrary()
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                permissionDenied = true,
            )
        }
    }

    fun loadLibrary() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching { repository.refreshLibrary() }
                .onSuccess { library ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        library = library,
                        permissionDenied = false,
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message,
                    )
                }
        }
    }

    fun selectTab(tab: LibraryTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun updateSearch(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query.take(100))
    }

    fun playSong(song: Song, queue: List<Song>) {
        val index = queue.indexOfFirst { it.id == song.id }.takeIf { it >= 0 } ?: 0
        playback.playSongs(queue, index)
    }

    fun playAll(songs: List<Song>) {
        if (songs.isNotEmpty()) playback.playSongs(songs)
    }

    fun playAllShuffled(songs: List<Song>) {
        if (songs.isNotEmpty()) playback.playSongsShuffled(songs)
    }

    fun createPlaylist(name: String, onResult: (Result<Long>) -> Unit) {
        viewModelScope.launch {
            onResult(repository.createPlaylist(name))
        }
    }

    fun filteredSongs(): List<Song> {
        val library = _uiState.value.library ?: return emptyList()
        val query = _uiState.value.searchQuery.trim().lowercase()
        if (query.isBlank()) return library.songs
        return library.songs.filter {
            it.title.lowercase().contains(query) ||
                it.artist.lowercase().contains(query) ||
                it.album.lowercase().contains(query)
        }
    }

    fun songsForArtist(artist: String, library: Library): List<Song> =
        repository.songsForArtist(artist, library)

    fun songsForAlbum(albumId: Long, library: Library): List<Song> =
        repository.songsForAlbum(albumId, library)

    fun songsForGenre(genre: String, library: Library): List<Song> =
        repository.songsForGenre(genre, library)

    fun songsForFolder(path: String, library: Library): List<Song> =
        repository.songsForFolder(path, library)
}
