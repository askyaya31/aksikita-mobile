package com.example.prototypevolunteerapp.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.Routes
import com.example.prototypevolunteerapp.data.remote.dto.RegistrationDto
import com.example.prototypevolunteerapp.ui.components.AppFooter
import com.example.prototypevolunteerapp.ui.components.LoadingIndicator

private val NavyDark    = Color(0xFF1E3A8A)
private val PrimaryBlue = Color(0xFF3B82F6)
private val BgScreen    = Color(0xFFF8FAFF)
private val TextDark    = Color(0xFF0F172A)
private val TextMuted   = Color(0xFF64748B)

private enum class HistoryTab(val label: String, val status: String?) {
    ALL(       "Semua",       null),
    PENDING(   "Menunggu",    "pending"),
    CONFIRMED( "Dikonfirmasi","confirmed"),
    ATTENDED(  "Hadir",       "attended"),
    CANCELLED( "Dibatalkan",  "cancelled")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityHistoryScreen(
    viewModel: ActivityHistoryViewModel = hiltViewModel()
) {
    val backStack  = LocalBackStack.current
    val uiState    by viewModel.uiState.collectAsState()
    var activeTab  by remember { mutableStateOf(HistoryTab.ALL) }

    val filtered = when (activeTab) {
        HistoryTab.ALL -> uiState.registrations
        else -> uiState.registrations.filter { it.status == activeTab.status }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Kegiatan", fontWeight = FontWeight.SemiBold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
            )
        },
        containerColor = BgScreen
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (!uiState.isLoading && uiState.errorMessage == null) {
                ScrollableTabRow(
                    selectedTabIndex    = activeTab.ordinal,
                    containerColor      = Color.White,
                    contentColor        = NavyDark,
                    edgePadding         = 16.dp,
                    divider             = { HorizontalDivider(color = Color(0xFFE2E8F0)) }
                ) {
                    HistoryTab.values().forEach { tab ->
                        val count = if (tab == HistoryTab.ALL) {
                            uiState.registrations.size
                        } else {
                            uiState.registrations.count { it.status == tab.status }
                        }
                        Tab(
                            selected = activeTab == tab,
                            onClick  = { activeTab = tab },
                            text     = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Text(tab.label, fontSize = 13.sp)
                                    if (count > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(99.dp),
                                            color = if (activeTab == tab) NavyDark else Color(0xFFE2E8F0)
                                        ) {
                                            Text(
                                                count.toString(),
                                                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontSize   = 10.sp,
                                                color      = if (activeTab == tab) Color.White else TextMuted,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator()
                    }
                }

                uiState.errorMessage != null -> {
                    Column(
                        modifier            = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadHistory() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) { Text("Coba Lagi") }
                    }
                }

                filtered.isEmpty() -> {
                    Column(
                        modifier            = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(64.dp)
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.EventBusy, null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (activeTab == HistoryTab.ALL) "Belum ada riwayat kegiatan"
                            else "Tidak ada kegiatan dengan status \"${activeTab.label}\"",
                            fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                            textAlign  = TextAlign.Center, color = TextDark
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Kegiatan yang sudah kamu ikuti akan muncul di sini.",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = TextMuted, textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier            = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding      = PaddingValues(top = 14.dp, bottom = 24.dp)
                    ) {
                        item {
                            Text(
                                "${filtered.size} kegiatan",
                                fontSize = 12.sp, color = TextMuted,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(filtered, key = { it.id }) { reg ->
                            HistoryCard(
                                registration = reg,
                                onClick = {
                                    val event = reg.event ?: return@HistoryCard
                                    backStack.add(
                                        Routes.ActivityDetailRoute(
                                            id       = event.id.toString(),
                                            slug     = event.slug ?: "",
                                            title    = event.title,
                                            location = "${event.city ?: ""}, ${event.province ?: ""}".trim(',',' '),
                                            desc     = event.description ?: "",
                                            imageRes = event.poster ?: ""
                                        )
                                    )
                                }
                            )
                        }
                        item { AppFooter() }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(registration: RegistrationDto, onClick: () -> Unit) {
    val event = registration.event

    val (statusColor, statusLabel, statusBg) = when (registration.status) {
        "confirmed" -> Triple(Color(0xFF0E7B6C), "Dikonfirmasi", Color(0xFFE3F5F2))
        "attended"  -> Triple(Color(0xFF1E3A8A), "Hadir",        Color(0xFFEEF3FF))
        "cancelled" -> Triple(Color(0xFF6B7280), "Dibatalkan",   Color(0xFFF1F5F9))
        "pending"   -> Triple(Color(0xFFD4900A), "Menunggu",     Color(0xFFFDF5E1))
        else        -> Triple(TextMuted,          registration.status, Color(0xFFF1F5F9))
    }

    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(99.dp), color = statusBg) {
                    Text(
                        statusLabel,
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize   = 11.sp, fontWeight = FontWeight.Bold, color = statusColor
                    )
                }
                registration.registered_at?.take(10)?.let {
                    Text(it, fontSize = 11.sp, color = TextMuted)
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                event?.title ?: "Kegiatan tidak tersedia",
                fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                color      = TextDark, maxLines = 2, overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            event?.organization?.organization_name?.let {
                Text(it, fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                event?.city?.let {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Default.LocationOn, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                        Text(it, fontSize = 11.sp, color = TextMuted)
                    }
                }
                event?.start_date?.let {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Default.CalendarToday, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                        Text(it, fontSize = 11.sp, color = TextMuted)
                    }
                }
            }

            registration.notes?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    "\"$it\"",
                    fontSize = 11.sp, color = TextMuted,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
            Row(
                modifier              = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(Icons.Default.ChevronRight, "Detail", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
            }
        }
    }
}