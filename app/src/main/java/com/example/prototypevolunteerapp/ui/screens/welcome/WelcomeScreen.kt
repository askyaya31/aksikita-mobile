package com.example.prototypevolunteerapp.ui.screens.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.example.prototypevolunteerapp.ui.components.AppFooter

@Composable
fun WelcomeScreen() {
    val backStack = LocalBackStack.current
    val fadeAnim  = remember { Animatable(0f) }
    val slideAnim = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        fadeAnim.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        slideAnim.animateTo(0f, tween(700, easing = FastOutSlowInEasing))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .alpha(fadeAnim.value),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter            = painterResource(id = R.drawable.bgwelcome),
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier
                .fillMaxWidth()
                .weight(0.42f)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.58f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFFFFFFF),
                            Color(0xFFDEEEFF),
                            Color(0xFFCCE0FF)
                        )
                    )
                )
                .graphicsLayer { translationY = slideAnim.value }
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = Color(0xFF253F99) )) {
                        append("Aksi")
                    }
                    withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold,  color = Color(0xFF4E6AC9))) {
                        append(" Nyata\nDimulai dari ")
                    }
                    withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = Color(0xFF253F99) )) {
                        append("Kita!")
                    }
                },
                style = TextStyle(
                    fontSize   = 30.sp,
                    textAlign  = TextAlign.Center,
                    lineHeight = 40.sp
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text  = "Terhubung dengan organisasi dan relawan\nuntuk menciptakan dampak nyata!",
                style = TextStyle(
                    fontSize   = 15.sp,
                    color = Color(0xFF1E3B8B).copy(alpha = 0.8f),
                    textAlign  = TextAlign.Center,
                    lineHeight = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(34.dp))

            Button(
                onClick  = { backStack.add(Routes.LoginRoute) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF4A90D9), Color(0xFF7BB8F0))
                        ),
                        shape = RoundedCornerShape(50.dp)
                    ),
                shape          = RoundedCornerShape(50.dp),
                colors         = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Text(
                    text       = "Login",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedButton(
                onClick  = { backStack.add(Routes.ActivitiesRoute) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape  = RoundedCornerShape(40.dp),
                border = BorderStroke(1.5.dp, Color(0xFF1E3B8B)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF1E3B8B)
                )
            ) {
                Text(
                    text       = "Temukan Kegiatan",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color(0xFF1E3B8B)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            TextButton(
                onClick  = { backStack.add(Routes.RegisterRoute) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text       = "Belum punya akun? Daftar sekarang",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E3B8B).copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}