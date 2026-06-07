package com.example.prototypevolunteerapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import coil.request.ImageRequest
import coil.request.CachePolicy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.Routes
import com.example.prototypevolunteerapp.ui.theme.TextDark

private val HeaderStart   = Color(0xFF3D5C2A)
private val HeaderEnd     = Color(0xFF5A7A5A)
private val BgColor       = Color(0xFFF4F7EF)
private val CardWhite     = Color(0xFFFFFFFF)
private val AccentGreen   = Color(0xFF5A7A5A)
private val TextPrimary   = Color(0xFF1E2D1E)
private val TextSecondary = Color(0xFF6E8F6E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val uiState   by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) {
            viewModel.onNavigateBackHandled()
            if (uiState.isLogoutAction) {
                while (backStack.isNotEmpty()) { backStack.removeLastOrNull() }
                backStack.add(Routes.WelcomeRoute)
            } else {
                backStack.removeLastOrNull()
            }
        }
    }

    if (!uiState.isLoggedIn && !uiState.shouldNavigateBack) return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Profil Saya", fontWeight = FontWeight.Bold,
                        fontSize = 18.sp, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                            tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { backStack.add(Routes.EditProfileRoute) }) {
                        Icon(Icons.Default.Edit, "Edit Profil", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.onLogout() }) {
                        Icon(Icons.Default.Logout, "Logout", tint = Color(0xFFFFCDD2))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderStart)
            )
        },
        containerColor = BgColor
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentGreen)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(HeaderStart, HeaderEnd)))
                    .padding(bottom = 32.dp)
            ) {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFFDAEFDC)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!uiState.avatarUrl.isNullOrBlank()) {
                            val context = LocalContext.current
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(uiState.avatarUrl)
                                    .diskCachePolicy(CachePolicy.DISABLED)
                                    .memoryCachePolicy(CachePolicy.DISABLED)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = uiState.userName,
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text       = uiState.userName
                                    .firstOrNull()
                                    ?.uppercaseChar()
                                    ?.toString() ?: "?",
                                fontSize   = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color      = AccentGreen
                            )
                        }
                    }

                    Text(uiState.userName,
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White)

                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Email, null,
                            tint = Color(0xFF9EB589), modifier = Modifier.size(13.dp))
                        Text(uiState.userEmail, fontSize = 12.sp, color = Color(0xFF9EB589))
                    }
                    if (!uiState.locationLabel.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.LocationOn, null,
                                tint = Color(0xFF9EB589), modifier = Modifier.size(13.dp))
                            Text(uiState.locationLabel!!, fontSize = 12.sp,
                                color = Color(0xFF9EB589))
                        }
                    }

                    if (!uiState.genderLabel.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Text(uiState.genderLabel!!,
                                fontSize = 11.sp, color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(Modifier.weight(1f), "Diikuti",  uiState.totalEventsJoined.toString(), Icons.Default.CheckCircle)
                        StatCard(Modifier.weight(1f), "Pending",  uiState.pendingRegistrations.toString(), Icons.Default.HourglassEmpty)
                        StatCard(Modifier.weight(1f), "Tersimpan", uiState.savedCount.toString(), Icons.Default.Bookmark)
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-20).dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                if (!uiState.hasProfile) {
                    ProfileCard(title = "Lengkapi Profil Kamu") {
                        Text(
                            "Profil kamu masih kosong. Klik ikon edit di atas untuk mengisi data diri.",
                            fontSize   = 14.sp,
                            color      = TextSecondary,
                            lineHeight = 21.sp
                        )
                    }
                }

                if (uiState.totalEventsJoined != null) {
                    ProfileCard(title = "Rekam Jejak") {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    uiState.totalEventsJoined.toString(),
                                    fontSize   = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = AccentGreen
                                )
                                Text("Kegiatan Diikuti",
                                    fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }

                if (!uiState.bio.isNullOrBlank()) {
                    ProfileCard(title = "Tentang Saya") {
                        Text(uiState.bio!!,
                            fontSize   = 14.sp,
                            color      = TextDark,
                            lineHeight = 21.sp)
                    }
                }
                ProfileCard(title = "Kontak") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (!uiState.userPhone.isNullOrBlank()) {
                            InfoRow(Icons.Default.Phone,
                                "WhatsApp", uiState.userPhone!!)
                        }
                        InfoRow(Icons.Default.Email, "Email", uiState.userEmail)
                        if (!uiState.dateOfBirth.isNullOrBlank()) {
                            InfoRow(Icons.Default.Cake,
                                "Tanggal Lahir", uiState.dateOfBirth!!)
                        }
                        if (!uiState.locationLabel.isNullOrBlank()) {
                            InfoRow(Icons.Default.LocationOn,
                                "Kota", uiState.locationLabel!!)
                        }
                    }
                }
                if (uiState.skills.isNotEmpty()) {
                    ProfileCard(title = "Keahlian") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement   = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.skills.forEach { skill ->
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFDAEFDC),
                                            RoundedCornerShape(50.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(skill, fontSize = 12.sp,
                                        color      = AccentGreen,
                                        fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                if (uiState.interests.isNotEmpty()) {
                    ProfileCard(title = "Minat") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement   = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.interests.forEach { interest ->
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFFF3CD),
                                            RoundedCornerShape(50.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(interest, fontSize = 12.sp,
                                        color      = Color(0xFF7A5C00),
                                        fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
                ProfileCard(title = "Aktivitas Saya") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ProfileMenuRow(
                            icon    = Icons.Default.Bookmark,
                            label   = "Kegiatan Tersimpan",
                            onClick = { backStack.add(Routes.SavedActivitiesRoute) }
                        )
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        ProfileMenuRow(
                            icon    = Icons.Default.Favorite,
                            label   = "Kegiatan Disukai",
                            onClick = { backStack.add(Routes.LikedActivitiesRoute) }
                        )
                    }
                }
                if (!uiState.errorMessage.isNullOrBlank()) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier              = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF3CD), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Icon(Icons.Default.Info, null,
                            tint = Color(0xFF7A5C00), modifier = Modifier.size(14.dp))
                        Text(uiState.errorMessage!!,
                            fontSize = 12.sp, color = Color(0xFF7A5C00))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProfileCard(
    title:   String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title,
                fontSize      = 12.sp,
                fontWeight    = FontWeight.SemiBold,
                color         = TextSecondary,
                letterSpacing = 0.5.sp)
            content()
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(36.dp)
                .background(Color(0xFFDAEFDC), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = AccentGreen, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Text(value, fontSize = 13.sp, color = TextPrimary,
                fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon:    ImageVector,
    label:   String,
    onClick: () -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(36.dp)
                    .background(Color(0xFFDAEFDC), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = AccentGreen, modifier = Modifier.size(18.dp))
            }
            Text(label, fontSize = 14.sp,
                color = TextPrimary, fontWeight = FontWeight.Medium)
        }
        Icon(Icons.Default.ChevronRight, null,
            tint = TextSecondary, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun StatCard(
    modifier:  Modifier,
    label:     String,
    value:     String,
    icon:      ImageVector
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null,
                tint     = AccentGreen,
                modifier = Modifier.size(18.dp))
            Text(value,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary)
            Text(label,
                fontSize  = 10.sp,
                color     = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}