package com.example.prototypevolunteerapp.ui.screens.organizer

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.Routes
import com.example.prototypevolunteerapp.data.remote.dto.EventDto

private val NavyDeep           = Color(0xFF1E3A8A)
private val PrimaryBlue        = Color(0xFF3D85FC)
private val BgColor            = Color(0xFFF0F5FF)
private val CardWhite          = Color(0xFFFFFFFF)
private val TextPrimary        = Color(0xFF0F172A)
private val TextSecondary      = Color(0xFF475569)
private val StatusPendingText  = Color(0xFFB45309)
private val StatusPendingBg    = Color(0xFFFEF3C7)
private val StatusActiveText   = Color(0xFF16A34A)
private val StatusActiveBg     = Color(0xFFDCFCE7)
private val StatusRejectedText = Color(0xFFDC2626)
private val StatusRejectedBg   = Color(0xFFFEE2E2)
private val StatusCompletedText= Color(0xFF059669)
private val StatusCompletedBg  = Color(0xFFD1FAE5)

@Composable
fun OrgActivitiesScreen(
    initialStatus: String? = null,
    viewModel: OrgActivitiesViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val uiState   by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.load(initialStatus) }

    val statusOptions = listOf(
        null             to "Semua",
        "published"      to "Aktif",
        "pending_review" to "Pending",
        "completed"      to "Selesai",
        "draft"          to "Draft",
        "cancelled"      to "Dibatalkan"
    )

    Scaffold(containerColor = BgColor) { padding ->
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(NavyDeep, PrimaryBlue)))
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
                        "Kelola Kegiatan",
                        color      = Color.White,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${uiState.filteredEvents.size} kegiatan",
                        color    = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { backStack.add(Routes.AddActivityRoute) }
                        .align(Alignment.CenterEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Tambah Kegiatan",
                        tint     = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            OutlinedTextField(
                value         = uiState.searchQuery,
                onValueChange = { viewModel.onSearchChange(it) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                placeholder   = { Text("Cari kegiatan...", fontSize = 13.sp) },
                leadingIcon   = {
                    Icon(Icons.Default.Search, null, tint = TextSecondary)
                },
                trailingIcon  = {
                    if (uiState.searchQuery.isNotBlank()) {
                        Icon(
                            Icons.Default.Close, null,
                            modifier = Modifier.clickable { viewModel.onSearchChange("") },
                            tint     = TextSecondary
                        )
                    }
                },
                shape         = RoundedCornerShape(14.dp),
                singleLine    = true,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = PrimaryBlue,
                    unfocusedBorderColor    = Color(0xFFE2E8F0),
                    focusedContainerColor   = CardWhite,
                    unfocusedContainerColor = CardWhite
                )
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statusOptions.forEach { (value, label) ->
                    val isSelected = uiState.selectedStatus == value
                    FilterChip(
                        selected = isSelected,
                        onClick  = { viewModel.onStatusSelected(value) },
                        label    = {
                            Text(
                                label,
                                fontSize   = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold
                                else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyDeep,
                            selectedLabelColor     = Color.White,
                            containerColor         = CardWhite,
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
            when {
                uiState.isLoading -> {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(bottom = padding.calculateBottomPadding()),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = PrimaryBlue) }
                }
                uiState.error != null -> {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(uiState.error!!, color = TextPrimary)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.reload() },
                            colors  = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) { Text("Coba Lagi") }
                    }
                }
                uiState.filteredEvents.isEmpty() -> {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.EventBusy, null,
                            modifier = Modifier.size(52.dp),
                            tint     = PrimaryBlue.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (uiState.searchQuery.isNotBlank() || uiState.selectedStatus != null)
                                "Tidak ada kegiatan yang cocok"
                            else "Belum ada kegiatan",
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 15.sp,
                            color      = TextPrimary
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier            = Modifier
                            .fillMaxSize()
                            .padding(bottom = padding.calculateBottomPadding()),
                        contentPadding      = PaddingValues(
                            start  = 16.dp,
                            end    = 16.dp,
                            top    = 4.dp,
                            bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.filteredEvents, key = { it.id }) { event ->
                            OrgActivityCard(
                                event        = event,
                                onCardClick  = { backStack.add(Routes.EventDetailRoute(eventId = event.id)) },
                                onEditClick  = { backStack.add(Routes.EditActivityRoute(eventId = event.id)) },
                                onCandidates = { backStack.add(Routes.CandidateListRoute(eventId = event.id)) },
                                onComplete   = { viewModel.onCompleteEvent(event.id) },
                                onCancel     = { viewModel.onCancelEventRequest(event.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrgActivityCard(
    event:        EventDto,
    onCardClick:  () -> Unit,
    onEditClick:  () -> Unit,
    onCandidates: () -> Unit,
    onComplete:   () -> Unit,
    onCancel:     () -> Unit
) {
    val (badgeColor, badgeBg, badgeText) = when (event.status) {
        "published"      -> Triple(StatusActiveText,    StatusActiveBg,    "Aktif")
        "pending_review" -> Triple(StatusPendingText,   StatusPendingBg,   "Pending")
        "cancelled"      -> Triple(StatusRejectedText,  StatusRejectedBg,  "Dibatalkan")
        "draft"          -> Triple(TextSecondary,        Color(0xFFECEFF1), "Draft")
        "completed"      -> Triple(StatusCompletedText,  StatusCompletedBg, "Selesai")
        else             -> Triple(TextSecondary,        Color(0xFFF0F0F0), event.status ?: "")
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCardClick() }
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
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
                            .background(
                                Brush.linearGradient(listOf(NavyDeep, PrimaryBlue))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Event, null,
                            tint     = Color.White,
                            modifier = Modifier.size(28.dp)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn, null,
                                tint     = Color(0xFF000000),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                locationStr,
                                fontSize = 10.sp,
                                color    = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                    }

                    if (!event.start_date.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday, null,
                                tint     = Color(0xFF000000),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(event.start_date, fontSize = 10.sp, color = TextSecondary)
                        }
                        Spacer(Modifier.height(2.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.People, null,
                            tint     = Color(0xFF000000),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            "${event.registered_count ?: 0} pendaftar",
                            fontSize   = 10.sp,
                            color      = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(topEnd = 16.dp, bottomStart = 12.dp),
                    color = badgeBg
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
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Row(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onEditClick() }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Edit, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Edit", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                }

                Box(Modifier.width(1.dp).height(20.dp).align(Alignment.CenterVertically).background(Color(0xFFF1F5F9)))
                if (event.status == "published") {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onComplete() }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = StatusActiveText, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Selesai", fontSize = 11.sp, color = StatusActiveText, fontWeight = FontWeight.Medium)
                    }
                    Box(Modifier.width(1.dp).height(20.dp).align(Alignment.CenterVertically).background(Color(0xFFF1F5F9)))
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onCancel() }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Cancel, null, tint = StatusRejectedText, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Batalkan", fontSize = 11.sp, color = StatusRejectedText, fontWeight = FontWeight.Medium)
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onCandidates() }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.People, null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Pendaftar", fontSize = 11.sp, color = PrimaryBlue, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}