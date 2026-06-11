package com.example.prototypevolunteerapp.ui.screens.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.Routes

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()

    NotificationsContent(
        uiState              = uiState,
        onBack               = { backStack.removeLastOrNull() },
        onMarkAllAsRead      = viewModel::onMarkAllAsRead,
        onNotificationClick  = { id ->
            viewModel.onNotificationOpened(id)
            backStack.add(Routes.NotificationDetailRoute(id))
        }
    )
}