package com.example.openplayer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.openplayer.data.local.entity.PlaylistEntity
import com.example.openplayer.data.local.entity.PlaylistSongEntity

@Database(
    entities = [PlaylistEntity::class, PlaylistSongEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class OpenPlayerDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
}
