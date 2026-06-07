package com.example.prototypevolunteerapp.ui.screens.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

private val BgColor       = Color(0xFFF4F7EF)
private val HeaderStart   = Color(0xFF3D5C2A)
private val HeaderEnd     = Color(0xFF5A7A5A)
private val AccentGreen   = Color(0xFF5A7A5A)
private val CardWhite     = Color(0xFFFFFFFF)
private val TextPrimary   = Color(0xFF1E2D1E)
private val TextSecondary = Color(0xFF6E8F6E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrgProfileScreen(
    viewModel: OrgProfileViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val profile   by viewModel.profileState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadProfile() }

    Scaffold(
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text("Profil Organisasi", fontWeight = FontWeight.Bold,
                        fontSize = 18.sp, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { backStack.add(Routes.EditOrgProfileRoute) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profil",
                            tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderStart)
            )
        }
    ) { padding ->

        if (profile.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator(color = AccentGreen)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            item(key = "header") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(HeaderStart, HeaderEnd)))
                        .padding(horizontal = 20.dp, vertical = 28.dp)
                ) {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .shadow(6.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            if (!profile.logoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model              = profile.logoUrl,
                                    contentDescription = "Logo organisasi",
                                    contentScale       = ContentScale.Crop,
                                    modifier           = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier         = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFDAEFDC)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text       = (profile.orgName.firstOrNull() ?: "O")
                                            .toString().uppercase(),
                                        color      = HeaderStart,
                                        fontSize   = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Text(
                            profile.orgName.ifBlank { "Nama Organisasi" },
                            color      = Color.White,
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(profile.email.ifBlank { "" },
                            color    = Color.White.copy(0.7f), fontSize = 13.sp)

                        val (verifLabel, verifBg, verifText) = when (profile.verificationStatus) {
                            "verified" -> Triple("✓ Terverifikasi", Color(0xFF2E5C1A).copy(0.25f), Color(0xFFACF2B8))
                            "rejected" -> Triple("✗ Ditolak Admin", Color(0xFFCC2222).copy(0.25f), Color(0xFFFFCDD2))
                            else       -> Triple("⏳ Menunggu Verifikasi", Color(0xFF7A5C00).copy(0.25f), Color(0xFFFFF3CD))
                        }
                        Surface(shape = RoundedCornerShape(20.dp), color = verifBg) {
                            Text(verifLabel, color = verifText, fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp))
                        }

                        OutlinedButton(
                            onClick = { backStack.add(Routes.EditOrgProfileRoute) },
                            shape   = RoundedCornerShape(50.dp),
                            colors  = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border  = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.6f)),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Edit Profil", fontSize = 13.sp)
                        }
                    }
                }
            }
            item(key = "desc") {
                OrgViewCard(title = "Tentang Organisasi", icon = Icons.Default.Info) {
                    if (profile.description.isNotBlank()) {
                        Text(profile.description, fontSize = 14.sp, color = TextPrimary,
                            lineHeight = 22.sp)
                    } else {
                        Text("Belum ada deskripsi. Tap Edit Profil untuk mengisi.",
                            fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }
            item(key = "contact") {
                OrgViewCard(title = "Kontak", icon = Icons.Default.ContactPhone) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (profile.phone.isNotBlank())
                            OrgInfoRow(Icons.Default.Phone,    "Telepon / WA",  profile.phone)
                        OrgInfoRow(Icons.Default.Email,        "Email",         profile.email)
                        if (profile.website.isNotBlank())
                            OrgInfoRow(Icons.Default.Language, "Website",       profile.website)
                        if (profile.address.isNotBlank())
                            OrgInfoRow(Icons.Default.LocationOn, "Alamat",      profile.address)
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
@Composable
private fun OrgViewCard(
    title:   String,
    icon:    ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            }
            HorizontalDivider(color = Color(0xFFEEEEEE))
            content()
        }
    }
}

@Composable
private fun OrgInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier.size(34.dp)
                .background(AccentGreen.copy(0.1f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = AccentGreen, modifier = Modifier.size(16.dp))
        }
        Column {
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Text(value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        }
    }
}