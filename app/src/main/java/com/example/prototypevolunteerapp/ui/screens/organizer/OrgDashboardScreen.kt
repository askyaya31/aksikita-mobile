package com.example.prototypevolunteerapp.ui.screens.organizer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
 import androidx.compose.foundation.background
 import androidx.compose.foundation.layout.Box
 import androidx.compose.foundation.layout.offset
 import androidx.compose.foundation.layout.size
 import androidx.compose.foundation.shape.CircleShape
 import androidx.compose.runtime.derivedStateOf
 import androidx.compose.runtime.getValue
 import androidx.compose.runtime.remember
 import androidx.compose.ui.unit.dp
 import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.material3.*
import com.example.prototypevolunteerapp.core.Routes
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.ui.components.AppFooter
import com.example.prototypevolunteerapp.ui.components.ActivityRowCard
import com.example.prototypevolunteerapp.ui.components.SubmissionStatusCard
import com.example.prototypevolunteerapp.ui.components.SectionHeader
import com.example.prototypevolunteerapp.data.preferences.SessionPreferences
import kotlinx.coroutines.launch

private val BgColor       = Color(0xFFF4F7EF)
private val HeaderStart   = Color(0xFF3D5C2A)
private val HeaderMid     = Color(0xFF5A7A5A)
private val HeaderEnd     = Color(0xFF9EB589)
private val CardWhite     = Color(0xFFFFFFFF)
private val AccentGreen   = Color(0xFF5A7A5A)
private val TextPrimary   = Color(0xFF1E2D1E)
private val TextSecondary = Color(0xFF6E8F6E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrgDashboardScreen(
    viewModel: OrgDashboardViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val context   = androidx.compose.ui.platform.LocalContext.current
    val scope     = rememberCoroutineScope()
    val org       = viewModel.currentOrg

    val uiState            by viewModel.uiState.collectAsState()
    val existingActivities = uiState.existingActivities
    val events        = uiState.events
    val totalActivities    = uiState.totalActivities
    val totalCandidates    = uiState.totalCandidates
    val totalSubmissions   = uiState.totalSubmissions

    val statsSheetState   = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadData()
        viewModel.onSectionsVisible()
    }
    val sectionsVisible  = uiState.sectionsVisible
    val showLogoutDialog = uiState.showLogoutDialog
    val showStatsSheet   = uiState.showStatsSheet
    val selectedTab = uiState.selectedTab
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onLogoutDialogDismiss() },
            shape = RoundedCornerShape(20.dp),
            containerColor = CardWhite,
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFFEBEB), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Logout,
                        null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    "Keluar dari Akun?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    "Yakin ingin keluar dari akun organisasi ini?",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            SessionPreferences(context).clearSession()
                            viewModel.logout()
                            viewModel.onLogoutDialogDismiss()
                            while (backStack.isNotEmpty()) {
                                backStack.removeLastOrNull()
                            }
                            backStack.add(Routes.WelcomeRoute)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) { Text("Ya, Keluar", fontSize = 13.sp) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.onLogoutDialogDismiss() },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Batal", fontSize = 13.sp) }
            }
        )
    }

    if (showStatsSheet) {
        val totalPending  = viewModel.pendingCount()
        val totalAccepted = viewModel.acceptedCount()
        val totalRejected = viewModel.rejectedCount()
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.onStatsSheetDismiss()
                viewModel.onTabSelected(OrgTab.DASHBOARD)
            },
            sheetState = statsSheetState,
            containerColor = CardWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 4.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .background(Color(0xFFDAEFDC), RoundedCornerShape(50))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFDAEFDC), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.BarChart,
                            null,
                            tint = AccentGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "Statistik Kandidat",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Text(
                            "${uiState.totalCandidates} total kandidat terdaftar",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFDAEFDC), thickness = 1.dp)
                StatDetailRow(
                    label = "Menunggu Keputusan",
                    count = totalPending,
                    textColor = Color(0xFF7A5C00),
                    bgColor = Color(0xFFFFF8E1),
                    icon = Icons.Default.HourglassEmpty
                )
                StatDetailRow(
                    label = "Diterima",
                    count = totalAccepted,
                    textColor = Color(0xFF2E5C1A),
                    bgColor = Color(0xFFD4EDCA),
                    icon = Icons.Default.CheckCircle
                )
                StatDetailRow(
                    label = "Ditolak",
                    count = totalRejected,
                    textColor = Color(0xFFB71C1C),
                    bgColor = Color(0xFFFFEBEE),
                    icon = Icons.Default.Cancel
                )

                HorizontalDivider(color = Color(0xFFDAEFDC), thickness = 1.dp)

                Text(
                    "Kandidat Setiap Kegiatan",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.heightIn(max = 280.dp)
                ) {
                    items(items = existingActivities, key = { "stat_${it.title}" }) { activity ->
                        val count = viewModel.candidateCountFor(activity.title)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF4F7EF), RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activity.title,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFDAEFDC), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    "$count kandidat",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AccentGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    val cancelTargetId  by viewModel.cancelTargetId.collectAsState()
    val cancelSuccess   by viewModel.cancelSuccess.collectAsState()

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
                    Text("Semua peserta yang terdaftar akan menerima notifikasi pembatalan.",
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    val unreadCount by remember {
                        derivedStateOf { uiState.unreadNotifCount }
                    }
                    Box {
                        IconButton(
                            onClick = {
                                viewModel.onNotificationsOpened()
                                backStack.add(Routes.NotificationsRoute)
                            }
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifikasi",
                                tint               = Color(0xFF3D5C2A)
                            )
                        }
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(Color(0xFFD32F2F), CircleShape)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-2).dp, y = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text     = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                    fontSize = 9.sp,
                                    color    = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 10.sp
                                )
                            }
                        }
                    }

                    IconButton(onClick = { viewModel.onLogoutDialogShow() }) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint               = Color(0xFFD32F2F)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor)
            )
        },

        bottomBar = {
            OrgBottomBar(
                selected = selectedTab,
                isEnabled  = true,
                onSelect = { tab ->
                    when (tab) {
                        OrgTab.TAMBAH -> backStack.add(Routes.AddActivityRoute)
                        OrgTab.KANDIDAT   -> {
                            val firstActivity = existingActivities.firstOrNull()
                            if (firstActivity != null) {
                                backStack.add(Routes.CandidateListRoute(eventId = firstActivity.id.toIntOrNull()))
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Belum ada kegiatan tersedia")
                                }
                            }
                        }
                        OrgTab.STATISTIK -> {
                            viewModel.onTabSelected(tab)
                            viewModel.onStatsSheetShow()
                        }
                        OrgTab.PROFIL  -> backStack.add(Routes.OrgProfileRoute)
                        OrgTab.DASHBOARD -> viewModel.onTabSelected(tab)
                    }
                }
            )
        },
        containerColor = BgColor
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item(key = "verification_banner") {
                val verStatus = uiState.events.firstOrNull()?.organization?.verification_status
                if (verStatus != null && verStatus != "verified") {
                    val (bannerColor, bannerBg, bannerIcon, bannerText) = when (verStatus) {
                        "rejected" -> listOf(
                            Color(0xFFB71C1C), Color(0xFFFFEBEE),
                            Icons.Default.Cancel,
                            "Akun organisasimu ditolak admin. Edit profil dan ajukan ulang."
                        )
                        else -> listOf(
                            Color(0xFF7A5C00), Color(0xFFFFF8E1),
                            Icons.Default.HourglassEmpty,
                            "Akun organisasimu sedang menunggu verifikasi admin. Beberapa fitur dibatasi."
                        )
                    }
                    @Suppress("UNCHECKED_CAST")
                    val color  = bannerColor  as Color
                    val bg     = bannerBg     as Color
                    val icon   = bannerIcon   as androidx.compose.ui.graphics.vector.ImageVector
                    val text   = bannerText   as String

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bg)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                        Text(text, fontSize = 12.sp, color = color, lineHeight = 16.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
            item(key="header") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(listOf(HeaderStart, HeaderMid, HeaderEnd))
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .offset(x = 220.dp, y = (-40).dp)
                            .background(Color.White.copy(alpha = 0.04f), CircleShape)
                            .align(Alignment.TopStart)
                    )
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .offset(x = 260.dp, y = 30.dp)
                            .background(Color.White.copy(alpha = 0.03f), CircleShape)
                            .align(Alignment.TopStart)
                    )
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!org?.logoUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model              = org!!.logoUrl,
                                        contentDescription = "Logo organisasi",
                                        contentScale       = ContentScale.Crop,
                                        modifier           = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Text(
                                        text       = (org?.name?.firstOrNull() ?: "O").toString().uppercase(),
                                        color      = Color.White,
                                        fontSize   = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    org?.name ?: "Organisasi",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    org?.email ?: "",
                                    color = Color.White.copy(0.5f),
                                    fontSize = 11.sp
                                )
                            }

                            val verificationStatus = uiState.events.firstOrNull()?.organization?.verification_status
                            if (verificationStatus != null) {
                                val badgeBg    = when (verificationStatus) { "verified" -> Color(0xFFE8F5E9); "rejected" -> Color(0xFFFFEBEE); else -> Color(0xFFFFF8E1) }
                                val badgeColor = when (verificationStatus) { "verified" -> Color(0xFF2E7D32); "rejected" -> Color(0xFFB71C1C);  else -> Color(0xFFE65100)  }
                                val badgeIcon  = when (verificationStatus) { "verified" -> Icons.Default.Verified; "rejected" -> Icons.Default.Cancel; else -> Icons.Default.HourglassEmpty }
                                val badgeLabel = when (verificationStatus) { "verified" -> "Terverifikasi"; "rejected" -> "Ditolak"; else -> "Menunggu Verifikasi" }

                                Spacer(Modifier.width(8.dp))
                                Surface(shape = RoundedCornerShape(20.dp), color = badgeBg.copy(alpha = 0.9f)) {
                                    Row(
                                        modifier              = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment     = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(badgeIcon, null, tint = badgeColor, modifier = Modifier.size(12.dp))
                                        Text(badgeLabel, fontSize = 10.sp, color = badgeColor, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                label = "Kegiatan",
                                value = "$totalActivities",
                                icon = Icons.Default.Event,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                label = "Kandidat",
                                value = "$totalCandidates",
                                icon = Icons.Default.People,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                label = "Pengajuan",
                                value = "$totalSubmissions",
                                icon = Icons.Default.Assignment,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            item(key = "activities_header") {
                AnimatedVisibility(
                    visible = sectionsVisible,
                    enter = fadeIn() + slideInVertically(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        initialOffsetY = { it / 4 }
                    )
                ) {
                    SectionHeader(
                        title = "Daftar Kegiatan",
                        subtitle = "Pilih untuk melihat kandidat",
                        icon = Icons.Default.Event
                    )
                }
            }

            item(key = "event_status_filter") {
                val statusFilter by remember { derivedStateOf { uiState.selectedEventStatus } }
                val statusOptions = listOf(
                    null             to "Semua",
                    "published"      to "Aktif",
                    "pending_review" to "Pending",
                    "draft"          to "Draft",
                    "cancelled"      to "Dibatalkan"
                )
                ScrollableRow_StatusChips(
                    options  = statusOptions,
                    selected = statusFilter,
                    onSelect = { viewModel.onEventStatusSelected(it) }
                )
            }

            items(
                items = uiState.filteredEvents,
                key   = { "event_${it.id}" }
            ) { event ->
                val candidateCount = viewModel.candidateCountFor(event.id)
                AnimatedVisibility(
                    visible = sectionsVisible,
                    enter = fadeIn() + slideInVertically(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        initialOffsetY = {it/3}
                    )
                ) {
                    ActivityRowCard(
                        title = event.title,
                        location = buildString {
                            if (!event.location_name.isNullOrBlank()) append(event.location_name)
                            if (!event.city.isNullOrBlank()) { if (isNotEmpty()) append(", "); append(event.city) }
                        },
                        candidateCount = candidateCount,
                        onClick = {
                            backStack.add(Routes.CandidateListRoute(eventId = event.id))
                        },
                        onEdit = {
                            backStack.add(Routes.EditActivityRoute(eventId = event.id))
                        },
                        onCancel = if (event.status !in listOf("cancelled", "completed")) {
                            { viewModel.onCancelEventRequest(event.id) }
                        } else null
                    )
                }
            }
            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(16.dp)) }
            item(key = "footer") { AppFooter() }
        }
    }
}

