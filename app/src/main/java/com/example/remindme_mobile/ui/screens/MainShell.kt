package com.example.remindme_mobile.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.remindme_mobile.ui.components.liquid.*
import com.example.remindme_mobile.ui.theme.AppColors

@Composable
fun MainShell() {
    var selectedIndex by remember { mutableStateOf(0) }

    LiquidScaffold {
        // App Bar
        LiquidAppBar(
            title = "RemindME",
            actions = {
                LiquidIconButton(
                    onClick = { /* TODO show glass menu */ },
                    imageVector = Icons.Rounded.MoreVert
                )
            }
        )

        // Main Content (IndexedStack equivalent)
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Crossfade(targetState = selectedIndex, label = "Tab transition") { index ->
                when (index) {
                    0 -> DashboardScreen()
                    1 -> PeopleScreen()
                    2 -> SubscriptionsScreen()
                    3 -> TasksScreen()
                    4 -> HolidaysScreen()
                }
            }
        }

        // Bottom Bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
                .navigationBarsPadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            LiquidBottomTabs(
                selectedTabIndex = { selectedIndex },
                onTabSelected = { selectedIndex = it },
                tabsCount = 5,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                LiquidBottomTab(
                    onClick = { selectedIndex = 0 },
                    selected = selectedIndex == 0
                ) {
                    LiquidIcon(Icons.Rounded.Home, color = if (selectedIndex == 0) AppColors.accent500 else AppColors.textTertiary)
                }
                LiquidBottomTab(
                    onClick = { selectedIndex = 1 },
                    selected = selectedIndex == 1
                ) {
                    LiquidIcon(Icons.Rounded.Person, color = if (selectedIndex == 1) AppColors.accent500 else AppColors.textTertiary)
                }
                LiquidBottomTab(
                    onClick = { selectedIndex = 2 },
                    selected = selectedIndex == 2
                ) {
                    LiquidIcon(Icons.Rounded.CreditCard, color = if (selectedIndex == 2) AppColors.accent500 else AppColors.textTertiary)
                }
                LiquidBottomTab(
                    onClick = { selectedIndex = 3 },
                    selected = selectedIndex == 3
                ) {
                    LiquidIcon(Icons.Rounded.Checklist, color = if (selectedIndex == 3) AppColors.accent500 else AppColors.textTertiary)
                }
                LiquidBottomTab(
                    onClick = { selectedIndex = 4 },
                    selected = selectedIndex == 4
                ) {
                    LiquidIcon(Icons.Rounded.Cake, color = if (selectedIndex == 4) AppColors.accent500 else AppColors.textTertiary)
                }
            }
        }

        // FAB
        if (selectedIndex != 4) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 16.dp, bottom = 100.dp) // Above BottomBar
                    .navigationBarsPadding(),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingGlassContainer(
                    borderRadius = 28.dp,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { /* TODO open add flow */ },
                        contentAlignment = Alignment.Center
                    ) {
                        val fabIcon = when (selectedIndex) {
                            1 -> Icons.Rounded.PersonAdd
                            2 -> Icons.Rounded.CreditCard
                            3 -> Icons.Rounded.AddTask
                            else -> Icons.Rounded.Add
                        }
                        LiquidIcon(imageVector = fabIcon, color = AppColors.accent500)
                    }
                }
            }
        }
    }
}
