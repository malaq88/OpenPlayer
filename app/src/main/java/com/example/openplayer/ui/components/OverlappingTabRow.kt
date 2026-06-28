package com.example.openplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.openplayer.ui.library.LibraryTab
import com.example.openplayer.ui.theme.OrangeListDark
import com.example.openplayer.ui.theme.OrangeListDarkDark
import com.example.openplayer.ui.theme.OrangeListLight
import com.example.openplayer.ui.theme.OrangeListLightDark
import com.example.openplayer.ui.theme.OrangePrimary
import com.example.openplayer.ui.theme.OrangeSecondary

private val TabOverlap = 8.dp
private val TabOverlapLeading = 4.dp
private val TabHeightSelected = 44.dp
private val TabHeightUnselected = 34.dp

private class OverlappingTabShape(
    private val cornerRadius: Dp = 10.dp,
    private val slant: Dp = 5.dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val radius = with(density) { cornerRadius.toPx() }
        val slantPx = with(density) { slant.toPx() }
        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(slantPx, radius)
            quadraticTo(slantPx, 0f, slantPx + radius, 0f)
            lineTo(size.width - slantPx - radius, 0f)
            quadraticTo(size.width - slantPx, 0f, size.width - slantPx, radius)
            lineTo(size.width, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

data class OverlappingTabColors(
    val stripBackground: Color,
    val tabUnselected: Color,
    val tabSelected: Color,
    val tabUnselectedText: Color,
    val tabSelectedText: Color,
    val panelBackground: Color,
)

@Composable
fun overlappingTabColors(): OverlappingTabColors {
    val dark = isSystemInDarkTheme()
    return if (dark) {
        OverlappingTabColors(
            stripBackground = OrangeListDarkDark,
            tabUnselected = OrangeSecondary.copy(alpha = 0.35f),
            tabSelected = OrangeListLightDark,
            tabUnselectedText = OrangeListLight.copy(alpha = 0.7f),
            tabSelectedText = OrangeListLight,
            panelBackground = OrangeListLightDark,
        )
    } else {
        OverlappingTabColors(
            stripBackground = OrangeSecondary.copy(alpha = 0.45f),
            tabUnselected = OrangeListDark.copy(alpha = 0.9f),
            tabSelected = OrangeListLight,
            tabUnselectedText = OrangePrimary.copy(alpha = 0.65f),
            tabSelectedText = OrangePrimary,
            panelBackground = OrangeListLight,
        )
    }
}

@Composable
fun OverlappingTabRow(
    tabs: List<Pair<LibraryTab, Int>>,
    selectedTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = overlappingTabColors()
    val listState = rememberLazyListState()
    val selectedIndex = tabs.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0)
    val tabShape = OverlappingTabShape()

    LaunchedEffect(selectedTab) {
        listState.animateScrollToItem(selectedIndex)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.stripBackground)
            .padding(top = 8.dp),
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 12.dp, end = 10.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            itemsIndexed(
                items = tabs,
                key = { _, item -> item.first },
            ) { index, (tab, labelRes) ->
                val isSelected = tab == selectedTab
                val z = if (isSelected) 100f else (tabs.size - index).toFloat()

                OverlappingTabItem(
                    label = stringResource(labelRes),
                    selected = isSelected,
                    colors = colors,
                    shape = tabShape,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier
                        .zIndex(z)
                        .then(
                            when (index) {
                                0 -> Modifier.padding(end = 6.dp)
                                1 -> Modifier.offset(x = -TabOverlapLeading)
                                else -> Modifier.offset(x = -TabOverlap)
                            },
                        ),
                )
            }
        }
    }
}

@Composable
private fun OverlappingTabItem(
    label: String,
    selected: Boolean,
    colors: OverlappingTabColors,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val height = if (selected) TabHeightSelected else TabHeightUnselected
    val backgroundColor = if (selected) colors.tabSelected else colors.tabUnselected
    val textColor = if (selected) colors.tabSelectedText else colors.tabUnselectedText
    val elevation = if (selected) 6.dp else 1.dp

    Box(
        modifier = modifier
            .padding(end = 2.dp)
            .shadow(elevation, shape, clip = false)
            .clip(shape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .height(height)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = if (selected) 14.sp else 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun OverlappingTabPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = overlappingTabColors()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.panelBackground),
    ) {
        content()
    }
}
