package com.example.prototypevolunteerapp.ui.screens.organizer

import androidx.compose.foundation.background
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

private val BlueGradientStart = Color(0xFFBBE0FF) // Latar belakang atas
private val BlueGradientEnd   = Color(0xFFFFFFFF) // Latar belakang bawah
private val PrimaryBlue       = Color(0xFF2865FF) // Kartu header
private val FieldBgBlue       = Color(0xFFCBE2FF) // Background text field
private val TextDark          = Color(0xFF1E1E1E)
private val TextLabel         = Color(0xFF333333)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrgProfileScreen(
    viewModel: OrgProfileViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val profile by viewModel.profileState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadProfile() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BlueGradientStart, BlueGradientEnd)))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Profil Organisasi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextDark
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { backStack.removeLastOrNull() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextDark)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->

            if (profile.isLoading) {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
                return@Scaffold
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(key = "header") {
                    ProfileHeaderCard(
                        profile = profile,
                        onEditClick = { backStack.add(Routes.EditOrgProfileRoute) }
                    )
                }

                item(key = "basic_info") {
                    SectionCard(title = "Informasi Dasar", icon = Icons.Default.Info) {
                        DataFieldBox("Nama Organisasi", profile.orgName.ifBlank { "Contoh: Aksi Solo Satu" })
                        Spacer(modifier = Modifier.height(12.dp))
                        DataFieldBox(
                            label = "Deskripsi Organisasi",
                            value = profile.description.ifBlank { "Organisasi yang menangani orang-orang..." }
                        )
                    }
                }

                item(key = "contact") {
                    SectionCard(title = "Kontak", icon = Icons.Default.ContactPhone) {
                        DataFieldBoxWithIcon("Instagram(@username)", Icons.Default.Tag, "Aksisolosatu") // Asumsi belum ada di model, gunakan placeholder
                        Spacer(modifier = Modifier.height(12.dp))
                        DataFieldBoxWithIcon("Website / Link", Icons.Default.Language, profile.website.ifBlank { "Aksisolosatu.com" })
                        Spacer(modifier = Modifier.height(12.dp))
                        DataFieldBoxWithIcon("Nomor Telepon / WhatsApp", Icons.Default.Phone, profile.phone.ifBlank { "082221213545" })
                        Spacer(modifier = Modifier.height(12.dp))
                        DataFieldBoxWithIcon("Alamat Organisasi", Icons.Default.LocationOn, profile.address.ifBlank { "Laweyan RT 02, RW03 Surakarta..." })
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun ProfileHeaderCard(
    profile: OrgProfileState,
    onEditClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(4.dp)
            ) {
                if (!profile.logoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = profile.logoUrl,
                        contentDescription = "Logo organisasi",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (profile.orgName.firstOrNull() ?: "A").toString().uppercase(),
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.orgName.ifBlank { "Aksi Solo Satu" },
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = profile.email.ifBlank { "aksisolosatu@gmail.com" },
                    color = Color.White.copy(0.9f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val verifLabel = if (profile.verificationStatus == "verified") "Organisasi Terverifikasi" else "Belum Terverifikasi"
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = Color.White.copy(0.2f)
                    ) {
                        Text(
                            text = verifLabel,
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        onClick = onEditClick,
                        shape = RoundedCornerShape(50.dp),
                        color = Color(0xFF1548C9)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Edit Profile",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                content = content
            )
        }

        Surface(
            shape = RoundedCornerShape(50.dp),
            color = Color(0xFF5A94FF),
            shadowElevation = 3.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.95f)
        ) {
            Row(
                modifier = Modifier
                    .background(Brush.horizontalGradient(listOf(Color(0xFF4A88FF), Color(0xFF6B9FFF))))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun DataFieldBox(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextLabel)
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(FieldBgBlue, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(text = value, fontSize = 13.sp, color = TextDark)
        }
    }
}

@Composable
private fun DataFieldBoxWithIcon(label: String, icon: ImageVector, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextLabel)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FieldBgBlue, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = TextDark, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = value, fontSize = 13.sp, color = TextDark)
        }
    }
}