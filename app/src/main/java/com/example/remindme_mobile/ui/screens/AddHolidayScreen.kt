package com.example.remindme_mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remindme_mobile.ui.components.liquid.LiquidScaffold
import com.example.remindme_mobile.ui.components.liquid.LiquidAppBar
import com.example.remindme_mobile.ui.theme.AppColors

@Composable
fun AddHolidayScreen(
    onNavigateBack: () -> Unit
) {
    LiquidScaffold(
        topBar = {
            LiquidAppBar(
                title = "Add Holiday",
                onNavIconClick = onNavigateBack,
                navIcon = "back"
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Add Holiday Form (Not yet implemented)",
                color = AppColors.textSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
