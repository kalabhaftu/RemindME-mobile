package com.remindme.app.ui.screens.people

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.remindme.app.R
import com.remindme.app.domain.models.ReminderItem
import com.remindme.app.ui.components.EmptyState
import com.remindme.app.ui.components.liquid.*
import com.remindme.app.ui.theme.*
import com.remindme.app.ui.screens.people.PeopleSort
import com.remindme.app.ui.screens.people.PeopleViewModel
import com.remindme.app.utils.AppConstants
import com.remindme.app.utils.ComputedFields
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(
    viewModel: PeopleViewModel = viewModel(),
    onNavigateToDetail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val people by viewModel.filteredPeople.collectAsState()
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    if (uiState.isLoading && people.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
            LiquidTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                placeholder = "Search people...",
                prefixIcon = { LiquidIcon(imageVector = Icons.Default.Search) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiquidFilterChip(
                    label = "Closest",
                    selected = uiState.sort == PeopleSort.DAYS_ASC,
                    onSelected = { viewModel.updateSort(PeopleSort.DAYS_ASC) }
                )
                LiquidFilterChip(
                    label = "A–Z",
                    selected = uiState.sort == PeopleSort.NAME_ASC,
                    onSelected = { viewModel.updateSort(PeopleSort.NAME_ASC) }
                )
                LiquidFilterChip(
                    label = "Age",
                    selected = uiState.sort == PeopleSort.AGE_DESC,
                    onSelected = { viewModel.updateSort(PeopleSort.AGE_DESC) }
                )
                LiquidFilterChip(
                    label = "Recent",
                    selected = uiState.sort == PeopleSort.RECENT,
                    onSelected = { viewModel.updateSort(PeopleSort.RECENT) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (people.isEmpty() && !uiState.isLoading) {
            item {
                Box(modifier = Modifier.padding(32.dp)) {
                    EmptyState(
                        iconRes = R.drawable.empty_people,
                        message = "No people yet. Tap + to add someone."
                    )
                }
            }
        } else {
            item {
                FloatingGlassContainer(
                    borderRadius = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("NAME", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextTertiary, letterSpacing = 0.5.sp), modifier = Modifier.weight(3f))
                        Text("AGE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextTertiary, letterSpacing = 0.5.sp), modifier = Modifier.weight(1f))
                        Text("DAYS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextTertiary, letterSpacing = 0.5.sp), modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(items = people, key = { it.id }) { person ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.deletePerson(person.id)
                            true
                        } else {
                            false
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(StateDanger)
                                .padding(end = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    },
                    content = {
                        PersonRow(person = person, onClick = { onNavigateToDetail(person.id) })
                    }
                )
            }
        }
    }
}

@Composable
fun PersonRow(person: ReminderItem, onClick: () -> Unit) {
    val bdStr = person.personDetails?.get("birthdate")?.toString()
    val birthdate = bdStr?.takeIf { it.isNotBlank() }?.let {
        try { LocalDate.parse(it.substring(0, 10)) } catch (e: Exception) { null }
    }
    val age = birthdate?.let { ComputedFields.calculateAge(it) } ?: 0
    val days = birthdate?.let { ComputedFields.calculateDaysToBirthday(it) } ?: 9999
    val zodiac = birthdate?.let { ComputedFields.getZodiacSign(it) } ?: "Unknown"
    val gender = person.personDetails?.get("gender")?.toString() ?: "unspecified"
    val relationship = person.personDetails?.get("relationship")?.toString() ?: "other"
    val relPair = AppConstants.RELATIONSHIP_LABELS[relationship]
    val glyph = AppConstants.ZODIAC_GLYPHS[zodiac] ?: "★"

    FloatingGlassContainer(
        borderRadius = 16.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.weight(3f), verticalAlignment = Alignment.CenterVertically) {
                    LiquidIcon(imageVector = Icons.Default.Person, size = 18.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = person.name,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = if (birthdate != null) "$age" else "—",
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                Box(modifier = Modifier.weight(1f)) {
                    FloatingGlassContainer(
                        borderRadius = 6.dp,
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Text(
                            text = if (birthdate != null) "${days} d" else "—",
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Pill(AppConstants.GENDER_LABELS[gender] ?: gender)
                Pill("$glyph $zodiac")
                Pill("${relPair?.second ?: ""} ${relPair?.first ?: relationship}")
                if (birthdate != null) {
                    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
                    Pill(birthdate.format(formatter))
                } else {
                    Pill("No birthdate")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            
            val progress = (age / 100f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Accent500,
                trackColor = BgSurface1
            )
        }
    }
}

@Composable
fun Pill(text: String) {
    FloatingGlassContainer(
        borderRadius = 8.dp,
        modifier = Modifier.wrapContentSize()
    ) {
        Text(
            text = text,
            color = TextPrimary,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
