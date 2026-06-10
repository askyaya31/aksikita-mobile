package com.example.prototypevolunteerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NavyDark   = Color(0xFF1E3A8A)
private val PrimaryBlue = Color(0xFF3B82F6)
private val ChipBlue   = Color(0xFFDDE8FF)

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    message: String = "Loading"
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(ChipBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(36.dp),
                    color       = PrimaryBlue,
                    strokeWidth = 3.dp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text       = message,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign  = TextAlign.Center,
                color      = NavyDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text      = "Mohon tunggu sebentar :)",
                fontSize  = 12.sp,
                textAlign = TextAlign.Center,
                color     = Color(0xFF64748B)
            )
        }
    }
}