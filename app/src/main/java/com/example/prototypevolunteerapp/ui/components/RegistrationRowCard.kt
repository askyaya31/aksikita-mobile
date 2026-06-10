package com.example.prototypevolunteerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.prototypevolunteerapp.data.remote.dto.UserDto

// Warna tema konsisten dengan dashboard
private val AccentBlue   = Color(0xFF5B9BD5)
private val DeclineRed   = Color(0xFFFF6B6B)
private val AcceptGreen  = Color(0xFF4CAF50)
private val AttendBlue   = Color(0xFF1A4D7A)
private val TextDark     = Color(0xFF1A1A2E)
private val TextMuted    = Color(0xFF777799)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationRowCard(
    name:         String,
    email:        String,
    status:       String,
    user:         UserDto?  = null,   // untuk detail sheet
    onConfirm:    () -> Unit,
    onReject:     () -> Unit,
    onAttend:     () -> Unit,
    onViewDetail: () -> Unit = {}
) {
    var showDetailSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ── Detail Bottom Sheet ───────────────────────────────────────────────────
    if (showDetailSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDetailSheet = false },
            sheetState       = sheetState,
            containerColor   = Color.White,
            shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // Avatar
                val avatarUrl = user?.volunteer_profile?.avatar ?: user?.avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(AccentBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model              = avatarUrl,
                            contentDescription = name,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Text(
                            text       = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            fontSize   = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color      = AccentBlue
                        )
                    }
                }

                // Nama & email
                Text(name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                Text(email, fontSize = 13.sp, color = TextMuted)

                // Status badge
                val (statusText, statusColor, statusBg) = statusMeta(status)
                Surface(shape = RoundedCornerShape(20.dp), color = statusBg) {
                    Text(
                        statusText,
                        fontSize   = 12.sp,
                        color      = statusColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))

                // Info rows
                val profile = user?.volunteer_profile
                DetailInfoRow(Icons.Default.LocationOn, "Kota",
                    profile?.city ?: user?.volunteer_profile?.province ?: "-")
                DetailInfoRow(Icons.Default.Phone, "Telepon", user?.phone ?: "-")
                DetailInfoRow(Icons.Default.CalendarToday, "Tanggal Lahir",
                    profile?.date_of_birth ?: "-")
                DetailInfoRow(Icons.Default.Person, "Gender",
                    profile?.gender?.replaceFirstChar { it.uppercase() } ?: "-")
                DetailInfoRow(Icons.Default.EmojiEvents, "Total Kegiatan Diikuti",
                    "${profile?.total_events_joined ?: 0} kegiatan")

                // Bio
                if (!profile?.bio.isNullOrBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F7FF), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Bio", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                        Text(profile!!.bio!!, fontSize = 13.sp, color = TextDark)
                    }
                }

                // Skills
                if (!profile?.skills.isNullOrEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Skills", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            profile!!.skills!!.take(5).forEach { skill ->
                                Surface(shape = RoundedCornerShape(20.dp), color = AccentBlue.copy(0.1f)) {
                                    Text(
                                        skill,
                                        fontSize = 11.sp,
                                        color    = AccentBlue,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Interests
                if (!profile?.interests.isNullOrEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Minat", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            profile!!.interests!!.take(5).forEach { interest ->
                                Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFFFF3CD)) {
                                    Text(
                                        interest,
                                        fontSize = 11.sp,
                                        color    = Color(0xFF7A5C00),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // ── Card utama ────────────────────────────────────────────────────────────
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Avatar kiri ───────────────────────────────────────────────────
            val avatarUrl = user?.volunteer_profile?.avatar ?: user?.avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AccentBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model              = avatarUrl,
                        contentDescription = name,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(
                        text       = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = AccentBlue
                    )
                }
            }

            // ── Konten tengah ─────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // Judul kegiatan (dari event jika tersedia, fallback ke nama)
                Text(
                    text       = name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 13.sp,
                    color      = TextDark,
                    maxLines   = 1
                )

                val profile = user?.volunteer_profile
                val cityLine = buildString {
                    if (!profile?.city.isNullOrBlank()) append(profile!!.city)
                    else if (!profile?.province.isNullOrBlank()) append(profile!!.province)
                }
                if (cityLine.isNotBlank()) {
                    Text(cityLine, fontSize = 11.sp, color = TextMuted)
                }

                val eventsJoined = profile?.total_events_joined ?: 0
                Text(
                    "$eventsJoined activity participated",
                    fontSize = 11.sp,
                    color    = TextMuted
                )

                Spacer(Modifier.height(6.dp))

                // ── Tombol Decline / Accept (atau Hadir) ──────────────────────
                when (status) {
                    "pending" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick  = onReject,
                                modifier = Modifier.height(32.dp),
                                shape    = RoundedCornerShape(8.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = DeclineRed),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                            ) { Text("Decline", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }

                            Button(
                                onClick  = onConfirm,
                                modifier = Modifier.height(32.dp),
                                shape    = RoundedCornerShape(8.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = AcceptGreen),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                            ) { Text("Accept", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        }
                    }
                    "confirmed" -> {
                        Button(
                            onClick  = onAttend,
                            modifier = Modifier.height(32.dp),
                            shape    = RoundedCornerShape(8.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = AttendBlue),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                        ) { Text("Tandai Hadir", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    }
                    else -> {
                        // Tampilkan status badge saja untuk cancelled/attended
                        val (statusText, statusColor, statusBg) = statusMeta(status)
                        Surface(shape = RoundedCornerShape(20.dp), color = statusBg) {
                            Text(
                                statusText,
                                fontSize   = 11.sp,
                                color      = statusColor,
                                fontWeight = FontWeight.SemiBold,
                                modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // ── Tombol (i) kanan ──────────────────────────────────────────────
            IconButton(
                onClick  = { showDetailSheet = true },
                modifier = Modifier
                    .size(36.dp)
                    .background(AccentBlue.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Detail volunteer",
                    tint     = AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ── Helper: status meta ───────────────────────────────────────────────────────
private fun statusMeta(status: String): Triple<String, Color, Color> = when (status) {
    "confirmed" -> Triple("Diterima", Color(0xFF2E5C1A), Color(0xFFD4EDCA))
    "cancelled" -> Triple("Ditolak",  Color(0xFF8B0000), Color(0xFFFDE8E8))
    "attended"  -> Triple("Hadir",    Color(0xFF1A4D7A), Color(0xFFD0E8FF))
    else        -> Triple("Pending",  Color(0xFF7A5C00), Color(0xFFFFF3CD))
}

// ── Helper: baris info di detail sheet ───────────────────────────────────────
@Composable
private fun DetailInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, tint = AccentBlue, modifier = Modifier.size(16.dp))
        Column {
            Text(label, fontSize = 10.sp, color = TextMuted)
            Text(value, fontSize = 13.sp, color = TextDark, fontWeight = FontWeight.Medium)
        }
    }
}