package com.example.remindme_mobile.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import com.example.remindme_mobile.ui.components.liquid.LiquidBottomTab
import com.example.remindme_mobile.ui.components.liquid.LiquidBottomTabs
import com.example.remindme_mobile.ui.components.liquid.LiquidIcon
import com.example.remindme_mobile.ui.components.liquid.LiquidScaffold
import com.example.remindme_mobile.ui.navigation.AddPerson
import com.example.remindme_mobile.ui.navigation.AddSubscription
import com.example.remindme_mobile.ui.navigation.AddTask
import com.example.remindme_mobile.ui.screens.dashboard.DashboardScreen
import com.example.remindme_mobile.ui.screens.holidays.HolidaysScreen
import com.example.remindme_mobile.ui.screens.people.PeopleScreen
import com.example.remindme_mobile.ui.screens.subscriptions.SubscriptionsScreen
import com.example.remindme_mobile.ui.screens.tasks.TasksScreen
import com.example.remindme_mobile.ui.theme.AppColors
import com.example.remindme_mobile.ui.theme.TextPrimary

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    LiquidScaffold(
        bottomBar = {
            LiquidBottomTabs(
                selectedTabIndex = { selectedTab },
                onTabSelected = { selectedTab = it },
                tabsCount = 5
            ) {
                LiquidBottomTab(
                    onClick = { selectedTab = 0 },
                    selected = selectedTab == 0
                ) {
                    LiquidIcon(
                        imageVector = Icons.Default.Home,
                        tint = if (selectedTab == 0) AppColors.accent500 else TextPrimary
                    )
                }
                LiquidBottomTab(
                    onClick = { selectedTab = 1 },
                    selected = selectedTab == 1
                ) {
                    LiquidIcon(
                        imageVector = Icons.Default.People,
                        tint = if (selectedTab == 1) AppColors.accent500 else TextPrimary
                    )
                }
                LiquidBottomTab(
                    onClick = { selectedTab = 2 },
                    selected = selectedTab == 2
                ) {
                    LiquidIcon(
                        imageVector = Icons.Default.CreditCard,
                        tint = if (selectedTab == 2) AppColors.accent500 else TextPrimary
                    )
                }
                LiquidBottomTab(
                    onClick = { selectedTab = 3 },
                    selected = selectedTab == 3
                ) {
                    LiquidIcon(
                        imageVector = Icons.Default.CheckCircle,
                        tint = if (selectedTab == 3) AppColors.accent500 else TextPrimary
                    )
                }
                LiquidBottomTab(
                    onClick = { selectedTab = 4 },
                    selected = selectedTab == 4
                ) {
                    LiquidIcon(
                        imageVector = Icons.Default.Event,
                        tint = if (selectedTab == 4) AppColors.accent500 else TextPrimary
                    )
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    onNavigateToAddPerson = { onItemClick(AddPerson) },
                    onNavigateToAddSubscription = { onItemClick(AddSubscription) },
                    onNavigateToAddTask = { onItemClick(AddTask) },
                    onNavigateToHolidays = { selectedTab = 4 }
                )
                1 -> PeopleScreen()
                2 -> SubscriptionsScreen()
                3 -> TasksScreen()
                4 -> HolidaysScreen()
            }
        }
    }
}
