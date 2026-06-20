package com.example.prototypevolunteerapp.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import coil.compose.AsyncImage
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.Routes
import com.example.prototypevolunteerapp.core.UserSession
import com.example.prototypevolunteerapp.ui.components.ActivityCard
import com.example.prototypevolunteerapp.ui.components.UpcomingScheduleMiniCard
import com.example.prototypevolunteerapp.data.model.ActivityData
import com.example.prototypevolunteerapp.data.remote.dto.RegistrationDto
import com.example.prototypevolunteerapp.ui.components.ProfileMenuItem
import com.example.prototypevolunteerapp.ui.components.RecommendationCard

private val NavyDark     = Color(0xFF1E3A8A)
private val PrimaryBlue  = Color(0xFF3B82F6)
private val BlueMid      = Color(0xFF60A5FA)
private val BluePale     = Color(0xFFBFDBFE)
private val AccentOrange = Color(0xFFE8501A)
private val BgScreen     = Color(0xFFF8FAFF)
private val TextDark     = Color(0xFF0F172A)
private val TextMuted    = Color(0xFF64748B)
private val GreenOnline  = Color(0xFF22C55E)

private data class BottomNavItem(
    val label:          String,
    val selectedIcon:   ImageVector,
    val unselectedIcon: ImageVector,
    val route:          NavKey
)

