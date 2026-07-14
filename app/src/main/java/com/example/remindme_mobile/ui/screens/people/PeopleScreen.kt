package com.example.remindme_mobile.ui.screens.people

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remindme_mobile.ui.components.liquid.FloatingGlassContainer
import com.example.remindme_mobile.ui.components.liquid.LiquidSpinner
import com.example.remindme_mobile.ui.theme.TextPrimary

@Composable
fun PeopleScreen(
    viewModel: PeopleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            LiquidSpinner()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 140.dp, bottom = 120.dp, start = 16.dp, end = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Search Input
            FloatingGlassContainer(borderRadius = 12.dp, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Search placeholder", color = TextPrimary)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Sort Chips
            FloatingGlassContainer(borderRadius = 12.dp, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Sort Chips placeholder", color = TextPrimary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(uiState.people) { person ->
            FloatingGlassContainer(
                borderRadius = 16.dp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text(text = person.name, color = TextPrimary)
                }
            }
        }
    }
}
