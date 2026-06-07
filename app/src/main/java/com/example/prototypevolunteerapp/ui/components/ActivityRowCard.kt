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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CardWhite     = Color(0xFFFFFFFF)
private val AccentGreen   = Color(0xFF5A7A5A)
private val TextPrimary   = Color(0xFF1E2D1E)
private val TextSecondary = Color(0xFF6E8F6E)

@Composable
fun ActivityRowCard(
    title         : String,
    location      : String,
    candidateCount: Int,
    onClick       : () -> Unit,
    onEdit        : () -> Unit = {},
    onCancel      : (() -> Unit)? = null
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 4.dp),
        onClick   = onClick
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFFDAEFDC), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Event, null, tint = AccentGreen, modifier = Modifier.size(20.dp))
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
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        tint     = TextSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(location, fontSize = 11.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFDAEFDC), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$candidateCount",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 15.sp,
                        color      = AccentGreen
                    )
                }
                Text("kandidat", fontSize = 9.sp, color = TextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = AccentGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (onCancel != null) {
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector        = Icons.Default.Cancel,
                        contentDescription = "Batalkan Kegiatan",
                        tint               = Color(0xFFCC2222),
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFB8D8C0), modifier = Modifier.size(18.dp))
        }
    }
}