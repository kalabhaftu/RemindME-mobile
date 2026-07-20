package com.remindme.app.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import com.remindme.app.ui.components.AppCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.NavKey
import com.remindme.app.ui.components.NavTab
import com.remindme.app.ui.components.NavTabs
import com.remindme.app.ui.components.AppIcon
import com.remindme.app.ui.components.AppIcons
import com.remindme.app.ui.components.AppScaffold
import com.remindme.app.ui.navigation.AddPerson
import com.remindme.app.ui.navigation.AddSubscription
import com.remindme.app.ui.navigation.AddTask
import com.remindme.app.ui.navigation.EditReminder
import com.remindme.app.ui.navigation.PersonDetail
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
    var showQuickAdd by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val tabTitles = listOf("RemindME", "People", "Subscriptions", "Tasks", "Holidays")

    AppScaffold(
        appBar = {
            com.remindme.app.ui.components.TopBar(
                title = tabTitles[selectedTab],
                actions = {
                    androidx.compose.foundation.layout.Box {
                        androidx.compose.material3.IconButton(onClick = { showMenu = true }) {
                            AppIcon(iconRes = AppIcons.MoreVert, color = AppColors.textPrimary)
                        }
                        com.remindme.app.ui.components.PopupMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            offset = DpOffset(0.dp, 4.dp)
                        ) {
                            com.remindme.app.ui.components.PopupMenuItem(
                                text = "Search",
                                onClick = { showMenu = false; onItemClick(com.remindme.app.ui.navigation.Search) },
                                icon = AppIcons.Search
                            )
                            com.remindme.app.ui.components.PopupMenuItem(
                                text = "Templates",
                                onClick = { showMenu = false; onItemClick(com.remindme.app.ui.navigation.Templates) },
                                icon = AppIcons.Book
                            )
                            com.remindme.app.ui.components.PopupMenuItem(
                                text = "Notifications",
                                onClick = { showMenu = false; onItemClick(com.remindme.app.ui.navigation.Notifications) },
                                icon = AppIcons.Notifications
                            )
                            com.remindme.app.ui.components.PopupMenuItem(
                                text = "Settings",
                                onClick = { showMenu = false; onItemClick(com.remindme.app.ui.navigation.Settings) },
                                icon = AppIcons.Settings
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavTabs(
                selectedTabIndex = { selectedTab },
                onTabSelected = { selectedTab = it },
                tabsCount = 5
            ) {
                NavTab(
                    onClick = { selectedTab = 0 },
                    selected = selectedTab == 0
                ) {
                    AppIcon(
                        iconRes = AppIcons.Home,
                        color = if (selectedTab == 0) Accent500 else TextPrimary
                    )
                }
                NavTab(
                    onClick = { selectedTab = 1 },
                    selected = selectedTab == 1
                ) {
                    AppIcon(
                        iconRes = AppIcons.People,
                        color = if (selectedTab == 1) Accent500 else TextPrimary
                    )
                }
                NavTab(
                    onClick = { selectedTab = 2 },
                    selected = selectedTab == 2
                ) {
                    AppIcon(
                        iconRes = AppIcons.CreditCard,
                        color = if (selectedTab == 2) Accent500 else TextPrimary
                    )
                }
                NavTab(
                    onClick = { selectedTab = 3 },
                    selected = selectedTab == 3
                ) {
                    AppIcon(
                        iconRes = AppIcons.CheckCircle,
                        color = if (selectedTab == 3) Accent500 else TextPrimary
                    )
                }
                NavTab(
                    onClick = { selectedTab = 4 },
                    selected = selectedTab == 4
                ) {
                    AppIcon(
                        iconRes = AppIcons.CalendarToday,
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
                1 -> PeopleScreen(onNavigateToDetail = { onItemClick(PersonDetail(it)) })
                2 -> SubscriptionsScreen(onNavigateToEdit = { onItemClick(EditReminder(it)) })
                3 -> TasksScreen(onNavigateToEdit = { onItemClick(EditReminder(it)) })
                4 -> HolidaysScreen()
            }
            
            // FAB — hidden on Holidays tab (tab 4, nothing to add)
            if (selectedTab != 4) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 110.dp, end = 24.dp)
                ) {
                    AppCard(
                        borderRadius = 50.dp,
                        elevated = true,
                        modifier = Modifier
                            .size(56.dp)
                            .clickable {
                                when (selectedTab) {
                                    0 -> showQuickAdd = true
                                    1 -> onItemClick(AddPerson)
                                    2 -> onItemClick(AddSubscription)
                                    3 -> onItemClick(AddTask)
                                }
                            }
                    ) {
                        // Contextually correct icon per tab
                        val fabIcon = when (selectedTab) {
                            1 -> AppIcons.UserRoundPlus
                            2 -> AppIcons.CreditCard
                            3 -> AppIcons.NoteAdd
                            else -> AppIcons.Add
                        }
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            AppIcon(
                                iconRes = fabIcon,
                                color = Color.White,
                                size = 26.dp
                            )
                        }
                    }
                }
            }
        }
        
        if (showQuickAdd) {
            QuickAddSheet(
                onDismiss = { showQuickAdd = false },
                onNavigateToAddPerson = { onItemClick(AddPerson) },
                onNavigateToAddSubscription = { onItemClick(AddSubscription) },
                onNavigateToAddTask = { onItemClick(AddTask) },
                onNavigateToAddHoliday = { selectedTab = 4 }
            )
        }
    }
}
