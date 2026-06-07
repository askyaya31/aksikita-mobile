package com.example.prototypevolunteerapp.ui.screens.organizer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.prototypevolunteerapp.core.LocalBackStack

private val BgColor       = Color(0xFFF4F7EF)
private val HeaderStart   = Color(0xFF3D5C2A)
private val HeaderEnd     = Color(0xFF5A7A5A)
private val AccentGreen   = Color(0xFF5A7A5A)
private val CardWhite     = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF6E8F6E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOrgProfileScreen(
    viewModel: OrgProfileViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val profile   by viewModel.profileState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadProfile() }

    LaunchedEffect(profile.isSaved) {
        if (profile.isSaved) {
            snackbarHostState.showSnackbar("Profil berhasil disimpan!")
            viewModel.onSavedHandled()
            backStack.removeLastOrNull()
        }
    }

    LaunchedEffect(profile.errorMessage) {
        profile.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorDismissed()
        }
    }
    val logoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.updateLogoUri(uri) }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text("Edit Profil Organisasi", fontWeight = FontWeight.Bold,
                        fontSize = 18.sp, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderStart)
            )
        }
    ) { padding ->

        if (profile.isLoading) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = AccentGreen) }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(HeaderStart, HeaderEnd)))
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier         = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(0.15f))
                            .border(2.dp, Color.White.copy(0.5f), CircleShape)
                            .clickable { logoLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            profile.logoUri != null -> {
                                AsyncImage(
                                    model              = profile.logoUri,
                                    contentDescription = "Logo baru",
                                    contentScale       = ContentScale.Crop,
                                    modifier           = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            }
                            !profile.logoUrl.isNullOrBlank() -> {
                                AsyncImage(
                                    model              = profile.logoUrl,
                                    contentDescription = "Logo organisasi",
                                    contentScale       = ContentScale.Crop,
                                    modifier           = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            }
                            else -> {
                                Text(
                                    text       = (profile.orgName.firstOrNull() ?: "O")
                                        .toString().uppercase(),
                                    color      = Color.White,
                                    fontSize   = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.BottomEnd)
                                .background(AccentGreen, CircleShape)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt, null,
                                tint     = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Text("Tap foto untuk ganti logo", fontSize = 11.sp,
                        color = Color.White.copy(0.7f))

                    if (profile.logoUri != null) {
                        TextButton(
                            onClick = { viewModel.updateLogoUri(null) },
                            colors  = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFFCDD2))
                        ) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Batalkan perubahan foto", fontSize = 12.sp)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                EditSection(title = "Informasi Dasar") {
                    OrgEditField(
                        label         = "Nama Organisasi",
                        value         = profile.orgName,
                        onValueChange = viewModel::updateOrgName,
                        icon          = Icons.Default.Business,
                        placeholder   = "Nama resmi organisasi kamu"
                    )
                    OrgEditField(
                        label         = "Deskripsi Organisasi",
                        value         = profile.description,
                        onValueChange = viewModel::updateDescription,
                        icon          = Icons.Default.Description,
                        placeholder   = "Jelaskan misi dan visi organisasi...",
                        minLines      = 3
                    )
                }
                EditSection(title = "Kontak & Lokasi") {
                    OrgEditField(
                        label         = "Nomor Telepon / WhatsApp",
                        value         = profile.phone,
                        onValueChange = viewModel::updatePhone,
                        icon          = Icons.Default.Phone,
                        placeholder   = "Contoh: 628123456789"
                    )
                    OrgEditField(
                        label         = "Website / Link",
                        value         = profile.website,
                        onValueChange = viewModel::updateWebsite,
                        icon          = Icons.Default.Language,
                        placeholder   = "https://website-organisasi.id"
                    )
                    OrgEditField(
                        label         = "Alamat",
                        value         = profile.address,
                        onValueChange = viewModel::updateAddress,
                        icon          = Icons.Default.LocationOn,
                        placeholder   = "Alamat lengkap kantor/sekretariat",
                        minLines      = 2
                    )
                    OrgEditField(
                        label         = "Kota",
                        value         = profile.city,
                        onValueChange = viewModel::updateCity,
                        icon          = Icons.Default.LocationCity,
                        placeholder   = "Contoh: Surakarta"
                    )
                    OrgEditField(
                        label         = "Provinsi",
                        value         = profile.province,
                        onValueChange = viewModel::updateProvince,
                        icon          = Icons.Default.Map,
                        placeholder   = "Contoh: Jawa Tengah"
                    )
                }
                Button(
                    onClick  = { viewModel.saveProfile() },
                    enabled  = !profile.isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    if (profile.isSaving) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Menyimpan...", fontSize = 15.sp, color = Color.White)
                    } else {
                        Icon(Icons.Default.Save, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Simpan Perubahan", fontSize = 15.sp,
                            color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
@Composable
private fun EditSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                color = TextSecondary, letterSpacing = 0.5.sp)
            content()
        }
    }
}

@Composable
private fun OrgEditField(
    label:         String,
    value:         String,
    onValueChange: (String) -> Unit,
    icon:          androidx.compose.ui.graphics.vector.ImageVector,
    placeholder:   String,
    minLines:      Int = 1
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label, fontSize = 13.sp) },
        placeholder   = { Text(placeholder, fontSize = 13.sp, color = TextSecondary) },
        leadingIcon   = {
            Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        },
        modifier      = Modifier.fillMaxWidth(),
        minLines      = minLines,
        shape         = RoundedCornerShape(12.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = AccentGreen,
            unfocusedBorderColor = Color(0xFFB8D8C0),
            focusedLabelColor    = AccentGreen
        )
    )
}