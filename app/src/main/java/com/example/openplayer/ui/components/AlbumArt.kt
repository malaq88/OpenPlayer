package com.example.openplayer.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

val ListItemArtSize = 56.dp

@Composable
fun AlbumArt(
    albumArtUri: Uri?,
    modifier: Modifier = Modifier,
    size: Dp = ListItemArtSize,
) {
    Surface(
        modifier = modifier.size(size),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center,
        ) {
            if (albumArtUri != null) {
                AsyncImage(
                    model = albumArtUri,
                    contentDescription = null,
                    modifier = Modifier.size(size),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(size * 0.45f),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun SongArt(
    modifier: Modifier = Modifier,
    size: Dp = ListItemArtSize,
) {
    AlbumArt(albumArtUri = null, modifier = modifier, size = size)
}

@Composable
fun StackedAlbumArt(
    albumArtUris: List<Uri?>,
    modifier: Modifier = Modifier,
    size: Dp = ListItemArtSize,
) {
    val covers = albumArtUris.take(3)
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        when (covers.size) {
            0 -> SongArt(size = size)
            1 -> AlbumArt(albumArtUri = covers[0], size = size)
            else -> {
                val backSize = size * 0.72f
                val frontSize = size * 0.82f
                AlbumArt(
                    albumArtUri = covers.last(),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp),
                    size = backSize,
                )
                AlbumArt(
                    albumArtUri = covers.first(),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-2).dp, y = 2.dp),
                    size = frontSize,
                )
            }
        }
    }
}
