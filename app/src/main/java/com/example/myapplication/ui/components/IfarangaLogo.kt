package com.example.myapplication.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

@Composable
fun IfarangaLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 64.dp,
    showTagline: Boolean = true,
    isHorizontal: Boolean = false,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    textColor: Color = MaterialTheme.colorScheme.primary,
    taglineColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val taglineText = "Know your money. (Amafaranga yawe. Uyamenye.)"

    if (isHorizontal) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_ifaranga_logo),
                contentDescription = "IFARANGA Logo",
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "IFARANGA",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    letterSpacing = 1.2.sp
                )
                if (showTagline) {
                    Text(
                        text = taglineText,
                        style = MaterialTheme.typography.labelMedium,
                        color = taglineColor
                    )
                }
            }
        }
    } else {
        Column(
            modifier = modifier,
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_ifaranga_logo),
                contentDescription = "IFARANGA Logo",
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "IFARANGA",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                letterSpacing = 1.5.sp
            )
            if (showTagline) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = taglineText,
                    style = MaterialTheme.typography.titleSmall,
                    color = taglineColor
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IfarangaLogoPreview() {
    Column {
        IfarangaLogo(showTagline = true)
        Spacer(modifier = Modifier.height(16.dp))
        IfarangaLogo(isHorizontal = true, showTagline = true, iconSize = 40.dp)
    }
}
