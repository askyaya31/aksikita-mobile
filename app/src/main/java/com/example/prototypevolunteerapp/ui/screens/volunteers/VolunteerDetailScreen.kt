package com.example.prototypevolunteerapp.ui.screens.volunteers

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.data.model.Volunteer
import com.example.prototypevolunteerapp.data.model.VolunteerExp

private val ScreenBg  = Color(0xFFF4F7EF)
private val CardBg    = Color(0xFFFFFFFF)
private val AccentGreen = Color(0xFF5A7A5A)
private val ChipBg    = Color(0xFFDAEFDC)
private val ChipText  = Color(0xFF3D5C2A)
private val MintChipBg  = Color(0xFFDAEFDC)
private val MintChipText = Color(0xFF2E5C1A)
private val DotColor  = Color(0xFF5A7A5A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerDetailScreen(volunteer: Volunteer) {
    val backStack = LocalBackStack.current
    val context   = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Who is ${volunteer.name.split(" ").firstOrNull() ?: "Volunteer"}?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E2D1E)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E2D1E)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ScreenBg)
            )
        },
        containerColor = ScreenBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = volunteer.imageRes),
                        contentDescription = volunteer.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = volunteer.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF1E2D1E),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = volunteer.education,
                        fontSize = 12.sp,
                        color = Color(0xFF6E8F6E),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        InfoChip(
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                            },
                            text = volunteer.birthPlace,
                            modifier = Modifier.weight(1f)
                        )
                        InfoChip(
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                            },
                            text = volunteer.birthDate,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            SectionCard(title = "About Me") {
                Text(
                    text = volunteer.about,
                    fontSize = 13.sp,
                    color = Color(0xFF6E8F6E),
                    lineHeight = 20.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            SectionCard(title = "Keahlian") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    volunteer.skills.forEach { skill ->
                        SkillChip(label = skill)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            SectionCard(title = "Minat") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    volunteer.interests.forEach { interest ->
                        InterestChip(label = interest)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            SectionCard(title = "Pengalaman Volunteer") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    volunteer.experiences.forEach { exp ->
                        ExperienceItem(exp = exp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    SosmedButton(
                        iconRes = com.example.prototypevolunteerapp.R.drawable.instagram_icon,
                        label = "Instagram",
                        onClick = { /*baru mockup dulu*/ }
                    )
                    SosmedButton(
                        iconRes = com.example.prototypevolunteerapp.R.drawable.gmail_icon,
                        label = "Gmail",
                        onClick = {
                            if (!volunteer.email.isNullOrEmpty()) {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = "mailto:${volunteer.email}".toUri()
                                    putExtra(Intent.EXTRA_SUBJECT, "Halo dari AksiKita!")
                                }
                                context.startActivity(intent)
                            }
                        }
                    )
                    SosmedButton(
                        iconRes = com.example.prototypevolunteerapp.R.drawable.whatsapp_icon,
                        label = "Whatsapp",
                        onClick = {
                            if (!volunteer.phone.isNullOrEmpty()) {
                                val uri = "https://wa.me/${volunteer.phone}".toUri()
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        }
                    )
                }
            }
        }
    }
}
@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color(0xFF6E8F6E),
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            content()
        }
    }
}
@Composable
private fun InfoChip(
    icon: @Composable () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0xFFF4F7EF), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        icon()
        Text(text = text, fontSize = 11.sp, color = Color(0xFF6E8F6E))
    }
}

@Composable
private fun SkillChip(label: String) {
    Box(
        modifier = Modifier
            .background(ChipBg, RoundedCornerShape(50.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = ChipText)
    }
}

@Composable
private fun InterestChip(label: String) {
    Box(
        modifier = Modifier
            .background(MintChipBg, RoundedCornerShape(50.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = MintChipText)
    }
}

@Composable
private fun ExperienceItem(exp: VolunteerExp) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(7.dp)
                .background(DotColor, CircleShape)
        )
        Column {
            Text(
                text = exp.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E2D1E)
            )
            Text(
                text = "${exp.role} · ${exp.year}",
                fontSize = 12.sp,
                color = Color(0xFF6E8F6E)
            )
        }
    }
}

@Composable
private fun HistoryChip(label: String) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Text(text = label, fontSize = 13.sp, color = Color(0xFF1E2D1E))
        }
    }
}

@Composable
private fun SosmedButton(
    iconRes: Int,
    label: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(26.dp)
        )
    }
}
