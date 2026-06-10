package com.example.prototypevolunteerapp.ui.screens.organizer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier // Baris krusial penangkal 67 error
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Notifications
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.Routes
import com.example.prototypevolunteerapp.ui.components.AppFooter
import com.example.prototypevolunteerapp.data.preferences.SessionPreferences
import kotlinx.coroutines.launch

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val BgColor        = Color(0xFFF5F7FF)
private val HeaderBgTop    = Color(0xFF86B8FF)
private val HeaderBgMiddle = Color(0xFF5B9BD5)
private val HeaderBgBottom = Color(0xFFCBE2FF)
private val CardPending    = Color(0xFF4A88FF)
private val AddButtonColor = Color(0xFF2865FF)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextPrimary    = Color(0xFF1A1A2E)
private val TextSecondary  = Color(0xFF555577)
private val hoverColor     = Color(0xFF0C3B65)

private val StatusPendingText  = Color(0xFFB45309)
private val StatusPendingBg    = Color(0xFFFEF3C7)
private val StatusAcceptedText = Color(0xFF16A34A)
private val StatusAcceptedBg   = Color(0xFFDCFCE7)
private val StatusRejectedText = Color(0xFFDC2626)
private val StatusRejectedBg    = Color(0xFFFEE2E2)

// Token warna tambahan untuk status Selesai
private val StatusCompletedText = Color(0xFF059669)
private val StatusCompletedBg   = Color(0xFFD1FAE5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrgDashboardScreen(
    viewModel: OrgDashboardViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val context   = androidx.compose.ui.platform.LocalContext.current
    val scope     = rememberCoroutineScope()
    val org       = viewModel.currentOrg

    val uiState         by viewModel.uiState.collectAsState()
    val events          = uiState.events

    val statsSheetState   = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadData()
        viewModel.onSectionsVisible()
    }

    val sectionsVisible  = uiState.sectionsVisible
    val showLogoutDialog = uiState.showLogoutDialog
    val showStatsSheet   = uiState.showStatsSheet
    val selectedTab      = uiState.selectedTab

    // ── Logout dialog ─────────────────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onLogoutDialogDismiss() },
            shape            = RoundedCornerShape(20.dp),
            containerColor   = CardWhite,
            icon = {
                Box(
                    modifier         = Modifier.size(48.dp).background(Color(0xFFFFEBEB), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Logout, null,
                        tint     = Color(0xFFD32F2F),
                        modifier = Modifier.size(24.dp))
                }
            },
            title = {
                Text("Keluar dari Akun?",
                    fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    color = TextPrimary, textAlign = TextAlign.Center)
            },
            text = {
                Text("Yakin ingin keluar dari akun organisasi ini?",
                    fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center)
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            SessionPreferences(context).clearSession()
                            viewModel.logout()
                            viewModel.onLogoutDialogDismiss()
                            while (backStack.isNotEmpty()) backStack.removeLastOrNull()
                            backStack.add(Routes.WelcomeRoute)
                        }
                    },
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) { Text("Ya, Keluar", fontSize = 13.sp) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.onLogoutDialogDismiss() },
                    shape   = RoundedCornerShape(12.dp)
                ) { Text("Batal", fontSize = 13.sp) }
            }
        )
    }

    // ── Stats bottom sheet — bar chart total pendaftar per kegiatan ───────────
    if (showStatsSheet) {
        val totalAll      = events.sumOf { it.registered_count ?: 0 }
        val maxCount      = events.maxOfOrNull { it.registered_count ?: 0 }.let { if ((it ?: 0) < 1) 1 else it!! }
            .let { if ((it ?: 0) < 1) 1 else it!! }

        // Trigger animasi bar setelah sheet terbuka
        var barsVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { barsVisible = true }

        ModalBottomSheet(
            onDismissRequest = {
                viewModel.onStatsSheetDismiss()
                viewModel.onTabSelected(OrgTab.DASHBOARD)
            },
            sheetState     = statsSheetState,
            containerColor = CardWhite,
            shape          = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier         = Modifier
                            .size(36.dp)
                            .background(Color(0xFFEAF2FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.BarChart, null,
                            tint     = AddButtonColor,
                            modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Statistik Pendaftar",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp,
                            color      = TextPrimary)
                        Text("$totalAll total pendaftar di semua kegiatan",
                            fontSize = 12.sp, color = TextSecondary)
                    }
                }

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("Total Pendaftar per Kegiatan",
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 13.sp,
                        color      = TextPrimary)
                }

                // ── Horizontal bar chart ──────────────────────────────────────
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier            = Modifier.heightIn(max = 340.dp)
                ) {
                    items(items = events, key = { "stat_${it.id}" }) { event ->
                        val count = event.registered_count ?: 0
                        val ratio = count.toFloat() / maxCount.toFloat()

                        // Animasi fill bar
                        val animatedRatio by animateFloatAsState(
                            targetValue    = if (barsVisible) ratio else 0f,
                            animationSpec  = tween(durationMillis = 600),
                            label          = "bar_${event.id}"
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Label baris: judul + angka
                            Row(
                                modifier              = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text(
                                    text     = event.title,
                                    fontSize = 12.sp,
                                    color    = TextPrimary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text       = "$count pendaftar",
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = AddButtonColor
                                )
                            }

                            // Track + fill
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .background(Color(0xFFEAF2FF), RoundedCornerShape(50.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = animatedRatio.coerceIn(0f, 1f))
                                        .height(10.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF3B82F6), AddButtonColor)
                                            ),
                                            RoundedCornerShape(50.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Cancel event dialog ───────────────────────────────────────────────────
    val cancelTargetId by viewModel.cancelTargetId.collectAsState()
    val cancelSuccess  by viewModel.cancelSuccess.collectAsState()
    LaunchedEffect(cancelSuccess) {
        cancelSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onCancelSuccessHandled()
        }
    }

    if (cancelTargetId != null) {
        var reasonText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.onCancelEventDismiss() },
            icon    = { Icon(Icons.Default.Cancel, null, tint = Color(0xFFCC2222)) },
            title   = { Text("Batalkan Kegiatan?", fontWeight = FontWeight.Bold) },
            text    = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Semua peserta akan menerima notifikasi pembatalan.",
                        fontSize = 13.sp, color = Color(0xFF555555))
                    OutlinedTextField(
                        value         = reasonText,
                        onValueChange = { reasonText = it },
                        label         = { Text("Alasan (opsional)", fontSize = 12.sp) },
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(10.dp),
                        maxLines      = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onCancelEventConfirmed(reasonText.ifBlank { null }) },
                    colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC2222)),
                    shape   = RoundedCornerShape(8.dp)
                ) { Text("Ya, Batalkan") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onCancelEventDismiss() }) { Text("Kembali") }
            }
        )
    }

    // ── Scaffold ──────────────────────────────────────────────────────────────
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar       = {},
        bottomBar    = {
            OrgBottomBar(
                selected  = selectedTab,
                isEnabled = true,
                onSelect  = { tab ->
                    when (tab) {
                        OrgTab.TAMBAH    -> backStack.add(Routes.AddActivityRoute)
                        OrgTab.KANDIDAT  -> {
                            val first = events.firstOrNull()
                            if (first != null) backStack.add(Routes.CandidateListRoute(eventId = first.id))
                            else scope.launch { snackbarHostState.showSnackbar("Belum ada kegiatan tersedia") }
                        }
                        OrgTab.STATISTIK -> { viewModel.onTabSelected(tab); viewModel.onStatsSheetShow() }
                        OrgTab.PROFIL    -> backStack.add(Routes.OrgProfileRoute)
                        OrgTab.DASHBOARD -> viewModel.onTabSelected(tab)
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BgColor, HeaderBgBottom, HeaderBgTop,
                            HeaderBgMiddle, HeaderBgBottom, BgColor
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier            = Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {

                // ── Header welcome ────────────────────────────────────────────
                item(key = "top_header") {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Welcome,",
                                    color    = TextPrimary.copy(alpha = 0.85f),
                                    fontSize = 13.sp)
                                Text(org?.name ?: "Organisasi",
                                    color      = TextPrimary,
                                    fontSize   = 20.sp,
                                    fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val unreadCount by remember { derivedStateOf { uiState.unreadNotifCount } }
                                Box {
                                    IconButton(onClick = {
                                        viewModel.onNotificationsOpened()
                                        backStack.add(Routes.NotificationsRoute)
                                    }) {
                                        Icon(Icons.Default.Notifications, "Notifikasi",
                                            tint     = TextPrimary,
                                            modifier = Modifier.size(28.dp))
                                    }
                                    if (unreadCount > 0) {
                                        Badge(
                                            modifier       = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = (-4).dp, y = 4.dp),
                                            containerColor = MaterialTheme.colorScheme.error
                                        ) {
                                            Text(
                                                if (unreadCount > 99) "99+" else unreadCount.toString(),
                                                fontSize = 9.sp)
                                        }
                                    }
                                }
                                Spacer(Modifier.width(4.dp))
                                IconButton(onClick = { viewModel.onLogoutDialogShow() }) {
                                    Icon(Icons.Default.Logout, "Logout",
                                        tint     = TextPrimary,
                                        modifier = Modifier.size(28.dp))
                                }
                            }
                        }
                    }
                }

                // ── Single stat card: Activity Pending ────────────────────────
                item(key = "stat_cards") {
                    val pendingCount = viewModel.pendingCount()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 20.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onEventStatusSelected("pending_review") },
                            shape  = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardPending)
                        ) {
                            Column(
                                modifier            = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp, horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier         = Modifier
                                        .size(52.dp)
                                        .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Warning, null,
                                        tint     = Color.White,
                                        modifier = Modifier.size(26.dp))
                                }
                                Text("$pendingCount",
                                    fontSize   = 40.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color      = Color.White,
                                    textAlign  = TextAlign.Center)
                                Text("Activity Pending",
                                    fontSize  = 14.sp,
                                    color     = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center)
                                Text("Menunggu persetujuan admin",
                                    fontSize  = 11.sp,
                                    color     = Color.White.copy(0.75f),
                                    textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                // ── Section header kegiatan ───────────────────────────────────
                item(key = "activities_section_header") {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Kegiatan yang didaftarkan",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary)
                        Spacer(Modifier.weight(1f))
                    }
                }

                // ── Status filter chips ───────────────────────────────────────
                item(key = "event_status_filter") {
                    val statusFilter by remember { derivedStateOf { uiState.selectedEventStatus } }
                    val statusOptions = listOf(
                        null             to "Semua",
                        "published"      to "Aktif",
                        "pending_review" to "Pending",
                        "completed"      to "Selesai", // Hanya ditambahkan ke list pilihan
                        "draft"          to "Draft",
                        "cancelled"      to "Dibatalkan"
                    )
                    ScrollableRow_StatusChips(
                        options  = statusOptions,
                        selected = statusFilter,
                        onSelect = { viewModel.onEventStatusSelected(it) }
                    )
                }

                // ── Event cards ───────────────────────────────────────────────
                items(items = uiState.filteredEvents, key = { "event_${it.id}" }) { event ->
                    val candidateCount = viewModel.candidateCountFor(event.id)
                    AnimatedVisibility(
                        visible = sectionsVisible,
                        enter   = fadeIn() + slideInVertically(
                            animationSpec  = spring(stiffness = Spring.StiffnessMediumLow),
                            initialOffsetY = { it / 3 }
                        )
                    ) {
                        Card(
                            modifier  = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clickable { backStack.add(Routes.CandidateListRoute(eventId = event.id)) },
                            shape     = RoundedCornerShape(16.dp),
                            colors    = CardDefaults.cardColors(containerColor = CardWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                Box(
                                    modifier         = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(HeaderBgBottom.copy(alpha = 0.7f), AddButtonColor)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Event, null,
                                        tint     = Color.White,
                                        modifier = Modifier.size(32.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    val (badgeColor, badgeBg, badgeText) = when (event.status) {
                                        "published"      -> Triple(StatusAcceptedText, StatusAcceptedBg, "Aktif")
                                        "pending_review" -> Triple(StatusPendingText,  StatusPendingBg,  "Pending")
                                        "cancelled"      -> Triple(StatusRejectedText, StatusRejectedBg, "Dibatalkan")
                                        "draft"          -> Triple(Color(0xFF37474F),  Color(0xFFECEFF1), "Draft")
                                        "completed"      -> Triple(StatusCompletedText, StatusCompletedBg, "Selesai") // Hanya disisipkan di sini
                                        else             -> Triple(TextSecondary, Color(0xFFF0F0F0), event.status ?: "")
                                    }
                                    Surface(shape = RoundedCornerShape(20.dp), color = badgeBg) {
                                        Text(badgeText, fontSize = 9.sp, color = badgeColor,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(event.title,
                                        fontSize   = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color      = TextPrimary,
                                        maxLines   = 2,
                                        overflow   = TextOverflow.Ellipsis)
                                    Spacer(Modifier.height(4.dp))
                                    val locationStr = buildString {
                                        if (!event.location_name.isNullOrBlank()) append(event.location_name)
                                        if (!event.city.isNullOrBlank()) {
                                            if (isNotEmpty()) append(", "); append(event.city)
                                        }
                                    }
                                    if (locationStr.isNotBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocationOn, null,
                                                tint     = TextSecondary,
                                                modifier = Modifier.size(11.dp))
                                            Spacer(Modifier.width(2.dp))
                                            Text(locationStr, fontSize = 10.sp, color = TextSecondary,
                                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        Spacer(Modifier.height(2.dp))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.People, null,
                                            tint     = Color(0xFF3B82F6),
                                            modifier = Modifier.size(11.dp))
                                        Spacer(Modifier.width(2.dp))
                                        Text("$candidateCount pendaftar",
                                            fontSize   = 10.sp,
                                            color      = Color(0xFF3B82F6),
                                            fontWeight = FontWeight.Medium)
                                    }
                                }
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    IconButton(
                                        onClick  = { backStack.add(Routes.EditActivityRoute(eventId = event.id)) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, null,
                                            tint     = TextSecondary,
                                            modifier = Modifier.size(16.dp))
                                    }

                                    // Selesai Skenario: Tombol Selesaikan Event (Icons.Default.Done) jika status aktif
                                    if (event.status == "published") {
                                        Spacer(Modifier.height(4.dp))
                                        IconButton(
                                            onClick  = { viewModel.onCompleteEvent(event.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Done, null,
                                                tint     = StatusCompletedText,
                                                modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    if (event.status !in listOf("cancelled", "completed")) {
                                        Spacer(Modifier.height(4.dp))
                                        IconButton(
                                            onClick  = { viewModel.onCancelEventRequest(event.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Cancel, null,
                                                tint     = Color(0xFFCC2222),
                                                modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item(key = "bottom_spacer") { Spacer(Modifier.height(16.dp)) }
                item(key = "footer") { AppFooter() }
            }
        }
    }
}

// ── Helper composables ────────────────────────────────────────────────────────

@Composable
private fun MiniLabel(text: String, color: Color) {
    Text(text, fontSize = 10.sp, color = color, fontWeight = FontWeight.Medium)
}

@Composable
private fun ScrollableRow_StatusChips(
    options:  List<Pair<String?, String>>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (value, label) ->
            val isSelected = selected == value
            Surface(
                shape    = RoundedCornerShape(20.dp),
                color    = if (isSelected) hoverColor else Color(0xFFF0F0F0),
                modifier = Modifier.clickable { onSelect(value) }
            ) {
                Text(
                    text       = label,
                    fontSize   = 12.sp,
                    color      = if (isSelected) Color.White else Color(0xFF444444),
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier   = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
private fun OrgBottomBar(
    selected:  OrgTab,
    onSelect:  (OrgTab) -> Unit,
    isEnabled: Boolean = true
) {
    Surface(
        modifier       = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape     = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                clip      = false
            ),
        shape          = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color          = CardWhite,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            OrgNavItem(OrgTab.DASHBOARD, Icons.Default.Home,         "Home",     selected, onSelect)
            OrgNavItem(OrgTab.KANDIDAT,  Icons.Default.Person,       "Kandidat", selected, onSelect)
            OrgAddButton(onClick = { onSelect(OrgTab.TAMBAH) }, isEnabled = isEnabled)
            OrgNavItem(OrgTab.STATISTIK, Icons.Default.BarChart,     "Statistik",selected, onSelect)
            OrgNavItem(OrgTab.PROFIL,    Icons.Default.AccountCircle,"Profile",  selected, onSelect)
        }
    }
}

@Composable
private fun OrgNavItem(
    tab:      OrgTab,
    icon:     ImageVector,
    label:    String,
    selected: OrgTab,
    onSelect: (OrgTab) -> Unit
) {
    val isSelected = selected == tab
    val tint       = if (isSelected) hoverColor else TextSecondary
    Column(
        modifier            = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect(tab) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isSelected) {
            Box(modifier = Modifier
                .width(20.dp).height(3.dp)
                .background(HeaderBgBottom, RoundedCornerShape(50)))
            Spacer(Modifier.height(4.dp))
        } else {
            Spacer(Modifier.height(7.dp))
        }
        Icon(icon, label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(2.dp))
        Text(label,
            fontSize   = 10.sp,
            color      = tint,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun OrgAddButton(onClick: () -> Unit, isEnabled: Boolean = true) {
    Box(
        modifier         = Modifier
            .size(52.dp)
            .shadow(
                elevation = if (isEnabled) 6.dp else 0.dp,
                shape     = CircleShape
            )
            .background(
                if (isEnabled)
                    Brush.linearGradient(listOf(hoverColor, Color(0xFF82B8E8)))
                else
                    Brush.linearGradient(listOf(Color(0xFFB0B0B0), Color(0xFFC0C0C0))),
                CircleShape
            )
            .clip(CircleShape)
            .clickable(enabled = isEnabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Add, "Tambah Kegiatan",
            tint     = Color.White,
            modifier = Modifier.size(26.dp))
    }
}