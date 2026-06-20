package com.example.prototypevolunteerapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.prototypevolunteerapp.data.model.ActivityData

private val PrimaryBlue = Color(0xFF3B82F6)
private val NavyDark    = Color(0xFF1E3A8A)
private val BluePale    = Color(0xFFBFDBFE)
private val TextDark    = Color(0xFF0F172A)
private val TextMuted   = Color(0xFF64748B)

@Composable
fun RecommendationCard(
    event:    ActivityData,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    val context    = LocalContext.current
    val localResId = remember(event.imageRes) {
        context.resources.getIdentifier(event.imageRes, "drawable", context.packageName)
    }

    Card(
        modifier  = modifier.clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {

            // ── Gambar (tinggi tetap) ──────────────────────────────────
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(120.dp)                     // fixed height agar seragam
                    .background(BluePale.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                if (event.imageRes.isNotBlank()) {
                    AsyncImage(
                        model              = if (localResId != 0) localResId else event.imageRes,
                        contentDescription = event.title,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        tint               = PrimaryBlue.copy(alpha = 0.45f),
                        modifier           = Modifier.size(36.dp)
                    )
                }

                // Badge kategori — pojok kiri atas
                if (!event.category.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp),
                        shape    = RoundedCornerShape(99.dp),
                        color    = Color.White.copy(alpha = 0.92f),
                        border   = BorderStroke(0.5.dp, BluePale)
                    ) {
                        Text(
                            event.category,
                            modifier   = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            fontSize   = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color      = Color(0xFF1D4ED8),
                            maxLines   = 1
                        )
                    }
                }
            }

            // ── Body ──────────────────────────────────────────────────
            Column(modifier = Modifier.padding(10.dp)) {

                // Judul event
                Text(
                    event.title,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextDark,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )

                Spacer(Modifier.height(6.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                Spacer(Modifier.height(6.dp))

                // Tanggal
                if (!event.startDate.isNullOrBlank()) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier              = Modifier.padding(bottom = 3.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday, null,
                            tint     = PrimaryBlue,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            event.startDate,
                            fontSize = 10.sp,
                            color    = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Lokasi
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier              = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn, null,
                        tint     = PrimaryBlue,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        event.location,
                        fontSize = 10.sp,
                        color    = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // ── Footer: kuota (opsional) + tombol full width ──────
                if (event.remainingQuota != null) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier              = Modifier.padding(bottom = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.Group, null,
                            tint     = PrimaryBlue,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            "${event.remainingQuota} kuota",
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color      = Color(0xFF1D4ED8)
                        )
                    }
                }

                // Tombol View Detail — full width agar tidak gepeng
                Button(
                    onClick      = onClick,
                    modifier     = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                    shape        = RoundedCornerShape(99.dp),
                    colors       = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        "View Detail",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White
                    )
                }
            }
        }
    }
}