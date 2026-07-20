package com.remindme.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.remindme.app.ui.theme.BgSurface3

private val SkeletonTint = Color.White.copy(alpha = 0.08f)

@Composable
private fun SkeletonLine(modifier: Modifier) {
    Spacer(modifier.clip(RoundedCornerShape(8.dp)).background(SkeletonTint))
}

@Composable
fun PeopleContentSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(4) {
            AppCard(borderRadius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(14.dp)) {
                    SkeletonLine(Modifier.size(36.dp))
                    Column(modifier = Modifier.padding(start = 12.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SkeletonLine(Modifier.fillMaxWidth(0.55f).height(14.dp))
                        SkeletonLine(Modifier.fillMaxWidth(0.3f).height(10.dp))
                    }
                    SkeletonLine(Modifier.size(width = 62.dp, height = 28.dp))
                }
            }
        }
    }
}

@Composable
fun ReminderListContentSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(4) {
            AppCard(borderRadius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    SkeletonLine(Modifier.fillMaxWidth(0.62f).height(15.dp))
                    SkeletonLine(Modifier.fillMaxWidth(0.38f).height(11.dp))
                    SkeletonLine(Modifier.fillMaxWidth(0.8f).height(8.dp))
                }
            }
        }
    }
}

@Composable
fun NotificationContentSkeleton() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(5) {
            AppCard(borderRadius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(14.dp)) {
                    SkeletonLine(Modifier.size(10.dp))
                    Column(modifier = Modifier.padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SkeletonLine(Modifier.fillMaxWidth(0.72f).height(14.dp))
                        SkeletonLine(Modifier.fillMaxWidth(0.48f).height(10.dp))
                    }
                }
            }
        }
    }
}
