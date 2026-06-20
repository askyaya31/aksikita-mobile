package com.example.prototypevolunteerapp.ui.screens.liked

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.Routes
import com.example.prototypevolunteerapp.data.remote.dto.LikedEventDto
import kotlinx.coroutines.launch

private val NavyDark     = Color(0xFF1E3A8A)
private val PrimaryBlue  = Color(0xFF3B82F6)
private val BgScreen     = Color(0xFFF8FAFF)
private val TextDark     = Color(0xFF0F172A)
private val TextMuted    = Color(0xFF64748B)
private val AccentOrange = Color(0xFFE8501A)

@Composable
fun LikedActivitiesScreen(
    viewModel: LikedActivitiesViewModel = hiltViewModel()
) {
    val backStack         = LocalBackStack.current
    val uiState           by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            scope.launch { snackbarHostState.showSnackbar(msg) }
            viewModel.onToastShown()
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = BgScreen
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(NavyDark, PrimaryBlue)))
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 20.dp)
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
                    "Favorite",
                    color      = Color.White,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.align(Alignment.Center)
                )
            }

            if (!uiState.isLoading && uiState.errorMessage == null) {
                Text(
                    text       = "${uiState.likedEvents.size} Opportunities Found",
                    fontSize   = 13.sp,
                    color      = TextMuted,
                    fontWeight = FontWeight.Medium,
                    modifier   = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
            when {
                uiState.isLoading -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = PrimaryBlue) }
                }

                uiState.errorMessage != null -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.WifiOff, null,
                                tint = Color(0xFFB0B0B0), modifier = Modifier.size(48.dp))
                            Text(uiState.errorMessage!!, color = TextMuted, fontSize = 14.sp)
                            Button(
                                onClick = { viewModel.loadAll() },
                                colors  = ButtonDefaults.buttonColors(containerColor = NavyDark)
                            ) {
                                Icon(Icons.Default.Refresh, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Coba Lagi")
                            }
                        }
                    }
                }

                uiState.likedEvents.isEmpty() -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier            = Modifier.padding(32.dp)
                        ) {
                            Icon(Icons.Default.FavoriteBorder, null,
                                tint = Color(0xFFB0B0B0), modifier = Modifier.size(64.dp))
                            Text(
                                "Belum ada kegiatan yang disukai",
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = TextDark
                            )
                            Text(
                                "Sukai kegiatan yang menarik dengan menekan ikon hati",
                                fontSize  = 13.sp,
                                color     = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start  = 16.dp,
                            end    = 16.dp,
                            top    = 4.dp,
                            bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.likedEvents, key = { it.id }) { liked ->
                            LikedBigCard(
                                liked   = liked,
                                isSaved = uiState.savedIds.contains(liked.event_id),
                                onLike  = { viewModel.unlike(liked.event_id) },
                                onSave  = { viewModel.toggleSave(liked.event_id) },
                                onClick = {
                                    val event = liked.event ?: return@LikedBigCard
                                    backStack.add(
                                        Routes.ActivityDetailRoute(
                                            id       = event.id.toString(),
                                            slug     = event.slug ?: "",
                                            title    = event.title,
                                            location = buildString {
                                                if (!event.location_name.isNullOrBlank()) append(event.location_name)
                                                if (!event.city.isNullOrBlank()) {
                                                    if (isNotEmpty()) append(", ")
                                                    append(event.city)
                                                }
                                            }.ifBlank { "${event.city ?: ""}, ${event.province ?: ""}".trim(',', ' ') },
                                            desc     = event.description ?: "",
                                            imageRes = event.poster ?: ""
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
private fun LikedBigCard(
    liked:   LikedEventDto,
    isSaved: Boolean,
    onLike:  () -> Unit,
    onSave:  () -> Unit,
    onClick: () -> Unit
) {
    val event = liked.event ?: return
    val context    = LocalContext.current
    val localResId = remember(event.poster) {
        if (event.poster?.startsWith("http") == true) 0
        else context.resources.getIdentifier(
            event.poster ?: "", "drawable", context.packageName
        )
    }

    Card(
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        modifier  = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model              = if (localResId != 0) localResId
                    else event.poster.takeIf { !it.isNullOrBlank() },
                    contentDescription = event.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(0.3f))
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        .clickable { onLike() }
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Unlike",
                        tint     = Color(0xFFE53935),
                        modifier = Modifier.size(20.dp)
                    )
                }
                val categoryName = event.categories?.firstOrNull()?.name
                if (!categoryName.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp),
                        shape = RoundedCornerShape(99.dp),
                        color = PrimaryBlue
                    ) {
                        Text(
                            categoryName,
                            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color.White
                        )
                    }
                }
            }

            Column(
                modifier            = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Text(
                        event.title,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextDark,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f),
                        lineHeight = 19.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                if (isSaved) PrimaryBlue.copy(alpha = 0.1f)
                                else Color(0xFFF1F5F9),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSave() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isSaved) Icons.Default.Bookmark
                            else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save",
                            tint     = if (isSaved) PrimaryBlue else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                val orgName = event.organization?.organization_name
                if (!orgName.isNullOrBlank()) {
                    Text(orgName, fontSize = 12.sp,
                        color = PrimaryBlue, fontWeight = FontWeight.Medium)
                }

                val location = buildString {
                    if (!event.location_name.isNullOrBlank()) append(event.location_name)
                    if (!event.city.isNullOrBlank()) {
                        if (isNotEmpty()) append(", ")
                        append(event.city)
                    }
                }.ifBlank { "${event.city ?: ""}, ${event.province ?: ""}".trim(',', ' ') }

                if (location.isNotBlank()) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, null,
                            tint = TextMuted, modifier = Modifier.size(13.dp))
                        Text(location, fontSize = 11.sp, color = TextMuted,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                if (event.start_date != null) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Schedule, null,
                            tint = TextMuted, modifier = Modifier.size(13.dp))
                        Text(
                            text = if (event.end_date != null && event.end_date != event.start_date)
                                "${event.start_date} – ${event.end_date}"
                            else event.start_date,
                            fontSize = 11.sp, color = TextMuted
                        )
                    }
                }
                if (event.remaining_quota != null) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Group, null,
                                tint = AccentOrange, modifier = Modifier.size(13.dp))
                            Text(
                                "${event.remaining_quota} spots left",
                                fontSize   = 11.sp,
                                color      = AccentOrange,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Surface(
                            shape    = RoundedCornerShape(99.dp),
                            color    = NavyDark,
                            modifier = Modifier.clickable { onClick() }
                        ) {
                            Text(
                                "View Detail",
                                modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}