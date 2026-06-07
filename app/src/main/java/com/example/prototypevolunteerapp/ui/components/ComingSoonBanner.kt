package com.example.prototypevolunteerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ComingSoonBanner(onTap:()-> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        onClick = onTap
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFFFFECB3), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.HourglassEmpty,
                    null,
                    tint     = Color(0xFF7A5C00),
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Detail Kandidat & Fitur Lainnya",
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 13.sp,
                    color      = Color(0xFF7A5C00)
                )
                Text(
                    "Ketuk untuk info lebih lanjut",
                    fontSize = 11.sp,
                    color    = Color(0xFF7A5C00).copy(alpha = 0.65f)
                )
            }
            Box(
                modifier = Modifier
                    .background(Color(0xFF7A5C00), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    "Coming Soon",
                    color      = Color.White,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
