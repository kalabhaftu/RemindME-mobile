package com.remindme.app.ui.screens.main

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
import com.remindme.app.ui.components.liquid.LiquidBottomTab
import com.remindme.app.ui.components.liquid.LiquidBottomTabs
import com.remindme.app.ui.components.liquid.LiquidIcon
import com.remindme.app.ui.components.liquid.LiquidScaffold
import com.remindme.app.ui.navigation.AddPerson
import com.remindme.app.ui.navigation.AddSubscription
import com.remindme.app.ui.navigation.AddTask
import com.remindme.app.ui.screens.dashboard.DashboardScreen
import com.remindme.app.ui.screens.holidays.HolidaysScreen
import com.remindme.app.ui.screens.people.PeopleScreen
import com.remindme.app.ui.screens.subscriptions.SubscriptionsScreen
import com.remindme.app.ui.screens.tasks.TasksScreen
import com.remindme.app.ui.theme.*

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
                        color = if (selectedTab == 0) Accent500 else TextPrimary
                    )
                }
                LiquidBottomTab(
                    onClick = { selectedTab = 1 },
                    selected = selectedTab == 1
                ) {
                    LiquidIcon(
                        imageVector = Icons.Default.People,
                        color = if (selectedTab == 1) Accent500 else TextPrimary
                    )
                }
                LiquidBottomTab(
                    onClick = { selectedTab = 2 },
                    selected = selectedTab == 2
                ) {
                    LiquidIcon(
                        imageVector = Icons.Default.CreditCard,
                        color = if (selectedTab == 2) Accent500 else TextPrimary
                    )
                }
                LiquidBottomTab(
                    onClick = { selectedTab = 3 },
                    selected = selectedTab == 3
                ) {
                    LiquidIcon(
                        imageVector = Icons.Default.CheckCircle,
                        color = if (selectedTab == 3) Accent500 else TextPrimary
                    )
                }
                LiquidBottomTab(
                    onClick = { selectedTab = 4 },
                    selected = selectedTab == 4
                ) {
                    LiquidIcon(
                        imageVector = Icons.Default.Event,
                        color = if (selectedTab == 4) Accent500 else TextPrimary
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
