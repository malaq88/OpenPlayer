package com.example.openplayer.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlife
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import java.text.Normalizer
import java.util.Locale

private val DefaultGenreIcon = Icons.Filled.LibraryMusic

private val exactGenreIcons = mapOf(
    // Populares (EN)
    "rock" to Icons.Filled.Whatshot,
    "pop" to Icons.Filled.Star,
    "jazz" to Icons.Filled.Piano,
    "classical" to Icons.Filled.Piano,
    "hip hop" to Icons.Filled.Mic,
    "hip-hop" to Icons.Filled.Mic,
    "hiphop" to Icons.Filled.Mic,
    "rap" to Icons.Filled.Mic,
    "electronic" to Icons.Filled.GraphicEq,
    "techno" to Icons.Filled.GraphicEq,
    "house" to Icons.Filled.GraphicEq,
    "trance" to Icons.Filled.GraphicEq,
    "edm" to Icons.Filled.GraphicEq,
    "dance" to Icons.Filled.Nightlife,
    "disco" to Icons.Filled.Nightlife,
    "country" to Icons.Filled.Landscape,
    "folk" to Icons.Filled.Headphones,
    "blues" to Icons.Filled.Mic,
    "metal" to Icons.Filled.ElectricBolt,
    "reggae" to Icons.Filled.Waves,
    "latin" to Icons.Filled.Celebration,
    "r&b" to Icons.Filled.Favorite,
    "rnb" to Icons.Filled.Favorite,
    "soul" to Icons.Filled.VolunteerActivism,
    "funk" to Icons.Filled.GraphicEq,
    "gospel" to Icons.Filled.Church,
    "punk" to Icons.Filled.ElectricBolt,
    "ambient" to Icons.Filled.Waves,
    "alternative" to Icons.Filled.Headphones,
    "indie" to Icons.Filled.Headphones,
    "soundtrack" to Icons.Filled.Movie,
    "world" to Icons.Filled.Public,
    "new age" to Icons.Filled.Spa,
    "acoustic" to Icons.Filled.Headphones,
    "instrumental" to Icons.Filled.Piano,
    "opera" to Icons.Filled.Piano,
    "comedy" to Icons.Filled.TheaterComedy,
    "podcast" to Icons.Filled.Mic,
    "audiobook" to Icons.Filled.Mic,
    "game" to Icons.Filled.SportsEsports,
    "other" to DefaultGenreIcon,
  // Populares (PT)
    "classica" to Icons.Filled.Piano,
    "musica classica" to Icons.Filled.Piano,
    "eletronica" to Icons.Filled.GraphicEq,
    "eletrônica" to Icons.Filled.GraphicEq,
    "sertanejo" to Icons.Filled.Landscape,
    "samba" to Icons.Filled.Celebration,
    "bossa nova" to Icons.Filled.Piano,
    "pagode" to Icons.Filled.Celebration,
    "forro" to Icons.Filled.Celebration,
    "funk carioca" to Icons.Filled.GraphicEq,
    "mpb" to Icons.Filled.Headphones,
    "rock nacional" to Icons.Filled.Whatshot,
    "genero desconhecido" to DefaultGenreIcon,
    "gênero desconhecido" to DefaultGenreIcon,
    "unknown" to DefaultGenreIcon,
)

private data class GenreKeywordRule(
    val keywords: List<String>,
    val icon: ImageVector,
)

