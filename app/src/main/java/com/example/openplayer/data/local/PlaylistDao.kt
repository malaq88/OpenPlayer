package com.example.openplayer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.openplayer.data.local.entity.PlaylistEntity
import com.example.openplayer.data.local.entity.PlaylistSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query(
        """
        SELECT p.id, p.name, p.createdAt, COUNT(ps.songId) AS songCount
        FROM playlists p
        LEFT JOIN playlist_songs ps ON p.id = ps.playlistId
        GROUP BY p.id
        ORDER BY p.createdAt DESC
        """,
    )
    fun observePlaylistsWithCounts(): Flow<List<PlaylistWithCount>>

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylist(playlistId: Long): PlaylistEntity?

    @Query("SELECT songId FROM playlist_songs WHERE playlistId = :playlistId ORDER BY position ASC")
    fun observePlaylistSongIds(playlistId: Long): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    fun observePlaylistSongCount(playlistId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylistSongs(songs: List<PlaylistSongEntity>)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun deleteAllSongsFromPlaylist(playlistId: Long)

    @Transaction
    suspend fun setPlaylistSongs(playlistId: Long, songIds: List<Long>) {
        deleteAllSongsFromPlaylist(playlistId)
        if (songIds.isEmpty()) return
        val now = System.currentTimeMillis()
        insertPlaylistSongs(
            songIds.mapIndexed { index, songId ->
                PlaylistSongEntity(
                    playlistId = playlistId,
                    songId = songId,
                    position = index,
                    addedAt = now,
                )
            },
        )
    }

    @Transaction
    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        if (songIds.isEmpty()) return
        val existing = observePlaylistSongIdsOnce(playlistId).toSet()
        val maxPosition = getMaxPosition(playlistId) ?: -1
        val now = System.currentTimeMillis()
        val newSongs = songIds
            .filter { it !in existing }
            .mapIndexed { index, songId ->
                PlaylistSongEntity(
                    playlistId = playlistId,
                    songId = songId,
                    position = maxPosition + index + 1,
                    addedAt = now,
                )
            }
        if (newSongs.isNotEmpty()) {
            insertPlaylistSongs(newSongs)
        }
    }

    @Query("SELECT songId FROM playlist_songs WHERE playlistId = :playlistId ORDER BY position ASC")
    suspend fun observePlaylistSongIdsOnce(playlistId: Long): List<Long>

    @Query("SELECT MAX(position) FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getMaxPosition(playlistId: Long): Int?
}
