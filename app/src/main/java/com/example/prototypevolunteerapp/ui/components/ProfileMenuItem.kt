package com.example.prototypevolunteerapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prototypevolunteerapp.ui.theme.Navy
import com.example.prototypevolunteerapp.ui.theme.TextDark

@Composable
fun ProfileMenuItem(
    icon:       ImageVector,
    label:      String,
    badgeCount: Int = 0,
    onClick:    () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Navy)
        Text(text = label, modifier = Modifier.weight(1f),
            color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        if (badgeCount > 0) {
            Badge { Text("$badgeCount") }
        }
    }
}