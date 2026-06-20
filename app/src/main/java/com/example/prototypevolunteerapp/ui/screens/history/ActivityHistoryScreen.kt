package com.example.prototypevolunteerapp.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

@Composable
fun ActivityHistoryScreen(
    viewModel: ActivityHistoryViewModel = hiltViewModel()
) {
    val backStack  = LocalBackStack.current
    val uiState    by viewModel.uiState.collectAsState()
    var activeTab  by remember { mutableStateOf(HistoryTab.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    val filtered = uiState.registrations
        .filter { reg ->
            activeTab == HistoryTab.ALL || reg.status == activeTab.status
        }
        .filter { reg ->
            if (searchQuery.isBlank()) true
            else {
                val q = searchQuery.trim().lowercase()
                reg.event?.title?.lowercase()?.contains(q) == true ||
                        reg.event?.organization?.organization_name?.lowercase()?.contains(q) == true ||
                        reg.event?.city?.lowercase()?.contains(q) == true
            }
        }

    Scaffold(
        containerColor = BgScreen
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(NavyDark, PrimaryBlue)))
                    .padding(horizontal = 16.dp)
                    .padding(
                        top    = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
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
                Text(
                    "Riwayat Kegiatan",
                    color      = Color.White,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.align(Alignment.Center)
                )
            }
            Surface(
                modifier        = Modifier.fillMaxWidth(),
                color           = Color.White,
                shadowElevation = 2.dp
            ) {
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder   = {
                        Text(
                            "Cari kegiatan, organisasi, atau kota…",
                            fontSize = 13.sp,
                            color    = TextMuted
                        )
                    },
                    leadingIcon   = {
                        Icon(Icons.Default.Search, null, tint = TextMuted)
                    },
                    trailingIcon  = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, null, tint = TextMuted)
                            }
                        }
                    },
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor     = NavyDark,
                        unfocusedBorderColor   = Color(0xFFE2E8F0),
                        focusedContainerColor   = Color(0xFFF8FAFF),
                        unfocusedContainerColor = Color(0xFFF8FAFF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                if (!uiState.isLoading && uiState.errorMessage == null) {
                    ScrollableTabRow(
                        selectedTabIndex = activeTab.ordinal,
                        containerColor   = Color.White,
                        contentColor     = NavyDark,
                        edgePadding      = 16.dp,
                        divider          = { HorizontalDivider(color = Color(0xFFE2E8F0)) },
                        indicator        = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                                color    = NavyDark
                            )
                        }
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
                            Text(
                                uiState.errorMessage!!,
                                color     = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadHistory() },
                                colors  = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
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
                                Icon(
                                    if (searchQuery.isNotBlank()) Icons.Default.SearchOff
                                    else Icons.Default.EventBusy,
                                    null,
                                    tint     = PrimaryBlue,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                when {
                                    searchQuery.isNotBlank() ->
                                        "Tidak ada hasil untuk \"$searchQuery\""
                                    activeTab == HistoryTab.ALL ->
                                        "Belum ada riwayat kegiatan"
                                    else ->
                                        "Tidak ada kegiatan dengan status \"${activeTab.label}\""
                                },
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 15.sp,
                                textAlign  = TextAlign.Center,
                                color      = TextDark
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                if (searchQuery.isNotBlank())
                                    "Coba kata kunci lain atau hapus pencarian."
                                else
                                    "Kegiatan yang sudah kamu ikuti akan muncul di sini.",
                                style     = MaterialTheme.typography.bodyMedium,
                                color     = TextMuted,
                                textAlign = TextAlign.Center
                            )
                            if (searchQuery.isNotBlank()) {
                                Spacer(Modifier.height(12.dp))
                                TextButton(onClick = { searchQuery = "" }) {
                                    Text("Hapus pencarian", color = PrimaryBlue)
                                }
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier            = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding      = PaddingValues(top = 14.dp, bottom = 24.dp)
                        ) {
                            item {
                                Row(
                                    modifier              = Modifier.fillMaxWidth(),
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "${filtered.size} kegiatan",
                                        fontSize = 12.sp,
                                        color    = TextMuted,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    if (searchQuery.isNotBlank()) {
                                        Text(
                                            "Hasil untuk \"$searchQuery\"",
                                            fontSize = 12.sp,
                                            color    = NavyDark,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                }
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
                                                location = "${event.city ?: ""}, ${event.province ?: ""}".trim(',', ' '),
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
}

@Composable
private fun HistoryCard(registration: RegistrationDto, onClick: () -> Unit) {
    val event = registration.event

    val (statusColor, statusLabel, statusBg) = when (registration.status) {
        "confirmed" -> Triple(Color(0xFF166534), "Dikonfirmasi", Color(0xFFDCFDE9))
        "attended"  -> Triple(Color(0xFF1E3A8A), "Hadir",        Color(0xFFDBEAFE))
        "cancelled" -> Triple(Color(0xFF991B1B), "Dibatalkan",   Color(0xFFFEE2E2))
        "pending"   -> Triple(Color(0xFF92400E), "Menunggu",     Color(0xFFFEF3C7))
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
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color      = statusColor
                    )
                }
                registration.registered_at?.take(10)?.let {
                    Text(it, fontSize = 11.sp, color = TextMuted)
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                event?.title ?: "Kegiatan tidak tersedia",
                fontWeight = FontWeight.SemiBold,
                fontSize   = 14.sp,
                color      = TextDark,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis
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
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                        Text(it, fontSize = 11.sp, color = TextMuted)
                    }
                }
                event?.start_date?.let {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                        Text(it, fontSize = 11.sp, color = TextMuted)
                    }
                }
            }

            registration.notes?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    "\"$it\"",
                    fontSize  = 11.sp,
                    color     = TextMuted,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    "Detail",
                    tint     = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}