@Composable
private fun ScrollableRow_StatusChips(
    options:  List<Pair<String?, String>>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    val AccentGreen = Color(0xFF3D5C2A)
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
                shape  = RoundedCornerShape(20.dp),
                color  = if (isSelected) AccentGreen else Color(0xFFF0F0F0),
                modifier = Modifier.clickable { onSelect(value) }
            ) {
                Text(
                    text     = label,
                    fontSize = 12.sp,
                    color    = if (isSelected) Color.White else Color(0xFF444444),
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }
    }
}
@Composable
private fun OrgBottomBar(
    selected: OrgTab,
    onSelect: (OrgTab) -> Unit,
    isEnabled: Boolean = true
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                clip = false
            ),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = CardWhite,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OrgNavItem(
                tab = OrgTab.DASHBOARD,
                icon = Icons.Default.Home,
                label = "Dashboard",
                selected = selected,
                onSelect = onSelect
            )
            OrgNavItem(
                tab = OrgTab.KANDIDAT,
                icon = Icons.Default.Person,
                label = "Kandidat",
                selected = selected,
                onSelect = onSelect
            )
            OrgAddButton(onClick = { onSelect(OrgTab.TAMBAH) }, isEnabled = isEnabled)
            OrgNavItem(
                tab = OrgTab.STATISTIK,
                icon = Icons.Default.BarChart,
                label = "Statistik",
                selected = selected,
                onSelect = onSelect
            )
            OrgNavItem(
                tab = OrgTab.PROFIL,
                icon = Icons.Default.AccountCircle,
                label = "Profil",
                selected = selected,
                onSelect = onSelect
            )
        }
    }
}

