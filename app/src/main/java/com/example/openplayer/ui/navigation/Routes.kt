package com.example.openplayer.ui.navigation

object Routes {
    const val LIBRARY = "library"
    const val NOW_PLAYING = "now_playing"
    const val ARTIST = "artist/{artistName}"
    const val ALBUM = "album/{albumId}"
    const val GENRE = "genre/{genreName}"
    const val FOLDER = "folder/{folderPath}"
    const val PLAYLIST = "playlist/{playlistId}"

    fun artist(name: String) = "artist/${UriEncoder.encode(name)}"
    fun album(id: Long) = "album/$id"
    fun genre(name: String) = "genre/${UriEncoder.encode(name)}"
    fun folder(path: String) = "folder/${UriEncoder.encode(path)}"
    fun playlist(id: Long) = "playlist/$id"
}

private object UriEncoder {
    fun encode(value: String): String =
        java.net.URLEncoder.encode(value, Charsets.UTF_8.name())
}
