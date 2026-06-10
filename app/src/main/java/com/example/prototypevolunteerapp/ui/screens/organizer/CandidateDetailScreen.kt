package com.example.prototypevolunteerapp.ui.screens.organizer

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.prototypevolunteerapp.core.DateUtils
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.OrganizerSession
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.remote.dto.RegistrationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CandidateDetailViewModel @Inject constructor(
    private val apiService:   ApiService,
    val organizerSession:     OrganizerSession
) : ViewModel() {

    private val _registration = MutableStateFlow<RegistrationDto?>(null)
    val registration: StateFlow<RegistrationDto?> = _registration.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _actionLoading = MutableStateFlow(false)
    val actionLoading: StateFlow<Boolean> = _actionLoading.asStateFlow()

    private val _actionSuccess = MutableStateFlow<String?>(null)
    val actionSuccess: StateFlow<String?> = _actionSuccess.asStateFlow()

    fun loadRegistration(registrationId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resp = apiService.getOrgRegistrationDetail(registrationId)
                if (resp.isSuccessful) {
                    _registration.value = resp.body()?.registration
                } else {
                    _error.value = "Gagal memuat detail (${resp.code()})"
                }
            } catch (e: Exception) {
                _error.value = "Koneksi gagal: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onConfirm(registrationId: Int) {
        viewModelScope.launch {
            _actionLoading.value = true
            try {
                val resp = apiService.confirmRegistration(registrationId)
                if (resp.isSuccessful) {
                    _actionSuccess.value = "Kandidat berhasil diterima!"
                    loadRegistration(registrationId)
                } else {
                    _error.value = "Gagal menerima kandidat (${resp.code()})"
                }
            } catch (e: Exception) {
                _error.value = "Koneksi gagal: ${e.localizedMessage}"
            } finally {
                _actionLoading.value = false
            }
        }
    }

    fun onReject(registrationId: Int, reason: String? = null) {
        viewModelScope.launch {
            _actionLoading.value = true
            try {
                val body = if (!reason.isNullOrBlank()) mapOf("reason" to reason) else emptyMap()
                val resp = apiService.rejectRegistration(registrationId, body)
                if (resp.isSuccessful) {
                    _actionSuccess.value = "Kandidat ditolak."
                    loadRegistration(registrationId)
                } else {
                    _error.value = "Gagal menolak kandidat (${resp.code()})"
                }
            } catch (e: Exception) {
                _error.value = "Koneksi gagal: ${e.localizedMessage}"
            } finally {
                _actionLoading.value = false
            }
        }
    }

    fun onMarkAttend(registrationId: Int) {
        viewModelScope.launch {
            _actionLoading.value = true
            try {
                val resp = apiService.attendRegistration(registrationId)
                if (resp.isSuccessful) {
                    _actionSuccess.value = "Kandidat ditandai hadir!"
                    loadRegistration(registrationId)
                } else {
                    _error.value = "Gagal tandai hadir (${resp.code()})"
                }
            } catch (e: Exception) {
                _error.value = "Koneksi gagal: ${e.localizedMessage}"
            } finally {
                _actionLoading.value = false
            }
        }
    }

    fun onActionHandled()  { _actionSuccess.value = null }
    fun onErrorDismissed() { _error.value = null }
}

