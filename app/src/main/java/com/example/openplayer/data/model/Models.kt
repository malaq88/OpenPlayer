package com.example.openplayer.data.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val genre: String,
    val folderPath: String,
    val contentUri: Uri,
    val trackNumber: Int,
)

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val songCount: Int,
    val albumArtUri: Uri?,
)

data class Artist(
    val name: String,
    val albumCount: Int,
    val songCount: Int,
)

data class Genre(
    val name: String,
    val songCount: Int,
)

data class MusicFolder(
    val path: String,
    val name: String,
    val songCount: Int,
)

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int,
    val createdAt: Long,
)

data class Library(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val folders: List<MusicFolder> = emptyList(),
)
