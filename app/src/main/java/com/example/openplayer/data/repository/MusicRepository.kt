package com.example.openplayer.data.repository

import com.example.openplayer.data.local.MediaStoreDataSource
import com.example.openplayer.data.local.PlaylistDao
import com.example.openplayer.data.local.entity.PlaylistEntity
import com.example.openplayer.data.model.Album
import com.example.openplayer.data.model.Artist
import com.example.openplayer.data.model.Genre
import com.example.openplayer.data.model.Library
import com.example.openplayer.data.model.MusicFolder
import com.example.openplayer.data.model.Playlist
import com.example.openplayer.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MusicRepository(
    private val mediaStoreDataSource: MediaStoreDataSource,
    private val playlistDao: PlaylistDao,
) {
    private val cacheMutex = Mutex()
    private var cachedLibrary: Library? = null

    suspend fun refreshLibrary(): Library = cacheMutex.withLock {
        mediaStoreDataSource.loadLibrary().also { cachedLibrary = it }
    }

    suspend fun getLibrary(): Library = cacheMutex.withLock {
        cachedLibrary ?: mediaStoreDataSource.loadLibrary().also { cachedLibrary = it }
    }

    fun observePlaylists(): Flow<List<Playlist>> =
        playlistDao.observePlaylistsWithCounts().map { rows ->
            rows.map { row ->
                Playlist(
                    id = row.id,
                    name = row.name,
                    songCount = row.songCount,
                    createdAt = row.createdAt,
                )
            }
        }

    fun observePlaylistDetails(playlistId: Long): Flow<Playlist?> =
        playlistDao.observePlaylistsWithCounts().map { rows ->
            rows.find { it.id == playlistId }?.let { row ->
                Playlist(
                    id = row.id,
                    name = row.name,
                    songCount = row.songCount,
                    createdAt = row.createdAt,
                )
            }
        }

    fun observePlaylistSongs(playlistId: Long, library: Library): Flow<List<Song>> =
        playlistDao.observePlaylistSongIds(playlistId).map { songIds ->
            val songMap = library.songs.associateBy { it.id }
            songIds.mapNotNull { songMap[it] }
        }

    suspend fun createPlaylist(name: String): Result<Long> = runCatching {
        val sanitized = sanitizePlaylistName(name)
        require(sanitized.isNotBlank()) { "Nome inválido" }
        playlistDao.insertPlaylist(
            PlaylistEntity(
                name = sanitized,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        playlistDao.addSongsToPlaylist(playlistId, songIds.distinct())
    }

    suspend fun addAlbumToPlaylist(playlistId: Long, albumId: Long, library: Library) {
        val songIds = library.songs.filter { it.albumId == albumId }.map { it.id }
        addSongsToPlaylist(playlistId, songIds)
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    suspend fun getPlaylistSongIds(playlistId: Long): List<Long> =
        playlistDao.observePlaylistSongIdsOnce(playlistId)

    suspend fun setPlaylistSongs(playlistId: Long, songIds: List<Long>) {
        playlistDao.setPlaylistSongs(playlistId, songIds.distinct())
    }

    fun songsForArtist(artist: String, library: Library): List<Song> =
        library.songs
            .filter { it.artist == artist }
            .sortedWith(compareBy({ it.album.lowercase() }, { it.trackNumber }, { it.title.lowercase() }))

    fun songsForAlbum(albumId: Long, library: Library): List<Song> =
        library.songs.filter { it.albumId == albumId }.sortedBy { it.trackNumber }

    fun songsForGenre(genre: String, library: Library): List<Song> =
        library.songs.filter { it.genre == genre }.sortedBy { it.title.lowercase() }

    fun songsForFolder(path: String, library: Library): List<Song> =
        library.songs.filter { it.folderPath == path }.sortedBy { it.title.lowercase() }

    fun findAlbum(albumId: Long, library: Library): Album? =
        library.albums.find { it.id == albumId }

    fun findArtist(name: String, library: Library): Artist? =
        library.artists.find { it.name == name }

    fun findGenre(name: String, library: Library): Genre? =
        library.genres.find { it.name == name }

    fun findFolder(path: String, library: Library): MusicFolder? =
        library.folders.find { it.path == path }

    companion object {
        private const val MAX_PLAYLIST_NAME_LENGTH = 100

        fun sanitizePlaylistName(name: String): String =
            name.trim()
                .take(MAX_PLAYLIST_NAME_LENGTH)
                .replace(Regex("[\\x00-\\x1F]"), "")
    }
}
