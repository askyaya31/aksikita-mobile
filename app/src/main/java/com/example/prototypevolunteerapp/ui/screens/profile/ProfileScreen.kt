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
import androidx.compose.material.icons.filled.Chat
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.ui.platform.LocalContext
import coil.request.CachePolicy
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
import com.example.prototypevolunteerapp.ui.components.ProfileMenuItem

private val HeaderStart    = Color(0xFF2B5CE6)
private val HeaderEnd      = Color(0xFF5B8DEF)
private val BgColor        = Color(0xFFEEF3FF)
private val CardWhite      = Color(0xFFFFFFFF)
private val AccentBlue     = Color(0xFF3D7BF5)
private val TextPrimary    = Color(0xFF1A1F36)
private val TextSecondary  = Color(0xFF6B7280)
private val ChipBlue       = Color(0xFFDDE8FF)
private val ChipBlueText   = Color(0xFF1A3A8F)
private val logout   = Color(0xFFB62121)
private val AccentGreen    = AccentBlue
private val ChipGreen      = ChipBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val backStack = LocalBackStack.current
    val uiState   by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false)}
    val unreadChatCount by viewModel.unreadChatCount.collectAsState(initial = 0)

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

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor   = Color(0xFFDDE8FF),
            shape            = RoundedCornerShape(20.dp),
            icon             = {
                Box(
                    modifier         = Modifier
                        .size(48.dp)
                        .background(Color(0xFFDDE8FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = null,
                        tint               = logout,
                        modifier           = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    "Keluar dari Akun?",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp,
                    color      = TextPrimary
                )
            },
            text = {
                Text(
                    "Kamu akan keluar dari AksiKita. Yakin ingin melanjutkan?",
                    fontSize   = 13.sp,
                    color      = TextSecondary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = logout),
                    shape  = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ya, Keluar", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    shape   = RoundedCornerShape(12.dp),
                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = logout),
                    border  = androidx.compose.foundation.BorderStroke(1.dp, logout),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Batal", fontSize = 13.sp)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile Account",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 17.sp,
                        color      = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, "Logout", tint = Color(0xFFB62121))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderStart)
            )
        },
        containerColor = BgColor
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentBlue)
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
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(ChipGreen),
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
                                text       = uiState.userName.firstOrNull()
                                    ?.uppercaseChar()?.toString() ?: "?",
                                fontSize   = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color      = AccentGreen
                            )
                        }
                    }
                    Text(
                        uiState.userName,
                        fontSize   = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Email, null,
                            tint     = Color(0xFFB3CFFF),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(uiState.userEmail, fontSize = 11.sp, color = Color(0xFFB3CFFF))
                    }

                    if (!uiState.locationLabel.isNullOrBlank()) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn, null,
                                tint     = Color(0xFFB3CFFF),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                uiState.locationLabel!!,
                                fontSize = 11.sp,
                                color    = Color(0xFFB3CFFF)
                            )
                        }
                    }
                    if (!uiState.genderLabel.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Text(
                                uiState.genderLabel!!,
                                fontSize = 10.sp,
                                color    = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    OutlinedButton(
                        onClick = { backStack.add(Routes.EditProfileRoute) },
                        shape  = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, Color.White.copy(alpha = 0.6f)
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit, null,
                            modifier = Modifier.size(11.dp),
                            tint     = Color.White
                        )
                        Spacer(Modifier.width(5.dp))
                        Text("Edit Profile", fontSize = 11.sp, color = Color.White)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-16).dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!uiState.hasProfile) {
                    ProfileCard(title = "Lengkapi Profil Kamu") {
                        Text(
                            "Profil kamu masih kosong. Klik Edit Profile untuk mengisi data diri.",
                            fontSize   = 13.sp,
                            color      = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
                if (!uiState.bio.isNullOrBlank()) {
                    LabeledCard(label = "About Me") {
                        Text(
                            uiState.bio!!,
                            fontSize   = 12.sp,
                            color      = TextPrimary,
                            lineHeight = 19.sp
                        )
                    }
                }
                if (uiState.skills.isNotEmpty()) {
                    LabeledCard(label = "Skills") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement   = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.skills.forEach { skill ->
                                Box(
                                    modifier = Modifier
                                        .background(ChipBlue, RoundedCornerShape(50.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        skill,
                                        fontSize   = 10.sp,
                                        color      = ChipBlueText,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                if (uiState.interests.isNotEmpty()) {
                    LabeledCard(label = "Interests") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement   = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.interests.forEach { interest ->
                                Box(
                                    modifier = Modifier
                                        .background(ChipBlue, RoundedCornerShape(50.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        interest,
                                        fontSize   = 10.sp,
                                        color      = ChipBlueText,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                LabeledCard(label = "Contacts") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (!uiState.userPhone.isNullOrBlank()) {
                            InfoRow(Icons.Default.Phone, "WhatsApp", uiState.userPhone!!)
                        }
                        InfoRow(Icons.Default.Email, "Email", uiState.userEmail)
                        if (!uiState.dateOfBirth.isNullOrBlank()) {
                            InfoRow(Icons.Default.Cake, "Tanggal Lahir", uiState.dateOfBirth!!)
                        }
                        if (!uiState.locationLabel.isNullOrBlank()) {
                            InfoRow(Icons.Default.LocationOn, "Kota", uiState.locationLabel!!)
                        }
                    }
                }

                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        ProfileMenuRow(
                            icon    = Icons.Default.Favorite,
                            label   = "Favorites",
                            onClick = { backStack.add(Routes.LikedActivitiesRoute) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color    = Color(0xFFEEF3FF)
                        )
                        ProfileMenuRow(
                            icon    = Icons.Default.Bookmark,
                            label   = "Saved Activities",
                            onClick = { backStack.add(Routes.SavedActivitiesRoute) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color    = Color(0xFFEEF3FF)
                        )
                        ProfileMenuRow(
                            icon    = Icons.Default.History,
                            label   = "My Activities",
                            onClick = { backStack.add(Routes.ActivityHistoryRoute) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color    = Color(0xFFEEF3FF)
                        )
                        ProfileMenuRow(
                            icon    = Icons.Default.HelpOutline,
                            label   = "Help / FAQ",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/file/d/1jipKk1JPAuyK7f_x7umv3FJwzRSZEQlP/view?usp=sharing"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                if (!uiState.errorMessage.isNullOrBlank()) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier              = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFDDE8FF), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Info, null,
                            tint     = ChipBlueText,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            uiState.errorMessage!!,
                            fontSize = 12.sp,
                            color    = ChipBlueText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


@Composable
private fun LabeledCard(
    label:   String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier  = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(
                modifier            = Modifier.padding(
                    start  = 16.dp,
                    end    = 16.dp,
                    top    = 20.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                content()
            }
        }
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .background(AccentBlue, RoundedCornerShape(50.dp))
                .padding(horizontal = 14.dp, vertical = 5.dp)
        ) {
            Text(
                label,
                fontSize   = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White
            )
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
            Text(
                title,
                fontSize      = 12.sp,
                fontWeight    = FontWeight.SemiBold,
                color         = TextSecondary,
                letterSpacing = 0.5.sp
            )
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
                .size(32.dp)
                .background(ChipBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = AccentBlue, modifier = Modifier.size(16.dp))
        }
        Column {
            Text(label, fontSize = 10.sp, color = TextSecondary)
            Text(value, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
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
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(32.dp)
                    .background(ChipBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = AccentBlue, modifier = Modifier.size(16.dp))
            }
            Text(label, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        }
        Icon(
            Icons.Default.ChevronRight, null,
            tint     = TextSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}
