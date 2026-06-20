package com.example.prototypevolunteerapp.ui.screens.organizer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.prototypevolunteerapp.core.DateUtils
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.Routes
import com.example.prototypevolunteerapp.data.remote.dto.RegistrationDto
import com.example.prototypevolunteerapp.ui.components.AppFooter

private val NavyDark      = Color(0xFF4468CC)
private val PrimaryBlue   = Color(0xFF3B82F6)
private val MediumBlue    = Color(0xFF60A5FA)
private val BluePale      = Color(0xFFBFDBFE)
private val BgColor       = Color(0xFFF0F5FF)
private val CardWhite     = Color(0xFFFFFFFF)
private val TextDark      = Color(0xFF0F172A)
private val TextMuted     = Color(0xFF64748B)
private val DeclineRed    = Color(0xFFEF4444)
private val AcceptGreen   = Color(0xFF16A34A)

private val StatusPendingText   = Color(0xFFB45309)
private val StatusPendingBg     = Color(0xFFFEF3C7)
private val StatusActiveText    = Color(0xFF16A34A)
private val StatusActiveBg      = Color(0xFFDCFCE7)
private val StatusRejectedText  = Color(0xFFDC2626)
private val StatusRejectedBg    = Color(0xFFFEE2E2)
private val StatusCompletedText = Color(0xFF059669)
private val StatusCompletedBg   = Color(0xFFD1FAE5)
private val StatusAttendedText  = Color(0xFF1A4D7A)
private val StatusAttendedBg    = Color(0xFFDBEAFE)

