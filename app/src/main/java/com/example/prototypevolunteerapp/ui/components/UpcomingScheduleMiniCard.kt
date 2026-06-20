package com.example.prototypevolunteerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prototypevolunteerapp.data.remote.dto.ScheduleEventDto
import com.example.prototypevolunteerapp.ui.theme.Navy
import com.example.prototypevolunteerapp.ui.theme.BluePale
import com.example.prototypevolunteerapp.ui.theme.TextDark
import com.example.prototypevolunteerapp.ui.theme.TextLight
import com.example.prototypevolunteerapp.ui.theme.Surface

@Composable
fun UpcomingScheduleMiniCard(event: ScheduleEventDto) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = BluePale),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier              = Modifier.padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = Icons.Filled.CalendarToday,
                contentDescription = null,
                modifier           = Modifier.size(24.dp),
                tint               = Navy
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = event.eventTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp,
                    color      = TextDark
                )
                Text(
                    text     = buildString {
                        append(event.startDate)
                        event.startTime?.let { append(" • $it") }
                    },
                    fontSize = 11.sp,
                    color    = TextLight
                )
            }
            Box(
                modifier = Modifier
                    .background(Navy, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text       = event.status,
                    fontSize   = 10.sp,
                    color      = Surface,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}