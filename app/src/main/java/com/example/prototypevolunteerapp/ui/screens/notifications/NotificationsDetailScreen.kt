package com.example.prototypevolunteerapp.ui.screens.notifications

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.prototypevolunteerapp.core.DateUtils
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.Routes
import com.example.prototypevolunteerapp.data.remote.dto.NotificationDto


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsDetailScreen(
    notificationId: Int,
    viewModel: NotifDetailViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val uiState   by viewModel.uiState.collectAsState()

    LaunchedEffect(notificationId) { viewModel.init(notificationId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Detail Notifikasi", fontWeight = FontWeight.Bold,
                        fontSize = 18.sp, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E3A8A))
            )
        },
        containerColor = Color(0xFFF8FAFF)
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF3B82F6))
                }
            }
            uiState.error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, null,
                            tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Text(uiState.error!!, color = Color.Gray, fontSize = 14.sp)
                        TextButton(onClick = { backStack.removeLastOrNull() }) {
                            Text("Kembali")
                        }
                    }
                }
            }
            uiState.notification != null -> {
                NotifDetailContent(
                    notif          = uiState.notification!!,
                    isLoadingEvent = uiState.isLoadingEvent,
                    padding        = padding,
                    onGoToEvent    = { eventId ->
                        viewModel.fetchRelatedEvent(
                            eventId  = eventId,
                            onFound  = { event ->
                                backStack.add(
                                    Routes.ActivityDetailRoute(
                                        id       = event.id.toString(),
                                        slug     = event.slug ?: "",
                                        title    = event.title,
                                        location = buildString {
                                            if (!event.location_name.isNullOrBlank()) append(event.location_name)
                                            if (!event.city.isNullOrBlank()) {
                                                if (isNotEmpty()) append(", ")
                                                append(event.city)
                                            }
                                        }.ifBlank { "" },
                                        desc     = event.description ?: "",
                                        imageRes = event.poster ?: ""
                                    )
                                )
                            },
                            onNotFound = {
                                backStack.add(Routes.ActivitiesRoute)
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun NotifDetailContent(
    notif:          NotificationDto,
    isLoadingEvent: Boolean,
    padding:        PaddingValues,
    onGoToEvent:    (Int) -> Unit
) {
    val (icon, iconBg, iconTint) = when (notif.type) {
        "registration_confirmed" -> Triple(Icons.Default.CheckCircle,  Color(0xFFE3F5F2), Color(0xFF0E7B6C))
        "registration_rejected"  -> Triple(Icons.Default.Cancel,        Color(0xFFFEE2E2), Color(0xFFEF4444))
        "event_cancelled"        -> Triple(Icons.Default.EventBusy,     Color(0xFFFEF3C7), Color(0xFFD4900A))
        "event_updated"          -> Triple(Icons.Default.Update,         Color(0xFFEFF6FF), Color(0xFF3B82F6))
        "new_registration"       -> Triple(Icons.Default.PersonAdd,      Color(0xFFEFF6FF), Color(0xFF3B82F6))
        "event_reminder"         -> Triple(Icons.Default.Alarm,          Color(0xFFFEF3C7), Color(0xFFD4900A))
        "event_reviewed"         -> Triple(Icons.Default.RateReview,     Color(0xFFEFF6FF), Color(0xFF1E3A8A))
        else                     -> Triple(Icons.Default.Notifications,  Color(0xFFEFF6FF), Color(0xFF3B82F6))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header card
        Card(
            shape     = RoundedCornerShape(18.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier            = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier         = Modifier.size(60.dp).clip(CircleShape).background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = iconTint, modifier = Modifier.size(28.dp))
                }
                Text(notif.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Schedule, null,
                        tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                    Text(DateUtils.formatDateTime(notif.created_at),
                        fontSize = 12.sp, color = Color(0xFF94A3B8))
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (notif.is_read) Color(0xFFF1F5F9) else Color(0xFFEFF6FF)
                ) {
                    Text(
                        if (notif.is_read) "Sudah Dibaca" else "Belum Dibaca",
                        fontSize = 11.sp,
                        color    = if (notif.is_read) Color(0xFF64748B) else Color(0xFF3B82F6),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Pesan card
        Card(
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier            = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailSectionLabel("Pesan", Icons.Default.Message)
                Text(notif.message, fontSize = 14.sp,
                    color = Color(0xFF334155), lineHeight = 22.sp)
            }
        }

        // Info, tombol kegiatan
        if (notif.type != null || notif.related_event_id != null) {
            Card(
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailSectionLabel("Informasi", Icons.Default.Info)

                    if (notif.type != null) {
                        DetailInfoRow(
                            icon  = Icons.Default.Category,
                            label = "Tipe Notifikasi",
                            value = when (notif.type) {
                                "registration_confirmed" -> "Pendaftaran Diterima"
                                "registration_rejected"  -> "Pendaftaran Ditolak"
                                "event_cancelled"        -> "Kegiatan Dibatalkan"
                                "event_updated"          -> "Kegiatan Diperbarui"
                                "new_registration"       -> "Pendaftar Baru"
                                "event_reminder"         -> "Pengingat Kegiatan"
                                "event_reviewed"         -> "Event Direview Admin"
                                else                     -> notif.type
                            }
                        )
                    }

                    if (notif.related_event_id != null) {
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Button(
                            onClick  = { onGoToEvent(notif.related_event_id) },
                            enabled  = !isLoadingEvent,
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape    = RoundedCornerShape(10.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E3A8A)
                            )
                        ) {
                            if (isLoadingEvent) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color       = Color.White
                                )
                            } else {
                                Icon(Icons.Default.OpenInNew, null,
                                    modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Lihat Kegiatan Terkait",
                                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}
@Composable
private fun DetailSectionLabel(text: String, icon: ImageVector) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(15.dp))
        Text(
            text          = text.uppercase(),
            fontSize      = 11.sp,
            fontWeight    = FontWeight.SemiBold,
            color         = Color(0xFF64748B),
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun DetailInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(32.dp)
                .background(Color(0xFFEFF6FF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(15.dp))
        }
        Column {
            Text(label, fontSize = 11.sp, color = Color(0xFF94A3B8))
            Text(value, fontSize = 13.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Medium)
        }
    }
}