@Composable
private fun OrgNavItem(
    tab: OrgTab,
    icon: ImageVector,
    label: String,
    selected: OrgTab,
    onSelect: (OrgTab) -> Unit
) {
    val isSelected = selected == tab
    val tint = if (isSelected) AccentGreen else TextSecondary

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect(tab) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(3.dp)
                    .background(AccentGreen, RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.height(4.dp))
        } else {
            Spacer(modifier = Modifier.height(7.dp))
        }
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = tint,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}


@Composable
private fun OrgAddButton(onClick: () -> Unit, isEnabled: Boolean = true) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .shadow(elevation = if (isEnabled) 6.dp else 0.dp, shape = CircleShape)
            .background(
                if (isEnabled)
                    Brush.linearGradient(listOf(HeaderStart, HeaderMid))
                else
                    Brush.linearGradient(listOf(Color(0xFFB0B0B0), Color(0xFFC0C0C0))),
                CircleShape
            )
            .clip(CircleShape)
            .clickable(enabled = isEnabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = Icons.Default.Add,
            contentDescription = "Tambah Kegiatan",
            tint               = Color.White,
            modifier           = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Text(label, color = Color.White.copy(0.65f), fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun StatDetailRow(
    label    : String,
    count    : Int,
    textColor: Color,
    bgColor  : Color,
    icon     : ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, tint = textColor, modifier = Modifier.size(18.dp))
            Text(label, fontSize = 13.sp, color = textColor, fontWeight = FontWeight.Medium)
        }
        Text("$count", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
    }
}