package com.example.prototypevolunteerapp.ui.screens.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import coil.compose.AsyncImage
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.ui.components.FormField

private val BgColor     = Color(0xFFF4F7EF)
private val CardWhite   = Color(0xFFFFFFFF)
private val AccentGreen = Color(0xFF5A7A5A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreen(
    viewModel: AddActivityViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val org       = viewModel.currentOrg
    val form by viewModel.formState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showAccessDenied by remember { mutableStateOf(!viewModel.hasAccess) }
    if (showAccessDenied) {
        AlertDialog(
            onDismissRequest = { backStack.removeLastOrNull() },
            title = { Text("Akses Ditolak", fontWeight = FontWeight.Bold) },
            text  = { Text("Anda harus login sebagai organisasi untuk menggunakan fitur ini.") },
            confirmButton = {
                TextButton(onClick = { backStack.removeLastOrNull() }) { Text("OK") }
            }
        )
    }

    form.errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.onErrorDismissed() },
            icon = { Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFCC2222)) },
            title = { Text("Gagal Menyimpan", fontWeight = FontWeight.Bold) },
            text  = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { viewModel.onErrorDismissed() }) { Text("OK") }
            }
        )
    }

    if (form.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Icon(Icons.Default.CheckCircle, null, tint = AccentGreen,
                    modifier = Modifier.size(36.dp))
            },
            title = { Text("Tersimpan sebagai Draft!", fontWeight = FontWeight.Bold) },
            text  = {
                Text("Kegiatan \"${form.namaKegiatan}\" berhasil dibuat.\n\nIngin langsung mengajukan ke review admin sekarang?")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onSubmitToReview() },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    enabled = !form.isSubmitting
                ) {
                    if (form.isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Submit ke Review")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    viewModel.onSuccessDialogDismissed()
                    backStack.removeLastOrNull()
                }) { Text("Simpan Draft Dulu") }
            }
        )
    }

    if (form.showSubmitSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Icon(Icons.Default.Send, null, tint = AccentGreen, modifier = Modifier.size(36.dp))
            },
            title = { Text("Pengajuan Terkirim!", fontWeight = FontWeight.Bold) },
            text  = { Text("Kegiatan \"${form.namaKegiatan}\" telah diajukan ke review admin.\nKamu akan mendapat notifikasi setelah ditinjau.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onSubmitSuccessDialogDismissed()
                        backStack.removeLastOrNull()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) { Text("Kembali ke Dashboard") }
            }
        )
    }

    if (form.showPreviewSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onPreviewDismissed() },
            sheetState       = sheetState,
            containerColor   = CardWhite,
            shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp).height(4.dp)
                        .background(Color(0xFFDAEFDC), RoundedCornerShape(50.dp))
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Ringkasan Pengajuan", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Periksa kembali sebelum submit", fontSize = 12.sp, color = Color(0xFF6E8F6E))
                HorizontalDivider(color = Color(0xFFDAEFDC))
                PreviewRow(Icons.Default.Event,       "Nama Kegiatan",  form.namaKegiatan)
                PreviewRow(Icons.Default.LocationOn,  "Lokasi",         form.lokasi)
                PreviewRow(Icons.Default.LocationCity,"Kota",           form.kota)
                PreviewRow(Icons.Default.LocationCity,"Provinsi",       form.provinsi)
                PreviewRow(Icons.Default.DateRange,   "Tanggal Mulai",  form.tanggalMulai)
                PreviewRow(Icons.Default.DateRange,   "Tanggal Selesai",form.tanggalSelesai)
                if (form.waktuMulai.isNotBlank())
                    PreviewRow(Icons.Default.Schedule,"Waktu Mulai",    form.waktuMulai)
                PreviewRow(Icons.Default.People,      "Kuota",          "${form.kuota} orang")
                PreviewRow(Icons.Default.Business,    "Organisasi",     org?.name ?: "-")
                if (form.persyaratan.isNotBlank())
                    PreviewRow(Icons.Default.Checklist,"Persyaratan",   form.persyaratan)
                if (form.kontakPerson.isNotBlank())
                    PreviewRow(Icons.Default.Person,  "Kontak Person",  form.kontakPerson)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Description, null, tint = AccentGreen, modifier = Modifier.size(14.dp))
                        Text("Deskripsi", fontSize = 11.sp, color = Color(0xFF6E8F6E))
                    }
                    Text(
                        form.deskripsi,
                        fontSize   = 13.sp,
                        color      = Color(0xFF1E2D1E),
                        lineHeight = 19.sp,
                        modifier   = Modifier
                            .background(Color(0xFFF4F7EF), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                            .fillMaxWidth()
                    )
                }
                HorizontalDivider(color = Color(0xFFDAEFDC))
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick  = { viewModel.onPreviewDismissed() },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape    = RoundedCornerShape(12.dp)
                    ) { Text("Edit") }
                    Button(
                        onClick  = { viewModel.onSubmitConfirmed() },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        enabled  = !form.isSubmitting
                    ) {
                        if (form.isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan Draft", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Tambah Kegiatan", fontWeight = FontWeight.Bold,
                        fontSize = 18.sp, color = Color(0xFF1E2D1E))
                },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back", tint = Color(0xFF1E2D1E))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor)
            )
        },
        containerColor = BgColor
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {
            item(key = "poster_card") {
                val posterLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? -> viewModel.onPosterSelected(uri) }

                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Poster Event (opsional)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 14.sp,
                            color      = Color(0xFF6E8F6E)
                        )
                        if (form.posterUri != null) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                AsyncImage(
                                    model    = form.posterUri,
                                    contentDescription = "Poster Preview",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .border(1.dp, Color(0xFFDAEFDC), RoundedCornerShape(10.dp))
                                )
                            }
                            OutlinedButton(
                                onClick = { viewModel.onPosterSelected(null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Delete, null, tint = Color(0xFFCC2222), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Hapus Poster", color = Color(0xFFCC2222), fontSize = 13.sp)
                            }
                        } else {
                            OutlinedButton(
                                onClick  = { posterLauncher.launch("image/*") },
                                modifier = Modifier.fillMaxWidth(),
                                shape    = RoundedCornerShape(10.dp),
                                colors   = ButtonDefaults.outlinedButtonColors(contentColor = AccentGreen)
                            ) {
                                Icon(Icons.Default.Image, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Pilih Foto Poster", fontSize = 13.sp)
                            }
                            Text(
                                "Format: JPG / PNG. Disarankan rasio 16:9.",
                                fontSize = 11.sp,
                                color    = Color(0xFF9E9E9E)
                            )
                        }
                    }
                }
            }

            item(key = "org_banner") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = CardDefaults.cardColors(containerColor = Color(0xFFD4EDCA))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Business, null, tint = Color(0xFF2E5C1A), modifier = Modifier.size(20.dp))
                        Text(
                            "Mengajukan sebagai: ${org?.name ?: "Organisasi"}",
                            fontSize   = 13.sp,
                            color      = Color(0xFF2E5C1A),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item(key = "form_card") {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Detail Kegiatan", fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp, color = Color(0xFF6E8F6E))

                        FormField(
                            label = "Nama Kegiatan *", value = form.namaKegiatan,
                            onValueChange = viewModel::onNamaKegiatanChange,
                            placeholder = "Contoh: Aksi Bersih Sungai Bengawan Solo",
                            icon = Icons.Default.Event,
                            isError = form.namaError, errorMsg = "Nama kegiatan tidak boleh kosong"
                        )
                        FormField(
                            label = "Lokasi / Nama Tempat *", value = form.lokasi,
                            onValueChange = viewModel::onLokasiChange,
                            placeholder = "Contoh: Balai Kota Solo",
                            icon = Icons.Default.LocationOn,
                            isError = form.lokasiError, errorMsg = "Lokasi tidak boleh kosong"
                        )
                        FormField(
                            label = "Kota *", value = form.kota,
                            onValueChange = viewModel::onKotaChange,
                            placeholder = "Contoh: Solo",
                            icon = Icons.Default.LocationCity,
                            isError = form.kotaError, errorMsg = "Kota tidak boleh kosong"
                        )
                        FormField(
                            label = "Provinsi *", value = form.provinsi,
                            onValueChange = viewModel::onProvinsiChange,
                            placeholder = "Contoh: Jawa Tengah",
                            icon = Icons.Default.LocationCity,
                            isError = form.provinsiError, errorMsg = "Provinsi tidak boleh kosong"
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                FormField(
                                    label = "Tgl Mulai * (YYYY-MM-DD)", value = form.tanggalMulai,
                                    onValueChange = viewModel::onTanggalMulaiChange,
                                    placeholder = "2026-07-20",
                                    icon = Icons.Default.DateRange,
                                    isError = form.tanggalMulaiError, errorMsg = "Wajib diisi"
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                FormField(
                                    label = "Tgl Selesai * (YYYY-MM-DD)", value = form.tanggalSelesai,
                                    onValueChange = viewModel::onTanggalSelesaiChange,
                                    placeholder = "2026-07-20",
                                    icon = Icons.Default.DateRange,
                                    isError = form.tanggalSelesaiError, errorMsg = "Wajib diisi"
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                FormField(
                                    label = "Waktu Mulai (opsional)", value = form.waktuMulai,
                                    onValueChange = viewModel::onWaktuMulaiChange,
                                    placeholder = "08:00",
                                    icon = Icons.Default.Schedule,
                                    isError = false, errorMsg = ""
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                FormField(
                                    label = "Waktu Selesai (opsional)", value = form.waktuSelesai,
                                    onValueChange = viewModel::onWaktuSelesaiChange,
                                    placeholder = "17:00",
                                    icon = Icons.Default.Schedule,
                                    isError = false, errorMsg = ""
                                )
                            }
                        }

                        FormField(
                            label = "Kuota Volunteer *", value = form.kuota,
                            onValueChange = viewModel::onKuotaChange,
                            placeholder = "Contoh: 20",
                            icon = Icons.Default.People,
                            isError = form.kuotaError, errorMsg = "Kuota harus berupa angka"
                        )

                        if (form.categories.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Category, null,
                                        tint = AccentGreen, modifier = Modifier.size(16.dp))
                                    Text("Kategori (pilih hingga 5)",
                                        fontSize = 13.sp, color = AccentGreen,
                                        fontWeight = FontWeight.Medium)
                                }
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement   = Arrangement.spacedBy(6.dp)
                                ) {
                                    form.categories.forEach { cat ->
                                        val isSelected = cat.id in form.selectedCategoryIds
                                        FilterChip(
                                            selected = isSelected,
                                            onClick  = { viewModel.onCategoryToggled(cat.id) },
                                            label    = { Text(cat.name, fontSize = 12.sp) },
                                            leadingIcon = if (isSelected) {
                                                { Icon(Icons.Default.Check, null,
                                                    modifier = Modifier.size(14.dp)) }
                                            } else null,
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor  = AccentGreen,
                                                selectedLabelColor      = Color.White,
                                                selectedLeadingIconColor = Color.White
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled          = true,
                                                selected         = isSelected,
                                                borderColor      = Color(0xFFB8D8C0),
                                                selectedBorderColor = AccentGreen
                                            )
                                        )
                                    }
                                }
                                if (form.selectedCategoryIds.size >= 5) {
                                    Text("Maksimal 5 kategori dipilih.",
                                        fontSize = 11.sp, color = Color(0xFF9E9E9E))
                                }
                            }
                        }
                    }
                }
            }

            item(key = "optional_card") {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Info Tambahan (opsional)", fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp, color = Color(0xFF6E8F6E))

                        FormField(
                            label = "Persyaratan Volunteer", value = form.persyaratan,
                            onValueChange = viewModel::onPersyaratanChange,
                            placeholder = "Contoh: Usia 18-25 tahun, bawa perlengkapan sendiri",
                            icon = Icons.Default.Checklist,
                            isError = false, errorMsg = ""
                        )
                        FormField(
                            label = "Nama Kontak Person", value = form.kontakPerson,
                            onValueChange = viewModel::onKontakPersonChange,
                            placeholder = "Contoh: Budi Santoso",
                            icon = Icons.Default.Person,
                            isError = false, errorMsg = ""
                        )
                        FormField(
                            label = "No. Telepon Kontak", value = form.kontakPhone,
                            onValueChange = viewModel::onKontakPhoneChange,
                            placeholder = "Contoh: 08123456789",
                            icon = Icons.Default.Phone,
                            isError = false, errorMsg = ""
                        )
                    }
                }
            }

            item(key = "desc_card") {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, null, tint = AccentGreen, modifier = Modifier.size(16.dp))
                            Text("Deskripsi Kegiatan *", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6E8F6E))
                        }
                        OutlinedTextField(
                            value         = form.deskripsi,
                            onValueChange = viewModel::onDeskripsiChange,
                            placeholder   = { Text("Jelaskan detail kegiatan...") },
                            modifier      = Modifier.fillMaxWidth().height(140.dp),
                            shape         = RoundedCornerShape(10.dp),
                            isError       = form.deskError,
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = AccentGreen,
                                unfocusedBorderColor = Color(0xFFDAEFDC)
                            )
                        )
                        if (form.deskError) {
                            Text("Deskripsi tidak boleh kosong",
                                color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                        }
                    }
                }
            }

            item(key = "info_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Alur Publikasi Event",
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7A5C00))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("1. Isi form → 2. Preview → 3. Simpan sebagai draft\n4. Submit ke review admin → 5. Admin setujui → 6. Tampil ke publik",
                            fontSize = 12.sp, color = Color(0xFF7A5C00))
                    }
                }
            }

            item(key = "submit_btn") {
                Button(
                    onClick  = { viewModel.onPreviewRequested() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    enabled  = !form.isSubmitting
                ) {
                    Icon(Icons.Default.Preview, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Preview & Submit", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PreviewRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = AccentGreen,
            modifier = Modifier.size(16.dp).padding(top = 2.dp))
        Column {
            Text(label,                 fontSize = 11.sp, color = Color(0xFF6E8F6E))
            Text(value.ifBlank { "-" }, fontSize = 13.sp, color = Color(0xFF1E2D1E))
        }
    }
}