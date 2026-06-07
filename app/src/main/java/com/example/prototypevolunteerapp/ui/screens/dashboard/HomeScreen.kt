package com.example.prototypevolunteerapp.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.prototypevolunteerapp.data.model.ActivityData
import com.example.prototypevolunteerapp.data.remote.dto.RegistrationDto

private val NavyDark    = Color(0xFF1E3A8A)
private val PrimaryBlue = Color(0xFF3B82F6)
private val BlueMid     = Color(0xFF60A5FA)
private val BluePale    = Color(0xFFBFDBFE)
private val BlueGhost   = Color(0xFFEFF6FF)
private val AccentOrange= Color(0xFFE8501A)
private val BgScreen    = Color(0xFFF8FAFF)
private val TextDark    = Color(0xFF0F172A)
private val TextMuted   = Color(0xFF64748B)

private data class BottomNavItem(
    val label:         String,
    val selectedIcon:  ImageVector,
    val unselectedIcon:ImageVector,
    val route:         NavKey
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val uiState   by viewModel.uiState.collectAsState()
    val currentUser = viewModel.currentUser
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.refreshUser()
    }
    val navItems = listOf(
        BottomNavItem("Beranda",    Icons.Filled.Home,              Icons.Outlined.Home,             Routes.HomeRoute),
        BottomNavItem("Kegiatan",   Icons.Filled.VolunteerActivism, Icons.Outlined.VolunteerActivism, Routes.ActivitiesRoute),
        BottomNavItem("Riwayat",    Icons.Filled.History,           Icons.Outlined.History,           Routes.ActivityHistoryRoute),
        BottomNavItem("Notifikasi", Icons.Filled.Notifications,     Icons.Outlined.NotificationsNone, Routes.NotificationsRoute),
        BottomNavItem("Profil",     Icons.Filled.Person,            Icons.Outlined.Person,            Routes.ProfileRoute)
    )

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshUser()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        bottomBar = {
            VolunteerBottomBar(
                navItems    = navItems,
                selectedTab = uiState.selectedTab,
                unreadCount = uiState.unreadNotifCount,
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
                    onNotifClick   = { backStack.add(Routes.NotificationsRoute) },
                    onSearchClick  = { backStack.add(Routes.ActivitiesRoute) }
                )
            }
            item(key = "stats") {
                HomeStatRow(
                    activeCount    = uiState.activeRegistrations.size,
                    completedCount = uiState.completedCount,
                    nearbyCount    = uiState.apiEvents.size
                )
            }

            if (uiState.activeRegistrations.isNotEmpty()) {
                item(key = "active_header") {
                    HomeSectionHeader(
                        title    = "Pendaftaranku",
                        subtitle = "Kegiatan yang sedang kamu ikuti",
                        onSeeAll = { backStack.add(Routes.ActivityHistoryRoute) }
                    )
                }
                item(key = "active_list") {
                    ActiveRegistrationList(
                        registrations = uiState.activeRegistrations,
                        onItemClick   = { reg ->
                            val event = reg.event ?: return@ActiveRegistrationList
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
            }
            if (uiState.nearbyEvents.isNotEmpty()) {
                item(key = "nearby_header") {
                    HomeSectionHeader(
                        title    = "Kegiatan di ${uiState.userCity ?: "Kotamu"}",
                        subtitle = null,
                        onSeeAll = { backStack.add(Routes.ActivitiesRoute) }
                    )
                }
                item(key = "nearby_events") {
                    EventHorizontalRow(
                        events     = uiState.nearbyEvents,
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
                        },
                        onSeeAll = { backStack.add(Routes.ActivitiesRoute) }
                    )
                }
            }

            item(key = "category_chips") {
                CategoryChipRow(
                    categories    = uiState.categoryChips,
                    selectedChip  = uiState.selectedCategory,
                    onChipSelected = { viewModel.onCategoryChipSelected(it) }
                )
            }

            item(key = "events_header") {
                HomeSectionHeader(
                    title    = "Kegiatan Tersedia",
                    subtitle = null,
                    onSeeAll = { backStack.add(Routes.ActivitiesRoute) }
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
                        EventHorizontalRow(
                            events    = uiState.apiEvents,
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
                            },
                            onSeeAll = { backStack.add(Routes.ActivitiesRoute) }
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
    onNotifClick:   () -> Unit,
    onSearchClick:  () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(NavyDark, Color(0xFF1A3575), PrimaryBlue)))
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 32.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier         = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(BlueMid.copy(alpha = 0.35f))
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
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Column {
                        Text(
                            "Selamat datang kembali,",
                            color    = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                        Text(
                            userName.split(" ").firstOrNull() ?: userName,
                            color      = Color.White,
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier         = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable { onNotifClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifikasi",
                        tint     = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .background(AccentOrange, CircleShape)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.14f))
                    .clickable { onSearchClick() }
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Search, null, tint = Color.White.copy(0.65f), modifier = Modifier.size(18.dp))
                Text("Cari kegiatan sosial...", color = Color.White.copy(0.55f), fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun HomeStatRow(
    activeCount:    Int,
    completedCount: Int,
    nearbyCount:    Int
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .offset(y = (-16).dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatMiniCard(
            modifier = Modifier.weight(1f),
            value    = activeCount.toString(),
            label    = "Aktif",
            color    = PrimaryBlue
        )
        StatMiniCard(
            modifier = Modifier.weight(1f),
            value    = completedCount.toString(),
            label    = "Selesai",
            color    = Color(0xFF0E7B6C)
        )
        StatMiniCard(
            modifier = Modifier.weight(1f),
            value    = nearbyCount.toString(),
            label    = "Tersedia",
            color    = Color(0xFFD4900A)
        )
    }
}

@Composable
private fun StatMiniCard(modifier: Modifier, value: String, label: String, color: Color) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 11.sp, color = TextMuted)
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
            if (subtitle != null) {
                Text(subtitle, fontSize = 11.5.sp, color = TextMuted)
            }
        }
        TextButton(onClick = onSeeAll, contentPadding = PaddingValues(0.dp)) {
            Text("Lihat semua", fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Default.ChevronRight, null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
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
                "attended"  -> Pair(PrimaryBlue,        "Hadir")
                else        -> Pair(TextMuted,           reg.status)
            }
            Card(
                modifier  = Modifier.fillMaxWidth().clickable { onItemClick(reg) },
                shape     = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier          = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
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
                            fontSize = 11.sp,
                            color    = PrimaryBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(99.dp),
                        color = statusColor.copy(alpha = 0.1f)
                    ) {
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
@Composable
private fun CategoryChipRow(
    categories:    List<String>,
    selectedChip:  String?,
    onChipSelected:(String?) -> Unit
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val allSelected = selectedChip == null
        FilterChip(
            selected = allSelected,
            onClick  = { onChipSelected(null) },
            label    = { Text("Semua", fontSize = 12.sp) },
            colors   = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NavyDark,
                selectedLabelColor     = Color.White
            )
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
                )
            )
        }
    }
}

