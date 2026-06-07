package com.example.prototypevolunteerapp.ui.screens.organizer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.example.prototypevolunteerapp.core.Routes
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.OrganizerSession
import com.example.prototypevolunteerapp.data.model.Candidate
import com.example.prototypevolunteerapp.data.model.CandidateStatus
import com.example.prototypevolunteerapp.ui.components.AppFooter
import com.example.prototypevolunteerapp.ui.components.CandidateRowCard
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.prototypevolunteerapp.ui.components.RegistrationRowCard

private val BgColor   = Color(0xFFF4F7EF)
private val CardWhite   = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateListScreen(eventId: Int? = null,viewModel: CandidateListViewModel = hiltViewModel()
) {
    LaunchedEffect(eventId) { viewModel.loadEvents(initialEventId = eventId) }
    val backStack    = LocalBackStack.current
    val snackbarHost = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()
    var accessDenied by remember { mutableStateOf(!viewModel.checkAccess()) }
    if (accessDenied) {
        AlertDialog(
            onDismissRequest = { backStack.removeLastOrNull() },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Lock, null, tint = Color(0xFFCC0000))
                    Text("Akses Ditolak", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "Anda tidak memiliki hak akses untuk melihat daftar kandidat.\n\n" +
                            "Silakan login terlebih dahulu sebagai organisasi."
                )
            },
            confirmButton = {
                Button(
                    onClick = { accessDenied = false; backStack.removeLastOrNull() },
                    colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC0000))
                ) { Text("Kembali") }
            }
        )
    }

    val allActivities    = uiState.events.map { it.title }
    val selectedActivity = uiState.selectedEvent?.title ?: ""
    val sheetState        = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showActivitySheet = uiState.showEventSheet


    if (showActivitySheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEventSheetDismiss() },
            sheetState = sheetState,
            containerColor = CardWhite,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp).height(4.dp)
                        .background(Color(0xFFDDDDDD), RoundedCornerShape(50.dp))
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Pilih Kegiatan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E2D1E))

                Text("${allActivities.size} kegiatan tersedia", fontSize = 12.sp, color = Color(0xFF6E8F6E))

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(color = Color(0xFFDAEFDC))
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(items = allActivities, key = { it }) { title ->
                        val isSelected = title == selectedActivity
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFD4EDCA) else Color(0xFFF4F7EF)
                            ),
                            onClick = { viewModel.onEventSelected(uiState.events.find { it.title == title } ?: return@Card)}
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    title,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color(0xFF2E5C1A) else Color(0xFF1E2D1E),
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E5C1A), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val filterOptions      = listOf("Semua", "Pending", "Diterima", "Ditolak")
    val selectedFilter     = uiState.selectedFilter
    val filteredCandidates = uiState.filteredRegistrations

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Daftar Kandidat", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E2D1E))
                },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF1E2D1E))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor)
            )
        },
        containerColor = BgColor
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
        ) {
            item(key = "activity_picker") {
                Text(
                    "Kegiatan Aktif", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6E8F6E), modifier = Modifier.padding(bottom = 4.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(1.dp),
                    onClick = { viewModel.onEventSheetShow() }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Event, null, tint = Color(0xFF5A7A5A), modifier = Modifier.size(18.dp))
                            Text(
                                selectedActivity.ifEmpty { "Pilih kegiatan" },
                                fontSize = 13.sp,
                                color = if (selectedActivity.isEmpty()) Color(0xFFAAAAAA) else Color(0xFF1E2D1E),
                                maxLines = 2
                            )
                        }
                        Icon(Icons.Default.ExpandMore, null, tint = Color(0xFFAAAAAA), modifier = Modifier.size(20.dp))
                    }
                }
            }
            item(key = "summary") {
                val pending  = uiState.allRegistrations.count { it.status == "pending" }
                val accepted = uiState.allRegistrations.count { it.status == "confirmed" }
                val rejected = uiState.allRegistrations.count { it.status == "cancelled" }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusSummaryChip("Pending",  pending,  Color(0xFF7A5C00), Color(0xFFFFF3CD), Modifier.weight(1f))
                    StatusSummaryChip("Diterima", accepted, Color(0xFF2E5C1A), Color(0xFFD4EDCA), Modifier.weight(1f))
                    StatusSummaryChip("Ditolak",  rejected, Color(0xFF8B0000), Color(0xFFFDE8E8), Modifier.weight(1f))
                }
            }

            item(key = "filter_row") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(items = filterOptions, key = { "filter_$it" }) { filter ->
                        val isActive = filter == selectedFilter
                        FilterChip(
                            selected = isActive,
                            onClick  = { viewModel.onFilterSelected(filter) },
                            label    = {
                                Text(filter, fontSize = 12.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF5A7A5A),
                                selectedLabelColor     = Color.White
                            )
                        )
                    }
                }
            }

            if (filteredCandidates.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PersonOff, null, tint = Color(0xFFBBBBBB), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (selectedFilter == "Semua") "Belum ada kandidat"
                                else "Tidak ada kandidat dengan status \"$selectedFilter\"",
                                color = Color(0xFFAAAAAA), fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(items = uiState.filteredRegistrations, key = { it.id }) { reg ->
                    val volunteer = reg.user
                    RegistrationRowCard(
                        name          = volunteer?.name  ?: "Volunteer",
                        email         = volunteer?.email ?: "",
                        status        = reg.status,
                        onConfirm     = { viewModel.onConfirmRegistration(reg.id) },
                        onReject      = { viewModel.onRejectRegistration(reg.id) },
                        onAttend      = { viewModel.onAttendRegistration(reg.id) },
                        onViewDetail  = { backStack.add(Routes.CandidateDetailRoute(reg.id)) }
                    )
                }
            }
            item(key = "footer") { AppFooter() }
        }
    }
}

@Composable
private fun StatusSummaryChip(label: String, count: Int, textColor: Color, bgColor: Color, modifier: Modifier) {
    Box(
        modifier = modifier.background(bgColor, RoundedCornerShape(10.dp)).padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$count", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
            Text(label, fontSize = 11.sp, color = textColor)
        }
    }
}