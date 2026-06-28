package com.example.openplayer.data.local

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.openplayer.data.model.Album
import com.example.openplayer.data.model.Artist
import com.example.openplayer.data.model.Genre
import com.example.openplayer.data.model.Library
import com.example.openplayer.data.model.MusicFolder
import com.example.openplayer.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaStoreDataSource(private val context: Context) {

    suspend fun loadLibrary(): Library = withContext(Dispatchers.IO) {
        val songs = querySongs()
        Library(
            songs = songs,
            albums = buildAlbums(songs),
            artists = buildArtists(songs),
            genres = buildGenres(songs),
            folders = buildFolders(songs),
        )
    }

    private fun querySongs(): List<Song> {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.GENRE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TRACK,
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(MIN_DURATION_MS.toString())
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        val songs = mutableListOf<Song>()
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder,
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val genreCol = cursor.getColumnIndex(MediaStore.Audio.Media.GENRE)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol)?.sanitize() ?: continue
                val artist = cursor.getString(artistCol)?.sanitize().orEmpty().ifBlank { UNKNOWN_ARTIST }
                val album = cursor.getString(albumCol)?.sanitize().orEmpty().ifBlank { UNKNOWN_ALBUM }
                val albumId = cursor.getLong(albumIdCol)
                val duration = cursor.getLong(durationCol)
                val genre = if (genreCol >= 0) {
                    cursor.getString(genreCol)?.sanitize().orEmpty().ifBlank { UNKNOWN_GENRE }
                } else {
                    UNKNOWN_GENRE
                }
                val dataPath = cursor.getString(dataCol)?.sanitize().orEmpty()
                val track = cursor.getInt(trackCol)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id,
                )
                songs += Song(
                    id = id,
                    title = title,
                    artist = artist,
                    album = album,
                    albumId = albumId,
                    durationMs = duration,
                    genre = genre,
                    folderPath = extractFolderPath(dataPath),
                    contentUri = contentUri,
                    trackNumber = track,
                )
            }
        }
        return songs
    }

    private fun buildAlbums(songs: List<Song>): List<Album> =
        songs
            .groupBy { it.albumId }
            .map { (albumId, albumSongs) ->
                val first = albumSongs.first()
                Album(
                    id = albumId,
                    title = first.album,
                    artist = first.artist,
                    songCount = albumSongs.size,
                    albumArtUri = albumArtUri(albumId),
                )
            }
            .sortedBy { it.title.lowercase() }

    private fun buildArtists(songs: List<Song>): List<Artist> =
        songs
            .groupBy { it.artist }
            .map { (name, artistSongs) ->
                Artist(
                    name = name,
                    albumCount = artistSongs.map { it.albumId }.distinct().size,
                    songCount = artistSongs.size,
                )
            }
            .sortedBy { it.name.lowercase() }

    private fun buildGenres(songs: List<Song>): List<Genre> =
        songs
            .groupBy { it.genre }
            .map { (name, genreSongs) ->
                Genre(name = name, songCount = genreSongs.size)
            }
            .sortedBy { it.name.lowercase() }

    private fun buildFolders(songs: List<Song>): List<MusicFolder> =
        songs
            .groupBy { it.folderPath }
            .map { (path, folderSongs) ->
                MusicFolder(
                    path = path,
                    name = path.substringAfterLast('/').ifBlank { path },
                    songCount = folderSongs.size,
                )
            }
            .sortedBy { it.name.lowercase() }

    private fun albumArtUri(albumId: Long): Uri? {
        val uri = ContentUris.withAppendedId(ALBUM_ART_URI, albumId)
        return try {
            context.contentResolver.openInputStream(uri)?.use { }
            uri
        } catch (_: Exception) {
            null
        }
    }

    private fun extractFolderPath(dataPath: String): String {
        if (dataPath.isBlank()) return UNKNOWN_FOLDER
        val parent = File(dataPath).parent ?: return UNKNOWN_FOLDER
        return parent.ifBlank { UNKNOWN_FOLDER }
    }

    private fun String.sanitize(): String = take(MAX_FIELD_LENGTH).trim()

    companion object {
        private const val MIN_DURATION_MS = 30_000L
        private const val MAX_FIELD_LENGTH = 512
        const val UNKNOWN_ARTIST = "Artista desconhecido"
        const val UNKNOWN_ALBUM = "Álbum desconhecido"
        const val UNKNOWN_GENRE = "Gênero desconhecido"
        const val UNKNOWN_FOLDER = "Pasta desconhecida"
        private val ALBUM_ART_URI = Uri.parse("content://media/external/audio/albumart")
    }
}