private data class RecommendationItem(
    val title: String,
    val category: String,
    val date: String,
    val time: String,
    val location: String,
    val slotsLeft: Int,
    val bgColor: Color
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val backStack      = LocalBackStack.current
    val uiState        by viewModel.uiState.collectAsState()
    val currentUser    = viewModel.currentUser
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    LaunchedEffect(Unit) { viewModel.refreshUser() }

    val navItems = listOf(
        BottomNavItem("Beranda",  Icons.Filled.Home,              Icons.Outlined.Home,              Routes.HomeRoute),
        BottomNavItem("Kegiatan", Icons.Filled.VolunteerActivism, Icons.Outlined.VolunteerActivism, Routes.ActivitiesRoute),
        BottomNavItem("Riwayat",  Icons.Filled.History,           Icons.Outlined.History,           Routes.ActivityHistoryRoute),
        BottomNavItem("Pesan",  Icons.Filled.Chat,           Icons.Outlined.Chat,           Routes.ChatListRoute()),
        //BottomNavItem("Saved",    Icons.Filled.Bookmark,          Icons.Outlined.BookmarkBorder,    Routes.SavedActivitiesRoute),
        BottomNavItem("Profil",   Icons.Filled.Person,            Icons.Outlined.Person,            Routes.ProfileRoute)
    )

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshUser()
                viewModel.onTabSelected(0)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        bottomBar = {
            VolunteerBottomBar(
                navItems    = navItems,
                selectedTab = uiState.selectedTab,
                onSelect    = { index ->
                    viewModel.onTabSelected(index)
                    if (index != 0) backStack.add(navItems[index].route)
                }
            )
        },
        containerColor = BgScreen
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(innerPadding).background(BgScreen),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item(key = "header") {
                HomeHeader(
                    userName       = uiState.userName,
                    avatarUrl      = currentUser?.avatarUrl,
                    unreadCount    = uiState.unreadNotifCount,
                    onProfileClick = { backStack.add(Routes.ProfileRoute) },
                    onNotifClick   = { backStack.add(Routes.NotificationsRoute) }
                )
            }
            item(key = "nearby_section") {
                HomeSectionHeader(
                    title    = "Kegiatan di ${uiState.userCity ?: "Sekitarmu"}",
                    subtitle = "Jangan lewatkan kegiatan di sekitarmu",
                    onSeeAll = { backStack.add(Routes.ActivitiesRoute) }
                )
                when {
                    uiState.isLoadingNearby -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(32.dp))
                        }
                    }
                    uiState.nearbyEvents.isNotEmpty() -> {
                        NearbyEventsPager(
                            events      = uiState.nearbyEvents,
                            onItemClick = { event ->
                                backStack.add(
                                    Routes.ActivityDetailRoute(
                                        id       = event.id,
                                        slug     = event.slug,
                                        title    = event.title,
                                        location = event.location,
                                        desc     = event.description,
                                        imageRes = event.imageRes
                                    )
                                )
                            }
                        )
                    }
                    else -> NearbyEventsPlaceholder()
                }
            }

            if (uiState.upcomingSchedule.isNotEmpty()) {
                item(key = "schedule_header") {
                    HomeSectionHeader(
                        title    = "Jadwal Mendatang",
                        subtitle = "Kegiatan yang akan kamu ikuti",
                        onSeeAll = { backStack.add(Routes.ScheduleRoute) }
                    )
                }
                item(key = "schedule_list") {
                    UpcomingScheduleList(
                        schedules = uiState.upcomingSchedule.take(3)
                    )
                }
            }

            item(key = "events_header") {
                HomeSectionHeader(
                    title    = "Kegiatan Tersedia",
                    subtitle = "Mari mulai berkontribusi!",
                    onSeeAll = { backStack.add(Routes.ActivitiesRoute) }
                )
            }
            item(key = "category_chips") {
                CategoryChipRow(
                    categories     = uiState.categoryChips,
                    selectedChip   = uiState.selectedCategory,
                    onChipSelected = { viewModel.onCategoryChipSelected(it) }
                )
            }
            item(key = "events") {
                when {
                    uiState.isLoadingApi -> {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(color = PrimaryBlue) }
                    }
                    uiState.apiEvents.isNotEmpty() -> {
                        EventVerticalList(
                            events      = uiState.apiEvents,
                            onItemClick = { event ->
                                backStack.add(
                                    Routes.ActivityDetailRoute(
                                        id       = event.id,
                                        slug     = event.slug,
                                        title    = event.title,
                                        location = event.location,
                                        desc     = event.description,
                                        imageRes = event.imageRes
                                    )
                                )
                            }
                        )
                    }
                    uiState.apiError != null -> {
                        Text(
                            text     = "Tidak dapat memuat kegiatan. Periksa koneksi.",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            if (uiState.recommendations.isNotEmpty()) {
                item(key = "recommendations_section") {
                    HomeSectionHeader(
                        title    = "Rekomendasi untukmu",
                        subtitle = "Berdasarkan minat & riwayatmu",
                        onSeeAll = { backStack.add(Routes.RecommendationsRoute) }
                    )
                    RecommendationCardRow(
                        items       = uiState.recommendations,
                        onItemClick = { event ->
                            backStack.add(
                                Routes.ActivityDetailRoute(
                                    id       = event.id,
                                    slug     = event.slug,
                                    title    = event.title,
                                    location = event.location,
                                    desc     = event.description,
                                    imageRes = event.imageRes
                                )
                            )
                        }
                    )
                }
            }
            item(key = "footer") {
                Text(
                    text      = "AksiKita © 2026",
                    style     = MaterialTheme.typography.bodySmall,
                    modifier  = Modifier.fillMaxWidth().padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color     = TextMuted.copy(alpha = 0.5f)
                )
            }
        }
    }
}
@Composable
private fun HomeHeader(
    userName:       String,
    avatarUrl:      String?,
    unreadCount:    Int,
    onProfileClick: () -> Unit,
    onNotifClick:   () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(PrimaryBlue)
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .offset(x = (-30).dp, y = (-30).dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(110.dp)
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-15).dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box {
                    Box(
                        modifier         = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(BlueMid.copy(alpha = 0.4f))
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model              = avatarUrl,
                                contentDescription = "Foto profil",
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Text(
                                text       = userName.take(1).uppercase(),
                                color      = Color.White,
                                fontSize   = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.BottomEnd)
                            .background(GreenOnline, CircleShape)
                            .padding(2.dp)
                    )
                }

                Column {
                    Text(
                        "Welcome Back",
                        color    = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                    Text(
                        "Hello, ${userName.split(" ").firstOrNull() ?: userName}!",
                        color      = Color.White,
                        fontSize   = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier         = Modifier
                    .size(46.dp)
                    .clickable { onNotifClick() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .align(Alignment.Center)
                )
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notifikasi",
                    tint     = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(AccentOrange, CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

@Composable
private fun NearbyEventsPlaceholder() {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors    = CardDefaults.cardColors(containerColor = BluePale.copy(alpha = 0.4f))
    ) {
        Box(
            modifier         = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.LocationOff,
                    contentDescription = null,
                    tint     = PrimaryBlue.copy(alpha = 0.5f),
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    "Belum ada kegiatan di sekitarmu",
                    fontSize   = 13.sp,
                    color      = TextMuted,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Coba cek kegiatan tersedia di bawah",
                    fontSize = 11.sp,
                    color    = TextMuted.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun HomeSectionHeader(title: String, subtitle: String?, onSeeAll: () -> Unit) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 4.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
            if (subtitle != null) Text(subtitle, fontSize = 11.5.sp, color = TextMuted)
        }
        TextButton(onClick = onSeeAll, contentPadding = PaddingValues(0.dp)) {
            Text("Lihat semua", fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Default.ChevronRight, null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
        }
    }
}
@Composable
private fun UpcomingScheduleList(
    schedules: List<com.example.prototypevolunteerapp.data.remote.dto.ScheduleEventDto>
) {
    Column(
        modifier            = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        schedules.forEach { event ->
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier              = Modifier.padding(14.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier         = Modifier
                            .size(42.dp)
                            .background(PrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint     = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = event.eventTitle,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextDark,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                        Text(
                            text       = event.organizerName,
                            fontSize   = 11.sp,
                            color      = PrimaryBlue,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Default.CalendarToday, null, tint = TextMuted, modifier = Modifier.size(10.dp))
                                Text(event.startDate, fontSize = 10.sp, color = TextMuted)
                            }
                            if (event.startTime != null) {
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(Icons.Default.Schedule, null, tint = TextMuted, modifier = Modifier.size(10.dp))
                                    val timeLabel = if (event.endTime != null)
                                        "${event.startTime} – ${event.endTime}"
                                    else event.startTime
                                    Text(timeLabel, fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(99.dp),
                        color = PrimaryBlue.copy(alpha = 0.1f)
                    ) {
                        Text(
                            "Upcoming",
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = PrimaryBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveRegistrationList(
    registrations: List<RegistrationDto>,
    onItemClick:   (RegistrationDto) -> Unit
) {
    Column(
        modifier            = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        registrations.take(3).forEach { reg ->
            val event = reg.event
            val (statusColor, statusLabel) = when (reg.status) {
                "confirmed" -> Pair(Color(0xFF0E7B6C), "Dikonfirmasi")
                "pending"   -> Pair(Color(0xFFD4900A), "Menunggu")
                "attended"  -> Pair(PrimaryBlue,       "Hadir")
                else        -> Pair(TextMuted,          reg.status)
            }
            Card(
                modifier  = Modifier.fillMaxWidth().clickable { onItemClick(reg) },
                shape     = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier              = Modifier.padding(14.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier         = Modifier
                            .size(42.dp)
                            .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Event, null, tint = statusColor, modifier = Modifier.size(20.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            event?.title ?: "Kegiatan",
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextDark,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                        Text(
                            event?.organization?.organization_name ?: "",
                            fontSize   = 11.sp,
                            color      = PrimaryBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Surface(shape = RoundedCornerShape(99.dp), color = statusColor.copy(alpha = 0.1f)) {
                        Text(
                            statusLabel,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = statusColor
                        )
                    }
                }
            }
        }
    }
}


@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun NearbyEventsPager(
    events:      List<ActivityData>,
    onItemClick: (ActivityData) -> Unit
) {
    if (events.isEmpty()) return

    val pageCount  = Int.MAX_VALUE
    val startIndex = pageCount / 2 - (pageCount / 2 % events.size)
    val pagerState = rememberPagerState(
        initialPage = startIndex,
        pageCount   = { pageCount }
    )

    Column {
        HorizontalPager(
            state             = pagerState,
            modifier          = Modifier.fillMaxWidth(),
            contentPadding    = PaddingValues(horizontal = 24.dp),
            pageSpacing       = 12.dp
        ) { page ->
            val realIndex = page % events.size
            val event     = events[realIndex]
            NearbyEventCard(event = event, onClick = { onItemClick(event) })
        }


        if (events.size > 1) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                repeat(events.size) { index ->
                    val isSelected = pagerState.currentPage % events.size == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 8.dp else 6.dp)
                            .background(
                                if (isSelected) PrimaryBlue else BluePale,
                                CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun NearbyEventCard(event: ActivityData, onClick: () -> Unit) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick() },
        shape     = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val context    = LocalContext.current
            val localResId = remember(event.imageRes) {
                context.resources.getIdentifier(event.imageRes, "drawable", context.packageName)
            }
            AsyncImage(
                model              = if (localResId != 0) localResId else event.imageRes.ifBlank { null },
                contentDescription = event.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                        )
                    )
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(99.dp),
                color = PrimaryBlue
            ) {
                Text(
                    "Nearby Events",
                    modifier   = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White
                )
            }

            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        event.title,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = Color.White.copy(0.8f), modifier = Modifier.size(12.dp))
                        Text(event.location, fontSize = 11.sp, color = Color.White.copy(0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Spacer(Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("View Detail", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ChevronRight, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChipRow(
    categories:    List<String>,
    selectedChip:  String?,
    onChipSelected:(String?) -> Unit
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedChip == null,
            onClick  = { onChipSelected(null) },
            label    = { Text("All", fontSize = 12.sp) },
            colors   = FilterChipDefaults.filterChipColors(
                selectedContainerColor = PrimaryBlue,
                selectedLabelColor     = Color.White
            ),
            shape = RoundedCornerShape(99.dp)
        )
        categories.forEach { cat ->
            val isSelected = selectedChip == cat
            FilterChip(
                selected = isSelected,
                onClick  = { onChipSelected(if (isSelected) null else cat) },
                label    = { Text(cat, fontSize = 12.sp) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor     = Color.White
                ),
                shape = RoundedCornerShape(99.dp)
            )
        }
    }
}

@Composable
private fun EventVerticalList(
    events:      List<ActivityData>,
    onItemClick: (ActivityData) -> Unit
) {
    Column(
        modifier            = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        events.take(5).forEach { event ->
            EventListCard(activity = event, onClick = { onItemClick(event) })
        }
    }
}

@Composable
private fun EventListCard(activity: ActivityData, onClick: () -> Unit) {
    val context    = LocalContext.current
    val localResId = remember(activity.imageRes) {
        context.resources.getIdentifier(activity.imageRes, "drawable", context.packageName)
    }

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model              = if (localResId != 0) localResId else activity.imageRes.ifBlank { null },
                    contentDescription = activity.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Text(
                        activity.title,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextDark,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f),
                        lineHeight = 17.sp
                    )
                    if (!activity.category.isNullOrBlank()) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(99.dp),
                            color = PrimaryBlue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                activity.category,
                                modifier   = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                fontSize   = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = PrimaryBlue
                            )
                        }
                    }
                }

                if (!activity.organizationName.isNullOrBlank()) {
                    Text(
                        activity.organizationName,
                        fontSize   = 11.sp,
                        color      = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = PrimaryBlue, modifier = Modifier.size(11.dp))
                    Text(activity.location, fontSize = 10.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                if (!activity.duration.isNullOrBlank()) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Default.Schedule, null, tint = TextMuted, modifier = Modifier.size(11.dp))
                        Text(activity.duration, fontSize = 10.sp, color = TextMuted)
                    }
                }
                if (activity.remainingQuota != null) {
                    Text(
                        "${activity.remainingQuota} spots left",
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = AccentOrange
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendationCardRow(
    items:       List<ActivityData>,
    onItemClick: (ActivityData) -> Unit
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { event ->
            RecommendationCard(
                event    = event,
                onClick  = { onItemClick(event) },
                modifier = Modifier.width(200.dp)
            )
        }
    }
}
@Composable
private fun VolunteerBottomBar(
    navItems:    List<BottomNavItem>,
    selectedTab: Int,
    onSelect:    (Int) -> Unit
) {
    Surface(
        modifier       = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        shape          = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color          = Color.White,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().navigationBarsPadding().height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            navItems.forEachIndexed { index, item ->
                val isSelected = selectedTab == index
                val tint = if (isSelected) NavyDark else TextMuted

                Column(
                    modifier            = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelect(index) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isSelected) {
                        Box(modifier = Modifier.width(20.dp).height(3.dp).background(NavyDark, RoundedCornerShape(50)))
                        Spacer(Modifier.height(4.dp))
                    } else {
                        Spacer(Modifier.height(7.dp))
                    }
                    Icon(
                        if (isSelected) item.selectedIcon else item.unselectedIcon,
                        item.label,
                        tint     = tint,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        item.label,
                        fontSize   = 10.sp,
                        color      = tint,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}