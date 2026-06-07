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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
    val uiState  by viewModel.uiState.collectAsState()
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

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            icon = {
                Icon(
                    Icons.Default.Cancel,
                    contentDescription = null,
                    tint     = Color(0xFFCC4444),
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text("Batalkan Pendaftaran?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Kamu yakin ingin membatalkan pendaftaran untuk kegiatan ini? " +
                            "Tindakan ini tidak bisa dibatalkan.",
                    fontSize   = 14.sp,
                    lineHeight = 21.sp,
                    color      = Color(0xFF555555)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        viewModel.onCancelRegistration()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC4444)),
                    shape  = RoundedCornerShape(10.dp)
                ) {
                    Text("Ya, Batalkan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Tidak", color = Color(0xFF555555))
                }
            }
        )
    }

    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onFeedbackShown()
        }
    }

    val displayTitle    = uiState.event?.title    ?: title    ?: "Kegiatan"
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
    val displayCategories   = uiState.event?.categories ?: emptyList()
    val displayOrg          = uiState.event?.organization
    val displayRequirements = uiState.event?.requirements
    val displayCreatedAt    = uiState.event?.created_at

    if (showOrgSheet && displayOrg != null) {
        OrgDetailBottomSheet(
            org          = displayOrg,
            requirements = displayRequirements,
            createdAt    = displayCreatedAt,
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detail Kegiatan", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    val likesCount by viewModel.likesCount.collectAsState()
                    val isLiked    by viewModel.isLiked.collectAsState()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (likesCount > 0) {
                            Text("$likesCount", fontSize = 12.sp, color = Color.White)
                            Spacer(Modifier.width(2.dp))
                        }
                        IconButton(onClick = { viewModel.toggleLike(uiState.event?.id ?: 0) }) {
                            Icon(
                                imageVector        = if (isLiked) Icons.Default.Favorite
                                else Icons.Default.FavoriteBorder,
                                contentDescription = if (isLiked) "Unlike" else "Like",
                                tint               = if (isLiked) Color(0xFFE53935) else Color.White
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.onToggleSaved() }) {
                        Icon(
                            imageVector        = if (uiState.isSaved) Icons.Default.Bookmark
                            else Icons.Default.BookmarkBorder,
                            contentDescription = if (uiState.isSaved) "Hapus Simpanan" else "Simpan",
                            tint               = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = Color(0xFF3D5C2A),
                    titleContentColor          = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor     = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model              = if (localResId != 0) localResId else displayPoster,
                contentDescription = displayTitle,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxWidth().height(260.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text       = displayTitle,
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF1A1A1A),
                    lineHeight = 30.sp
                )

                if (displayCategories.isNotEmpty()) {
                    Row(
                        modifier              = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        displayCategories.forEach { category ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFEEF4E8)
                            ) {
                                Row(
                                    modifier              = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Label,
                                        null,
                                        tint     = Color(0xFF5A7A5A),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text       = category.name,
                                        fontSize   = 11.sp,
                                        color      = Color(0xFF3D5C2A),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                if (displayOrg != null) {
                    val isVerified = displayOrg.verification_status == "verified"
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier              = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF4F7EF), RoundedCornerShape(12.dp))
                            .clickable { showOrgSheet = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(40.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!displayOrg.logo.isNullOrBlank()) {
                                AsyncImage(
                                    model              = displayOrg.logo,
                                    contentDescription = "Logo ${displayOrg.organization_name}",
                                    contentScale       = ContentScale.Crop,
                                    modifier           = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Text(
                                    text       = displayOrg.organization_name.take(1).uppercase(),
                                    fontSize   = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = Color(0xFF3D5C2A)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = displayOrg.organization_name,
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = Color(0xFF1A1A1A),
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis
                            )
                            val orgLocation = listOfNotNull(displayOrg.city, displayOrg.province)
                                .joinToString(", ")
                            if (orgLocation.isNotBlank()) {
                                Text(orgLocation, fontSize = 11.sp, color = Color(0xFF6E8F6E))
                            }
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isVerified) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                            ) {
                                Row(
                                    modifier              = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        if (isVerified) Icons.Default.Verified else Icons.Default.HourglassEmpty,
                                        null,
                                        tint     = if (isVerified) Color(0xFF2E7D32) else Color(0xFFE65100),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        if (isVerified) "Terverifikasi" else "Belum Terverifikasi",
                                        fontSize   = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color      = if (isVerified) Color(0xFF2E7D32) else Color(0xFFE65100)
                                    )
                                }
                            }
                            Text(
                                "Lihat profil →",
                                fontSize = 10.sp,
                                color    = Color(0xFF5A7A5A)
                            )
                        }
                    }
                }

                if (displayLocation.isNotBlank()) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier              = Modifier
                            .background(Color(0xFFF4F7EF), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, null,
                            tint = Color(0xFF5A7A5A), modifier = Modifier.size(16.dp))
                        Text(displayLocation, fontSize = 13.sp,
                            color = Color(0xFF5A7A5A), fontWeight = FontWeight.Medium)
                    }
                }

                if (displayStartDate != null) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, null,
                            tint = Color(0xFF5A7A5A), modifier = Modifier.size(16.dp))
                        Text(
                            text = if (displayEndDate != null && displayEndDate != displayStartDate)
                                "$displayStartDate – $displayEndDate"
                            else displayStartDate,
                            fontSize = 13.sp,
                            color    = Color(0xFF5A7A5A)
                        )
                    }
                }

                if (displayQuota != null) {
                    val isFull = uiState.event?.is_full == true
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Group, null,
                            tint     = if (isFull) Color(0xFFCC4444) else Color(0xFF5A7A5A),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isFull) "Kuota penuh"
                            else "Sisa kuota: ${displayRemaining ?: "?"} / $displayQuota",
                            fontSize = 13.sp,
                            color    = if (isFull) Color(0xFFCC4444) else Color(0xFF5A7A5A)
                        )
                    }
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
                        shape = RoundedCornerShape(20.dp),
                        color = badgeColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text       = badgeLabel,
                            color      = badgeColor,
                            fontSize   = 12.sp,
                            modifier   = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                HorizontalDivider(
                    color    = Color(0xFFEEEEEE),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text("Tentang Kegiatan", fontSize = 15.sp,
                    fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                Text(
                    text       = displayDesc.ifBlank { "Tidak ada deskripsi." },
                    fontSize   = 14.sp,
                    color      = Color(0xFF375422),
                    lineHeight = 23.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
                if (!displayRequirements.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text       = "Persyaratan Peserta",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF1A1A1A)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF4F7EF), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text       = displayRequirements,
                            fontSize   = 14.sp,
                            color      = Color(0xFF444444),
                            lineHeight = 22.sp
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                                border   = androidx.compose.foundation.BorderStroke(
                                    1.5.dp, Color(0xFFCC4444)
                                )
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color       = Color(0xFFCC4444)
                                    )
                                } else {
                                    Icon(Icons.Default.Close, null,
                                        tint = Color(0xFFCC4444), modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Batalkan Pendaftaran",
                                        color = Color(0xFFCC4444), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        isFull -> {
                            Button(
                                onClick  = {},
                                enabled  = false,
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                shape    = RoundedCornerShape(12.dp)
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
                                shape    = RoundedCornerShape(12.dp),
                                colors   = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF5A7A5A)
                                )
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color       = Color.White
                                    )
                                } else {
                                    Icon(Icons.Default.HowToReg, null,
                                        modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        if (uiState.isLoggedIn) "Daftar Kegiatan"
                                        else "Login untuk Daftar",
                                        fontWeight = FontWeight.Bold,
                                        fontSize   = 15.sp
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
                        border   = androidx.compose.foundation.BorderStroke(
                            1.5.dp, Color(0xFF5A7A5A)
                        )
                    ) {
                        Icon(Icons.Default.CameraAlt, null,
                            tint = Color(0xFF5A7A5A), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Lihat Instagram",
                            color = Color(0xFF5A7A5A), fontWeight = FontWeight.Bold)
                    }
                }

                if (!link.isNullOrEmpty()) {
                    Button(
                        onClick  = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A9A6A))
                    ) {
                        Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Buka Link Pendaftaran",
                            fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            AppFooter()
            Spacer(modifier = Modifier.height(16.dp))
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
                    .background(Color(0xFFDAEFDC), RoundedCornerShape(50.dp))
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
                        .background(Color(0xFFEEF4E8)),
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
                            text       = org.organization_name.take(1).uppercase(),
                            fontSize   = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFF3D5C2A)
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text       = org.organization_name,
                        fontSize   = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF1A1A1A)
                    )
                    val location = listOfNotNull(org.city, org.province).joinToString(", ")
                    if (location.isNotBlank()) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, null,
                                tint = Color(0xFF5A7A5A), modifier = Modifier.size(12.dp))
                            Text(location, fontSize = 12.sp, color = Color(0xFF6E8F6E))
                        }
                    }
                    val isVerified = org.verification_status == "verified"
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isVerified) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    ) {
                        Row(
                            modifier              = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                if (isVerified) Icons.Default.Verified
                                else Icons.Default.HourglassEmpty,
                                null,
                                tint     = if (isVerified) Color(0xFF2E7D32) else Color(0xFFE65100),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text       = if (isVerified) "Organisasi Terverifikasi"
                                else "Belum Terverifikasi",
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = if (isVerified) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                        }
                    }
                }
            }
            HorizontalDivider(
                color    = Color(0xFFEEEEEE),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (!org.description.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Info, null,
                            tint = Color(0xFF5A7A5A), modifier = Modifier.size(15.dp))
                        Text("Tentang", fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold, color = Color(0xFF3D5C2A))
                    }
                    Text(
                        text       = org.description,
                        fontSize   = 13.sp,
                        color      = Color(0xFF444444),
                        lineHeight = 20.sp
                    )
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
                        .background(Color(0xFFF4F7EF), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Informasi Kontak", fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold, color = Color(0xFF3D5C2A))

                    if (!org.address.isNullOrBlank()) {
                        OrgInfoRow(Icons.Default.LocationOn, org.address!!)
                    }
                    if (!org.user?.phone.isNullOrBlank()) {
                        OrgInfoRow(Icons.Default.Phone, org.user!!.phone!!)
                    }
                    if (!org.user?.email.isNullOrBlank()) {
                        OrgInfoRow(Icons.Default.Email, org.user!!.email)
                    }
                }
            }

            if (!org.website.isNullOrBlank()) {
                Button(
                    onClick  = { onOpenLink(org.website!!) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D5C2A))
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
        Icon(icon, null, tint = Color(0xFF5A7A5A), modifier = Modifier.size(14.dp))
        Text(
            text     = text,
            fontSize = 12.sp,
            color    = Color(0xFF444444),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}