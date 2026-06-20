package com.example.prototypevolunteerapp.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.data.remote.dto.ChatMessageDto
import com.example.prototypevolunteerapp.ui.theme.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.SubcomposeAsyncImage

private val NavyDark    = Color(0xFF1E3A8A)
private val PrimaryBlue = Color(0xFF3B82F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    roomId: Int,
    eventTitle: String,
    organizerName: String,
    isOrganizer: Boolean = false,
    organizerLogo: String? = null,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val state by viewModel.roomState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.loadMessages(roomId, asOrganizer = isOrganizer)
        viewModel.startPolling(roomId)
    }

    DisposableEffect(Unit) { onDispose { viewModel.stopPolling() } }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Scaffold(
        containerColor = BgScreen,
        bottomBar = {
            if (state.isOpen) {
                ChatInputBar(
                    text         = state.inputText,
                    onTextChange = viewModel::onInputChange,
                    onSend       = { viewModel.sendMessage(roomId) },
                    isSending    = state.isSending
                )
            } else {
                Surface(color = SageMist) {
                    Text(
                        "Chat ini sudah ditutup",
                        modifier  = Modifier.fillMaxWidth().padding(16.dp),
                        fontSize  = 13.sp,
                        color     = TextLight,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(NavyDark, PrimaryBlue)))
                    .padding(horizontal = 16.dp)
                    .padding(
                        top    = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                        bottom = 20.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { backStack.removeLastOrNull() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint     = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!organizerLogo.isNullOrBlank()) {
                        SubcomposeAsyncImage(
                            model              = organizerLogo,
                            contentDescription = organizerName,
                            modifier           = Modifier.size(36.dp).clip(CircleShape),
                            contentScale       = ContentScale.Crop,
                            loading = {
                                Text(
                                    organizerName.first().uppercaseChar().toString(),
                                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp
                                )
                            },
                            error = {
                                Text(
                                    organizerName.first().uppercaseChar().toString(),
                                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp
                                )
                            }
                        )
                    } else {
                        Text(
                            organizerName.first().uppercaseChar().toString(),
                            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        eventTitle,
                        color      = Color.White,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Text(
                        organizerName,
                        color    = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            LazyColumn(
                state               = listState,
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding()),
                contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.messages) { msg -> MessageBubble(msg) }
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessageDto) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (msg.isMe) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (msg.isMe) 16.dp else 4.dp,
                            bottomEnd   = if (msg.isMe) 4.dp else 16.dp
                        )
                    )
                    .background(if (msg.isMe) SageDeepForest else White)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    msg.message,
                    color = if (msg.isMe) White else TextDark,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(msg.createdAt, fontSize = 10.sp, color = TextLight)
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean
) {
    Surface(
        color = White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ketik pesan…", fontSize = 14.sp, color = TextLight) },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = SageMedium,
                    unfocusedBorderColor = SageMist
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = TextDark)
            )
            FloatingActionButton(
                onClick  = onSend,
                modifier = Modifier.size(48.dp),
                containerColor = SageDeepForest,
                contentColor   = White
            ) {
                if (isSending) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Send, "Kirim", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}