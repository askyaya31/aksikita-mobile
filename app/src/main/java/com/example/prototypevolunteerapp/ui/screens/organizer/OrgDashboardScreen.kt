package com.example.prototypevolunteerapp.ui.screens.organizer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.ChevronRight
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
import com.example.prototypevolunteerapp.core.Routes
import com.example.prototypevolunteerapp.ui.components.AppFooter
import androidx.compose.material.icons.filled.CalendarMonth
import kotlinx.coroutines.launch
import com.example.prototypevolunteerapp.ui.theme.AppColors

private val BgColor             = AppColors.BgColor
private val PrimaryBlue         = AppColors.PrimaryBlue
private val NavyDeep            = AppColors.NavyDark
private val BlueMid             = AppColors.MediumBlue
private val BlueLight           = AppColors.LightBlue
private val BluePale            = AppColors.PalestBlue
private val CardWhite           = AppColors.CardWhite
private val TextPrimary         = AppColors.TextDark
private val TextSecondary       = AppColors.TextMuted

private val StatusPendingText   = AppColors.WarningText
private val StatusPendingBg     = AppColors.WarningBg
private val StatusActiveText    = AppColors.SuccessText
private val StatusActiveBg      = AppColors.SuccessBg
private val StatusRejectedText  = AppColors.DangerText
private val StatusRejectedBg    = AppColors.DangerBg
private val StatusCompletedText = AppColors.CompletedText
private val StatusCompletedBg   = AppColors.CompletedBg
private val NotifAccent         = AppColors.NotifAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrgDashboardScreen(
    viewModel: OrgDashboardViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val scope     = rememberCoroutineScope()
    val org       = viewModel.currentOrg

    val uiState         by viewModel.uiState.collectAsState()
    val events          = uiState.events
    val selectedTab     = uiState.selectedTab
    val sectionsVisible = uiState.sectionsVisible
    val showStatsSheet  = uiState.showStatsSheet

    val statsSheetState   = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadData()
        viewModel.onSectionsVisible()
    }

    if (showStatsSheet) {
        val sortedEvents = remember(events) {
            events.sortedByDescending { it.registered_count ?: 0 }
        }
        val totalAll = sortedEvents.sumOf { it.registered_count ?: 0 }
        val maxCount = (sortedEvents.maxOfOrNull { it.registered_count ?: 0 } ?: 0)
        val referenceScale = maxOf(10, maxCount).toFloat()
        var barsVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { barsVisible = true }

        ModalBottomSheet(
            onDismissRequest = { viewModel.onStatsSheetDismiss() },
            sheetState       = statsSheetState,
            containerColor   = CardWhite,
            shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier         = Modifier
                            .size(36.dp)
                            .background(AppColors.InfoBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PieChart, null,
                            tint     = AppColors.InfoText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "Statistik Pendaftar",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp,
                            color      = TextPrimary
                        )
                        Text(
                            "$totalAll total pendaftar di semua kegiatan",
                            fontSize = 12.sp,
                            color    = TextSecondary
                        )
                    }
                }
                Text(
                    "Total Pendaftar per Kegiatan",
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 13.sp,
                    color      = TextPrimary
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier            = Modifier.heightIn(max = 340.dp)
                ) {
                    items(items = sortedEvents, key = { "stat_${it.id}" }) { event ->
                        val count = event.registered_count ?: 0
                        val ratio = count.toFloat() / referenceScale
                        val animatedRatio by animateFloatAsState(
                            targetValue   = if (barsVisible) ratio else 0f,
                            animationSpec = tween(durationMillis = 600),
                            label         = "bar_${event.id}"
                        )
                        Column(modifier = Modifier.fillMaxWidth()) {
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
                                    text       = if (count == 0) "Belum ada pendaftar" else "$count pendaftar",
                                    fontSize   = 12.sp,
                                    fontWeight = if (count == 0) FontWeight.Normal else FontWeight.Bold,
                                    color      = if (count == 0) TextSecondary else PrimaryBlue
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .background(BluePale, RoundedCornerShape(50.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = animatedRatio.coerceIn(0f, 1f))
                                        .height(10.dp)
                                        .background(
                                            Brush.horizontalGradient(listOf(NavyDeep, PrimaryBlue)),
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
            title   = { Text("Batalkan Kegiatan?", fontWeight = FontWeight.Bold) },
            text    = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Semua peserta akan menerima notifikasi pembatalan.",
                        fontSize = 13.sp,
                        color    = TextSecondary
                    )
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
                    colors  = ButtonDefaults.buttonColors(containerColor = StatusRejectedText),
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
        topBar       = {},
        bottomBar    = {
            OrgBottomBar(
                selected       = selectedTab,
                unreadMessages = 0,
                onSelect       = { tab ->
                    when (tab) {
                        OrgTab.TAMBAH    -> backStack.add(Routes.AddActivityRoute)
                        OrgTab.KEGIATAN    -> backStack.add(Routes.OrgActivitiesRoute())
                        OrgTab.PESAN     -> backStack.add(Routes.ChatListRoute(isOrganizer = true))
                        OrgTab.PROFIL    -> backStack.add(Routes.OrgProfileRoute)
                        OrgTab.DASHBOARD -> viewModel.onTabSelected(tab)
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 16.dp)
            ) {
                item(key = "top_header") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(Brush.linearGradient(listOf(NavyDeep, PrimaryBlue)))
                            .padding(horizontal = 20.dp)
                            .padding(top = 24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .offset(x = (-30).dp, y = (-30).dp)
                                .background(Color.White.copy(alpha = 0.07f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 20.dp, y = (-20).dp)
                                .background(Color.White.copy(alpha = 0.07f), CircleShape)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier         = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val avatarUrl = org?.logoUrl
                                    if (!avatarUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model              = avatarUrl,
                                            contentDescription = "Logo",
                                            contentScale       = ContentScale.Crop,
                                            modifier           = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Business, null,
                                            tint     = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        "Welcome,",
                                        color    = Color.White.copy(alpha = 0.75f),
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        org?.name ?: "Organisasi",
                                        color      = Color.White,
                                        fontSize   = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines   = 1,
                                        overflow   = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            val unreadCount by remember { derivedStateOf { uiState.unreadNotifCount } }
                            Box(
                                modifier         = Modifier
                                    .size(42.dp)
                                    .background(Color.White.copy(alpha = 0.18f), CircleShape)
                                    .clickable {
                                        viewModel.onNotificationsOpened()
                                        backStack.add(Routes.NotificationsRoute)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifikasi",
                                    tint               = Color.White,
                                    modifier           = Modifier.size(22.dp)
                                )
                                if (unreadCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .background(NotifAccent, CircleShape)
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }
                        }
                    }
                }

                item(key = "overview_section") {
                    val pendingCount   = viewModel.pendingCount()
                    val activeCount    = events.count { it.status == "published" }
                    val completedCount = events.count { it.status == "completed" }
                    val cancelledCount = events.count { it.status == "cancelled" }
                    val totalRegs      = events.sumOf { it.registered_count ?: 0 }
                    val totalQuota = events.sumOf { it.quota ?: 0 }
                    val registeredRatio = if (totalQuota > 0) totalRegs.toFloat() / totalQuota else 0f

                    val totalEvents    = events.size
                    val activeRatio    = if (totalEvents > 0) activeCount.toFloat() / totalEvents else 0f

                    Column(
                        modifier            = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier  = Modifier.fillMaxWidth(),
                            shape     = RoundedCornerShape(16.dp),
                            colors    = CardDefaults.cardColors(containerColor = CardWhite),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { backStack.add(Routes.OrgActivitiesRoute()) }
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier              = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment     = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment     = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier         = Modifier
                                                    .size(24.dp)
                                                    .background(PrimaryBlue.copy(alpha = 0.12f), RoundedCornerShape(7.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Event, null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                                            }
                                            Text("Kegiatan", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                        }
                                        Icon(Icons.Default.ChevronRight, null, tint = TextSecondary.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text("$totalEvents", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("Total kegiatan kamu", fontSize = 10.sp, color = TextSecondary)
                                    Spacer(Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .background(BluePale.copy(alpha = 0.5f), RoundedCornerShape(50.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction = activeRatio.coerceIn(0f, 1f))
                                                .height(5.dp)
                                                .background(StatusActiveText, RoundedCornerShape(50.dp))
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "$activeCount dari $totalEvents sedang aktif",
                                        fontSize   = 9.sp,
                                        color      = StatusActiveText,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, BlueLight, RoundedCornerShape(50.dp))
                                            .padding(vertical = 7.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Kelola Kegiatan", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .fillMaxHeight()
                                        .background(Color(0xFFEEF2F7))
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            val first = events.firstOrNull()
                                            if (first != null) backStack.add(Routes.CandidateListRoute(eventId = first.id))
                                            else scope.launch { snackbarHostState.showSnackbar("Belum ada kegiatan tersedia") }
                                        }
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier              = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment     = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment     = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier         = Modifier
                                                    .size(24.dp)
                                                    .background(PrimaryBlue.copy(alpha = 0.12f), RoundedCornerShape(7.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.People, null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                                            }
                                            Text("Pendaftar", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                        }
                                        Icon(Icons.Default.ChevronRight, null, tint = TextSecondary.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text("$totalRegs", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("Total pendaftar masuk", fontSize = 10.sp, color = TextSecondary)
                                    Spacer(Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .background(BluePale.copy(alpha = 0.5f), RoundedCornerShape(50.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction = registeredRatio.coerceIn(0f, 1f))
                                                .height(5.dp)
                                                .background(BlueMid, RoundedCornerShape(50.dp))
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "$totalRegs dari $totalQuota kuota terisi",
                                        fontSize = 9.sp, color = BlueMid, fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, BlueLight, RoundedCornerShape(50.dp))
                                            .padding(vertical = 7.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Lihat Pendaftar", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                                    }
                                }
                            }
                        }
                        Card(
                            modifier  = Modifier.fillMaxWidth(),
                            shape     = RoundedCornerShape(16.dp),
                            colors    = CardDefaults.cardColors(containerColor = Color(0xFF6882DE)),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                            ) {
                                StatusRingItem(
                                    icon     = Icons.Default.EventAvailable,
                                    value    = activeCount,
                                    label    = "Aktif",
                                    color    = Color(0xFF172554),
                                    onClick  = { backStack.add(Routes.OrgActivitiesRoute(initialStatus = "published")) },
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(0.5.dp)
                                        .fillMaxHeight()
                                        .background(Color.White.copy(alpha = 0.3f))
                                )
                                StatusRingItem(
                                    icon     = Icons.Default.HourglassTop,
                                    value    = pendingCount,
                                    label    = "Pending",
                                    color    = Color(0xFF172554),
                                    onClick  = { backStack.add(Routes.OrgActivitiesRoute(initialStatus = "pending_review")) },
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(0.5.dp)
                                        .fillMaxHeight()
                                        .background(Color.White.copy(alpha = 0.3f))
                                )
                                StatusRingItem(
                                    icon     = Icons.Default.DoneAll,
                                    value    = completedCount,
                                    label    = "Selesai",
                                    color    = Color(0xFF172554),
                                    onClick  = { backStack.add(Routes.OrgActivitiesRoute(initialStatus = "completed")) },
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(0.5.dp)
                                        .fillMaxHeight()
                                        .background(Color.White.copy(alpha = 0.3f))
                                )
                                StatusRingItem(
                                    icon     = Icons.Default.Block,
                                    value    = cancelledCount,
                                    label    = "Dibatalkan",
                                    color    = Color(0xFF172554),
                                    onClick  = { backStack.add(Routes.OrgActivitiesRoute(initialStatus = "cancelled")) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            QuickActionItem(
                                icon    = Icons.Default.Add,
                                label   = "Tambah\nKegiatan",
                                bg      = NavyDeep.copy(alpha = 0.10f),
                                tint    = NavyDeep.copy(alpha = 0.80f),
                                onClick = { backStack.add(Routes.AddActivityRoute) }
                            )
                            QuickActionItem(
                                icon    = Icons.Default.PersonSearch,
                                label   = "Pendaftar",
                                bg      =NavyDeep.copy(alpha = 0.10f),
                                tint    = NavyDeep.copy(alpha = 0.80f),
                                onClick = {
                                    val first = events.firstOrNull()
                                    if (first != null) backStack.add(Routes.CandidateListRoute(eventId = first.id))
                                    else scope.launch { snackbarHostState.showSnackbar("Belum ada kegiatan tersedia") }
                                }
                            )
                            QuickActionItem(
                                icon      = Icons.Default.PendingActions,
                                label     = "Pengajuan",
                                bg        = NavyDeep.copy(alpha = 0.10f),
                                tint      = NavyDeep.copy(alpha = 0.80f),
                                onClick   = { backStack.add(Routes.OrgActivitiesRoute(initialStatus = "pending_review")) }
                            )
                            QuickActionItem(
                                icon    = Icons.Default.CalendarMonth,
                                label   = "Kalender",
                                bg      = NavyDeep.copy(alpha = 0.10f),
                                tint    = NavyDeep.copy(alpha = 0.80f),
                                onClick = { backStack.add(Routes.OrgScheduleRoute) }
                            )
                            QuickActionItem(
                                icon    = Icons.Default.PieChart,
                                label   = "Statistik",
                                bg      = NavyDeep.copy(alpha = 0.10f),
                                tint    = NavyDeep.copy(alpha = 0.80f),
                                onClick = { viewModel.onStatsSheetShow() }
                            )
                        }
                    }
                }
                item(key = "inspiration_banner") {
                    Card(
                        onClick   = { backStack.add(Routes.ActivitiesRoute) },
                        modifier  = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape     = RoundedCornerShape(16.dp),
                        colors    = CardDefaults.cardColors(
                            containerColor = Color(0xFF7497DA).copy(alpha = 0.1f)
                        ),
                        border    = BorderStroke(1.dp, NavyDeep.copy(alpha = 0.1f)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier         = Modifier
                                    .size(42.dp)
                                    .background(
                                        Brush.linearGradient(listOf(NavyDeep, PrimaryBlue)),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.YoutubeSearchedFor,
                                    contentDescription = null,
                                    tint               = Color.White,
                                    modifier           = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Butuh inspirasi kegiatan?",
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = NavyDeep
                                )
                                Text(
                                    "Temukan ide kegiatan dari organisasi\ndi seluruh Indonesia.",
                                    fontSize   = 11.sp,
                                    color      = NavyDeep,
                                    lineHeight = 16.sp
                                )
                            }
                            Icon(
                                imageVector        = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint               = NavyDeep.copy(alpha = 0.45f),
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                item(key = "activities_section_header") {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Kegiatan yang didaftarkan",
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color      = TextPrimary
                            )
                            Text(
                                "${events.size} total kegiatan",
                                fontSize = 11.sp,
                                color    = TextSecondary
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        TextButton(
                            onClick        = { backStack.add(Routes.OrgActivitiesRoute()) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Lihat Semua", fontSize = 12.sp, color = Color(0xFF273F9D), fontWeight = FontWeight.SemiBold)
                            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF273F9D), modifier = Modifier.size(16.dp))
                        }

                    }
                }
                item(key = "event_status_filter") {
                    val statusFilter by remember { derivedStateOf { uiState.selectedEventStatus } }
                    val statusOptions = listOf(
                        null             to "Semua",
                        "published"      to "Aktif",
                        "pending_review" to "Pending",
                        "completed"      to "Selesai",
                        "draft"          to "Draft",
                        "cancelled"      to "Dibatalkan"
                    )
                    ScrollableStatusChips(
                        options  = statusOptions,
                        selected = statusFilter,
                        onSelect = { viewModel.onEventStatusSelected(it) }
                    )
                }
                items(items = uiState.filteredEvents.take(4), key = { "event_${it.id}" }) { event ->
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
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape     = RoundedCornerShape(16.dp),
                            colors    = CardDefaults.cardColors(containerColor = CardWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { backStack.add(Routes.EventDetailRoute(eventId = event.id)) }
                                        .padding(14.dp)
                                ) {
                                    if (!event.poster.isNullOrBlank()) {
                                        AsyncImage(
                                            model              = event.poster,
                                            contentDescription = null,
                                            contentScale       = ContentScale.Crop,
                                            modifier           = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Brush.linearGradient(listOf(NavyDeep, PrimaryBlue))),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Event, null, tint = Color.White, modifier = Modifier.size(28.dp))
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            event.title,
                                            fontSize   = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color      = TextPrimary,
                                            maxLines   = 2,
                                            overflow   = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        val locationStr = buildString {
                                            if (!event.location_name.isNullOrBlank()) append(event.location_name)
                                            if (!event.city.isNullOrBlank()) {
                                                if (isNotEmpty()) append(", ")
                                                append(event.city)
                                            }
                                        }
                                        if (locationStr.isNotBlank()) {
                                            Row(
                                                verticalAlignment     = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Icon(Icons.Default.LocationOn, null, tint = TextPrimary, modifier = Modifier.size(11.dp))
                                                Text(
                                                    locationStr, fontSize = 10.sp, color = TextSecondary,
                                                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                //Icon(Icons.Default.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                            }
                                            Spacer(Modifier.height(2.dp))
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.People, null, tint = TextPrimary, modifier = Modifier.size(11.dp))
                                            Spacer(Modifier.width(2.dp))
                                            Text("$candidateCount pendaftar", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                                        }
                                    }

                                    val (badgeColor, badgeBg, badgeText) = when (event.status) {
                                        "published"      -> Triple(StatusActiveText,    StatusActiveBg,    "Aktif")
                                        "pending_review" -> Triple(StatusPendingText,   StatusPendingBg,   "Pending")
                                        "cancelled"      -> Triple(StatusRejectedText,  StatusRejectedBg,  "Dibatalkan")
                                        "draft"          -> Triple(TextSecondary,        Color(0xFFECEFF1), "Draft")
                                        "completed"      -> Triple(StatusCompletedText,  StatusCompletedBg, "Selesai")
                                        else             -> Triple(TextSecondary,        Color(0xFFF0F0F0), event.status ?: "")
                                    }
                                    Surface(
                                        shape    = RoundedCornerShape(topEnd = 16.dp, bottomStart = 16.dp),
                                        color    = badgeBg,
                                        modifier = Modifier.align(Alignment.Top)
                                    ) {
                                        Text(
                                            badgeText,
                                            fontSize   = 10.sp,
                                            color      = badgeColor,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                                val footerActions = buildList {
                                    add(FooterAction(Icons.Default.Edit, "Edit", TextSecondary) {
                                        backStack.add(Routes.EditActivityRoute(eventId = event.id))
                                    })
                                    if (event.status == "published") {
                                        add(FooterAction(Icons.Default.Done, "Selesai", StatusActiveText) {
                                            viewModel.onCompleteEvent(event.id)
                                        })
                                    }
                                    if (event.status !in listOf("cancelled", "completed")) {
                                        add(FooterAction(Icons.Default.Close, "Batalkan", StatusRejectedText) {
                                            viewModel.onCancelEventRequest(event.id)
                                        })
                                    }
                                }

                                if (footerActions.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFFF1F5F9))
                                    )
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        footerActions.forEachIndexed { index, action ->
                                            if (index > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(1.dp)
                                                        .height(20.dp)
                                                        .align(Alignment.CenterVertically)
                                                        .background(Color(0xFFF1F5F9))
                                                )
                                            }
                                            Row(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { action.onClick() }
                                                    .padding(vertical = 9.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment     = Alignment.CenterVertically
                                            ) {
                                                Icon(action.icon, null, tint = action.color, modifier = Modifier.size(14.dp))
                                                Spacer(Modifier.width(5.dp))
                                                Text(action.label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = action.color)
                                            }
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

private data class FooterAction(
    val icon:    ImageVector,
    val label:   String,
    val color:   Color,
    val onClick: () -> Unit
)

@Composable
private fun StatusRingItem(
    icon:     ImageVector,
    value:    Int,
    label:    String,
    color:    Color,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(32.dp)
                .background(Color.White.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon, null,
                tint     = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            "$value",
            fontSize   = 16.sp,
            fontWeight = FontWeight.Medium,
            color      = Color.White,
            lineHeight = 18.sp
        )
        Text(
            label,
            fontSize = 9.sp,
            color    = Color.White.copy(alpha = 0.65f)
        )
    }
}
@Composable
private fun QuickActionItem(
    icon:      ImageVector,
    label:     String,
    bg:        Color,
    tint:      Color,
    showBadge: Boolean = false,
    onClick:   () -> Unit
) {
    Column(
        modifier            = Modifier
            .width(58.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier         = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            if (showBadge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(9.dp)
                        .background(NotifAccent, CircleShape)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text       = label,
            fontSize   = 9.sp,
            color      = TextSecondary,
            textAlign  = TextAlign.Center,
            lineHeight = 11.sp,
            maxLines   = 2
        )
    }
}

@Composable
private fun ScrollableStatusChips(
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
            FilterChip(
                selected = isSelected,
                onClick  = { onSelect(value) },
                label    = {
                    Text(
                        text       = label,
                        fontSize   = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NavyDeep,
                    selectedLabelColor     = Color.White,
                    containerColor         = Color.White,
                    labelColor             = NavyDeep
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled             = true,
                    selected            = isSelected,
                    borderColor         = NavyDeep.copy(alpha = 0.4f),
                    selectedBorderColor = Color.Transparent,
                    borderWidth         = 1.dp,
                    selectedBorderWidth = 0.dp
                )
            )
        }
    }
}

@Composable
private fun OrgBottomBar(
    selected:       OrgTab,
    unreadMessages: Int = 0,
    onSelect:       (OrgTab) -> Unit
) {
    Surface(
        modifier = Modifier
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
            OrgNavItem(OrgTab.DASHBOARD, Icons.Default.Home,          "Home",     selected, onSelect)
            OrgNavItem(OrgTab.KEGIATAN,  Icons.Default.VolunteerActivism,        "Kegiatan", selected, onSelect)
            OrgAddButton(onClick = { onSelect(OrgTab.TAMBAH) })
            OrgNavItemWithBadge(
                tab         = OrgTab.PESAN,
                icon        = Icons.Default.Chat,
                label       = "Pesan",
                selected    = selected,
                onSelect    = onSelect,
                unreadCount = unreadMessages
            )
            OrgNavItem(OrgTab.PROFIL, Icons.Default.AccountCircle, "Profile", selected, onSelect)
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
    val tint       = if (isSelected) NavyDeep else TextSecondary
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect(tab) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isSelected) {
            Box(modifier = Modifier.width(20.dp).height(3.dp).background(PrimaryBlue, RoundedCornerShape(50)))
            Spacer(Modifier.height(4.dp))
        } else {
            Spacer(Modifier.height(7.dp))
        }
        Icon(icon, label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 10.sp, color = tint, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun OrgNavItemWithBadge(
    tab:         OrgTab,
    icon:        ImageVector,
    label:       String,
    selected:    OrgTab,
    onSelect:    (OrgTab) -> Unit,
    unreadCount: Int = 0
) {
    val isSelected = selected == tab
    val tint       = if (isSelected) NavyDeep else TextSecondary
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect(tab) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isSelected) {
            Box(modifier = Modifier.width(20.dp).height(3.dp).background(PrimaryBlue, RoundedCornerShape(50)))
            Spacer(Modifier.height(4.dp))
        } else {
            Spacer(Modifier.height(7.dp))
        }
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, label, tint = tint, modifier = Modifier.size(22.dp))
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-3).dp)
                        .size(8.dp)
                        .background(NotifAccent, CircleShape)
                        .border(1.5.dp, CardWhite, CircleShape)
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 10.sp, color = tint, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun OrgAddButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .shadow(elevation = 6.dp, shape = CircleShape)
            .background(Brush.linearGradient(listOf(NavyDeep, PrimaryBlue)), CircleShape)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Add, contentDescription = "Tambah Kegiatan", tint = Color.White, modifier = Modifier.size(26.dp))
    }
}