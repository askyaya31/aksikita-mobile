package com.example.prototypevolunteerapp.ui.screens.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.launch

private val HeaderBgTop    = Color(0xFF86B8FF)
private val HeaderBgMiddle = Color(0xFF5B9BD5)
private val HeaderBgBottom = Color(0xFFCBE2FF)
private val BgColor        = Color(0xFFF5F7FF)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextDark       = Color(0xFF1A1A2E)
private val TextMuted      = Color(0xFF777799)
private val DeclineRed     = Color(0xFFFF4D4D)
private val AcceptGreen    = Color(0xFF16A34A)
private val AccentBlue     = Color(0xFF5B9BD5)
private val PresentBlue    = Color(0xFF1A4D7A)
private val CardBgLightBlue= Color(0xFFEAF2FF)

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
                                containerColor = if (isAllSelected) Color(0xFFDBEAFE) else BgColor
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
                                containerColor = if (isSelected) Color(0xFFDBEAFE) else BgColor
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
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Volunteer Waiting", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        Text("Change their status", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.background(Brush.linearGradient(listOf(HeaderBgMiddle, HeaderBgTop)))
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0.0f to HeaderBgMiddle,
                    0.25f to HeaderBgBottom,
                    0.4f to BgColor,
                    1.0f to BgColor
                )
            )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
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

                item(key = "summary") {
                    val pending  = uiState.allRegistrations.count { it.status == "pending" }
                    val accepted = uiState.allRegistrations.count { it.status == "confirmed" }
                    val rejected = uiState.allRegistrations.count { it.status == "cancelled" }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryChip("Pending",  pending,  StatusPending.first,  StatusPending.second,  Modifier.weight(1f))
                        SummaryChip("Diterima", accepted, StatusAccepted.first, StatusAccepted.second, Modifier.weight(1f))
                        SummaryChip("Ditolak",  rejected, StatusRejected.first, StatusRejected.second, Modifier.weight(1f))
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
                                    selectedContainerColor = AccentBlue,
                                    selectedLabelColor = Color.White
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
                            onAttend   = { viewModel.onAttendRegistration(reg.id) }
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
    onAttend:  () -> Unit
) {
    val user    = reg.user
    val profile = user?.volunteer_profile
    var showDetail by remember { mutableStateOf(false) }

    if (showDetail) {
        ModalBottomSheet(
            onDismissRequest = { showDetail = false },
            containerColor   = CardWhite,
            shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Detail Relawan", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                HorizontalDivider(color = Color(0xFFEEEEEE))

                val location = profile?.city ?: profile?.province ?: "-"
                DetailRow(Icons.Default.LocationOn,   "Asal Kota", location)
                DetailRow(Icons.Default.Phone,        "Telepon",   user?.phone ?: "-")
                DetailRow(Icons.Default.CalendarToday,"Tgl Lahir", profile?.date_of_birth ?: "-")

                if (!profile?.bio.isNullOrBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgColor, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text("Bio", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                        Text(profile!!.bio!!, fontSize = 13.sp, color = TextDark)
                    }
                }
            }
        }
    }

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBgLightBlue),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // ── Avatar ──
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
                    Text(
                        text = user?.name ?: "Volunteer",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
                    onClick = { showDetail = true },
                    modifier = Modifier.size(32.dp).background(AccentBlue.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Info, null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (sc, sb, st) = statusMeta(reg.status)
                Surface(shape = RoundedCornerShape(8.dp), color = sb) {
                    Text(
                        st,
                        fontSize = 11.sp,
                        color = sc,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.End) {
                    when (reg.status) {
                        "pending" -> {
                            OutlinedButton(
                                onClick = onReject,
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DeclineRed),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DeclineRed.copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) { Text("Decline", fontSize = 12.sp, fontWeight = FontWeight.Bold) }

                            Spacer(Modifier.width(8.dp))

                            Button(
                                onClick = onConfirm,
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AcceptGreen),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) { Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        }
                        "confirmed" -> {
                            Button(
                                onClick = onAttend,
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PresentBlue),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) { Text("Tandai Hadir", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
    }
}

// Helpers
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
private fun SummaryChip(label: String, count: Int, textColor: Color, bgColor: Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$count", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
            Text(label, fontSize = 11.sp, color = textColor)
        }
    }
}