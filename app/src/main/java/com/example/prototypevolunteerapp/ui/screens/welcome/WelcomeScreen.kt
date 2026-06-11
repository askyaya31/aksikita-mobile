package com.example.prototypevolunteerapp.ui.screens.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prototypevolunteerapp.R
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.Routes

@Composable
fun WelcomeScreen() {
    val backStack = LocalBackStack.current
    val fadeAnim  = remember { Animatable(0f) }
    val slideAnim = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        fadeAnim.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        slideAnim.animateTo(0f, tween(700, easing = FastOutSlowInEasing))
    }

    // Background gradient lembut sesuai gambar
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFE6F0FF),
                        Color(0xFFCCE0FF)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .alpha(fadeAnim.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Gambar atas dengan bentuk melengkung (convex) ─────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                Image(
                    painter            = painterResource(id = R.drawable.bg_welcome),
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .fillMaxSize()
                        .clip(
                            RoundedCornerShape(
                                bottomStartPercent = 100,
                                bottomEndPercent = 100
                            )
                        )
                )

                // Logo di pojok kiri atas gambar - ukuran diperbesar
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 24.dp, top = 16.dp)
                        .height(60.dp)
                        .align(Alignment.TopStart),
                    contentScale = ContentScale.Fit
                )
            }

            // ── Konten bawah ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { translationY = slideAnim.value }
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Judul: "Aksi" biru bold, "Nyata" hitam, "Kita" biru bold
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = Color(0xFF031E66))) {
                            append("Aksi")
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E3B8B))) {
                            append(" Nyata\nDimulai dari ")
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E3B8B))) {
                            append("Kita!")
                        }
                    },
                    style = TextStyle(
                        fontSize   = 28.sp,
                        textAlign  = TextAlign.Center,
                        lineHeight = 40.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Subtitle — warna gelap, bukan putih
                Text(
                    text  = "Terhubung dengan organisasi dan relawan\nuntuk menciptakan dampak nyata!",
                    style = TextStyle(
                        fontSize   = 13.sp,
                        color      = Color(0xFF2A5EEA),
                        textAlign  = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Tombol Login — Gradient blue pill shape
                Button(
                    onClick  = { backStack.add(Routes.LoginRoute) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF61A5FA), Color(0xFF1E3B8B))
                            ),
                            shape = RoundedCornerShape(50.dp)
                        ),
                    shape  = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Text(
                        text       = "Login",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tombol Jelajahi Aksi — Outline biru brand
                OutlinedButton(
                    onClick  = { backStack.add(Routes.ActivitiesRoute) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape  = RoundedCornerShape(50.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF1E3B8B)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF1E3B8B)
                    )
                ) {
                    Text(
                        text = "Jelajahi Aksi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E3B8B)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Daftar — tetap ada sebagai TextButton kecil
                TextButton(
                    onClick  = { backStack.add(Routes.RegisterRoute) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text       = "Belum punya akun? Daftar sekarang",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color      = Color(0xFF4A7FE5)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}