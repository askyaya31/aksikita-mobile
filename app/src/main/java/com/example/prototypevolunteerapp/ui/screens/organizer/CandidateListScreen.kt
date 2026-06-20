package com.example.prototypevolunteerapp.ui.screens.organizer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.prototypevolunteerapp.core.Routes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.data.remote.dto.RegistrationDto
import com.example.prototypevolunteerapp.ui.components.AppFooter

private val NavyDark       = Color(0xFF1E3A8A)
private val PrimaryBlue    = Color(0xFF3B82F6)
private val MediumBlue     = Color(0xFF60A5FA)
private val LightBlue      = Color(0xFF93C5FD)
private val PalestBlue     = Color(0xFFBFDBFE)
private val BgColor        = Color(0xFFF0F5FF)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextDark       = Color(0xFF0F172A)
private val TextMuted      = Color(0xFF64748B)
private val DeclineRed     = Color(0xFFEF4444)
private val AcceptGreen    = Color(0xFF16A34A)
private val AccentBlue     = PrimaryBlue
private val PresentBlue    = PrimaryBlue

private val StatusAccepted = Triple(Color(0xFF16A34A), Color(0xFFDCFCE7), "Diterima")
private val StatusRejected = Triple(Color(0xFFDC2626), Color(0xFFFEE2E2), "Ditolak")
private val StatusAttended = Triple(Color(0xFF1A4D7A), Color(0xFFDBEAFE), "Hadir")
private val StatusPending  = Triple(Color(0xFFB45309), Color(0xFFFEF3C7), "Pending")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateListScreen(
    eventId: Int? = null,
    initialFilter: String = "Semua",
    viewModel: CandidateListViewModel = hiltViewModel()
) {
    LaunchedEffect(eventId, initialFilter) {
        viewModel.loadEvents(initialEventId = eventId, initialFilter = initialFilter)
    }

    val backStack    = LocalBackStack.current
    val snackbarHost = remember { SnackbarHostState() }
    val uiState      by viewModel.uiState.collectAsState()
    var accessDenied by remember { mutableStateOf(!viewModel.checkAccess()) }

    if (accessDenied) {
        AlertDialog(
            onDismissRequest = { backStack.removeLastOrNull() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Lock, null, tint = Color(0xFFCC0000))
                    Text("Akses Ditolak", fontWeight = FontWeight.Bold)
                }
            },
            text = { Text("Anda tidak memiliki hak akses.\nSilakan login sebagai organisasi.") },
            confirmButton = {
                Button(
                    onClick = { accessDenied = false; backStack.removeLastOrNull() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC0000))
                ) { Text("Kembali") }
            }
        )
    }

    val sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showEventSheet   = uiState.showEventSheet
    val selectedActivity = if (uiState.selectedEvent == null) "Semua Kegiatan"
    else uiState.selectedEvent?.title ?: ""

    if (showEventSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEventSheetDismiss() },
            sheetState = sheetState,
            containerColor = CardWhite,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)
            ) {
                Box(
                    modifier = Modifier.width(40.dp).height(4.dp)
                        .background(Color(0xFFDDDDDD), RoundedCornerShape(50.dp))
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(12.dp))
                Text("Pilih Kegiatan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                Text("${uiState.events.size} kegiatan tersedia", fontSize = 12.sp, color = TextMuted)
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    item(key = "all_events") {
                        val isAllSelected = uiState.selectedEvent == null
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAllSelected) PalestBlue else BgColor
                            ),
                            onClick = { viewModel.onEventSelected(null) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Semua Kegiatan",
                                        fontSize = 13.sp,
                                        color = if (isAllSelected) PresentBlue else TextDark,
                                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text("Lihat pendaftar dari semua kegiatan", fontSize = 10.sp, color = TextMuted)
                                }
                                if (isAllSelected) Icon(Icons.Default.CheckCircle, null, tint = PresentBlue, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    items(items = uiState.events, key = { it.id }) { event ->
                        val isSelected = event.id == uiState.selectedEvent?.id
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) PalestBlue else BgColor
                            ),
                            onClick = { viewModel.onEventSelected(event) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        event.title,
                                        fontSize = 13.sp,
                                        color = if (isSelected) PresentBlue else TextDark,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                    Text(event.status ?: "", fontSize = 10.sp, color = TextMuted)
                                }
                                if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = PresentBlue, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    val filterOptions      = listOf("Semua", "Pending", "Diterima", "Ditolak", "Hadir")
    val selectedFilter     = uiState.selectedFilter
    val filteredCandidates = uiState.filteredRegistrations

    LaunchedEffect(uiState.actionSuccess) {
        uiState.actionSuccess?.let { snackbarHost.showSnackbar(it); viewModel.onActionSuccessHandled() }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHost.showSnackbar(it); viewModel.onErrorDismissed() }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHost) },
        containerColor = BgColor
    ) { _ ->
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(NavyDark, PrimaryBlue)))
                    .padding(horizontal = 16.dp)
                    .padding(
                        top    = WindowInsets.statusBars.asPaddingValues()
                            .calculateTopPadding() + 8.dp,
                        bottom = 20.dp
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { backStack.removeLastOrNull() }
                        .align(Alignment.CenterStart),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint     = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(
                    modifier            = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Volunteer Waiting",
                        color      = Color.White,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Change their status",
                        color    = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item(key = "event_picker") {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(2.dp),
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
                                Icon(Icons.Default.Event, null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                                Text(
                                    selectedActivity.ifEmpty { "Pilih kegiatan" },
                                    fontSize = 13.sp,
                                    color = TextDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(Icons.Default.ExpandMore, null, tint = Color(0xFFAAAAAA), modifier = Modifier.size(20.dp))
                        }
                    }
                }

                item(key = "filter_row") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        items(items = filterOptions, key = { "filter_$it" }) { filter ->
                            val isActive = filter == selectedFilter
                            FilterChip(
                                selected = isActive,
                                onClick  = { viewModel.onFilterSelected(filter) },
                                label    = {
                                    Text(
                                        filter,
                                        fontSize = 12.sp,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor  = NavyDark,
                                    selectedLabelColor      = Color.White,
                                    containerColor          = Color.White,
                                    labelColor              = NavyDark
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled        = true,
                                    selected       = isActive,
                                    borderColor    = NavyDark.copy(alpha = 0.4f),
                                    selectedBorderColor = Color.Transparent,
                                    borderWidth    = 1.dp,
                                    selectedBorderWidth = 0.dp
                                )
                            )
                        }
                    }
                }

                if (filteredCandidates.isEmpty()) {
                    item(key = "empty") {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PersonOff, null, tint = Color(0xFFBBBBBB), modifier = Modifier.size(56.dp))
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    if (selectedFilter == "Semua") "Belum ada kandidat"
                                    else "Tidak ada kandidat \"$selectedFilter\"",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(items = filteredCandidates, key = { it.id }) { reg ->
                        val eventTitle = reg.event?.title ?: "Nama Kegiatan"

                        VolunteerWaitingCard(
                            reg        = reg,
                            eventTitle = eventTitle,
                            onConfirm  = { viewModel.onConfirmRegistration(reg.id) },
                            onReject   = { viewModel.onRejectRegistration(reg.id) },
                            onDetail   = { backStack.add(Routes.CandidateDetailRoute(candidateId = reg.id)) }
                        )
                    }
                }

                item(key = "footer") { AppFooter() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VolunteerWaitingCard(
    reg: RegistrationDto,
    eventTitle: String,
    onConfirm: () -> Unit,
    onReject:  () -> Unit,
    onDetail:  () -> Unit
) {
    val user    = reg.user
    val profile = user?.volunteer_profile

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

                val avatarUrl = profile?.avatar ?: user?.avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .shadow(1.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Text(
                            user?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = user?.name ?: "Volunteer",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        val (sc, sb, st) = statusMeta(reg.status)
                        Surface(shape = RoundedCornerShape(6.dp), color = sb) {
                            Text(
                                st,
                                fontSize = 10.sp,
                                color = sc,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Event, null, tint = AccentBlue, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = eventTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.School, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = profile?.city ?: "Asal Daerah / Institusi Kosong",
                            fontSize = 11.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onDetail,
                    modifier = Modifier.size(32.dp).background(AccentBlue.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Info, null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                }
            }
            if (reg.status == "pending") {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DeclineRed),
                        border = BorderStroke(1.dp, DeclineRed.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) { Text("Decline", fontSize = 12.sp, fontWeight = FontWeight.Bold) }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AcceptGreen),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) { Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

private fun statusMeta(status: String): Triple<Color, Color, String> = when (status) {
    "confirmed" -> StatusAccepted
    "cancelled" -> StatusRejected
    "attended"  -> StatusAttended
    else        -> StatusPending
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, tint = AccentBlue, modifier = Modifier.size(16.dp))
        Column {
            Text(label, fontSize = 10.sp, color = TextMuted)
            Text(value, fontSize = 13.sp, color = TextDark, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SheetSectionTitle(title: String) {
    Text(
        title.uppercase(),
        fontSize      = 11.sp,
        fontWeight    = FontWeight.SemiBold,
        color         = TextMuted,
        letterSpacing = 0.8.sp
    )
}

@Composable
private fun SheetInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(34.dp)
                .background(AccentBlue.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = AccentBlue, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 10.sp, color = TextMuted)
            Text(value, fontSize = 13.sp, color = TextDark,
                fontWeight = FontWeight.Medium, lineHeight = 18.sp)
        }
    }
}