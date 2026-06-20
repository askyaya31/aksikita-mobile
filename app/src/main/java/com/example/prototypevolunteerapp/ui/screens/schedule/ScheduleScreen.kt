package com.example.prototypevolunteerapp.ui.screens.schedule

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.Routes
import com.example.prototypevolunteerapp.data.remote.dto.ScheduleEventDto
import com.example.prototypevolunteerapp.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val NavyDark    = Color(0xFF1E3A8A)
private val PrimaryBlue = Color(0xFF3B82F6)
private val BluePale    = Color(0xFFEFF6FF)
private val TextDark    = Color(0xFF0F172A)
private val TextMuted   = Color(0xFF64748B)

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = hiltViewModel()) {
    val backStack = LocalBackStack.current
    val state by viewModel.state.collectAsState()
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    LaunchedEffect(state.selectedMonth, state.selectedYear) {
        selectedDate = null
    }

    val displayList = remember(state.activeTab, state.upcoming, state.past, selectedDate) {
        val base = if (state.activeTab == ScheduleTab.UPCOMING) state.upcoming else state.past
        if (selectedDate == null) base
        else base.filter { event ->
            runCatching { LocalDate.parse(event.startDate) == selectedDate }.getOrDefault(false)
        }
    }

    Scaffold(containerColor = Color(0xFFF8FAFF)) { padding ->
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

                Column(
                    modifier            = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Jadwal Saya",
                        color      = Color.White,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${state.upcoming.size} kegiatan mendatang",
                        color    = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else {
                LazyColumn(
                    modifier       = Modifier
                        .fillMaxSize()
                        .padding(bottom = padding.calculateBottomPadding()),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        MiniCalendar(
                            month        = state.selectedMonth,
                            year         = state.selectedYear,
                            markedDates  = state.eventsByDate.keys.toSet(),
                            selectedDate = selectedDate,
                            onPrev       = { viewModel.changeMonth(-1) },
                            onNext       = { viewModel.changeMonth(+1) },
                            onDateClick  = { date ->
                                selectedDate = if (selectedDate == date) null else date
                            }
                        )
                        Spacer(Modifier.height(20.dp))
                    }
                    item {
                        ScheduleTabRow(
                            activeTab     = state.activeTab,
                            onSelect      = {
                                selectedDate = null
                                viewModel.setTab(it)
                            },
                            upcomingCount = state.upcoming.size,
                            pastCount     = state.past.size
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    if (selectedDate != null) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Kegiatan ${selectedDate!!.dayOfMonth} " +
                                            "${selectedDate!!.month.getDisplayName(TextStyle.FULL, Locale("id"))} " +
                                            "${selectedDate!!.year}",
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = TextDark,
                                    modifier   = Modifier.weight(1f)
                                )
                                TextButton(
                                    onClick        = { selectedDate = null },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        "Lihat Semua",
                                        fontSize = 12.sp,
                                        color    = PrimaryBlue
                                    )
                                }
                            }
                        }
                    } else {
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                    if (displayList.isEmpty()) {
                        item {
                            EmptyScheduleState(
                                activeTab    = state.activeTab,
                                isFiltered   = selectedDate != null
                            )
                        }
                    } else {
                        items(displayList.size) { index ->
                            val event   = displayList[index]
                            val isLast  = index == displayList.lastIndex
                            TimelineEventItem(
                                event    = event,
                                isLast   = isLast,
                                isPast   = state.activeTab == ScheduleTab.PAST,
                                onClick  = {
                                    backStack.add(
                                        Routes.ActivityDetailRoute(
                                            id       = event.eventId.toString(),
                                            slug     = event.eventSlug,
                                            title    = event.eventTitle,
                                            location = event.location,
                                            desc     = event.description ?: "",
                                            imageRes = event.posterUrl ?: ""
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun TimelineEventItem(
    event:   ScheduleEventDto,
    isLast:  Boolean,
    isPast:  Boolean,
    onClick: () -> Unit
) {
    val date = try { LocalDate.parse(event.startDate) } catch (e: Exception) { null }
    val lineColor = Color(0xFFCBD5E1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isPast) Color(0xFFE2E8F0) else PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        date?.dayOfMonth?.toString() ?: "--",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (isPast) TextMuted else Color.White,
                        lineHeight = 14.sp
                    )
                    Text(
                        date?.month?.getDisplayName(TextStyle.SHORT, Locale("id"))
                            ?.uppercase() ?: "",
                        fontSize   = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (isPast) TextMuted.copy(alpha = 0.7f)
                        else Color.White.copy(alpha = 0.85f),
                        lineHeight = 10.sp
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(lineColor)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Card(
            modifier  = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 12.dp)
                .clickable { onClick() },
            shape     = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    event.eventTitle,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextDark,
                    maxLines   = 2
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    event.organizerName,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color      = PrimaryBlue
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                Spacer(Modifier.height(8.dp))

                event.startTime?.let { t ->
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier              = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(Icons.Default.Schedule, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                        Text(
                            buildString {
                                append(t)
                                event.endTime?.let { append(" – $it") }
                            },
                            fontSize = 12.sp,
                            color    = TextMuted
                        )
                    }
                }

                if (!event.location.isNullOrBlank()) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier              = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                        Text(event.location, fontSize = 12.sp, color = TextMuted, maxLines = 1)
                    }
                }

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    val (bgColor, txtColor, label) = when (event.status) {
                        "attended"  -> Triple(Color(0xFFDCFCE7), Color(0xFF16A34A), "Selesai")
                        "confirmed" -> Triple(Color(0xFFEFF6FF), PrimaryBlue,       "Terkonfirmasi")
                        "pending"   -> Triple(Color(0xFFFEF9C3), Color(0xFFCA8A04), "Menunggu")
                        else        -> Triple(Color(0xFFF1F5F9), TextMuted,         event.status)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(bgColor)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = txtColor)
                    }
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("Lihat riwayat", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = PrimaryBlue)
                        Icon(Icons.Default.ChevronRight, null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}
@Composable
private fun MiniCalendar(
    month:        Int,
    year:         Int,
    markedDates:  Set<String>,
    selectedDate: LocalDate?,
    onPrev:       () -> Unit,
    onNext:       () -> Unit,
    onDateClick:  (LocalDate) -> Unit
) {
    val ym       = YearMonth.of(year, month)
    val today    = LocalDate.now()
    val days     = (1..ym.lengthOfMonth()).map { ym.atDay(it) }
    val firstDow = ym.atDay(1).dayOfWeek.value % 7

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrev, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ChevronLeft, null, tint = PrimaryBlue)
                }
                Text(
                    "${ym.month.getDisplayName(TextStyle.FULL, Locale("id"))} $year",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp,
                    color      = TextDark
                )
                IconButton(onClick = onNext, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ChevronRight, null, tint = PrimaryBlue)
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth()) {
                listOf("Min","Sen","Sel","Rab","Kam","Jum","Sab").forEach { d ->
                    Text(
                        d,
                        modifier   = Modifier.weight(1f),
                        textAlign  = TextAlign.Center,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextMuted
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            val cells = MutableList<LocalDate?>(firstDow) { null } + days
            cells.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    (0 until 7).forEach { col ->
                        val date       = week.getOrNull(col)
                        val dateStr    = date?.toString() ?: ""
                        val hasEvent   = markedDates.contains(dateStr)
                        val isToday    = date == today
                        val isSelected = date != null && date == selectedDate

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> PrimaryBlue
                                        isToday    -> NavyDark
                                        hasEvent   -> BluePale
                                        else       -> Color.Transparent
                                    }
                                )
                                .then(
                                    if (date != null) Modifier.clickable { onDateClick(date) }
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    date?.dayOfMonth?.toString() ?: "",
                                    fontSize   = 12.sp,
                                    fontWeight = if (isToday || hasEvent || isSelected)
                                        FontWeight.Bold else FontWeight.Normal,
                                    color      = when {
                                        isSelected || isToday -> Color.White
                                        hasEvent              -> PrimaryBlue
                                        date != null          -> TextDark
                                        else                  -> Color.Transparent
                                    }
                                )
                                if (hasEvent && !isToday && !isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryBlue)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleTabRow(
    activeTab:     ScheduleTab,
    onSelect:      (ScheduleTab) -> Unit,
    upcomingCount: Int,
    pastCount:     Int
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(5.dp)) {
            listOf(
                ScheduleTab.UPCOMING to "Mendatang ($upcomingCount)",
                ScheduleTab.PAST     to "Lalu ($pastCount)"
            ).forEach { (tab, label) ->
                val selected = tab == activeTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) NavyDark else Color.Transparent)
                        .clickable { onSelect(tab) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (selected) Color.White else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyScheduleState(
    activeTab:  ScheduleTab,
    isFiltered: Boolean = false
) {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier         = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(BluePale),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CalendarMonth, null,
                    modifier = Modifier.size(32.dp),
                    tint     = PrimaryBlue
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                when {
                    isFiltered                          -> "Tidak ada kegiatan"
                    activeTab == ScheduleTab.UPCOMING   -> "Tidak ada jadwal mendatang"
                    else                                -> "Tidak ada riwayat kegiatan"
                },
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextDark
            )
            Spacer(Modifier.height(4.dp))
            Text(
                when {
                    isFiltered                          -> "Tidak ada kegiatan pada tanggal ini"
                    activeTab == ScheduleTab.UPCOMING   ->
                        "Daftar ke kegiatan untuk melihat\njadwalmu di sini"
                    else                                ->
                        "Kegiatan yang telah kamu ikuti\nakan muncul di sini"
                },
                fontSize  = 13.sp,
                color     = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}