package com.example.prototypevolunteerapp.ui.screens.organizer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.prototypevolunteerapp.core.LocalBackStack

private val BlueGradientStart = Color(0xFFBBE0FF)
private val BlueGradientEnd   = Color(0xFFFFFFFF)
private val PrimaryBlue       = Color(0xFF2865FF)
private val FieldBgBlue       = Color(0xFFCBE2FF)
private val TextDark          = Color(0xFF1E1E1E)
private val TextLabel         = Color(0xFF333333)

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BlueGradientStart, BlueGradientEnd)))
    ) {
        Scaffold(
            snackbarHost   = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Edit Profil",
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
                    actions = {
                        if (profile.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp).padding(end = 16.dp),
                                color = PrimaryBlue,
                                strokeWidth = 2.dp
                            )
                        } else {
                            TextButton(
                                onClick = { viewModel.saveProfile() }
                            ) {
                                Text(
                                    text = if (profile.isSaved) "Tersimpan" else "Simpan",
                                    color = if (profile.isSaved) Color(0xFF4CAF50) else PrimaryBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
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
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.wrapContentSize()) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(2.dp, PrimaryBlue, CircleShape)
                                    .clickable { logoLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    profile.logoUri != null -> {
                                        AsyncImage(
                                            model = profile.logoUri,
                                            contentDescription = "Logo baru",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                                        )
                                    }
                                    !profile.logoUrl.isNullOrBlank() -> {
                                        AsyncImage(
                                            model = profile.logoUrl,
                                            contentDescription = "Logo organisasi",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = (profile.orgName.firstOrNull() ?: "O").toString().uppercase(),
                                            color = PrimaryBlue,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-3).dp, y = (-6).dp)
                                    .background(Color(0xFF1548C9), CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(13.dp))
                            }
                        }

                        Text("Tap foto untuk ganti logo", fontSize = 11.sp, color = PrimaryBlue)

                        if (profile.logoUri != null) {
                            TextButton(onClick = { viewModel.updateLogoUri(null) }) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp), tint = PrimaryBlue)
                                Spacer(Modifier.width(4.dp))
                                Text("Batalkan foto baru", fontSize = 12.sp, color = PrimaryBlue)
                            }
                        }
                    }
                }

                item(key = "basic_info") {
                    SectionCard(title = "Informasi Dasar", icon = Icons.Default.Info) {
                        EditableFieldBox(
                            label = "Nama Organisasi",
                            value = profile.orgName,
                            onValueChange = viewModel::updateOrgName,
                            placeholder = "Contoh: Aksi Solo Satu"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        EditableFieldBox(
                            label = "Deskripsi Organisasi",
                            value = profile.description,
                            onValueChange = viewModel::updateDescription,
                            placeholder = "Jelaskan tentang organisasi...",
                            minLines = 3
                        )
                    }
                }

                item(key = "contact_info") {
                    SectionCard(title = "Kontak & Lokasi", icon = Icons.Default.ContactPhone) {
                        EditableFieldBox(
                            label = "Website / Link",
                            value = profile.website,
                            onValueChange = viewModel::updateWebsite,
                            icon = Icons.Default.Language,
                            placeholder = "Aksisolosatu.com"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        EditableFieldBox(
                            label = "Nomor Telepon / WhatsApp",
                            value = profile.phone,
                            onValueChange = viewModel::updatePhone,
                            icon = Icons.Default.Phone,
                            placeholder = "082221213545"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        EditableFieldBox(
                            label = "Alamat Jalan",
                            value = profile.address,
                            onValueChange = viewModel::updateAddress,
                            icon = Icons.Default.LocationOn,
                            placeholder = "Laweyan RT 02, RW 03..."
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        EditableFieldBox(
                            label = "Kota",
                            value = profile.city,
                            onValueChange = viewModel::updateCity,
                            icon = Icons.Default.LocationCity,
                            placeholder = "Surakarta"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        EditableFieldBox(
                            label = "Provinsi",
                            value = profile.province,
                            onValueChange = viewModel::updateProvince,
                            icon = Icons.Default.Map,
                            placeholder = "Jawa Tengah"
                        )
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
                content = content
            )
        }

        Surface(
            shape = RoundedCornerShape(50.dp),
            color = Color(0xFF5A94FF),
            shadowElevation = 3.dp,
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(0.95f)
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
private fun EditableFieldBox(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector? = null,
    minLines: Int = 1
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextLabel)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FieldBgBlue, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = if (minLines > 1) Alignment.Top else Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextDark,
                    modifier = Modifier.padding(top = if (minLines > 1) 12.dp else 0.dp).size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                textStyle = TextStyle(fontSize = 13.sp, color = TextDark),
                minLines = minLines,
                cursorBrush = SolidColor(PrimaryBlue),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (value.isEmpty()) {
                            Text(text = placeholder, fontSize = 13.sp, color = TextDark.copy(alpha = 0.4f))
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}