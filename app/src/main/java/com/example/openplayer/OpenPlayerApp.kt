package com.example.openplayer

import android.app.Application
import androidx.room.Room
import com.example.openplayer.data.local.MediaStoreDataSource
import com.example.openplayer.data.local.OpenPlayerDatabase
import com.example.openplayer.data.repository.MusicRepository
import com.example.openplayer.player.PlaybackConnection

class OpenPlayerApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.playbackConnection.ensureConnected()
    }
}

class AppContainer(application: Application) {
    private val database = Room.databaseBuilder(
        application,
        OpenPlayerDatabase::class.java,
        "openplayer.db",
    ).build()

    val musicRepository = MusicRepository(
        mediaStoreDataSource = MediaStoreDataSource(application),
        playlistDao = database.playlistDao(),
    )

    val playbackConnection = PlaybackConnection(application)
}
