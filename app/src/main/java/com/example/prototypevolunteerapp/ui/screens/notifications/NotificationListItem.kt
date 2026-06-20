package com.example.prototypevolunteerapp.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
    val isRead = notification.is_read
    val (icon, iconBg, iconTint) = notifIconStyle(notification.type)
    val (badgeColor, badgeLabel) = notifBadgeStyle(notification.type)
    Spacer(Modifier.width(6.dp))
    Card(
        modifier  = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(if (isRead) 1.dp else 3.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (isRead) Color.White else Color(0xFFEFF6FF)
        )
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(48.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Text(
                        notification.title,
                        fontSize   = 13.sp,
                        fontWeight = if (isRead) FontWeight.Normal else FontWeight.SemiBold,
                        color      = Color(0xFF0F172A),
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(6.dp))
                    if (badgeLabel != null) {
                        Surface(
                            shape = RoundedCornerShape(99.dp),
                            color = badgeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                badgeLabel,
                                modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = badgeColor
                            )
                        }
                    }
                }

                Text(
                    notification.message,
                    fontSize   = 12.sp,
                    color      = Color(0xFF64748B),
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )

                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    Text(
                        DateUtils.formatDateTime(notification.created_at),
                        fontSize = 11.sp,
                        color    = Color(0xFF94A3B8)
                    )
                    if (!isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF3B82F6), CircleShape)
                        )
                    }
                }
            }
        }
    }
}

private fun notifBadgeStyle(type: String?): Pair<Color, String?> {
    return when (type) {
        "registration_confirmed" -> Pair(Color(0xFF059669), "Diterima")
        "registration_rejected"  -> Pair(Color(0xFFDC2626), "Ditolak")
        "event_updated"          -> Pair(Color(0xFF3B82F6), "Update")
        "event_cancelled"        -> Pair(Color(0xFFB45309), "Dibatalkan")
        "new_registration"       -> Pair(Color(0xFF3B82F6), "Baru")
        "event_reminder"         -> Pair(Color(0xFFB45309), "Pengingat")
        "event_approved"         -> Pair(Color(0xFF059669), "Disetujui")
        "event_rejected"         -> Pair(Color(0xFFDC2626), "Ditolak")
        "registration_pending"   -> Pair(Color(0xFFB45309), "Pending")
        else                     -> Pair(Color(0xFF3B82F6), null)
    }
}

@Composable
private fun notifIconStyle(type: String?): Triple<ImageVector, Color, Color> {
    return when (type) {
        "registration_pending"   -> Triple(Icons.Default.HourglassEmpty, Color(0xFFFEF3C7), Color(0xFFB45309))
        "registration_confirmed" -> Triple(Icons.Default.CheckCircle,    Color(0xFFDCFCE7), Color(0xFF059669))
        "registration_rejected"  -> Triple(Icons.Default.Cancel,         Color(0xFFFEE2E2), Color(0xFFDC2626))
        "attendance_recorded"    -> Triple(Icons.Default.HowToReg,       Color(0xFFDCFCE7), Color(0xFF059669))
        "event_reminder"         -> Triple(Icons.Default.Alarm,          Color(0xFFFEF3C7), Color(0xFFB45309))
        "event_updated"          -> Triple(Icons.Default.Update,         Color(0xFFEFF6FF), Color(0xFF3B82F6))
        "event_cancelled"        -> Triple(Icons.Default.EventBusy,      Color(0xFFFEF3C7), Color(0xFFB45309))
        "new_registration"       -> Triple(Icons.Default.PersonAdd,      Color(0xFFEFF6FF), Color(0xFF3B82F6))
        "event_approved"         -> Triple(Icons.Default.Verified,       Color(0xFFDCFCE7), Color(0xFF059669))
        "event_rejected"         -> Triple(Icons.Default.GppBad,         Color(0xFFFEE2E2), Color(0xFFDC2626))
        "event_reviewed"         -> Triple(Icons.Default.RateReview,     Color(0xFFEFF6FF), Color(0xFF1E3A8A))
        else                     -> Triple(Icons.Default.Notifications,  Color(0xFFEFF6FF), Color(0xFF3B82F6))
    }
}