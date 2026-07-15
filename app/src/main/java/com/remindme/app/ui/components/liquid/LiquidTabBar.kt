package com.remindme.app.ui.components.liquid

import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.remindme.app.ui.theme.AppColors

@Composable
fun LiquidTabBar(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Color.Transparent,
        contentColor = AppColors.accent500,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                color = AppColors.accent500
            )
        },
        divider = {}
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (selectedIndex == index) AppColors.accent500 else AppColors.textSecondary
                    )
                }
            )
        }
    }
}
