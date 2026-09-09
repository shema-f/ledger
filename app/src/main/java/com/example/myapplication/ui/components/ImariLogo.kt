package com.example.myapplication.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ImariLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 64.dp,
    showTagline: Boolean = true,
    isHorizontal: Boolean = false,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    textColor: Color = MaterialTheme.colorScheme.primary,
    taglineColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    IfarangaLogo(
        modifier = modifier,
        iconSize = iconSize,
        showTagline = showTagline,
        isHorizontal = isHorizontal,
        horizontalAlignment = horizontalAlignment,
        textColor = textColor,
        taglineColor = taglineColor
    )
}

@Preview(showBackground = true)
@Composable
fun ImariLogoPreview() {
    IfarangaLogoPreview()
}