private val BgColor       = Color(0xFFF4F7EF)
private val CardWhite     = Color(0xFFFFFFFF)
private val AccentGreen   = Color(0xFF5A7A5A)
private val TextPrimary   = Color(0xFF1E2D1E)
private val TextSecondary = Color(0xFF6E8F6E)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CandidateDetailScreen(
    candidateId: Int,
    viewModel: CandidateDetailViewModel = hiltViewModel()
) {
    val backStack    = LocalBackStack.current
    val registration by viewModel.registration.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val error        by viewModel.error.collectAsState()
    val actionLoad   by viewModel.actionLoading.collectAsState()
    val actionMsg    by viewModel.actionSuccess.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(candidateId) { viewModel.loadRegistration(candidateId) }
    LaunchedEffect(actionMsg) {
        actionMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onActionHandled()
        }
    }

    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason     by remember { mutableStateOf("") }
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false; rejectReason = "" },
            icon  = { Icon(Icons.Default.Cancel, null, tint = Color(0xFFCC2222), modifier = Modifier.size(28.dp)) },
            title = { Text("Tolak Kandidat?", fontWeight = FontWeight.Bold) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Masukkan alasan penolakan (opsional):", fontSize = 13.sp, color = TextSecondary)
                    OutlinedTextField(
                        value         = rejectReason,
                        onValueChange = { rejectReason = it },
                        placeholder   = { Text("Contoh: Kuota sudah terpenuhi") },
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(10.dp),
                        minLines      = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reg = registration ?: return@Button
                        viewModel.onReject(reg.id, rejectReason.ifBlank { null })
                        showRejectDialog = false; rejectReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC2222))
                ) { Text("Tolak") }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false; rejectReason = "" }) { Text("Batal") }
            }
        )
    }

    var accessDenied by remember { mutableStateOf(!viewModel.organizerSession.hasAccess()) }
    if (accessDenied) {
        AlertDialog(
            onDismissRequest = { backStack.removeLastOrNull() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Lock, null, tint = Color(0xFFCC0000))
                    Text("Akses Ditolak", fontWeight = FontWeight.Bold)
                }
            },
            text  = { Text("Silakan login terlebih dahulu sebagai organisasi.") },
            confirmButton = {
                Button(
                    onClick = { accessDenied = false; backStack.removeLastOrNull() },
                    colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC0000))
                ) { Text("Kembali") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detail Kandidat", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor)
            )
        },
        containerColor = BgColor
    ) { innerPadding ->
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentGreen)
                }
            }
            error != null -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.ErrorOutline, null, tint = Color.Red, modifier = Modifier.size(40.dp))
                        Text(error ?: "Terjadi kesalahan", color = Color.Red, fontSize = 14.sp)
                        TextButton(onClick = { viewModel.onErrorDismissed(); backStack.removeLastOrNull() }) {
                            Text("Kembali")
                        }
                    }
                }
            }
            registration == null -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text("Data tidak ditemukan", color = TextSecondary)
                }
            }
            else -> {
                val reg       = registration!!
                val volunteer = reg.user
                val vp        = volunteer?.volunteer_profile
                val event     = reg.event

                LazyColumn(
                    modifier            = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding      = PaddingValues(bottom = 32.dp, top = 8.dp)
                ) {
                    item(key = "profile_hero") {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {

                                // Sentuhan border putih pada Avatar
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDAEFDC))
                                        .border(3.dp, CardWhite, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val avatarUrl = volunteer?.avatar
                                    if (!avatarUrl.isNullOrBlank()) {
                                        AsyncImage(model = avatarUrl, contentDescription = volunteer?.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                                    } else {
                                        Text(
                                            volunteer?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                            fontSize = 32.sp, fontWeight = FontWeight.Bold, color = AccentGreen
                                        )
                                    }
                                }

                                Spacer(Modifier.height(4.dp))
                                Text(volunteer?.name ?: "Volunteer", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Email, null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                                    Text(volunteer?.email ?: "", fontSize = 12.sp, color = TextSecondary)
                                }
                                if (!volunteer?.phone.isNullOrBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.Phone, null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                                        Text(volunteer!!.phone!!, fontSize = 12.sp, color = TextSecondary)
                                    }
                                }
                                val kotaProvinsi = listOfNotNull(vp?.city, vp?.province).joinToString(", ")
                                if (kotaProvinsi.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.LocationOn, null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                                        Text(kotaProvinsi, fontSize = 12.sp, color = TextSecondary)
                                    }
                                }
                                if (!vp?.gender.isNullOrBlank()) {
                                    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFEEEEEE)) {
                                        Text(when (vp?.gender) { "male" -> "Laki-laki"; "female" -> "Perempuan"; else -> vp?.gender ?: "" },
                                            fontSize = 11.sp, color = Color(0xFF555555), modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
                                    }
                                }
                                if (!vp?.date_of_birth.isNullOrBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.Cake, null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                                        Text(
                                            DateUtils.formatDate(vp?.date_of_birth),
                                            fontSize = 12.sp, color = TextSecondary
                                        )
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                val (statusText, statusColor, statusBg) = when (reg.status) {
                                    "confirmed" -> Triple("Diterima", Color(0xFF2E5C1A), Color(0xFFD4EDCA))
                                    "cancelled" -> Triple("Ditolak",  Color(0xFF8B0000), Color(0xFFFDE8E8))
                                    "attended"  -> Triple("Hadir",    Color(0xFF1A4D7A), Color(0xFFD0E8FF))
                                    else        -> Triple("Pending",  Color(0xFF7A5C00), Color(0xFFFFF3CD))
                                }
                                Box(modifier = Modifier.background(statusBg, RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 6.dp)) {
                                    Text("Status: $statusText", fontSize = 13.sp, color = statusColor, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    item(key = "stats") {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("REKAM JEJAK VOLUNTEER", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = TextSecondary, letterSpacing = 0.5.sp)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    StatBox("Total\nKegiatan", vp?.total_events_joined?.toString() ?: "-", Color(0xFFEEF4FF), Color(0xFF1A4D7A), Modifier.weight(1f))
                                    StatBox("Bergabung\n(Profil)", vp?.total_events_joined?.toString() ?: "-", Color(0xFFD4EDCA), Color(0xFF2E5C1A), Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    if (!vp?.bio.isNullOrBlank()) {
                        item(key = "bio") {
                            InfoSectionCard(title = "Tentang Relawan") {
                                QuoteBioBox(bio = vp!!.bio!!)
                            }
                        }
                    }

                    if (!vp?.skills.isNullOrEmpty()) {
                        item(key = "skills") {
                            InfoSectionCard(title = "Keahlian Utama") {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    vp!!.skills!!.forEach { skill ->
                                        ModernChip(text = skill, isSkill = true)
                                    }
                                }
                            }
                        }
                    }

                    if (!vp?.interests.isNullOrEmpty()) {
                        item(key = "interests") {
                            InfoSectionCard(title = "Minat & Ketertarikan") {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    vp!!.interests!!.forEach { interest ->
                                        ModernChip(text = interest, isSkill = false)
                                    }
                                }
                            }
                        }
                    }

                    item(key = "activity") {
                        InfoSectionCard(title = "Kegiatan yang Didaftari") {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Event, null, tint = AccentGreen, modifier = Modifier.size(15.dp))
                                    Text(event?.title ?: "-", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                }
                                if (!event?.city.isNullOrBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.LocationOn, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                        Text("${event?.city ?: ""}, ${event?.province ?: ""}".trim(',', ' '), fontSize = 12.sp, color = TextSecondary)
                                    }
                                }
                                if (!event?.start_date.isNullOrBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.CalendarToday, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                        Text(DateUtils.formatDate(event!!.start_date), fontSize = 12.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }

                    item(key = "registration_info") {
                        InfoSectionCard(title = "Info Pendaftaran") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                InfoRow(
                                    icon  = Icons.Default.CalendarToday,
                                    label = "Tanggal Daftar",
                                    value = DateUtils.formatDateTime(reg.registered_at)
                                )
                                if (!reg.notes.isNullOrBlank()) {
                                    InfoRow(Icons.Default.Notes, "Catatan", reg.notes)
                                }
                                if (reg.status == "cancelled" && !reg.cancellation_reason.isNullOrBlank()) {
                                    InfoRow(Icons.Default.ErrorOutline, "Alasan Ditolak", reg.cancellation_reason, valueColor = Color(0xFF8B0000))
                                }
                            }
                        }
                    }

                    if (reg.status == "pending" || reg.status == "confirmed") {
                        item(key = "actions") {
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("TINDAKAN", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = TextSecondary, letterSpacing = 0.5.sp)
                                    if (reg.status == "pending") {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(onClick = { viewModel.onConfirm(reg.id) }, enabled = !actionLoad, modifier = Modifier.weight(1f).height(46.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentGreen), shape = RoundedCornerShape(10.dp)) {
                                                if (actionLoad) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                                else { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Terima", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                                            }
                                            OutlinedButton(onClick = { showRejectDialog = true }, enabled = !actionLoad, modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCC2222))) {
                                                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Tolak", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                    if (reg.status == "confirmed") {
                                        Button(onClick = { viewModel.onMarkAttend(reg.id) }, enabled = !actionLoad, modifier = Modifier.fillMaxWidth().height(46.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A4D7A)), shape = RoundedCornerShape(10.dp)) {
                                            if (actionLoad) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                            else { Icon(Icons.Default.HowToReg, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Tandai Hadir", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Helper UI Components ---

@Composable
private fun InfoSectionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardWhite), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title.uppercase(), fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = TextSecondary, letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 10.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String, valueColor: Color = TextPrimary) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.size(32.dp).background(Color(0xFFDAEFDC), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = AccentGreen, modifier = Modifier.size(15.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Text(value, fontSize = 13.sp, color = valueColor, fontWeight = FontWeight.Medium, lineHeight = 19.sp)
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, bg: Color, textColor: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(bg, RoundedCornerShape(12.dp)).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = textColor, lineHeight = 24.sp)
            Text(label, fontSize = 10.sp, color = textColor.copy(alpha = 0.7f), lineHeight = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(horizontal = 4.dp))
        }
    }
}

@Composable
private fun ModernChip(text: String, isSkill: Boolean) {
    val bgColor = if (isSkill) Color(0xFFE8F5E9) else Color(0xFFFFF8E1)
    val contentColor = if (isSkill) Color(0xFF2E7D32) else Color(0xFFF57F17)
    val borderColor = if (isSkill) Color(0xFFA5D6A7) else Color(0xFFFFE082)
    val icon = if (isSkill) Icons.Default.Star else Icons.Default.LocalFireDepartment

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = contentColor
            )
            Spacer(Modifier.width(4.dp))
            Text(text = text, fontSize = 12.sp, color = contentColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun QuoteBioBox(bio: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FBF9), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(IntrinsicSize.Min)
                .background(AccentGreen, RoundedCornerShape(50.dp))
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "\"$bio\"",
            fontSize = 13.sp,
            color = TextPrimary.copy(alpha = 0.8f),
            lineHeight = 20.sp,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}