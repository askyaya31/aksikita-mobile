package com.example.prototypevolunteerapp.ui.screens.activities

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.Routes
import com.example.prototypevolunteerapp.data.remote.dto.OrgDto
import com.example.prototypevolunteerapp.ui.components.AppFooter

private val NavyDark     = Color(0xFF1E3A8A)
private val NavyLight     = Color(0xFF4159BE)
private val PrimaryBlue  = Color(0xFF3B82F6)
private val TextDark     = Color(0xFF0F172A)
private val TextMuted    = Color(0xFF64748B)
private val AccentOrange = Color(0xFFE8501A)
private val BgScreen     = Color(0xFFF8FAFF)
private val RedCancel    = Color(0xFFCC4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    id:        String,
    title:     String?,
    location:  String?,
    desc:      String?,
    imageRes:  String,
    instagram: String?,
    link:      String?,
    slug:      String = "",
    viewModel: ActivityDetailViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val context   = LocalContext.current
    val uiState   by viewModel.uiState.collectAsState()
    var showCancelDialog by remember { mutableStateOf(false) }
    var showOrgSheet     by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        viewModel.init(
            eventId   = id.toIntOrNull() ?: 0,
            slug      = slug,
            titleHint = title ?: "",
            descHint  = desc ?: ""
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onFeedbackShown()
        }
    }

    val isLikedInitialized = remember { mutableStateOf(false) }
    val isSavedInitialized = remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLiked) {
        if (!isLikedInitialized.value) { isLikedInitialized.value = true; return@LaunchedEffect }
        snackbarHostState.showSnackbar(
            if (uiState.isLiked) "Kegiatan disukai!" else "Batal menyukai kegiatan"
        )
    }

    LaunchedEffect(uiState.isSaved) {
        if (!isSavedInitialized.value) { isSavedInitialized.value = true; return@LaunchedEffect }
        snackbarHostState.showSnackbar(
            if (uiState.isSaved) "Kegiatan disimpan!" else "Kegiatan dihapus dari simpanan"
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            containerColor   = Color.White,
            iconContentColor = RedCancel,
            titleContentColor = TextDark,
            textContentColor  = Color(0xFF555555),
            icon  = {
                Icon(Icons.Default.Cancel, null,
                    tint = RedCancel, modifier = Modifier.size(28.dp))
            },
            title = { Text("Batalkan Pendaftaran?", fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    "Kamu yakin ingin membatalkan pendaftaran untuk kegiatan ini? Tindakan ini tidak bisa dibatalkan.",
                    fontSize = 14.sp, lineHeight = 21.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showCancelDialog = false; viewModel.onCancelRegistration() },
                    colors  = ButtonDefaults.buttonColors(containerColor = RedCancel),
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Ya, Batalkan", fontWeight = FontWeight.Bold, color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCancelDialog = false },
                    shape   = RoundedCornerShape(10.dp),
                    border  = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD))
                ) {
                    Text("Tidak", color = Color(0xFF555555))
                }
            }
        )
    }

    val displayTitle    = uiState.event?.title ?: title ?: "Kegiatan"
    val displayLocation = uiState.event?.let { ev ->
        buildString {
            if (!ev.location_name.isNullOrBlank()) append(ev.location_name)
            if (!ev.city.isNullOrBlank()) { if (isNotEmpty()) append(", "); append(ev.city) }
        }.ifBlank { location ?: "" }
    } ?: location ?: ""
    val displayDesc         = uiState.event?.description ?: desc ?: ""
    val displayPoster       = uiState.event?.poster ?: imageRes
    val displayQuota        = uiState.event?.quota
    val displayRemaining    = uiState.event?.remaining_quota
    val displayStatus       = uiState.event?.status
    val displayStartDate    = uiState.event?.start_date
    val displayEndDate      = uiState.event?.end_date
    val displayStartTime    = uiState.event?.start_time
    val displayEndTime      = uiState.event?.end_time
    val displayCategories   = uiState.event?.categories ?: emptyList()
    val displayOrg          = uiState.event?.organization
    val displayRequirements = uiState.event?.requirements

    if (showOrgSheet && displayOrg != null) {
        OrgDetailBottomSheet(
            org          = displayOrg,
            requirements = displayRequirements,
            createdAt    = uiState.event?.created_at,
            onDismiss    = { showOrgSheet = false },
            onOpenLink   = { url ->
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
            }
        )
    }

    val localResId = remember(displayPoster) {
        if (displayPoster.startsWith("http")) 0
        else context.resources.getIdentifier(displayPoster, "drawable", context.packageName)
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = BgScreen
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                AsyncImage(
                    model              = if (localResId != 0) localResId else displayPoster,
                    contentDescription = displayTitle,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(0.5f), Color.Transparent)
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(38.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .clickable { backStack.removeLastOrNull() }
                        .align(Alignment.TopStart),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint     = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(38.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .clickable { viewModel.toggleLike() }
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (uiState.isLiked) Icons.Default.Favorite
                        else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint     = if (uiState.isLiked) Color(0xFFE53935) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .offset(y = (-20).dp)
                    .padding(horizontal = 16.dp),
                shape     = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            displayCategories.forEach { category ->
                                Surface(
                                    shape = RoundedCornerShape(99.dp),
                                    color = PrimaryBlue.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        category.name,
                                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize   = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color      = PrimaryBlue
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    if (uiState.isSaved) PrimaryBlue.copy(alpha = 0.1f)
                                    else Color(0xFFF1F5F9),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.toggleSave() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (uiState.isSaved) Icons.Default.Bookmark
                                else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Save",
                                tint     = if (uiState.isSaved) PrimaryBlue else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        displayTitle,
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextDark,
                        lineHeight = 27.sp
                    )

                    if (displayOrg != null) {
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFF), RoundedCornerShape(10.dp))
                                .clickable { showOrgSheet = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier         = Modifier
                                    .size(36.dp)
                                    .shadow(2.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!displayOrg.logo.isNullOrBlank()) {
                                    AsyncImage(
                                        model              = displayOrg.logo,
                                        contentDescription = displayOrg.organization_name,
                                        contentScale       = ContentScale.Crop,
                                        modifier           = Modifier.fillMaxSize().clip(CircleShape)
                                    )
                                } else {
                                    Text(
                                        displayOrg.organization_name.take(1).uppercase(),
                                        fontSize   = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = NavyDark
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    displayOrg.organization_name,
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = TextDark,
                                    maxLines   = 1,
                                    overflow   = TextOverflow.Ellipsis
                                )
                                val isVerified = displayOrg.verification_status == "verified"
                                Text(
                                    if (isVerified) "Terverifikasi" else "Belum Terverifikasi",
                                    fontSize = 11.sp,
                                    color    = if (isVerified) Color(0xFF2E7D32) else TextMuted
                                )
                            }
                            Icon(Icons.Default.ChevronRight, null,
                                tint = TextMuted, modifier = Modifier.size(18.dp))
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    if (displayLocation.isNotBlank()) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, null,
                                tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                            Text(displayLocation, fontSize = 13.sp, color = TextMuted)
                        }
                    }

                    if (displayStartDate != null) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, null,
                                tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                            Text(
                                text = if (displayEndDate != null && displayEndDate != displayStartDate)
                                    "$displayStartDate – $displayEndDate"
                                else displayStartDate,
                                fontSize = 13.sp, color = TextMuted
                            )
                        }
                    }

                    if (displayStartTime != null) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Schedule, null,
                                tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                            Text(
                                text = if (displayEndTime != null)
                                    "$displayStartTime – $displayEndTime"
                                else displayStartTime,
                                fontSize = 13.sp, color = TextMuted
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        if (displayRemaining != null || displayStatus != null) {
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                if (displayRemaining != null) {
                                    val isFull = uiState.event?.is_full == true
                                    Row(
                                        verticalAlignment     = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(Icons.Default.Group, null,
                                            tint     = if (isFull) RedCancel else AccentOrange,
                                            modifier = Modifier.size(15.dp))
                                        Text(
                                            if (isFull) "Kuota penuh" else "$displayRemaining spot tersisa",
                                            fontSize   = 12.sp,
                                            color      = if (isFull) RedCancel else AccentOrange,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                } else {
                                    Spacer(Modifier.width(1.dp))
                                }

                                if (displayStatus != null) {
                                    val (badgeColor, badgeLabel) = when (displayStatus) {
                                        "published"      -> Color(0xFF4CAF50) to "Dibuka"
                                        "completed"      -> Color(0xFF9E9E9E) to "Selesai"
                                        "cancelled"      -> Color(0xFFF44336) to "Dibatalkan"
                                        "pending_review" -> Color(0xFFFF9800) to "Menunggu Review"
                                        else             -> Color(0xFF9E9E9E) to displayStatus
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(99.dp),
                                        color = badgeColor.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            badgeLabel,
                                            color      = badgeColor,
                                            fontSize   = 11.sp,
                                            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        if (uiState.registrationStatus == "confirmed" && uiState.chatRoomId != null) {
                            Button(
                                onClick = {
                                    backStack.add(
                                        Routes.ChatRoomRoute(
                                            roomId        = uiState.chatRoomId!!,
                                            eventTitle    = displayTitle,
                                            organizerName = displayOrg?.organization_name ?: "Organisasi"
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().height(35.dp),
                                shape    = RoundedCornerShape(15.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = NavyLight)
                            ) {
                                Icon(Icons.Outlined.Chat, null, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Chat Organisasi", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-12).dp)
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier            = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("About", fontSize = 15.sp,
                            fontWeight = FontWeight.Bold, color = TextDark)
                        Text(
                            displayDesc.ifBlank { "Tidak ada deskripsi." },
                            fontSize = 14.sp, color = TextMuted, lineHeight = 22.sp
                        )
                    }
                }

                if (!displayRequirements.isNullOrBlank()) {
                    Card(
                        shape     = RoundedCornerShape(16.dp),
                        colors    = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier            = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Requirements", fontSize = 15.sp,
                                fontWeight = FontWeight.Bold, color = TextDark)
                            Text(
                                displayRequirements,
                                fontSize = 14.sp, color = TextMuted, lineHeight = 22.sp
                            )
                        }
                    }
                }

                if (displayStatus == "published" || displayStatus == null) {
                    val isFull       = uiState.event?.is_full == true
                    val isRegistered = uiState.isRegistered
                    val isProcessing = uiState.isProcessing

                    when {
                        isRegistered -> {
                            OutlinedButton(
                                onClick  = { showCancelDialog = true },
                                enabled  = !isProcessing,
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                shape    = RoundedCornerShape(12.dp),
                                border   = androidx.compose.foundation.BorderStroke(1.5.dp, RedCancel)
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color       = RedCancel
                                    )
                                } else {
                                    Icon(Icons.Default.Close, null,
                                        tint = RedCancel, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Batalkan Pendaftaran",
                                        color = RedCancel, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        isFull -> {
                            Button(
                                onClick  = {},
                                enabled  = false,
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                shape    = RoundedCornerShape(99.dp)
                            ) {
                                Text("Kuota Penuh", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                        else -> {
                            Button(
                                onClick  = {
                                    if (uiState.isLoggedIn) viewModel.onRegisterToEvent()
                                    else backStack.add(Routes.LoginRoute)
                                },
                                enabled  = !isProcessing,
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                shape    = RoundedCornerShape(99.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color       = Color.White
                                    )
                                } else {
                                    Text(
                                        if (uiState.isLoggedIn) "Join Now!" else "Login untuk Daftar",
                                        fontWeight = FontWeight.Bold, fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                if (!instagram.isNullOrEmpty()) {
                    OutlinedButton(
                        onClick  = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, instagram.toUri()))
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape    = RoundedCornerShape(12.dp),
                        border   = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryBlue)
                    ) {
                        Icon(Icons.Default.CameraAlt, null,
                            tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Lihat Instagram", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    }
                }

                AppFooter()
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrgDetailBottomSheet(
    org:          OrgDto,
    requirements: String?,
    createdAt:    String?,
    onDismiss:    () -> Unit,
    onOpenLink:   (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Color.White,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp).height(4.dp)
                    .background(Color(0xFFE2E8F0), RoundedCornerShape(50.dp))
                    .align(Alignment.CenterHorizontally)
            )

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier         = Modifier
                        .size(64.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFFF8FAFF)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!org.logo.isNullOrBlank()) {
                        AsyncImage(
                            model              = org.logo,
                            contentDescription = org.organization_name,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Text(
                            org.organization_name.take(1).uppercase(),
                            fontSize   = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color      = NavyDark
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(org.organization_name,
                        fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    val location = listOfNotNull(org.city, org.province).joinToString(", ")
                    if (location.isNotBlank()) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, null,
                                tint = PrimaryBlue, modifier = Modifier.size(12.dp))
                            Text(location, fontSize = 12.sp, color = TextMuted)
                        }
                    }
                    val isVerified = org.verification_status == "verified"
                    Surface(
                        shape = RoundedCornerShape(99.dp),
                        color = if (isVerified) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    ) {
                        Row(
                            modifier              = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                if (isVerified) Icons.Default.Verified else Icons.Default.HourglassEmpty,
                                null,
                                tint     = if (isVerified) Color(0xFF2E7D32) else Color(0xFFE65100),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                if (isVerified) "Organisasi Terverifikasi" else "Belum Terverifikasi",
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = if (isVerified) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            if (!org.description.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Info, null,
                            tint = PrimaryBlue, modifier = Modifier.size(15.dp))
                        Text("Tentang", fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold, color = TextDark)
                    }
                    Text(org.description,
                        fontSize = 13.sp, color = TextMuted, lineHeight = 20.sp)
                }
            }

            val hasContact = !org.address.isNullOrBlank()
                    || !org.website.isNullOrBlank()
                    || !org.user?.phone.isNullOrBlank()
                    || !org.user?.email.isNullOrBlank()

            if (hasContact) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFF), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Informasi Kontak", fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold, color = TextDark)
                    if (!org.address.isNullOrBlank())
                        OrgInfoRow(Icons.Default.LocationOn, org.address!!)
                    if (!org.user?.phone.isNullOrBlank())
                        OrgInfoRow(Icons.Default.Phone, org.user!!.phone!!)
                    if (!org.user?.email.isNullOrBlank())
                        OrgInfoRow(Icons.Default.Email, org.user!!.email)
                }
            }

            if (!org.website.isNullOrBlank()) {
                Button(
                    onClick  = { onOpenLink(org.website!!) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = NavyDark)
                ) {
                    Icon(Icons.Default.Language, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Kunjungi Website", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun OrgInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
        Text(text, fontSize = 12.sp, color = TextMuted,
            maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}