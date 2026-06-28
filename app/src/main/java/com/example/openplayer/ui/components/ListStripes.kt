package com.example.openplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import com.example.openplayer.ui.theme.OrangeListDark
import com.example.openplayer.ui.theme.OrangeListDarkDark
import com.example.openplayer.ui.theme.OrangeListLight
import com.example.openplayer.ui.theme.OrangeListLightDark

@Composable
fun stripedRowColor(index: Int): Color =
    if (isSystemInDarkTheme()) {
        if (index % 2 == 0) OrangeListLightDark else OrangeListDarkDark
    } else {
        if (index % 2 == 0) OrangeListLight else OrangeListDark
    }

fun Modifier.stripedRowBackground(index: Int): Modifier = composed {
    val color = if (isSystemInDarkTheme()) {
        if (index % 2 == 0) OrangeListLightDark else OrangeListDarkDark
    } else {
        if (index % 2 == 0) OrangeListLight else OrangeListDark
    }
    background(color)
}