private const val VOLUNTEER_PREVIEW_COUNT = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId:   Int,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val backStack    = LocalBackStack.current
    val snackbarHost = remember { SnackbarHostState() }
    val uiState      by viewModel.uiState.collectAsState()
    var accessDenied by remember { mutableStateOf(!viewModel.checkAccess()) }
    var showAllVolunteers by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) { viewModel.loadData(eventId) }

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let { snackbarHost.showSnackbar(it); viewModel.onActionMessageHandled() }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHost.showSnackbar(it); viewModel.onErrorDismissed() }
    }

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
                    colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC0000))
                ) { Text("Kembali") }
            }
        )
    }

    val event = uiState.event

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Detail Kegiatan", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        Text(
                            event?.title ?: "Memuat...",
                            fontSize = 11.sp,
                            color    = Color.White.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Color.White)
                    }
                },
                colors   = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.background(Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF5393F8) )))
            )
        },
        containerColor = BgColor
    ) { innerPadding ->
        if (uiState.isLoading && event == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = PrimaryBlue) }
            return@Scaffold
        }

        if (event == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Kegiatan tidak ditemukan", color = TextMuted)
            }
            return@Scaffold
        }

        val displayedRegistrations = if (showAllVolunteers) {
            uiState.registrations
        } else {
            uiState.registrations.take(VOLUNTEER_PREVIEW_COUNT)
        }

        LazyColumn(
            modifier        = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding  = PaddingValues(bottom = 24.dp)
        ) {
            item(key = "header_section") {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (!event.poster.isNullOrBlank()) {
                        AsyncImage(
                            model              = event.poster,
                            contentDescription = event.title,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxWidth().height(180.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(Brush.linearGradient(listOf(NavyDark, PrimaryBlue))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Event, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(48.dp))
                        }
                    }

                    Card(
                        modifier = Modifier
                            .padding(top = 152.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth(),
                        shape     = RoundedCornerShape(14.dp),
                        colors    = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    event.title,
                                    fontSize   = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = TextDark,
                                    modifier   = Modifier.weight(1f).padding(end = 8.dp)
                                )
                                val (badgeColor, badgeBg, badgeText) = statusMeta(event.status)
                                Surface(shape = RoundedCornerShape(20.dp), color = badgeBg) {
                                    Text(
                                        badgeText,
                                        fontSize   = 11.sp,
                                        color      = badgeColor,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            if (!event.categories.isNullOrEmpty()) {
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    event.categories!!.forEach { category ->
                                        Surface(shape = RoundedCornerShape(99.dp), color = PrimaryBlue.copy(alpha = 0.1f)) {
                                            Text(
                                                category.name,
                                                fontSize   = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color      = PrimaryBlue,
                                                modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item(key = "aksi_section") {
                SectionCard(title = "Aksi") {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick  = { backStack.add(Routes.EditActivityRoute(eventId = event.id)) },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape    = RoundedCornerShape(10.dp),
                            border   = BorderStroke(1.dp, NavyDark.copy(alpha = 0.4f)),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = NavyDark)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Edit", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        if (event.status == "published") {
                            Button(
                                onClick  = { viewModel.onCompleteEvent() },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape    = RoundedCornerShape(10.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = NavyDark)
                            ) {
                                Icon(Icons.Default.Done, null, tint = Color.White, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(3.dp))
                                Text("Selesaikan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }
                }
            }
            item(key = "quick_info_section") {
                val registered = event.registered_count ?: 0
                val quota      = event.quota ?: 0
                val quotaProgress = if (quota > 0) (registered.toFloat() / quota.toFloat()).coerceIn(0f, 1f) else 0f

                SectionCard(title = null) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        InfoBlock(
                            modifier = Modifier.weight(1f),
                            icon     = Icons.Default.CalendarToday,
                            label    = "Tanggal",
                            value    = buildString {
                                append(DateUtils.formatDateShort(event.start_date))
                                if (!event.end_date.isNullOrBlank() && event.end_date != event.start_date) {
                                    append(" – "); append(DateUtils.formatDateShort(event.end_date))
                                }
                            }
                        )
                        InfoBlock(
                            modifier = Modifier.weight(1f),
                            icon     = Icons.Default.Schedule,
                            label    = "Waktu",
                            value    = buildString {
                                append(event.start_time?.take(8) ?: "-")
                                if (!event.end_time.isNullOrBlank()) { append(" – "); append(event.end_time!!.take(8)) }
                            }
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        InfoBlock(
                            modifier = Modifier.weight(1f),
                            icon     = Icons.Default.LocationOn,
                            label    = "Lokasi",
                            value    = buildString {
                                if (!event.location_name.isNullOrBlank()) append(event.location_name)
                                if (!event.city.isNullOrBlank()) { if (isNotEmpty()) append("\n"); append(event.city) }
                            }.ifBlank { "-" }
                        )
                        InfoBlock(
                            modifier = Modifier.weight(1f),
                            icon     = Icons.Default.People,
                            label    = "Kuota",
                            value    = "$registered / $quota"
                        ) {
                            Spacer(Modifier.height(5.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(BluePale.copy(alpha = 0.5f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(quotaProgress)
                                        .background(PrimaryBlue)
                                )
                            }
                        }
                    }
                }
            }
            if (!event.contact_person.isNullOrBlank() || !event.contact_phone.isNullOrBlank()) {
                item(key = "contact_section") {
                    SectionCard(title = "Narahubung") {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Person, null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                            Column {
                                Text(event.contact_person ?: "-", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                                if (!event.contact_phone.isNullOrBlank()) {
                                    Text(event.contact_phone!!, fontSize = 12.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }

            if (!event.requirements.isNullOrBlank()) {
                item(key = "requirements_section") {
                    SectionCard(title = "Persyaratan") {
                        Text(event.requirements!!, fontSize = 13.sp, color = TextMuted, lineHeight = 19.sp)
                    }
                }
            }
            if (!event.description.isNullOrBlank()) {
                item(key = "description_section") {
                    SectionCard(title = "Deskripsi Event") {
                        Text(event.description!!, fontSize = 13.sp, color = TextMuted, lineHeight = 19.sp)
                    }
                }
            }
            item(key = "volunteer_summary") {
                SectionCard(title = "Volunteer") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        VolunteerSummaryRow("Menunggu", uiState.menungguCount, StatusPendingText)
                        VolunteerSummaryRow("Dikonfirmasi", uiState.dikonfirmasiCount, AcceptGreen)
                        VolunteerSummaryRow("Hadir", uiState.hadirCount, StatusAttendedText)
                        VolunteerSummaryRow("Dibatalkan", uiState.dibatalkanCount, TextMuted)
                    }
                }
            }
            item(key = "volunteer_header") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Daftar Volunteer", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text("${uiState.registrations.size} pendaftar", fontSize = 12.sp, color = TextMuted)
                }
            }

            if (uiState.registrations.isEmpty()) {
                item(key = "volunteer_empty") {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PersonOff, null, tint = Color(0xFFBBBBBB), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Belum ada volunteer mendaftar", color = Color(0xFFAAAAAA), fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(items = displayedRegistrations, key = { "reg_${it.id}" }) { reg ->
                    VolunteerRowCard(
                        reg        = reg,
                        onDetail   = { backStack.add(Routes.CandidateDetailRoute(candidateId = reg.id)) },
                        onConfirm  = { viewModel.onConfirmRegistration(reg.id) },
                        onReject   = { viewModel.onRejectRegistration(reg.id) },
                        onAttend   = { viewModel.onAttendRegistration(reg.id) },
                        onOpenChat = { roomId ->
                            backStack.add(
                                Routes.ChatRoomRoute(
                                    roomId        = roomId,
                                    eventTitle    = event.title,
                                    organizerName = reg.user?.name ?: "Volunteer",
                                    isOrganizer   = true
                                )
                            )
                        }
                    )
                }

                if (uiState.registrations.size > VOLUNTEER_PREVIEW_COUNT) {
                    item(key = "volunteer_show_more") {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(onClick = { showAllVolunteers = !showAllVolunteers }) {
                                Text(
                                    if (showAllVolunteers) {
                                        "Tampilkan Lebih Sedikit"
                                    } else {
                                        "Lihat Semua (${uiState.registrations.size})"
                                    },
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = PrimaryBlue
                                )
                                Icon(
                                    if (showAllVolunteers) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    null,
                                    tint     = PrimaryBlue,
                                    modifier = Modifier.size(16.dp).padding(start = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            item(key = "footer") { AppFooter() }
        }
    }
}

@Composable
private fun SectionCard(title: String?, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (title != null) {
                Text(
                    title.uppercase(),
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.SemiBold,
                    color         = TextMuted,
                    letterSpacing = 0.8.sp
                )
            }
            content()
        }
    }
}

@Composable
private fun InfoBlock(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    extra: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, tint = PrimaryBlue, modifier = Modifier.size(12.dp))
            Text(label.uppercase(), fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
        }
        Text(value, fontSize = 13.sp, color = TextDark, fontWeight = FontWeight.Medium, lineHeight = 18.sp)
        extra?.invoke()
    }
}

@Composable
private fun VolunteerSummaryRow(label: String, count: Int, dotColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(8.dp).background(dotColor, CircleShape))
            Text(label, fontSize = 13.sp, color = TextDark)
        }
        Text("$count", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VolunteerRowCard(
    reg:        RegistrationDto,
    onDetail:   () -> Unit,
    onConfirm:  () -> Unit,
    onReject:   () -> Unit,
    onAttend:   () -> Unit,
    onOpenChat: (Int) -> Unit
) {
    val user    = reg.user
    val profile = user?.volunteer_profile

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val avatarUrl = profile?.avatar ?: user?.avatar
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(PrimaryBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model              = avatarUrl,
                            contentDescription = user?.name,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Text(
                            user?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color      = PrimaryBlue
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        user?.name ?: "Volunteer",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextDark,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Email, null, tint = TextMuted, modifier = Modifier.size(11.dp))
                        Text(
                            buildString {
                                append(user?.email ?: "-")
                                if (!profile?.city.isNullOrBlank()) { append(" · "); append(profile!!.city) }
                            },
                            fontSize = 11.sp,
                            color    = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                val (badgeColor, badgeBg, badgeText) = registrationStatusMeta(reg.status)
                Surface(shape = RoundedCornerShape(20.dp), color = badgeBg) {
                    Text(
                        badgeText,
                        fontSize   = 10.sp,
                        color      = badgeColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (!profile?.skills.isNullOrEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    profile!!.skills!!.take(3).forEach { skill ->
                        Surface(shape = RoundedCornerShape(20.dp), color = BluePale.copy(alpha = 0.5f)) {
                            Text(
                                skill,
                                fontSize = 10.sp,
                                color    = NavyDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    val remaining = (profile?.skills?.size ?: 0) - 3
                    if (remaining > 0) {
                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFF0F0F0)) {
                            Text(
                                "+$remaining",
                                fontSize = 10.sp,
                                color    = TextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick  = onDetail,
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape    = RoundedCornerShape(8.dp),
                    border   = BorderStroke(1.dp, TextMuted.copy(alpha = 0.4f)),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextDark),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) { Text("Detail", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }

                when (reg.status) {
                    "pending" -> {
                        OutlinedButton(
                            onClick  = onReject,
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape    = RoundedCornerShape(8.dp),
                            border   = BorderStroke(1.dp, DeclineRed.copy(alpha = 0.5f)),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = DeclineRed),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) { Text("Decline", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }

                        Button(
                            onClick  = onConfirm,
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape    = RoundedCornerShape(8.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = AcceptGreen),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) { Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    }
                    "confirmed" -> {
                        Button(
                            onClick  = onAttend,
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape    = RoundedCornerShape(8.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) { Text("Hadir", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }

                        if (reg.chat_room_id != null) {
                            Button(
                                onClick  = { onOpenChat(reg.chat_room_id) },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape    = RoundedCornerShape(8.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = NavyDark),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Outlined.Chat, null, tint = Color.White, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Chat", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }
                    "attended" -> {
                        if (reg.chat_room_id != null) {
                            Button(
                                onClick  = { onOpenChat(reg.chat_room_id) },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape    = RoundedCornerShape(8.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = NavyDark),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Outlined.Chat, null, tint = Color.White, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Chat", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
private fun statusMeta(status: String?): Triple<Color, Color, String> = when (status) {
    "published"      -> Triple(StatusActiveText,   StatusActiveBg,    "Aktif")
    "pending_review" -> Triple(StatusPendingText,  StatusPendingBg,   "Pending")
    "cancelled"       -> Triple(StatusRejectedText, StatusRejectedBg,  "Dibatalkan")
    "draft"           -> Triple(TextMuted,          Color(0xFFECEFF1), "Draft")
    "completed"       -> Triple(StatusCompletedText, StatusCompletedBg, "Selesai")
    else              -> Triple(TextMuted,          Color(0xFFF0F0F0), status ?: "")
}

private fun registrationStatusMeta(status: String): Triple<Color, Color, String> = when (status) {
    "confirmed" -> Triple(AcceptGreen,        Color(0xFFDCFCE7), "Dikonfirmasi")
    "cancelled" -> Triple(StatusRejectedText, StatusRejectedBg,  "Dibatalkan")
    "attended"  -> Triple(StatusAttendedText, StatusAttendedBg,  "Hadir")
    else        -> Triple(StatusPendingText,  StatusPendingBg,   "Menunggu")
}