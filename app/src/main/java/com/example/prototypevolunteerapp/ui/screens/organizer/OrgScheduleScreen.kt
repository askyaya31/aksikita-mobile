package com.example.prototypevolunteerapp.ui.screens.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import java.time.format.DateTimeFormatter
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.prototypevolunteerapp.core.DateUtils
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.Routes
import com.example.prototypevolunteerapp.ui.theme.AppColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JTextStyle
import java.util.Locale

private val BgColor     = AppColors.BgColor
private val NavyDeep    = AppColors.NavyDark
private val PrimaryBlue = AppColors.PrimaryBlue
private val BlueMid     = AppColors.MediumBlue
private val BluePale    = AppColors.PalestBlue
private val CardWhite   = AppColors.CardWhite
private val TextPrimary = AppColors.TextDark
private val TextSecondary = AppColors.TextMuted

private val StatusActiveText    = Color(0xFF1D4ED8)
private val StatusActiveBg      = Color(0xFFDBEAFE)
private val StatusPendingText   = Color(0xFF92400E)
private val StatusPendingBg     = Color(0xFFFEF3C7)
private val StatusRejectedText  = Color(0xFF991B1B)
private val StatusRejectedBg    = Color(0xFFFEE2E2)
private val StatusCompletedText = Color(0xFF1E3A8A)
private val StatusCompletedBg   = Color(0xFFEFF6FF)

