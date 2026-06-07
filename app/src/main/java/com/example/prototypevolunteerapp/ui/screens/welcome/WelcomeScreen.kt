package com.example.prototypevolunteerapp.ui.screens.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter        = painterResource(id = R.drawable.bg_welcome),
            contentDescription = null,
            contentScale   = ContentScale.Crop,
            modifier       = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Black.copy(alpha = 0.6f),
                            0.40f to Color.Transparent,
                            1.00f to Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .alpha(fadeAnim.value)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter        = painterResource(id = R.drawable.logo3),
                    contentDescription = "Logo AksiKita",
                    modifier       = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text  = "AksiKita",
                    style = TextStyle(
                        fontSize      = 21.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = Color.White,
                        letterSpacing = 0.5.sp
                    )
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { translationY = slideAnim.value }
                    .padding(horizontal = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text  = "Aksi Nyata\nDimulai dari Kita!",
                    style = TextStyle(
                        fontSize   = 29.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White,
                        textAlign  = TextAlign.Center,
                        lineHeight = 42.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text  = "Terhubung dengan organisasi dan relawan\nuntuk menciptakan dampak nyata.",
                    style = TextStyle(
                        fontSize   = 15.sp,
                        color      = Color.White.copy(alpha = 0.9f),
                        textAlign  = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick  = { backStack.add(Routes.LoginRoute) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape  = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A7A5A))
                ) {
                    Text(
                        text       = "Login",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick  = { backStack.add(Routes.ActivitiesRoute) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape  = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(
                        text       = "Jelajahi Aksi",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick  = { backStack.add(Routes.RegisterRoute) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text       = "Belum punya akun? Daftar sekarang",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color      = Color.White.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}