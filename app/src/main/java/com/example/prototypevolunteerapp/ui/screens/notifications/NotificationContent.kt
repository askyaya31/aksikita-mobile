package com.example.prototypevolunteerapp.ui.screens.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsContent(
    uiState: NotificationsUiState,
    onBack: () -> Unit,
    onMarkAllAsRead: () -> Unit,
    onNotificationClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Notifikasi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (uiState.hasUnread) {
                        TextButton(
                            onClick = onMarkAllAsRead,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Text("Tandai semua dibaca")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isEmpty) {
            EmptyNotifications(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(uiState.notifications, key = { it.id }) { notif ->
                    NotificationListItem(
                        notification = notif,
                        onClick = { onNotificationClick(notif.id) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}