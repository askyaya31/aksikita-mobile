package com.example.prototypevolunteerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.prototypevolunteerapp.data.model.ActivitySubmissionStatus

private val TextPrimary   = Color(0xFF1E2D1E)
private val TextSecondary = Color(0xFF6E8F6E)
private val CardWhite     = Color(0xFFFFFFFF)

@Composable
fun SubmissionStatusCard(
    title:  String,
    lokasi: String,
    status: ActivitySubmissionStatus,
    onEdit: (() -> Unit)? = null
) {
    val (statusText, statusColor, statusBg, statusIcon) = when (status) {
        ActivitySubmissionStatus.MENUNGGU_VERIFIKASI ->
            arrayOf("Menunggu Verifikasi", Color(0xFF7A5C00), Color(0xFFFFF8E1), Icons.Default.HourglassEmpty)
        ActivitySubmissionStatus.DISETUJUI ->
            arrayOf("Disetujui ✓", Color(0xFF2E5C1A), Color(0xFFD4EDCA), Icons.Default.CheckCircle)
        ActivitySubmissionStatus.DITOLAK ->
            arrayOf("Ditolak", Color(0xFFB71C1C), Color(0xFFFFEBEE), Icons.Default.Cancel)
    }
    @Suppress("UNCHECKED_CAST")
    val typedStatusColor = statusColor as Color
    @Suppress("UNCHECKED_CAST")
    val typedStatusBg    = statusBg as Color
    @Suppress("UNCHECKED_CAST")
    val typedStatusIcon  = statusIcon as ImageVector

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically){
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(typedStatusBg, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(typedStatusIcon, null, tint = typedStatusColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 13.sp,
                        color      = TextPrimary,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null,
                            tint = TextSecondary, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(lokasi, fontSize = 11.sp, color = TextSecondary)
                    }
                }
                Box(
                    modifier = Modifier
                        .background(typedStatusBg, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        statusText as String,
                        fontSize   = 10.sp,
                        color      = typedStatusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (onEdit != null) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFFDAEFDC))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (status) {
                            ActivitySubmissionStatus.MENUNGGU_VERIFIKASI ->
                                "Pengajuan sedang ditinjau admin"
                            ActivitySubmissionStatus.DISETUJUI ->
                                "Kegiatan sudah dipublikasikan"
                            ActivitySubmissionStatus.DITOLAK ->
                                "Pengajuan ditolak, bisa diedit & ajukan ulang"
                        },
                        fontSize = 11.sp,
                        color    = TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = onEdit,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Edit, null,
                            modifier = Modifier.size(14.dp),
                            tint     = Color(0xFF5A7A5A))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 12.sp, color = Color(0xFF5A7A5A))
                    }
                }
            }
        }
    }
}