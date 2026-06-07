package com.example.prototypevolunteerapp.ui.screens.organizer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.ui.components.FormField

private val BgColor     = Color(0xFFF4F7EF)
private val CardWhite   = Color(0xFFFFFFFF)
private val AccentGreen = Color(0xFF5A7A5A)

fun activityStatusUi(statusFromApi: String): Triple<String, Color, Color> =
    when (statusFromApi) {
        "published"      -> Triple("Aktif / Published", Color(0xFF2E6B3E), Color(0xFFD4EDDA))
        "completed"      -> Triple("Selesai",           Color(0xFF5A7A5A), Color(0xFFDAEFDC))
        "cancelled"      -> Triple("Dibatalkan",        Color(0xFF8B0000), Color(0xFFFDE8E8))
        "pending_review" -> Triple("Menunggu Review",   Color(0xFF7A5C00), Color(0xFFFFF3CD))
        "rejected"       -> Triple("Ditolak Admin",     Color(0xFF8B0000), Color(0xFFFDE8E8))
        "draft"          -> Triple("Draft",             Color(0xFF555555), Color(0xFFEEEEEE))
        else             -> Triple(statusFromApi.ifBlank { "Draft" },
            Color(0xFF555555), Color(0xFFEEEEEE))
    }
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivityScreen(
    eventId:   Int,
    viewModel: EditActivityViewModel = hiltViewModel(key = "edit_$eventId")
) {
    LaunchedEffect(eventId) { viewModel.loadEvent(eventId) }
    val backStack  = LocalBackStack.current
    val form       by viewModel.formState.collectAsState()
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
    if (form.notFound) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ErrorOutline, null,
                    tint     = Color(0xFFAAAAAA),
                    modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Kegiatan tidak ditemukan", color = Color(0xFFAAAAAA))
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { backStack.removeLastOrNull() }) { Text("Kembali") }
            }
        }
        return
    }
    if (form.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentGreen)
        }
        return
    }
    if (form.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDeleteDialogDismiss() },
            icon = {
                Icon(Icons.Default.DeleteForever, null,
                    tint     = Color(0xFFCC2222),
                    modifier = Modifier.size(32.dp))
            },
            title = { Text("Hapus Kegiatan?", fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    "Kegiatan \"${form.namaKegiatan}\" akan dihapus secara permanen.\n\n" +
                            "Data kandidat yang telah mendaftar tidak akan terpengaruh, " +
                            "namun kegiatan tidak akan bisa dipulihkan."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onDeleteConfirmed(onSuccess = {
                            backStack.removeLastOrNull()
                            backStack.removeLastOrNull()
                        })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC2222))
                ) { Text("Ya, Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDeleteDialogDismiss() }) { Text("Batal") }
            }
        )
    }

    if (form.showSubmitSuccessDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onSubmitSuccessDialogDismissed() },
            icon = {
                Icon(Icons.Default.CheckCircle, null,
                    tint     = AccentGreen,
                    modifier = Modifier.size(36.dp))
            },
            title = { Text("Berhasil Disubmit!", fontWeight = FontWeight.Bold) },
            text  = { Text("Kegiatan kamu sudah dikirim ke admin untuk ditinjau. Tunggu ya!") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onSubmitSuccessDialogDismissed()
                        backStack.removeLastOrNull()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) { Text("OK") }
            }
        )
    }
    if (form.showStatusSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onStatusSheetDismiss() },
            sheetState       = sheetState,
            containerColor   = CardWhite,
            shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                Text("Aksi Status Kegiatan",
                    fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Status saat ini: ${activityStatusUi(form.statusFromApi).first}",
                    fontSize = 12.sp, color = Color(0xFF6E8F6E))
                HorizontalDivider(color = Color(0xFFDAEFDC))
                Spacer(modifier = Modifier.height(4.dp))

                if (form.statusFromApi == "draft" || form.statusFromApi == "rejected") {
                    StatusOptionButton(
                        icon           = Icons.Default.Send,
                        label          = "Submit ke Review Admin",
                        sublabel       = "Kirim kegiatan untuk diperiksa admin",
                        containerColor = Color(0xFF5A7A5A),
                        isSelected     = false,
                        onClick        = {
                            viewModel.onStatusSheetDismiss()
                            viewModel.onSubmitToReviewDialogShow()
                        }
                    )
                }

                if (form.statusFromApi == "published") {
                    StatusOptionButton(
                        icon           = Icons.Default.CheckCircle,
                        label          = "Tandai Selesai",
                        sublabel       = "Kegiatan telah selesai dilaksanakan",
                        containerColor = Color(0xFF5A7A5A),
                        isSelected     = false,
                        onClick        = {
                            viewModel.onStatusSheetDismiss()
                            // TODO: panggil viewModel.onCompleteEvent() jika sudah ada
                        }
                    )
                }

                if (form.statusFromApi != "cancelled" && form.statusFromApi != "completed") {
                    StatusOptionButton(
                        icon           = Icons.Default.Cancel,
                        label          = "Batalkan Kegiatan",
                        sublabel       = "Kegiatan tidak jadi dilaksanakan",
                        containerColor = Color(0xFFCC2222),
                        isSelected     = false,
                        onClick        = {
                            viewModel.onStatusSheetDismiss()
                            // TODO: panggil viewModel.onCancelEvent() jika sudah ada
                        }
                    )
                }

                OutlinedButton(
                    onClick  = { viewModel.onStatusSheetDismiss() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(12.dp)
                ) { Text("Tutup", color = Color(0xFF6E8F6E)) }
            }
        }
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
                Text("Ringkasan Perubahan",
                    fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Periksa kembali sebelum menyimpan",
                    fontSize = 12.sp, color = Color(0xFF6E8F6E))
                HorizontalDivider(color = Color(0xFFDAEFDC))

                EditPreviewRow(Icons.Default.Event,      "Nama Kegiatan",   form.namaKegiatan)
                EditPreviewRow(Icons.Default.LocationOn, "Lokasi",          form.lokasi)
                EditPreviewRow(Icons.Default.LocationCity, "Kota",          form.kota)
                EditPreviewRow(Icons.Default.DateRange,  "Tanggal Mulai",   form.tanggalMulai)
                EditPreviewRow(Icons.Default.DateRange,  "Tanggal Selesai", form.tanggalSelesai)
                EditPreviewRow(Icons.Default.People,     "Kuota",           form.kuota)

                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.RadioButtonChecked, null,
                        tint     = AccentGreen,
                        modifier = Modifier.size(14.dp))
                    Text("Status", fontSize = 11.sp, color = Color(0xFF6E8F6E))
                }
                val (stLabel, stColor, stBg) = activityStatusUi(form.statusFromApi)
                Box(
                    modifier = Modifier
                        .background(stBg, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(stLabel, fontSize = 13.sp,
                        color = stColor, fontWeight = FontWeight.SemiBold)
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Description, null,
                            tint     = AccentGreen,
                            modifier = Modifier.size(14.dp))
                        Text("Deskripsi", fontSize = 11.sp, color = Color(0xFF6E8F6E))
                    }
                    Text(
                        form.deskripsi,
                        fontSize   = 13.sp,
                        color      = Color(0xFF333333),
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
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick  = { viewModel.onPreviewDismissed() },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape    = RoundedCornerShape(12.dp)
                    ) { Text("Edit") }

                    Button(
                        onClick = {
                            viewModel.onSaveConfirmed()
                            viewModel.onPreviewDismissed()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        enabled  = !form.isSubmitting
                    ) {
                        if (form.isSubmitting) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(16.dp),
                                color       = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(form.successMessage) {
        form.successMessage?.let {
            snackbarHost.showSnackbar(it)
            viewModel.onSuccessHandled()
        }
    }
    LaunchedEffect(form.errorMessage) {
        form.errorMessage?.let {
            snackbarHost.showSnackbar(it)
            viewModel.onErrorDismissed()
        }
    }
    val posterLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.onPosterSelected(uri) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Edit Kegiatan",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp,
                        color      = Color(0xFF1A1A1A))
                },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint               = Color(0xFF1A1A1A))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onDeleteDialogShow() }) {
                        Icon(Icons.Default.DeleteForever,
                            contentDescription = "Hapus",
                            tint               = Color(0xFFCC2222))
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
            contentPadding      = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {

            item(key = "poster_card") {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Poster Kegiatan (opsional)",
                            fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                            color = Color(0xFF666666))

                        when {
                            form.posterUri != null -> {
                                AsyncImage(
                                    model              = form.posterUri,
                                    contentDescription = "Poster baru",
                                    contentScale       = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier           = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, Color(0xFFDAEFDC), RoundedCornerShape(10.dp))
                                )
                                OutlinedButton(
                                    onClick  = { viewModel.onPosterSelected(null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape    = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Delete, null,
                                        tint = Color(0xFFCC2222), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Hapus Poster Baru", color = Color(0xFFCC2222), fontSize = 13.sp)
                                }
                            }
                            !form.existingPosterUrl.isNullOrBlank() -> {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    AsyncImage(
                                        model              = form.existingPosterUrl,
                                        contentDescription = "Poster saat ini",
                                        contentScale       = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier           = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                }
                                OutlinedButton(
                                    onClick  = { posterLauncher.launch("image/*") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape    = RoundedCornerShape(10.dp),
                                    colors   = ButtonDefaults.outlinedButtonColors(
                                        contentColor = AccentGreen)
                                ) {
                                    Icon(Icons.Default.Image, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Ganti Poster", fontSize = 13.sp)
                                }
                            }
                            else -> {
                                OutlinedButton(
                                    onClick  = { posterLauncher.launch("image/*") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape    = RoundedCornerShape(10.dp),
                                    colors   = ButtonDefaults.outlinedButtonColors(
                                        contentColor = AccentGreen)
                                ) {
                                    Icon(Icons.Default.Image, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Pilih Foto Poster", fontSize = 13.sp)
                                }
                                Text("Format: JPG / PNG. Disarankan rasio 16:9.",
                                    fontSize = 11.sp, color = Color(0xFF9E9E9E))
                            }
                        }
                    }
                }
            }
            item(key = "status_card") {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier            = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Status Kegiatan",
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 14.sp,
                            color      = Color(0xFF666666))

                        val (stLabel, stColor, stBg) = activityStatusUi(form.statusFromApi)
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(stBg, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(stLabel,
                                    fontSize   = 13.sp,
                                    color      = stColor,
                                    fontWeight = FontWeight.SemiBold)
                            }
                            if (form.statusFromApi != "cancelled" &&
                                form.statusFromApi != "completed") {
                                TextButton(
                                    onClick = { viewModel.onStatusSheetShow() },
                                    colors  = ButtonDefaults.textButtonColors(
                                        contentColor = AccentGreen)
                                ) {
                                    Icon(Icons.Default.Edit, null,
                                        modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ubah", fontSize = 13.sp)
                                }
                            }
                        }
                        if (form.isReadOnly) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Info, null,
                                    tint     = Color(0xFF7A5C00),
                                    modifier = Modifier.size(14.dp))
                                Text(
                                    "Kegiatan ini hanya bisa diedit saat status draft atau ditolak.",
                                    fontSize = 11.sp,
                                    color    = Color(0xFF7A5C00)
                                )
                            }
                        }
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
                        modifier            = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Detail Kegiatan",
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 14.sp,
                            color      = Color(0xFF666666))

                        FormField(
                            label         = "Nama Kegiatan *",
                            value         = form.namaKegiatan,
                            onValueChange = viewModel::onNamaKegiatanChange,
                            placeholder   = "Nama kegiatan",
                            icon          = Icons.Default.Event,
                            isError       = form.namaError,
                            errorMsg      = "Nama kegiatan tidak boleh kosong"
                        )
                        FormField(
                            label         = "Lokasi *",
                            value         = form.lokasi,
                            onValueChange = viewModel::onLokasiChange,
                            placeholder   = "Nama venue / alamat",
                            icon          = Icons.Default.LocationOn,
                            isError       = form.lokasiError,
                            errorMsg      = "Lokasi tidak boleh kosong"
                        )
                        FormField(
                            label         = "Kota *",
                            value         = form.kota,
                            onValueChange = viewModel::onKotaChange,
                            placeholder   = "Contoh: Surakarta",
                            icon          = Icons.Default.LocationCity,
                            isError       = form.kotaError,
                            errorMsg      = "Kota tidak boleh kosong"
                        )
                        FormField(
                            label         = "Tanggal Mulai *",
                            value         = form.tanggalMulai,
                            onValueChange = viewModel::onTanggalMulaiChange,
                            placeholder   = "Contoh: 2026-06-20",
                            icon          = Icons.Default.DateRange,
                            isError       = form.tanggalMulaiError,
                            errorMsg      = "Tanggal mulai tidak boleh kosong"
                        )
                        FormField(
                            label         = "Tanggal Selesai *",
                            value         = form.tanggalSelesai,
                            onValueChange = viewModel::onTanggalSelesaiChange,
                            placeholder   = "Contoh: 2026-06-21",
                            icon          = Icons.Default.DateRange,
                            isError       = form.tanggalSelesaiError,
                            errorMsg      = "Tanggal selesai tidak boleh kosong"
                        )
                        FormField(
                            label         = "Kuota Volunteer *",
                            value         = form.kuota,
                            onValueChange = viewModel::onKuotaChange,
                            placeholder   = "Contoh: 20",
                            icon          = Icons.Default.People,
                            isError       = form.kuotaError,
                            errorMsg      = "Kuota harus berupa angka"
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Description,
                                    contentDescription = null,
                                    tint               = AccentGreen,
                                    modifier           = Modifier.size(16.dp))
                                Text("Deskripsi Kegiatan *",
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color      = Color(0xFF444444))
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
                                    color    = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp)
                            }
                        }

                        FormField(
                            label         = "Persyaratan (opsional)",
                            value         = form.persyaratan,
                            onValueChange = viewModel::onPersyaratanChange,
                            placeholder   = "Contoh: Mahasiswa aktif, usia 18–25",
                            icon          = Icons.Default.AssignmentInd,
                            isError       = false,
                            errorMsg      = ""
                        )

                        FormField(
                            label         = "Nama Kontak Person (opsional)",
                            value         = form.kontakPerson,
                            onValueChange = viewModel::onKontakPersonChange,
                            placeholder   = "Contoh: Budi Santoso",
                            icon          = Icons.Default.Person,
                            isError       = false,
                            errorMsg      = ""
                        )
                        FormField(
                            label         = "No. WhatsApp Kontak (opsional)",
                            value         = form.kontakPhone,
                            onValueChange = viewModel::onKontakPhoneChange,
                            placeholder   = "Contoh: 6281234567890",
                            icon          = Icons.Default.Phone,
                            isError       = false,
                            errorMsg      = ""
                        )
                    }
                }
            }

            item(key = "danger_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F0))
                ) {
                    Row(
                        modifier              = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hapus Kegiatan",
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 13.sp,
                                color      = Color(0xFFCC2222))
                            Text("Tindakan ini tidak bisa dibatalkan",
                                fontSize = 11.sp,
                                color    = Color(0xFF6E8F6E))
                        }
                        OutlinedButton(
                            onClick = { viewModel.onDeleteDialogShow() },
                            shape   = RoundedCornerShape(10.dp),
                            colors  = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFCC2222)),
                            border  = BorderStroke(1.dp, Color(0xFFCC2222))
                        ) {
                            Icon(Icons.Default.Delete, null,
                                modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hapus", fontSize = 13.sp)
                        }
                    }
                }
            }

            item(key = "save_btn") {
                Button(
                    onClick  = { viewModel.onPreviewRequested() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    enabled  = !form.isReadOnly && !form.isSubmitting
                ) {
                    Icon(Icons.Default.Preview, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Preview & Simpan",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold)
                }
            }

            item(key = "submit_btn") {
                if (form.statusFromApi == "draft" || form.statusFromApi == "rejected") {
                    OutlinedButton(
                        onClick  = { viewModel.onSubmitToReviewDialogShow() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        border   = BorderStroke(1.5.dp, AccentGreen)
                    ) {
                        Icon(Icons.Default.Send, null,
                            modifier = Modifier.size(16.dp),
                            tint     = AccentGreen)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submit ke Review Admin", color = AccentGreen)
                    }
                }
            }
        }
    }
}
@Composable
private fun StatusOptionButton(
    icon:           ImageVector,
    label:          String,
    sublabel:       String,
    containerColor: Color,
    isSelected:     Boolean,
    onClick:        () -> Unit
) {
    Button(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth().height(64.dp),
        shape    = RoundedCornerShape(14.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) containerColor else Color(0xFFF4F7EF),
            contentColor   = if (isSelected) Color.White    else Color(0xFF6E8F6E)
        )
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            horizontalAlignment = Alignment.Start,
            modifier            = Modifier.weight(1f)
        ) {
            Text(label,    fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(sublabel, fontSize   = 10.sp,
                color = if (isSelected) Color.White.copy(alpha = 0.8f)
                else            Color(0xFF6E8F6E))
        }
        if (isSelected) {
            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun EditPreviewRow(
    icon:  ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null,
            tint     = AccentGreen,
            modifier = Modifier.size(16.dp).padding(top = 2.dp))
        Column {
            Text(label,                fontSize = 11.sp, color = Color(0xFF6E8F6E))
            Text(value.ifBlank { "-" }, fontSize = 13.sp, color = Color(0xFF1A1A1A))
        }
    }
}