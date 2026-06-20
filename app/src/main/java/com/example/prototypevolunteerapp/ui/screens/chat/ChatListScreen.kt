package com.example.prototypevolunteerapp.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.Routes
import com.example.prototypevolunteerapp.ui.theme.*

private val NavyDark    = Color(0xFF1E3A8A)
private val PrimaryBlue = Color(0xFF3B82F6)
private val BgScreen    = Color(0xFFF8FAFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    isOrganizer: Boolean = false,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val state by viewModel.listState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadChatRooms(asOrganizer = isOrganizer) }

    val filteredRooms = remember(state.rooms, searchQuery) {
        if (searchQuery.isBlank()) state.rooms
        else state.rooms.filter {
            it.eventTitle.contains(searchQuery, ignoreCase = true) ||
                    it.organizerName.contains(searchQuery, ignoreCase = true)
        }
    }
    Scaffold(containerColor = BgScreen) { _ ->
        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(NavyDark, PrimaryBlue)))
                    .padding(horizontal = 16.dp)
                    .padding(
                        top    = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                        bottom = 20.dp
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { backStack.removeLastOrNull() }
                        .align(Alignment.CenterStart),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint     = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    "Pesan",
                    color      = Color.White,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.align(Alignment.Center)
                )
            }

            if (!state.isLoading && state.rooms.isNotEmpty()) {
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder   = { Text("Cari percakapan...", color = TextLight) },
                    leadingIcon   = { Icon(Icons.Default.Search, null, tint = PrimaryBlue) },
                    trailingIcon  = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, null,
                                    modifier = Modifier.size(18.dp), tint = TextLight)
                            }
                        }
                    },
                    singleLine    = true,
                    shape         = RoundedCornerShape(50.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = PrimaryBlue,
                        unfocusedBorderColor    = Color(0xFFE2E8F0),
                        focusedContainerColor   = White,
                        unfocusedContainerColor = White
                    )
                )
            }
            when {
                state.isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
                state.rooms.isEmpty() -> EmptyChatState(Modifier.fillMaxSize())
                filteredRooms.isEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada percakapan yang cocok", fontSize = 13.sp, color = TextLight)
                }
                else -> LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredRooms) { room ->
                        ChatRoomCard(
                            room    = room,
                            onClick = {
                                backStack.add(
                                    Routes.ChatRoomRoute(
                                        roomId        = room.id,
                                        eventTitle    = room.eventTitle,
                                        organizerName = room.organizerName,
                                        isOrganizer   = isOrganizer,
                                        organizerLogo = room.organizerLogo
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

private val avatarColors = listOf(
    Color(0xFF378ADD),
    Color(0xFF1D9E75),
    Color(0xFFD85A30),
    Color(0xFF534AB7),
    Color(0xFFD4537E),
)

private fun formatChatTime(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    return try {
        val formatters = listOf("yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss")
        var parsed: java.util.Date? = null
        for (pattern in formatters) {
            try {
                parsed = java.text.SimpleDateFormat(pattern, java.util.Locale("id", "ID")).parse(raw)
                if (parsed != null) break
            } catch (_: Exception) {}
        }
        if (parsed == null) return raw

        val now      = java.util.Calendar.getInstance()
        val msgCal   = java.util.Calendar.getInstance().apply { time = parsed }
        val isToday  = now.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR) &&
                now.get(java.util.Calendar.DAY_OF_YEAR) == msgCal.get(java.util.Calendar.DAY_OF_YEAR)
        val yesterday = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR) &&
                yesterday.get(java.util.Calendar.DAY_OF_YEAR) == msgCal.get(java.util.Calendar.DAY_OF_YEAR)
        val withinWeek = now.timeInMillis - parsed.time < 6L * 24 * 60 * 60 * 1000

        when {
            isToday     -> java.text.SimpleDateFormat("HH:mm", java.util.Locale("id", "ID")).format(parsed)
            isYesterday -> "Kemarin"
            withinWeek  -> java.text.SimpleDateFormat("EEE", java.util.Locale("id", "ID")).format(parsed)
            else        -> java.text.SimpleDateFormat("d MMM", java.util.Locale("id", "ID")).format(parsed)
        }
    } catch (e: Exception) { raw }
}

@Composable
private fun ChatRoomCard(
    room: com.example.prototypevolunteerapp.data.remote.dto.ChatRoomDto,
    onClick: () -> Unit
) {
    val avatarColor = remember(room.id) { avatarColors[room.id % avatarColors.size] }
    val timeLabel   = remember(room.lastMessageTime) { formatChatTime(room.lastMessageTime) }

    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier              = Modifier.padding(14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier         = Modifier.size(46.dp).clip(CircleShape).background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                if (!room.organizerLogo.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model              = room.organizerLogo,
                        contentDescription = room.organizerName,
                        modifier           = Modifier.size(46.dp).clip(CircleShape),
                        contentScale       = androidx.compose.ui.layout.ContentScale.Crop,
                        loading = {
                            Text(room.organizerName.first().uppercaseChar().toString(),
                                color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        },
                        error = {
                            Text(room.organizerName.first().uppercaseChar().toString(),
                                color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    )
                } else {
                    Text(room.organizerName.first().uppercaseChar().toString(),
                        color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Column(Modifier.weight(1f)) {
                Text(room.eventTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                    color = TextDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(room.organizerName, fontSize = 12.sp, color = TextLight, maxLines = 1)
                Spacer(Modifier.height(3.dp))
                if (room.lastMessage.isNullOrBlank()) {
                    Text("Belum ada pesan", fontSize = 12.sp, color = TextLight,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, maxLines = 1)
                } else {
                    Text(
                        room.lastMessage,
                        fontSize   = 12.sp,
                        color      = if (room.unreadCount > 0) TextDark else TextLight,
                        fontWeight = if (room.unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                timeLabel?.let {
                    Text(it, fontSize = 11.sp,
                        color      = if (room.unreadCount > 0) PrimaryBlue else TextLight,
                        fontWeight = if (room.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal)
                }
                if (room.unreadCount > 0) {
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier         = Modifier.size(20.dp).clip(CircleShape).background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (room.unreadCount > 9) "9+" else room.unreadCount.toString(),
                            color = White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChatState(modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Outlined.Chat, null, modifier = Modifier.size(56.dp), tint = SageSoft)
        Spacer(Modifier.height(12.dp))
        Text("Belum ada percakapan", fontWeight = FontWeight.SemiBold, color = TextDark, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        Text("Chat akan muncul setelah\nkamu mendaftar ke suatu kegiatan",
            fontSize = 13.sp, color = TextLight, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}