private val genreKeywordRules = listOf(
    GenreKeywordRule(listOf("soundtrack", "trilha", "ost", "banda sonora", "score"), Icons.Filled.Movie),
    GenreKeywordRule(listOf("hip hop", "hip-hop", "hiphop", "trap", "drill"), Icons.Filled.Mic),
    GenreKeywordRule(listOf("rap", "grime"), Icons.Filled.Mic),
    GenreKeywordRule(listOf("metal", "grunge", "hardcore", "thrash", "death", "black metal"), Icons.Filled.ElectricBolt),
    GenreKeywordRule(listOf("punk", "ska", "emo"), Icons.Filled.ElectricBolt),
    GenreKeywordRule(listOf("rock", "altern"), Icons.Filled.Whatshot),
    GenreKeywordRule(listOf("jazz", "swing", "bebop", "bossa"), Icons.Filled.Piano),
    GenreKeywordRule(listOf("classical", "classica", "clasica", "orchestr", "symphon", "baroque", "chamber"), Icons.Filled.Piano),
    GenreKeywordRule(listOf("opera", "oper"), Icons.Filled.Piano),
    GenreKeywordRule(listOf("electro", "techno", "house", "trance", "dubstep", "drum and bass", "dnb", "edm", "synth"), Icons.Filled.GraphicEq),
    GenreKeywordRule(listOf("dance", "disco", "club", "rave"), Icons.Filled.Nightlife),
    GenreKeywordRule(listOf("pop", "teen", "k-pop", "kpop", "j-pop"), Icons.Filled.Star),
    GenreKeywordRule(listOf("country", "sertanejo", "bluegrass", "western"), Icons.Filled.Landscape),
    GenreKeywordRule(listOf("folk", "acoustic", "celtic", "mpb", "cantautor"), Icons.Filled.Headphones),
    GenreKeywordRule(listOf("blues"), Icons.Filled.Mic),
    GenreKeywordRule(listOf("reggae", "dancehall", "ska"), Icons.Filled.Waves),
    GenreKeywordRule(listOf("latin", "latino", "salsa", "bachata", "merengue", "cumbia", "tango", "samba", "pagode", "forro", "axé", "axe"), Icons.Filled.Celebration),
    GenreKeywordRule(listOf("r&b", "rnb", "rhythm"), Icons.Filled.Favorite),
    GenreKeywordRule(listOf("soul", "motown"), Icons.Filled.VolunteerActivism),
    GenreKeywordRule(listOf("funk"), Icons.Filled.GraphicEq),
    GenreKeywordRule(listOf("gospel", "christian", "worship", "spiritual", "hymn"), Icons.Filled.Church),
    GenreKeywordRule(listOf("ambient", "chill", "lofi", "lo-fi", "downtempo", "meditation"), Icons.Filled.Waves),
    GenreKeywordRule(listOf("new age", "spa", "relax"), Icons.Filled.Spa),
    GenreKeywordRule(listOf("world", "ethnic", "african", "celtic", "flamenco", "fado"), Icons.Filled.Public),
    GenreKeywordRule(listOf("indie", "alternative", "alt "), Icons.Filled.Headphones),
    GenreKeywordRule(listOf("radio", "talk", "podcast", "audiobook", "spoken"), Icons.Filled.Radio),
    GenreKeywordRule(listOf("comedy", "humor", "stand-up"), Icons.Filled.TheaterComedy),
    GenreKeywordRule(listOf("game", "vgm", "video game"), Icons.Filled.SportsEsports),
    GenreKeywordRule(listOf("instrumental", "piano", "violin", "guitar"), Icons.Filled.Piano),
)

fun genreIconFor(genreName: String): ImageVector {
    val normalized = normalizeGenreName(genreName)
    if (normalized.isBlank()) return DefaultGenreIcon

    exactGenreIcons[normalized]?.let { return it }

    for (rule in genreKeywordRules) {
        if (rule.keywords.any { keyword -> normalized.contains(keyword) }) {
            return rule.icon
        }
    }

    return DefaultGenreIcon
}

private fun normalizeGenreName(name: String): String {
    val withoutAccents = Normalizer.normalize(name, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
    return withoutAccents
        .lowercase(Locale.ROOT)
        .replace(Regex("[_\\-/]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

@Composable
fun GenreArt(
    genreName: String,
    modifier: Modifier = Modifier,
    size: Dp = ListItemArtSize,
) {
    val icon = genreIconFor(genreName)
    Surface(
        modifier = modifier.size(size),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (icon == DefaultGenreIcon && genreName.isBlank()) {
                    Icons.Filled.MusicNote
                } else {
                    icon
                },
                contentDescription = null,
                modifier = Modifier.size(size * 0.45f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
