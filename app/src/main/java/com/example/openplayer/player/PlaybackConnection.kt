package com.example.openplayer.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.openplayer.data.model.Song
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PlaybackConnection(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val sessionToken = SessionToken(
        appContext,
        ComponentName(appContext, MusicPlaybackService::class.java),
    )
    private var controllerFuture: ListenableFuture<MediaController>? = null

    private val _controller = MutableStateFlow<MediaController?>(null)
    val controller: StateFlow<MediaController?> = _controller.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            updateState(player)
        }
    }

    private val _playbackState = MutableStateFlow(PlaybackUiState())
    val playbackState: StateFlow<PlaybackUiState> = _playbackState.asStateFlow()

    private val pendingActions = mutableListOf<() -> Unit>()
    private var isConnecting = false

    fun ensureConnected() {
        if (_controller.value != null || isConnecting) return
        isConnecting = true
        scope.launch {
            runCatching { connect() }
            isConnecting = false
        }
    }

    suspend fun connect() {
        if (_controller.value != null) return

        if (controllerFuture == null) {
            controllerFuture = MediaController.Builder(appContext, sessionToken).buildAsync()
        }

        val mediaController = controllerFuture!!.await()
        mediaController.removeListener(listener)
        mediaController.addListener(listener)
        mediaController.shuffleModeEnabled = false
        mediaController.repeatMode = Player.REPEAT_MODE_OFF
        _controller.value = mediaController
        updateState(mediaController)
        flushPendingActions()
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        runWhenReady {
            val controller = _controller.value ?: return@runWhenReady
            val safeIndex = startIndex.coerceIn(0, songs.lastIndex)
            val mediaItems = songs.map { it.toMediaItem() }
            controller.shuffleModeEnabled = false
            controller.repeatMode = Player.REPEAT_MODE_OFF
            controller.setMediaItems(mediaItems, safeIndex, 0L)
            controller.prepare()
            controller.play()
            updateState(controller)
        }
    }

    fun playSongsShuffled(songs: List<Song>) {
        if (songs.isEmpty()) return
        playSongs(songs.shuffled(), startIndex = 0)
    }

    fun togglePlayPause() {
        runWhenReady {
            val controller = _controller.value ?: return@runWhenReady
            if (controller.isPlaying) controller.pause() else controller.play()
        }
    }

    fun skipToNext() {
        runWhenReady {
            _controller.value?.seekToNextMediaItem()
        }
    }

    fun skipToPrevious() {
        runWhenReady {
            _controller.value?.seekToPreviousMediaItem()
        }
    }

    fun seekTo(positionMs: Long) {
        _controller.value?.seekTo(positionMs)
    }

    private fun runWhenReady(action: () -> Unit) {
        if (_controller.value != null) {
            action()
        } else {
            pendingActions += action
            ensureConnected()
        }
    }

    private fun flushPendingActions() {
        val actions = pendingActions.toList()
        pendingActions.clear()
        actions.forEach { it() }
    }

    private fun updateState(player: Player) {
        val metadata = player.mediaMetadata
        _playbackState.value = PlaybackUiState(
            title = metadata.title?.toString().orEmpty(),
            artist = metadata.artist?.toString().orEmpty(),
            album = metadata.albumTitle?.toString().orEmpty(),
            isPlaying = player.isPlaying,
            positionMs = player.currentPosition,
            durationMs = player.duration.coerceAtLeast(0),
            hasActiveMedia = player.mediaItemCount > 0,
        )
    }
}

data class PlaybackUiState(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val hasActiveMedia: Boolean = false,
)

fun Song.toMediaItem(): MediaItem =
    MediaItem.Builder()
        .setUri(contentUri)
        .setMediaId(id.toString())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .build(),
        )
        .build()

private suspend fun <T> ListenableFuture<T>.await(): T =
    suspendCancellableCoroutine { continuation ->
        addListener(
            {
                try {
                    continuation.resume(get())
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            },
            MoreExecutors.directExecutor(),
        )
        continuation.invokeOnCancellation { cancel(true) }
    }
