package com.example.prototypevolunteerapp.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prototypevolunteerapp.core.DateUtils
import com.example.prototypevolunteerapp.data.remote.dto.NotificationDto

@Composable
fun NotificationListItem(
    notification: NotificationDto,
    onClick:      () -> Unit,
    modifier:     Modifier = Modifier
) {
    val isRead     = notification.is_read
    val background = if (isRead) MaterialTheme.colorScheme.surface else Color(0xFFEEF3FF)
    val dotColor   = Color(0xFF3B82F6)

    val (icon, iconBg, iconTint) = notifIconStyle(notification.type)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier
                .size(44.dp)
                .background(iconBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = notification.title,
                fontSize   = 14.sp,
                fontWeight = if (isRead) FontWeight.Normal else FontWeight.SemiBold,
                color      = Color(0xFF0F172A),
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text       = notification.message,
                fontSize   = 13.sp,
                color      = Color(0xFF64748B),
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text     = DateUtils.formatDateTime(notification.created_at),
                fontSize = 11.sp,
                color    = Color(0xFF94A3B8)
            )
        }

        Spacer(Modifier.width(8.dp))

        if (!isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape)
            )
        }
    }
}

@Composable
private fun notifIconStyle(type: String?): Triple<ImageVector, Color, Color> {
    return when (type) {
        "registration_pending"   -> Triple(
            Icons.Default.HourglassEmpty,
            Color(0xFFFEF3C7),
            Color(0xFFD4900A)
        )
        "registration_confirmed" -> Triple(
            Icons.Default.CheckCircle,
            Color(0xFFE3F5F2),
            Color(0xFF0E7B6C)
        )
        "registration_rejected"  -> Triple(
            Icons.Default.Cancel,
            Color(0xFFFEE2E2),
            Color(0xFFEF4444)
        )
        "attendance_recorded"    -> Triple(
            Icons.Default.HowToReg,
            Color(0xFFE3F5F2),
            Color(0xFF0E7B6C)
        )
        "event_reminder"         -> Triple(
            Icons.Default.Alarm,
            Color(0xFFFEF3C7),
            Color(0xFFD4900A)
        )
        "event_updated"          -> Triple(
            Icons.Default.Update,
            Color(0xFFEFF6FF),
            Color(0xFF3B82F6)
        )
        "event_cancelled"        -> Triple(
            Icons.Default.EventBusy,
            Color(0xFFFEF3C7),
            Color(0xFFD4900A)
        )
        "new_registration"       -> Triple(
            Icons.Default.PersonAdd,
            Color(0xFFEFF6FF),
            Color(0xFF3B82F6)
        )
        "event_approved"         -> Triple(
            Icons.Default.Verified,
            Color(0xFFE3F5F2),
            Color(0xFF0E7B6C)
        )
        "event_rejected"         -> Triple(
            Icons.Default.GppBad,
            Color(0xFFFFF3E0),
            Color(0xFFE65100)
        )
        "event_reviewed"         -> Triple(
            Icons.Default.RateReview,
            Color(0xFFEFF6FF),
            Color(0xFF1E3A8A)
        )
        else                     -> Triple(
            Icons.Default.Notifications,
            Color(0xFFEFF6FF),
            Color(0xFF3B82F6)
        )
    }
}