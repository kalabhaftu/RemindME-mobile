package com.remindme.app.ui.screens.peopledetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import android.app.Application
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.remindme.app.ui.components.*
import com.remindme.app.ui.components.SnackbarHost
import com.remindme.app.ui.theme.*
import com.remindme.app.utils.AppConstants
import com.remindme.app.utils.ComputedFields
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PersonDetailScreen(
    personId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: PersonDetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PersonDetailViewModel(personId, context.applicationContext as Application) as T
            }
        }
    )
    
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onBack()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    AppScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        appBar = {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircledBackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(12.dp))
                TopBar(
                    title = uiState.person?.name ?: "",
                    statusBarsPadding = false,
                    modifier = Modifier.weight(1f),
                    actions = {
                        IconButton(onClick = { onEdit(personId) }) {
                            AppIcon(iconRes = PremiumIcons.Edit, color = TextSecondary)
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            AppIcon(iconRes = PremiumIcons.Delete, color = StateDanger)
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            val person = uiState.person
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Spinner()
                }
            } else if (person != null) {
                    val bdStr = person.person?.birthdate
                    val birthdate = bdStr?.takeIf { it.isNotBlank() }?.let { 
                        try { LocalDate.parse(it.substring(0, 10)) } catch (e: Exception) { null } 
                    }
                    val age = birthdate?.let { ComputedFields.calculateAge(it) }
                    val days = birthdate?.let { ComputedFields.calculateDaysToBirthday(it) }
                    val zodiac = birthdate?.let { ComputedFields.getZodiacSign(it) }
                    val genderKey = person.person?.gender ?: "unspecified"
                    val relKey = person.person?.relationship ?: "other"
                    
                    val rel = AppConstants.RELATIONSHIP_LABELS[relKey]
                    val gender = AppConstants.GENDER_LABELS[genderKey]
                    val glyph = zodiac?.let { AppConstants.ZODIAC_GLYPHS[it] }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AppCard(
                                borderRadius = 32.dp,
                                modifier = Modifier.wrapContentSize()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .size(64.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(Accent500.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val avatarUrl = person.person?.avatarUrl
                                    if (!avatarUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = avatarUrl,
                                            contentDescription = "${person.name} avatar",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text(
                                            text = person.name.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull() }.joinToString("").uppercase(),
                                            color = Accent400,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(person.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                if (birthdate != null) {
                                    Text(
                                        birthdate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.heightIn(max = 300.dp),
                            userScrollEnabled = false
                        ) {
                            if (age != null) {
                                item { StatCard("AGE", "$age") }
                            }
                            if (days != null) {
                                item { StatCard("DAYS TO BIRTHDAY", "$days", valueColor = Accent500) }
                            }
                            if (zodiac != null && glyph != null) {
                                item { StatCard("ZODIAC", "$glyph $zodiac") }
                            }
                            if (rel != null) {
                                item { StatCard("RELATIONSHIP", "${rel.second} ${rel.first}") }
                            }
                        }

                        if (gender != null && gender != "—") {
                            Spacer(modifier = Modifier.height(24.dp))
                            AppCard(
                                borderRadius = 16.dp,
                                modifier = Modifier.wrapContentSize()
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    Text(gender, color = TextPrimary)
                                }
                            }
                        }

                        if (!person.notes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("NOTES", fontSize = 10.sp, color = TextTertiary, letterSpacing = 0.5.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(person.notes, fontSize = 14.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete ${uiState.person?.name}?") },
                text = { Text("Are you sure you want to delete this person?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteConfirm = false
                        viewModel.deletePerson()
                    }) {
                        Text("Delete", color = StateDanger)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = BgElevated,
                titleContentColor = TextPrimary,
                textContentColor = TextSecondary
            )
        }
    }
}

@Composable
fun StatCard(label: String, value: String, valueColor: Color = TextPrimary) {
    AppCard(
        borderRadius = 16.dp,
        modifier = Modifier.fillMaxWidth().aspectRatio(2.5f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, fontSize = 10.sp, color = TextTertiary, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = valueColor)
        }
    }
}
