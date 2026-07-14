package com.example.remindme_mobile.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.remindme_mobile.ui.screens.main.MainScreen
import com.example.remindme_mobile.ui.screens.add.AddPersonScreen
import com.example.remindme_mobile.ui.screens.add.AddSubscriptionScreen
import com.example.remindme_mobile.ui.screens.add.AddTaskScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Main)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Main> {
          MainScreen(onItemClick = { navKey -> backStack.add(navKey) }, modifier = Modifier.safeDrawingPadding().padding(16.dp))
        }
        entry<AddPerson> {
          AddPersonScreen(onBack = { backStack.removeLastOrNull() })
        }
        entry<AddSubscription> {
          AddSubscriptionScreen(onBack = { backStack.removeLastOrNull() })
        }
        entry<AddTask> {
          AddTaskScreen(onBack = { backStack.removeLastOrNull() })
        }
      },
  )
}
