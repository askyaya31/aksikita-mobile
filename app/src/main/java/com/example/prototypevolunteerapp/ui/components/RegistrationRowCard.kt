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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegistrationRowCard(
    name:          String,
    email:         String,
    status:        String,
    onConfirm:     () -> Unit,
    onReject:      () -> Unit,
    onAttend:      () -> Unit,
    onViewDetail:  () -> Unit = {}
) {
    val (statusText, statusColor, statusBg) = when (status) {
        "confirmed" -> Triple("Diterima",  Color(0xFF2E5C1A), Color(0xFFD4EDCA))
        "cancelled" -> Triple("Ditolak",   Color(0xFF8B0000), Color(0xFFFDE8E8))
        "attended"  -> Triple("Hadir",     Color(0xFF1A4D7A), Color(0xFFD0E8FF))
        else        -> Triple("Pending",   Color(0xFF7A5C00), Color(0xFFFFF3CD))
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        onClick   = onViewDetail
    ) {
        Column(
            modifier            = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp, color = Color(0xFF1E2D1E))
                    Text(email, fontSize = 12.sp, color = Color(0xFF6E8F6E))
                }
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(statusBg, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(statusText, fontSize = 11.sp,
                            color = statusColor, fontWeight = FontWeight.Medium)
                    }
                    Icon(
                        Icons.Default.ChevronRight, null,
                        tint     = Color(0xFFBBBBBB),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (status == "pending") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick  = onConfirm,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape    = RoundedCornerShape(8.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5C1A))
                    ) { Text("Terima", fontSize = 12.sp) }
                    OutlinedButton(
                        onClick  = onReject,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape    = RoundedCornerShape(8.dp)
                    ) { Text("Tolak", fontSize = 12.sp, color = Color(0xFFCC2222)) }
                }
            }
            if (status == "confirmed") {
                Button(
                    onClick  = onAttend,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape    = RoundedCornerShape(8.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A4D7A))
                ) { Text("Tandai Hadir", fontSize = 12.sp) }
            }
        }
    }
}