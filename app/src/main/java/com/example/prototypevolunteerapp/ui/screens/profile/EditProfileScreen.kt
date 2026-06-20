package com.example.prototypevolunteerapp.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.BackHandler
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

private val HeaderStart = Color(0xFF2B5CE6)
private val HeaderEnd   = Color(0xFF5B8DEF)
private val BgColor     = Color(0xFFEEF3FF)
private val CardWhite   = Color(0xFFFFFFFF)
private val AccentBlue  = Color(0xFF3D7BF5)
private val TextHint    = Color(0xFF6B7280)
private val TextDark    = Color(0xFF1A1F36)
private val TagBg       = Color(0xFFEFF6FF)
private val TagText     = Color(0xFF1D4ED8)
private val TagBorder   = Color(0xFFBFDBFE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val uiState  by viewModel.uiState.collectAsState()
    val snackbar  = remember { SnackbarHostState() }
    var showSaveDialog   by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var initialName         by remember { mutableStateOf<String?>(null) }
    var initialPhone        by remember { mutableStateOf<String?>(null) }
    var initialAbout        by remember { mutableStateOf<String?>(null) }
    var initialBirthDate    by remember { mutableStateOf<String?>(null) }
    var initialGender       by remember { mutableStateOf<String?>(null) }
    var initialCity         by remember { mutableStateOf<String?>(null) }
    var initialProvince     by remember { mutableStateOf<String?>(null) }
    var initialSkillsRaw    by remember { mutableStateOf<String?>(null) }
    var initialInterestsRaw by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && initialName == null) {
            initialName         = uiState.name
            initialPhone        = uiState.phone
            initialAbout        = uiState.about
            initialBirthDate    = uiState.birthDate
            initialGender       = uiState.gender
            initialCity         = uiState.city
            initialProvince     = uiState.province
            initialSkillsRaw    = uiState.skillsRaw
            initialInterestsRaw = uiState.interestsRaw
        }
    }

    val hasUnsavedChanges = remember(
        uiState.name, uiState.phone, uiState.about, uiState.birthDate,
        uiState.gender, uiState.city, uiState.province,
        uiState.skillsRaw, uiState.interestsRaw, uiState.selectedImageUri
    ) {
        initialName != null && (
                uiState.name         != initialName         ||
                        uiState.phone        != initialPhone        ||
                        uiState.about        != initialAbout        ||
                        uiState.birthDate    != initialBirthDate    ||
                        uiState.gender       != initialGender       ||
                        uiState.city         != initialCity         ||
                        uiState.province     != initialProvince     ||
                        uiState.skillsRaw    != initialSkillsRaw    ||
                        uiState.interestsRaw != initialInterestsRaw ||
                        uiState.selectedImageUri != null
                )
    }

    BackHandler(enabled = hasUnsavedChanges) {
        showDiscardDialog = true
    }

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
    ) { uri: Uri? -> viewModel.onImageSelected(uri) }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor   = CardWhite,
            shape            = RoundedCornerShape(20.dp),
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier         = Modifier
                            .size(56.dp)
                            .background(AccentBlue.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Save, null,
                            tint     = AccentBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Simpan Perubahan?",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 17.sp,
                        color      = TextDark,
                        textAlign  = TextAlign.Center
                    )
                }
            },
            text = {
                Text(
                    "Pastikan semua data yang kamu isi sudah benar. Perubahan akan langsung diterapkan ke profilmu.",
                    fontSize   = 14.sp,
                    lineHeight = 21.sp,
                    color      = TextHint,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick  = { showSaveDialog = false; viewModel.onSave() },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Ya, Simpan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick  = { showSaveDialog = false },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape    = RoundedCornerShape(12.dp),
                    border   = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2E8F0))
                ) {
                    Text("Cek Lagi", color = TextHint, fontSize = 14.sp)
                }
            }
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            containerColor   = CardWhite,
            shape            = RoundedCornerShape(20.dp),
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier         = Modifier
                            .size(56.dp)
                            .background(Color(0xFFFFF3CD), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Warning, null,
                            tint     = Color(0xFFD97706),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Keluar Tanpa Menyimpan?",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 17.sp,
                        color      = TextDark,
                        textAlign  = TextAlign.Center
                    )
                }
            },
            text = {
                Text(
                    "Kamu punya perubahan yang belum disimpan. Kalau keluar sekarang, semua perubahan akan hilang.",
                    fontSize   = 14.sp,
                    lineHeight = 21.sp,
                    color      = TextHint,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick  = { showDiscardDialog = false; backStack.removeLastOrNull() },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Keluar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick  = { showDiscardDialog = false },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape    = RoundedCornerShape(12.dp),
                    border   = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2E8F0))
                ) {
                    Text("Lanjut Edit", color = TextHint, fontSize = 14.sp)
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Profil",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp,
                        color      = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasUnsavedChanges) showDiscardDialog = true
                        else backStack.removeLastOrNull()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderStart)
            )
        },
        containerColor = BgColor
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                Modifier.fillMaxSize().padding(innerPadding),
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
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    "Lengkapi profilmu agar organisasi lebih mudah mengenalimu.",
                    fontSize = 12.sp,
                    color    = Color(0xFFBFDBFE)
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
                            .background(Brush.linearGradient(listOf(HeaderStart, HeaderEnd)))
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
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
                                        Icons.Default.CameraAlt, null,
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
                                    shape    = RoundedCornerShape(50.dp),
                                    color    = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(50.dp))
                                        .clickable { imagePickerLauncher.launch("image/*") }
                                ) {
                                    Row(
                                        modifier              = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment     = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CameraAlt, null,
                                            tint     = Color.White.copy(alpha = 0.9f),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            if (uiState.selectedImageUri != null) "Foto dipilih" else "Ganti Foto",
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
                        label         = "Nama Lengkap",
                        value         = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        icon          = Icons.Default.Person,
                        placeholder   = "Nama lengkap kamu"
                    )
                    ProfileField(
                        label         = "Nomor Telepon",
                        value         = uiState.phone,
                        onValueChange = viewModel::onPhoneChange,
                        icon          = Icons.Default.Phone,
                        placeholder   = "08xxxxxxxxxx"
                    )

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = {},
                        label         = { Text("Email", fontSize = 13.sp) },
                        leadingIcon   = {
                            Icon(Icons.Default.Email, null,
                                tint = TextHint, modifier = Modifier.size(20.dp))
                        },
                        enabled    = false,
                        modifier   = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape      = RoundedCornerShape(12.dp),
                        colors     = OutlinedTextFieldDefaults.colors(
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
                        label         = "Tanggal Lahir",
                        value         = uiState.birthDate,
                        onValueChange = viewModel::onBirthDateChange,
                        icon          = Icons.Default.CalendarMonth,
                        placeholder   = "YYYY-MM-DD (contoh: 2000-08-17)"
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
                        label         = "Keahlian",
                        value         = uiState.skillsRaw,
                        onValueChange = viewModel::onSkillsRawChange,
                        icon          = Icons.Default.School,
                        placeholder   = "Komunikasi, Desain, Coding, Fotografi…"
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
                        label         = "Minat",
                        value         = uiState.interestsRaw,
                        onValueChange = viewModel::onInterestsRawChange,
                        icon          = Icons.Default.Favorite,
                        placeholder   = "Lingkungan, Pendidikan, Kesehatan, Sosial…"
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
                    onClick  = { showSaveDialog = true },
                    enabled  = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = AccentBlue)
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
                        Icon(Icons.Default.Check, null,
                            tint = Color.White, modifier = Modifier.size(18.dp))
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
                    onClick  = {
                        if (hasUnsavedChanges) showDiscardDialog = true
                        else backStack.removeLastOrNull()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(14.dp),
                    border   = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2E8F0))
                ) {
                    Text("Batal", fontSize = 15.sp, color = TextHint)
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

    FlowRow(
        modifier              = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement   = Arrangement.spacedBy(6.dp)
    ) {
        tags.forEach { tag ->
            Surface(
                shape  = RoundedCornerShape(50.dp),
                color  = TagBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, TagBorder)
            ) {
                Text(
                    text       = tag,
                    fontSize   = 12.sp,
                    color      = TagText,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
            leadingIcon   = {
                Icon(Icons.Default.Wc, null,
                    tint = TextHint, modifier = Modifier.size(20.dp))
            },
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
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
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
    focusedBorderColor        = AccentBlue,
    unfocusedBorderColor      = Color(0xFFE2E8F0),
    focusedLabelColor         = AccentBlue,
    focusedLeadingIconColor   = AccentBlue,
    unfocusedLeadingIconColor = TextHint
)