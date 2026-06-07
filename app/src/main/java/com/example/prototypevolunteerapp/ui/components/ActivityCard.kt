package com.example.prototypevolunteerapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.prototypevolunteerapp.R
import com.example.prototypevolunteerapp.data.model.ActivityData

@Composable
fun ActivityCard(
    activity: ActivityData,
    onViewDetail: () -> Unit
) {
    val context = LocalContext.current

    val localResId = remember(activity.imageRes) {
        context.resources.getIdentifier(
            activity.imageRes, "drawable", context.packageName
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(4.dp, Color(0xFF9EB589)),
        colors = CardDefaults.cardColors(containerColor = Color(0xCFDAEFDC))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            AsyncImage(
                model              = if (localResId != 0) localResId else activity.imageRes.ifBlank { null },
                contentDescription = activity.title,
                contentScale       = ContentScale.Crop,
                error              = painterResource(R.drawable.social_activity1),
                placeholder        = painterResource(R.drawable.social_activity1),
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = activity.title, style = MaterialTheme.typography.titleMedium)
            Text(text = activity.location, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onViewDetail,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Detail")
            }
        }
    }
}