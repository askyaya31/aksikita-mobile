package com.example.prototypevolunteerapp.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NavyDark    = Color(0xFF1E3A8A)
private val PrimaryBlue = Color(0xFF3B82F6)
private val BgScreen    = Color(0xFFF8FAFF)
private val TextDark    = Color(0xFF0F172A)
private val TextMuted   = Color(0xFF64748B)

@Composable
fun NotificationsContent(
    uiState:             NotificationsUiState,
    onBack:              () -> Unit,
    onMarkAllAsRead:     () -> Unit,
    onNotificationClick: (Int) -> Unit,
    modifier:            Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("all") }

    val filteredNotifs = when (selectedFilter) {
        "unread" -> uiState.notifications.filter { !it.is_read }
        else     -> uiState.notifications
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgScreen)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(NavyDark, PrimaryBlue))
                )
                .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Back button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint     = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Judul
                    Text(
                        "Notifications",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                }

                // Mark all read button
                if (uiState.hasUnread) {
                    Surface(
                        shape    = RoundedCornerShape(99.dp),
                        color    = Color.White.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { onMarkAllAsRead() }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Mark all read",
                                fontSize   = 12.sp,
                                color      = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                Icons.Default.DoneAll,
                                null,
                                tint     = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Text(
                "Stay updated with your activities",
                fontSize = 13.sp,
                color    = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.padding(start = 50.dp)
            )
        }


        // FILTER CHIPS
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedFilter == "all",
                onClick  = { selectedFilter = "all" },
                label    = { Text("All", fontSize = 12.sp) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor     = Color.White
                ),
                shape = RoundedCornerShape(99.dp)
            )

            FilterChip(
                selected = selectedFilter == "unread",
                onClick  = { selectedFilter = "unread" },
                label    = {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Unread", fontSize = 12.sp)
                        if (uiState.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(
                                        if (selectedFilter == "unread") Color.White
                                        else PrimaryBlue,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${uiState.unreadCount}",
                                    fontSize   = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = if (selectedFilter == "unread") PrimaryBlue
                                    else Color.White
                                )
                            }
                        }
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor     = Color.White
                ),
                shape = RoundedCornerShape(99.dp)
            )
        }

        when {
            uiState.isLoading -> {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = PrimaryBlue) }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.WifiOff, null,
                            tint = Color(0xFFB0B0B0), modifier = Modifier.size(48.dp))
                        Text(uiState.errorMessage!!, color = TextMuted, fontSize = 14.sp)
                    }
                }
            }

            filteredNotifs.isEmpty() -> {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.NotificationsNone, null,
                            tint     = Color(0xFFB0B0B0),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            if (selectedFilter == "unread") "Semua notifikasi sudah dibaca"
                            else "Belum ada notifikasi",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextDark
                        )
                        Text(
                            "Notifikasi baru akan muncul di sini",
                            fontSize = 13.sp,
                            color    = TextMuted
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier       = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 8.dp,
                        end = 16.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredNotifs, key = { it.id }) { notif ->
                        NotificationListItem(
                            notification = notif,
                            onClick      = { onNotificationClick(notif.id) }
                        )
                    }
                }
            }
        }
    }
}