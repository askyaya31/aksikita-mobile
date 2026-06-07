package com.example.prototypevolunteerapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prototypevolunteerapp.data.model.Candidate
import com.example.prototypevolunteerapp.data.model.CandidateStatus

private val CardWhite = Color(0xFFFFFFFF)
@Composable
fun CandidateRowCard(candidate: Candidate, onViewDetail: () -> Unit) {
    val (statusText, statusColor, statusBg) = when (candidate.status) {
        CandidateStatus.PENDING  -> Triple("Pending",    Color(0xFF7A5C00), Color(0xFFFFF3CD))
        CandidateStatus.DITERIMA -> Triple("Diterima", Color(0xFF2E6B3E), Color(0xFFD4EDCA))
        CandidateStatus.DITOLAK  -> Triple("Ditolak",  Color(0xFF8B0000), Color(0xFFFDE8E8))
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onViewDetail
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(candidate.imageRes),
                contentDescription = candidate.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(CircleShape)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(candidate.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E2D1E))
                Text(candidate.education, fontSize = 12.sp, color = Color(0xFF6E8F6E))
            }
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(statusText, fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFB8D8C0), modifier = Modifier.size(16.dp))
            }
        }
    }
}