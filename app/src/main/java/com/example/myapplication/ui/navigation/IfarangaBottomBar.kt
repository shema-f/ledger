package com.example.myapplication.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class IfarangaTab(
    val label: String,
    val icon: ImageVector
) {
    HOME("Home", Icons.Rounded.Home),
    MONEY("Money", Icons.Rounded.AccountBalanceWallet),
    BUSINESS("Business", Icons.Rounded.Storefront),
    INSIGHTS("Insights", Icons.Rounded.Psychology),
    PROFILE("Profile", Icons.Rounded.Person)
}

@Composable
fun IfarangaBottomBar(
    selectedTab: IfarangaTab,
    onTabSelected: (IfarangaTab) -> Unit,
    onQuickActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Tab 1: Home
            BottomNavItem(
                tab = IfarangaTab.HOME,
                isSelected = selectedTab == IfarangaTab.HOME,
                onClick = { onTabSelected(IfarangaTab.HOME) },
                modifier = Modifier.weight(1f)
            )

            // Tab 2: Money
            BottomNavItem(
                tab = IfarangaTab.MONEY,
                isSelected = selectedTab == IfarangaTab.MONEY,
                onClick = { onTabSelected(IfarangaTab.MONEY) },
                modifier = Modifier.weight(1f)
            )

            // Central Floating Quick Action Button (+)
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = onQuickActionClick,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 12.dp
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Quick Action (+)",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // Tab 3: Business
            BottomNavItem(
                tab = IfarangaTab.BUSINESS,
                isSelected = selectedTab == IfarangaTab.BUSINESS,
                onClick = { onTabSelected(IfarangaTab.BUSINESS) },
                modifier = Modifier.weight(1f)
            )

            // Tab 4: Insights
            BottomNavItem(
                tab = IfarangaTab.INSIGHTS,
                isSelected = selectedTab == IfarangaTab.INSIGHTS,
                onClick = { onTabSelected(IfarangaTab.INSIGHTS) },
                modifier = Modifier.weight(1f)
            )

            // Tab 5: Profile
            BottomNavItem(
                tab = IfarangaTab.PROFILE,
                isSelected = selectedTab == IfarangaTab.PROFILE,
                onClick = { onTabSelected(IfarangaTab.PROFILE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: IfarangaTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp, 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = tab.label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) activeColor else inactiveColor
        )
    }
}
