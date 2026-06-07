package com.example.prototypevolunteerapp.ui.screens.profile

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.R

private val GreenDark  = Color(0xFF3D5C2A)
private val GreenMid   = Color(0xFF5A7A5A)
private val GreenLight = Color(0xFFEEF4E8)
private val GreenBorder= Color(0xFFB8D8C0)
private val BgColor    = Color(0xFFF4F7EF)
private val CardWhite  = Color(0xFFFFFFFF)
private val TextHint   = Color(0xFF6E8F6E)
private val TextDark   = Color(0xFF1E2D1E)
private val TagBg      = Color(0xFFEFF6FF)
private val TagText    = Color(0xFF1D4ED8)
private val TagBorder  = Color(0xFFBFDBFE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val uiState  by viewModel.uiState.collectAsState()
    val snackbar  = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            viewModel.onSavedHandled()
            backStack.removeLastOrNull()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbar.showSnackbar(it) }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onImageSelected(uri)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Edit Profil", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenDark)
            )
        },
        containerColor = BgColor
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenMid)
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
                    .background(Brush.verticalGradient(listOf(GreenDark, GreenMid)))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    "Lengkapi profilmu agar organisasi lebih mudah mengenalimu.",
                    fontSize = 12.sp,
                    color    = Color(0xFFCFE7CD)
                )
            }

            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))))
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                val imageSource: Any? = uiState.selectedImageUri
                                    ?: uiState.currentAvatarUrl?.takeIf { it.isNotBlank() }

                                if (imageSource != null) {
                                    AsyncImage(
                                        model              = imageSource,
                                        contentDescription = "Foto profil",
                                        contentScale       = ContentScale.Crop,
                                        modifier           = Modifier.fillMaxSize(),
                                        error              = null,
                                        placeholder        = null
                                    )
                                } else {
                                    Text(
                                        text       = uiState.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                        fontSize   = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = Color.White
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.18f), CircleShape),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        null,
                                        tint     = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier.size(18.dp).padding(bottom = 4.dp)
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    uiState.name.ifBlank { "Nama belum diisi" },
                                    color      = Color.White,
                                    fontSize   = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(50.dp))
                                        .clickable { imagePickerLauncher.launch("image/*") }
                                ) {
                                    Row(
                                        modifier             = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment    = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CameraAlt,
                                            null,
                                            tint     = Color.White.copy(alpha = 0.9f),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            if (uiState.selectedImageUri != null) "Foto dipilih ✓" else "Ganti Foto",
                                            color      = Color.White.copy(alpha = 0.9f),
                                            fontSize   = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Text(
                                    "JPG, PNG, WEBP. Maks 2 MB.",
                                    color    = Color(0xFFBFDBFE).copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                ProfileSection(title = "Data Akun") {
                    ProfileField(
                        label       = "Nama Lengkap",
                        value       = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        icon        = Icons.Default.Person,
                        placeholder = "Nama lengkap kamu"
                    )
                    ProfileField(
                        label       = "Nomor Telepon",
                        value       = uiState.phone,
                        onValueChange = viewModel::onPhoneChange,
                        icon        = Icons.Default.Phone,
                        placeholder = "08xxxxxxxxxx"
                    )
                    OutlinedTextField(
                        value         = "Email tidak dapat diubah",
                        onValueChange = {},
                        label         = { Text("Email", fontSize = 13.sp) },
                        leadingIcon   = { Icon(Icons.Default.Email, null, tint = TextHint, modifier = Modifier.size(20.dp)) },
                        enabled       = false,
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                        shape         = RoundedCornerShape(12.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            disabledTextColor        = Color(0xFF94A3B8),
                            disabledBorderColor      = Color(0xFFE2E8F0),
                            disabledContainerColor   = Color(0xFFF8FAFC),
                            disabledLabelColor       = TextHint,
                            disabledLeadingIconColor = TextHint
                        )
                    )
                }

                ProfileSection(title = "Data Pribadi") {
                    ProfileField(
                        label       = "Tanggal Lahir",
                        value       = uiState.birthDate,
                        onValueChange = viewModel::onBirthDateChange,
                        icon        = Icons.Default.CalendarMonth,
                        placeholder = "YYYY-MM-DD (contoh: 2000-08-17)"
                    )

                    GenderDropdown(
                        selected = uiState.gender,
                        onSelect = viewModel::onGenderChange
                    )

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileField(
                            label         = "Kota",
                            value         = uiState.city,
                            onValueChange = viewModel::onCityChange,
                            icon          = Icons.Default.LocationOn,
                            placeholder   = "Yogyakarta",
                            modifier      = Modifier.weight(1f)
                        )
                        ProfileField(
                            label         = "Provinsi",
                            value         = uiState.province,
                            onValueChange = viewModel::onProvinceChange,
                            icon          = Icons.Default.Map,
                            placeholder   = "DI Yogyakarta",
                            modifier      = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value         = uiState.about,
                        onValueChange = viewModel::onAboutChange,
                        label         = { Text("Bio", fontSize = 13.sp) },
                        placeholder   = {
                            Text(
                                "Ceritakan sedikit tentang dirimu, motivasimu menjadi volunteer…",
                                fontSize = 13.sp,
                                color    = TextHint
                            )
                        },
                        modifier  = Modifier.fillMaxWidth(),
                        minLines  = 3,
                        maxLines  = 6,
                        shape     = RoundedCornerShape(12.dp),
                        colors    = profileFieldColors()
                    )
                    Text(
                        "Maks 500 karakter.",
                        fontSize = 11.sp,
                        color    = TextHint,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                ProfileSection(title = "Keahlian & Minat") {
                    ProfileField(
                        label       = "Keahlian",
                        value       = uiState.skillsRaw,
                        onValueChange = viewModel::onSkillsRawChange,
                        icon        = Icons.Default.School,
                        placeholder = "Komunikasi, Desain, Coding, Fotografi…"
                    )
                    Text(
                        "Pisahkan dengan koma.",
                        fontSize = 11.sp,
                        color    = TextHint,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    TagsPreview(raw = uiState.skillsRaw)

                    Spacer(Modifier.height(4.dp))

                    ProfileField(
                        label       = "Minat",
                        value       = uiState.interestsRaw,
                        onValueChange = viewModel::onInterestsRawChange,
                        icon        = Icons.Default.Favorite,
                        placeholder = "Lingkungan, Pendidikan, Kesehatan, Sosial…"
                    )
                    Text(
                        "Pisahkan dengan koma.",
                        fontSize = 11.sp,
                        color    = TextHint,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    TagsPreview(raw = uiState.interestsRaw)
                }

                Button(
                    onClick  = { viewModel.onSave() },
                    enabled  = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Menyimpan…", fontSize = 15.sp, color = Color.White)
                    } else {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Simpan Perubahan",
                            fontSize   = 15.sp,
                            color      = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                OutlinedButton(
                    onClick  = { backStack.removeLastOrNull() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(14.dp),
                    border   = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2E8F0))
                ) {
                    Text("Batal", fontSize = 15.sp, color = Color(0xFF64748B))
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TagsPreview(raw: String) {
    val tags = raw.split(",").map { it.trim() }.filter { it.isNotBlank() }
    if (tags.isEmpty()) return

    androidx.compose.foundation.layout.FlowRow(
        modifier              = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement   = Arrangement.spacedBy(6.dp)
    ) {
        tags.forEach { tag ->
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = TagBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, TagBorder)
            ) {
                Text(
                    text     = tag,
                    fontSize = 12.sp,
                    color    = TagText,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderDropdown(selected: String, onSelect: (String) -> Unit) {
    val options  = listOf("" to "-- Pilih --", "male" to "Laki-laki", "female" to "Perempuan", "other" to "Lainnya")
    var expanded by remember { mutableStateOf(false) }
    val label    = options.find { it.first == selected }?.second ?: "-- Pilih --"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value         = label,
            onValueChange = {},
            readOnly      = true,
            label         = { Text("Jenis Kelamin", fontSize = 13.sp) },
            leadingIcon   = { Icon(Icons.Default.Wc, null, tint = TextHint, modifier = Modifier.size(20.dp)) },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier      = Modifier.fillMaxWidth().menuAnchor(),
            shape         = RoundedCornerShape(12.dp),
            colors        = profileFieldColors()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.drop(1).forEach { (value, display) ->
                DropdownMenuItem(
                    text    = { Text(display) },
                    onClick = { onSelect(value); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
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
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text          = title.uppercase(),
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color         = Color(0xFF94A3B8)
                )
                HorizontalDivider(
                    modifier  = Modifier.weight(1f),
                    thickness = 1.dp,
                    color     = Color(0xFFE2E8F0)
                )
            }
            content()
        }
    }
}

@Composable
private fun ProfileField(
    label:         String,
    value:         String,
    onValueChange: (String) -> Unit,
    icon:          ImageVector,
    placeholder:   String,
    modifier:      Modifier = Modifier
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label, fontSize = 13.sp) },
        placeholder   = { Text(placeholder, fontSize = 13.sp, color = TextHint) },
        leadingIcon   = { Icon(icon, null, tint = TextHint, modifier = Modifier.size(20.dp)) },
        modifier      = modifier.fillMaxWidth(),
        singleLine    = true,
        shape         = RoundedCornerShape(12.dp),
        colors        = profileFieldColors()
    )
}

@Composable
private fun profileFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = Color(0xFF3B82F6),
    unfocusedBorderColor = Color(0xFFE2E8F0),
    focusedLabelColor    = Color(0xFF3B82F6),
    focusedLeadingIconColor   = GreenMid,
    unfocusedLeadingIconColor = TextHint
)