@Composable
fun OrgScheduleScreen(
    viewModel: OrgScheduleViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val uiState by viewModel.uiState.collectAsState()
    val events    = uiState.events
    val isLoading = uiState.isLoading
    val loadError = uiState.error
    val today = remember { LocalDate.now() }
    var selectedMonth by remember { mutableIntStateOf(today.monthValue) }
    var selectedYear  by remember { mutableIntStateOf(today.year) }
    var selectedDate  by remember { mutableStateOf<LocalDate?>(null) }
    val eventsByDate: Map<String, List<Any>> = remember(events) {
        events
            .filter { !it.start_date.isNullOrBlank() }
            .groupBy { parseOrgEventDate(it.start_date)?.toString() ?: "" }
    }
    val ym = YearMonth.of(selectedYear, selectedMonth)
    val displayedEvents = remember(events, selectedDate, selectedMonth, selectedYear) {
        if (selectedDate != null) {
            events.filter { parseOrgEventDate(it.start_date) == selectedDate }
        } else {
            events.filter { ev ->
                runCatching {
                    val datePart = DateUtils.extractDatePart(ev.start_date)
                    if (datePart.isBlank()) return@filter false
                    val d = parseOrgEventDate(ev.start_date) ?: return@filter false
                    d.year == selectedYear && d.monthValue == selectedMonth
                }.getOrDefault(false)
            }
        }
    }

    Scaffold(containerColor = BgColor) { padding ->
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(NavyDeep, PrimaryBlue)))
                    .padding(horizontal = 16.dp)
                    .padding(
                        top    = WindowInsets.statusBars.asPaddingValues()
                            .calculateTopPadding() + 16.dp,
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
                        "Kalender Kegiatan",
                        color      = Color.White,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${events.size} total kegiatan",
                        color    = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            when {
                isLoading && events.isEmpty() -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                loadError != null && events.isEmpty() -> {
                    Box(
                        modifier         = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.ErrorOutline, null,
                                tint     = TextSecondary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                loadError,
                                fontSize  = 13.sp,
                                color     = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.retry() },
                                colors  = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape   = RoundedCornerShape(10.dp)
                            ) { Text("Coba Lagi") }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier       = Modifier
                            .fillMaxSize()
                            .padding(bottom = padding.calculateBottomPadding()),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item(key = "calendar") {
                            Spacer(Modifier.height(16.dp))
                            OrgMiniCalendar(
                                month        = selectedMonth,
                                year         = selectedYear,
                                markedDates  = eventsByDate.keys.toSet(),
                                selectedDate = selectedDate,
                                onPrev       = {
                                    selectedDate = null
                                    var m = selectedMonth - 1
                                    var y = selectedYear
                                    if (m < 1) { m = 12; y-- }
                                    selectedMonth = m; selectedYear = y
                                },
                                onNext       = {
                                    selectedDate = null
                                    var m = selectedMonth + 1
                                    var y = selectedYear
                                    if (m > 12) { m = 1; y++ }
                                    selectedMonth = m; selectedYear = y
                                },
                                onDateClick  = { date ->
                                    selectedDate = if (selectedDate == date) null else date
                                }
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                        item(key = "list_header") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        if (selectedDate != null)
                                            "Kegiatan ${selectedDate!!.dayOfMonth} " +
                                                    "${selectedDate!!.month.getDisplayName(JTextStyle.FULL, Locale("id"))} " +
                                                    "${selectedDate!!.year}"
                                        else
                                            "${ym.month.getDisplayName(JTextStyle.FULL, Locale("id"))} $selectedYear",
                                        fontSize   = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = TextPrimary
                                    )
                                    Text(
                                        "${displayedEvents.size} kegiatan",
                                        fontSize = 11.sp,
                                        color    = TextSecondary
                                    )
                                }
                                if (selectedDate != null) {
                                    Spacer(Modifier.weight(1f))
                                    TextButton(
                                        onClick        = { selectedDate = null },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Lihat Semua", fontSize = 12.sp, color = PrimaryBlue)
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                        if (displayedEvents.isEmpty()) {
                            item(key = "empty") {
                                Box(
                                    modifier         = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .height(180.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier         = Modifier
                                                .size(60.dp)
                                                .clip(CircleShape)
                                                .background(PrimaryBlue.copy(alpha = 0.10f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.CalendarMonth, null,
                                                modifier = Modifier.size(30.dp),
                                                tint     = PrimaryBlue
                                            )
                                        }
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                            "Tidak ada kegiatan",
                                            fontSize   = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color      = TextPrimary
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            if (selectedDate != null) "Tidak ada kegiatan pada tanggal ini"
                                            else "Tidak ada kegiatan di bulan ini",
                                            fontSize  = 13.sp,
                                            color     = TextSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(
                                items = displayedEvents,
                                key   = { "org_event_${it.id}" }
                            ) { event ->
                                OrgCalendarEventItem(
                                    event   = event,
                                    onClick = { backStack.add(Routes.EventDetailRoute(eventId = event.id)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun parseOrgEventDate(raw: String?): LocalDate? {
    if (raw.isNullOrBlank()) return null
    return try {
        LocalDate.parse(raw.substring(0, 10))
    } catch (_: Exception) {
        try {
            LocalDate.parse(raw.trim(), DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id")))
        } catch (_: Exception) {
            try {
                LocalDate.parse(raw.trim(), DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))
            } catch (_: Exception) {
                null
            }
        }
    }
}
@Composable
private fun OrgCalendarEventItem(
    event:   com.example.prototypevolunteerapp.data.remote.dto.EventDto,
    onClick: () -> Unit
) {
    val date = parseOrgEventDate(event.start_date)
    val (badgeText, badgeColor, badgeBg) = when (event.status) {
        "published"      -> Triple("Aktif",      StatusActiveText,    StatusActiveBg)
        "pending_review" -> Triple("Pending",    StatusPendingText,   StatusPendingBg)
        "cancelled"      -> Triple("Dibatalkan", StatusRejectedText,  StatusRejectedBg)
        "rejected"       -> Triple("Ditolak",    StatusRejectedText,  StatusRejectedBg)
        "completed"      -> Triple("Selesai",    StatusCompletedText, StatusCompletedBg)
        "draft"          -> Triple("Draft",      TextSecondary,       Color(0xFFF1F5F9))
        else             -> Triple(event.status ?: "-", TextSecondary, Color(0xFFF1F5F9))
    }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() },
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(listOf(NavyDeep, PrimaryBlue))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        date?.dayOfMonth?.toString() ?: "--",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White,
                        lineHeight = 17.sp
                    )
                    Text(
                        date?.month?.getDisplayName(JTextStyle.SHORT, Locale("id"))
                            ?.uppercase() ?: "",
                        fontSize   = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White.copy(alpha = 0.85f),
                        lineHeight = 11.sp
                    )
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
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn, null,
                            tint     = TextSecondary,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            locationStr,
                            fontSize = 11.sp,
                            color    = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                }
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        Icons.Default.People, null,
                        tint     = TextSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        "${event.registered_count ?: 0} pendaftar",
                        fontSize = 11.sp,
                        color    = TextSecondary
                    )
                }
            }

            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeBg
            ) {
                Text(
                    badgeText,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = badgeColor,
                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun OrgMiniCalendar(
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
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
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
                    "${ym.month.getDisplayName(JTextStyle.FULL, Locale("id"))} $year",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp,
                    color      = TextPrimary
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
                        color      = TextSecondary
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
                                        isToday    -> NavyDeep
                                        hasEvent   -> BluePale.copy(alpha = 0.5f)
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
                                        date != null          -> TextPrimary
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