@Composable
private fun EventHorizontalRow(
    events:      List<ActivityData>,
    onItemClick: (ActivityData) -> Unit,
    onSeeAll:    () -> Unit
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        events.take(8).forEach { event ->
            EventMiniCard(activity = event, onClick = { onItemClick(event) })
        }
    }
}

@Composable
private fun EventMiniCard(activity: ActivityData, onClick: () -> Unit) {
    val context    = LocalContext.current
    val localResId = remember(activity.imageRes) {
        context.resources.getIdentifier(activity.imageRes, "drawable", context.packageName)
    }
    Card(
        shape     = RoundedCornerShape(16.dp),
        modifier  = Modifier.width(168.dp).clickable(onClick = onClick),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                AsyncImage(
                    model              = if (localResId != 0) localResId else activity.imageRes.ifBlank { null },
                    contentDescription = activity.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.35f)))
                        )
                )
            }
            Column(
                modifier            = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    activity.title,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextDark,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = PrimaryBlue, modifier = Modifier.size(11.dp))
                    Text(activity.location, fontSize = 10.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun VolunteerBottomBar(
    navItems:    List<BottomNavItem>,
    selectedTab: Int,
    unreadCount: Int,
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
                    Box {
                        Icon(
                            if (isSelected) item.selectedIcon else item.unselectedIcon,
                            item.label,
                            tint     = tint,
                            modifier = Modifier.size(22.dp)
                        )
                        if (index == 3 && unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(AccentOrange, CircleShape)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
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