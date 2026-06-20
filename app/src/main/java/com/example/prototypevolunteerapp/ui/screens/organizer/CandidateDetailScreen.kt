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
import androidx.compose.material.icons.outlined.Chat
import com.example.prototypevolunteerapp.core.Routes
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
    private val apiService: ApiService,
    val organizerSession: OrganizerSession
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

    fun onActionHandled() { _actionSuccess.value = null }
    fun onErrorDismissed() { _error.value = null }
}
private val NavyDark    = Color(0xFF2850C4)
private val PrimaryBlue = Color(0xFF205CCB)
private val MediumBlue  = Color(0xFF60A5FA)
private val PalestBlue  = Color(0xFFBFDBFE)
private val BgColor     = Color(0xFFF0F5FF)
private val CardWhite   = Color(0xFFFFFFFF)
private val AccentBlue  = PrimaryBlue
private val DeepBlue    = NavyDark
private val TextDark    = Color(0xFF0F172A)
private val TextMuted   = Color(0xFF64748B)
private val DeclineRed  = Color(0xFFF67979)
private val AcceptGreen = Color(0xFF3DAD54)
private val PendingAmber  = Color(0xFFF39F5F)
private val AcceptGreenBg = Color(0xFFDCFDE9)
private val DeclineRedBg  = Color(0xFFFEE2E2)
private val AttendedBlueBg = Color(0xFFDBEAFE)
private val PendingAmberBg = Color(0xFFFEF3C7)
private val InterestBlueBg = Color(0xFFDBEAFE)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CandidateDetailScreen(
    candidateId: Int,
    viewModel: CandidateDetailViewModel = hiltViewModel()
) {
    val backStack         = LocalBackStack.current
    val registration      by viewModel.registration.collectAsState()
    val isLoading         by viewModel.isLoading.collectAsState()
    val error             by viewModel.error.collectAsState()
    val actionLoad        by viewModel.actionLoading.collectAsState()
    val actionMsg         by viewModel.actionSuccess.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(candidateId) { viewModel.loadRegistration(candidateId) }
    LaunchedEffect(actionMsg) {
        actionMsg?.let { snackbarHostState.showSnackbar(it); viewModel.onActionHandled() }
    }

    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason     by remember { mutableStateOf("") }
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false; rejectReason = "" },
            icon  = { Icon(Icons.Default.Cancel, null, tint = DeclineRed, modifier = Modifier.size(28.dp)) },
            title = { Text("Tolak Kandidat?", fontWeight = FontWeight.Bold) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Masukkan alasan penolakan (opsional):", fontSize = 13.sp, color = TextMuted)
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
                    colors = ButtonDefaults.buttonColors(containerColor = DeclineRed)
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
                title = {
                    Column {
                        Text("Detail Volunteer", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        Text("See who register the activity", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors   = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.background(Brush.linearGradient(listOf(NavyDark, PrimaryBlue)))
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
        ) {
            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentBlue)
                    }
                }
                error != null -> {
                    Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.ErrorOutline, null, tint = DeclineRed, modifier = Modifier.size(40.dp))
                            Text(error ?: "Terjadi kesalahan", color = DeclineRed, fontSize = 14.sp)
                            TextButton(onClick = { viewModel.onErrorDismissed(); backStack.removeLastOrNull() }) { Text("Kembali") }
                        }
                    }
                }
                registration == null -> {
                    Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        Text("Data tidak ditemukan", color = TextMuted)
                    }
                }
                else -> {
                    val reg       = registration!!
                    val volunteer = reg.user
                    val vp        = volunteer?.volunteer_profile
                    val event     = reg.event

                    LazyColumn(
                        modifier            = Modifier.fillMaxSize().padding(innerPadding),
                        contentPadding      = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        item(key = "hero") {
                            Card(
                                modifier  = Modifier
                                    .fillMaxWidth(),
                                shape     = RoundedCornerShape(20.dp),
                                colors    = CardDefaults.cardColors(containerColor = CardWhite),
                                elevation = CardDefaults.cardElevation(3.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {

                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(90.dp)
                                                .background(
                                                    Brush.linearGradient(listOf(NavyDark, PrimaryBlue, MediumBlue))
                                                )
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .offset(y = 44.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(88.dp)
                                                    .clip(CircleShape)
                                                    .background(CardWhite)
                                                    .border(3.dp, CardWhite, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val avatarUrl = vp?.avatar ?: volunteer?.avatar
                                                if (!avatarUrl.isNullOrBlank()) {
                                                    AsyncImage(
                                                        model              = avatarUrl,
                                                        contentDescription = volunteer?.name,
                                                        contentScale       = ContentScale.Crop,
                                                        modifier           = Modifier.fillMaxSize().clip(CircleShape)
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(
                                                                Brush.linearGradient(listOf(NavyDark, PrimaryBlue)),
                                                                CircleShape
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            volunteer?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                                            fontSize   = 34.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color      = Color.White
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(52.dp))

                                    Column(
                                        modifier            = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Text(
                                            volunteer?.name ?: "Volunteer",
                                            fontWeight = FontWeight.Bold,
                                            fontSize   = 20.sp,
                                            color      = TextDark
                                        )

                                        val kotaInstitusi = listOfNotNull(vp?.city, vp?.province).joinToString(", ")
                                        if (kotaInstitusi.isNotBlank()) {
                                            Row(
                                                verticalAlignment     = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(Icons.Default.LocationOn, null,
                                                    tint     = AccentBlue,
                                                    modifier = Modifier.size(13.dp))
                                                Text(kotaInstitusi, fontSize = 13.sp, color = TextMuted)
                                            }
                                        }

                                        val (statusText, statusColor, statusBg, statusIcon) = when (reg.status) {
                                            "confirmed" -> arrayOf("Diterima", Color(0xFF166534), AcceptGreenBg,  Icons.Default.CheckCircle)
                                            "cancelled" -> arrayOf("Ditolak",  Color(0xFF991B1B), DeclineRedBg,   Icons.Default.Cancel)
                                            "attended"  -> arrayOf("Hadir",    Color(0xFF1E3A8A), AttendedBlueBg, Icons.Default.HowToReg)
                                            else        -> arrayOf("Pending",  Color(0xFF92400E), PendingAmberBg, Icons.Default.HourglassEmpty)
                                        }
                                        @Suppress("UNCHECKED_CAST")
                                        Surface(
                                            shape = RoundedCornerShape(99.dp),
                                            color = statusBg as Color
                                        ) {
                                            Row(
                                                modifier              = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                                                verticalAlignment     = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Icon(
                                                    statusIcon as androidx.compose.ui.graphics.vector.ImageVector,
                                                    null,
                                                    tint     = statusColor as Color,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    statusText as String,
                                                    fontSize   = 12.sp,
                                                    color      = statusColor,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                                    val stats = volunteer?.volunteer_stats
                                    if (stats != null) {
                                        Row(
                                            modifier              = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 14.dp),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            HeroStat("${stats.total_daftar}",  "Total Daftar", NavyDark)
                                            VerticalDivider(modifier = Modifier.height(36.dp), color = Color(0xFFEEEEEE))
                                            HeroStat("${stats.total_confirmed}", "Diterima",  NavyDark)
                                            VerticalDivider(modifier = Modifier.height(36.dp), color = Color(0xFFEEEEEE))
                                            HeroStat("${stats.total_hadir}",   "Hadir",       NavyDark)
                                        }
                                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)
                                    }
                                }
                            }
                        }

                        item(key = "kontak") {
                            DetailSectionCard(title = "Informasi Kontak") {
                                if (!volunteer?.email.isNullOrBlank())
                                    DetailInfoRow(Icons.Default.Email, "Email", volunteer!!.email)
                                if (!volunteer?.phone.isNullOrBlank())
                                    DetailInfoRow(Icons.Default.Phone, "Telepon", volunteer!!.phone!!)
                                if (!vp?.date_of_birth.isNullOrBlank())
                                    DetailInfoRow(Icons.Default.Cake, "Tanggal Lahir", DateUtils.formatDate(vp?.date_of_birth))
                                if (!vp?.gender.isNullOrBlank())
                                    DetailInfoRow(
                                        Icons.Default.Person, "Gender",
                                        when (vp?.gender) { "male" -> "Laki-laki"; "female" -> "Perempuan"; else -> vp?.gender ?: "" }
                                    )
                            }
                        }

                        if (!vp?.bio.isNullOrBlank()) {
                            item(key = "bio") {
                                DetailSectionCard(title = "Tentang Relawan") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(BgColor, RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .heightIn(min = 20.dp)
                                                .background(AccentBlue, RoundedCornerShape(50.dp))
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            vp!!.bio!!,
                                            fontSize   = 13.sp,
                                            color      = TextDark.copy(alpha = 0.8f),
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }
                        }

                        if (!vp?.skills.isNullOrEmpty()) {
                            item(key = "skills") {
                                DetailSectionCard(title = "Keahlian") {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement   = Arrangement.spacedBy(8.dp),
                                        modifier              = Modifier.fillMaxWidth()
                                    ) {
                                        vp!!.skills!!.forEach { skill -> VolChip(text = skill, isSkill = true) }
                                    }
                                }
                            }
                        }

                        if (!vp?.interests.isNullOrEmpty()) {
                            item(key = "interests") {
                                DetailSectionCard(title = "Minat") {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement   = Arrangement.spacedBy(8.dp),
                                        modifier              = Modifier.fillMaxWidth()
                                    ) {
                                        vp!!.interests!!.forEach { interest -> VolChip(text = interest, isSkill = false) }
                                    }
                                }
                            }
                        }

                        val history = volunteer?.registration_history
                            ?.filter { it.status == "attended" || it.status == "confirmed" }
                        if (!history.isNullOrEmpty()) {
                            item(key = "history") {
                                DetailSectionCard(title = "Riwayat Kegiatan") {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        history.forEachIndexed { index, h ->
                                            val (hColor, hBg) = when (h.status) {
                                                "confirmed" -> AcceptGreen to AcceptGreenBg
                                                "attended"  -> DeepBlue    to AttendedBlueBg
                                                "cancelled" -> DeclineRed  to DeclineRedBg
                                                else        -> PendingAmber to PendingAmberBg
                                            }
                                            val hLabel = when (h.status) {
                                                "confirmed" -> "Diterima"
                                                "attended"  -> "Hadir"
                                                "cancelled" -> "Dibatalkan"
                                                else        -> "Pending"
                                            }
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Text(
                                                    h.event_title ?: "-",
                                                    fontSize   = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color      = TextDark,
                                                    maxLines   = 2,
                                                    lineHeight = 18.sp
                                                )
                                                val sub = listOfNotNull(
                                                    h.start_date?.let { DateUtils.formatDate(it) },
                                                    h.city
                                                ).joinToString(" · ")
                                                if (sub.isNotBlank()) {
                                                    Text(sub, fontSize = 11.sp, color = TextMuted)
                                                }
                                            }
                                            if (index < history.lastIndex) {
                                                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item(key = "event") {
                            DetailSectionCard(title = "Kegiatan yang Didaftari") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    DetailInfoRow(Icons.Default.Event, "Nama Kegiatan", event?.title ?: "-")
                                    if (!event?.city.isNullOrBlank())
                                        DetailInfoRow(
                                            Icons.Default.LocationOn, "Lokasi",
                                            listOfNotNull(event?.city, event?.province).joinToString(", ")
                                        )
                                    if (!event?.start_date.isNullOrBlank())
                                        DetailInfoRow(Icons.Default.CalendarToday, "Tanggal", DateUtils.formatDate(event!!.start_date))
                                }
                            }
                        }

                        item(key = "reg_info") {
                            DetailSectionCard(title = "Info Pendaftaran") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    DetailInfoRow(Icons.Default.CalendarToday, "Tanggal Daftar", DateUtils.formatDateTime(reg.registered_at))
                                    if (!reg.notes.isNullOrBlank())
                                        DetailInfoRow(Icons.Default.Notes, "Catatan", reg.notes)
                                    if (reg.status == "cancelled" && !reg.cancellation_reason.isNullOrBlank())
                                        DetailInfoRow(Icons.Default.ErrorOutline, "Alasan Ditolak", reg.cancellation_reason, valueColor = DeclineRed)
                                }
                            }
                        }
                        if (reg.status == "pending" || reg.status == "confirmed" || reg.status == "attended") {
                            item(key = "actions") {
                                when (reg.status) {
                                    "pending" -> {
                                        Row(
                                            modifier              = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick  = { showRejectDialog = true },
                                                enabled  = !actionLoad,
                                                modifier = Modifier.weight(1f).height(48.dp),
                                                shape    = RoundedCornerShape(12.dp),
                                                colors   = ButtonDefaults.outlinedButtonColors(contentColor = DeclineRed),
                                                border   = BorderStroke(1.5.dp, DeclineRed.copy(alpha = 0.6f))
                                            ) {
                                                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text("Decline", fontWeight = FontWeight.Bold)
                                            }
                                            Button(
                                                onClick  = { viewModel.onConfirm(reg.id) },
                                                enabled  = !actionLoad,
                                                modifier = Modifier.weight(1f).height(48.dp),
                                                shape    = RoundedCornerShape(12.dp),
                                                colors   = ButtonDefaults.buttonColors(containerColor = AcceptGreen)
                                            ) {
                                                if (actionLoad) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                                else {
                                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                                    Spacer(Modifier.width(6.dp))
                                                    Text("Accept", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                    "confirmed" -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            if (reg.chat_room_id != null) {
                                                OutlinedButton(
                                                    onClick  = {
                                                        backStack.add(
                                                            Routes.ChatRoomRoute(
                                                                roomId        = reg.chat_room_id,
                                                                eventTitle    = event?.title ?: "Kegiatan",
                                                                organizerName = volunteer?.name ?: "Volunteer",
                                                                isOrganizer   = true
                                                            )
                                                        )
                                                    },
                                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                                    shape    = RoundedCornerShape(12.dp),
                                                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = NavyDark),
                                                    border   = BorderStroke(1.5.dp, NavyDark.copy(alpha = 0.5f))
                                                ) {
                                                    Icon(Icons.Outlined.Chat, null, modifier = Modifier.size(16.dp))
                                                    Spacer(Modifier.width(6.dp))
                                                    Text("Buka Chat dengan Volunteer", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Button(
                                                onClick  = { viewModel.onMarkAttend(reg.id) },
                                                enabled  = !actionLoad,
                                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                                shape    = RoundedCornerShape(12.dp),
                                                colors   = ButtonDefaults.buttonColors(containerColor = DeepBlue)
                                            ) {
                                                if (actionLoad) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                                else {
                                                    Spacer(Modifier.width(6.dp))
                                                    Text("Tandai Hadir", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                    "attended" -> {
                                        if (reg.chat_room_id != null) {
                                            OutlinedButton(
                                                onClick  = {
                                                    backStack.add(
                                                        Routes.ChatRoomRoute(
                                                            roomId        = reg.chat_room_id,
                                                            eventTitle    = event?.title ?: "Kegiatan",
                                                            organizerName = volunteer?.name ?: "Volunteer",
                                                            isOrganizer   = true
                                                        )
                                                    )
                                                },
                                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                                shape    = RoundedCornerShape(12.dp),
                                                colors   = ButtonDefaults.outlinedButtonColors(contentColor = NavyDark),
                                                border   = BorderStroke(1.5.dp, NavyDark.copy(alpha = 0.5f))
                                            ) {
                                                Icon(Icons.Outlined.Chat, null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text("Chat dengan Volunteer", fontWeight = FontWeight.Bold)
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
}

@Composable
private fun HeroStat(value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DetailSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                title.uppercase(),
                fontWeight    = FontWeight.SemiBold,
                fontSize      = 11.sp,
                color         = TextMuted,
                letterSpacing = 0.8.sp
            )
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
            content()
        }
    }
}

@Composable
private fun DetailInfoRow(
    icon:       ImageVector,
    label:      String,
    value:      String,
    valueColor: Color = TextDark
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier         = Modifier.size(34.dp).background(AccentBlue.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = AccentBlue, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 10.sp, color = TextMuted)
            Text(value, fontSize = 13.sp, color = valueColor, fontWeight = FontWeight.Medium, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun VolChip(text: String, isSkill: Boolean) {
    val bgColor      = if (isSkill) PalestBlue else InterestBlueBg
    val contentColor = if (isSkill) NavyDark else PrimaryBlue
    val borderColor  = if (isSkill) AccentBlue.copy(alpha = 0.3f) else NavyDark.copy(alpha = 0.25f)

    Surface(
        shape  = RoundedCornerShape(8.dp),
        color  = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text,
            fontSize   = 12.sp,
            color      = contentColor,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun StatBox(
    modifier: Modifier,
    value:    String,
    label:    String,
    valueCl:  Color,
    bgColor:  Color
) {
    Column(
        modifier            = modifier
            .background(bgColor, RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = valueCl)
        Text(label, fontSize = 10.sp, color = valueCl.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
    }
}