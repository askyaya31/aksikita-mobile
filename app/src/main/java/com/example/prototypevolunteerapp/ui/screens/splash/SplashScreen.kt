package com.example.prototypevolunteerapp.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prototypevolunteerapp.R
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.OrganizerSession
import com.example.prototypevolunteerapp.core.Routes
import com.example.prototypevolunteerapp.core.UserSession
import com.example.prototypevolunteerapp.data.model.NotificationRepository
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.preferences.SessionPreferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SplashDependencies {
    fun sessionPreferences(): SessionPreferences
    fun userSession(): UserSession
    fun organizerSession(): OrganizerSession
    fun notificationRepository(): NotificationRepository
    fun apiService(): ApiService
}

@Composable
fun SplashScreen() {
    val backStack    = LocalBackStack.current
    val context      = LocalContext.current
    val dependencies = remember {
        EntryPointAccessors.fromApplication(context, SplashDependencies::class.java)
    }
    val sessionPrefs     = remember { dependencies.sessionPreferences() }
    val userSession      = dependencies.userSession()
    val organizerSession = dependencies.organizerSession()
    val notificationRepo = dependencies.notificationRepository()
    val apiService       = remember { dependencies.apiService() }

    val logoAnim    = remember { Animatable(0f) }
    val nameAnim    = remember { Animatable(0f) }
    val taglineAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        logoAnim.animateTo(
            targetValue   = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
        delay(100)
        nameAnim.animateTo(
            targetValue   = 1f,
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
        )
        delay(80)
        taglineAnim.animateTo(
            targetValue   = 1f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
        delay(1200)

        val savedSession = sessionPrefs.savedSession.first()

        if (savedSession != null) {
            val tokenValid = try {
                val meResp = apiService.getMe()
                meResp.isSuccessful
            } catch (e: Exception) {
                true
            }

            if (!tokenValid) {
                sessionPrefs.clearSession()
                backStack.removeLastOrNull()
                backStack.add(Routes.WelcomeRoute)
                return@LaunchedEffect
            }

            when (savedSession.role) {
                SessionPreferences.ROLE_VOLUNTEER -> {
                    userSession.restoreSession(
                        email     = savedSession.email,
                        name      = savedSession.name,
                        volunteer = null,
                        avatarUrl = savedSession.avatarUrl
                    )
                    notificationRepo.initForUser(isDummyAccount = false)

                    backStack.removeLastOrNull()
                    backStack.add(Routes.HomeRoute)
                }
                SessionPreferences.ROLE_ORGANIZER -> {
                    organizerSession.restoreSession(
                        email   = savedSession.email,
                        name    = savedSession.name,
                        logoUrl = savedSession.logoUrl
                    )
                    backStack.removeLastOrNull()
                    backStack.add(Routes.OrgDashboardRoute)
                }
                else -> {
                    backStack.removeLastOrNull()
                    backStack.add(Routes.WelcomeRoute)
                }
            }
        } else {
            backStack.removeLastOrNull()
            backStack.add(Routes.WelcomeRoute)
        }
    }

    val logoScale = 0.78f + (1f - 0.78f) * logoAnim.value

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7EF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter            = painterResource(id = R.drawable.logo),
                contentDescription = "AksiKita Logo",
                modifier           = Modifier
                    .size(260.dp)
                    .scale(logoScale)
                    .alpha(logoAnim.value)
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = buildAnnotatedString {
                    append("ubah niat menjadi ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Aksi") }
                    append(", ubah jarak menjadi ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Kita") }
                },
                fontSize  = 19.sp,
                color     = Color(0xFF6E8F6E),
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .alpha(taglineAnim.value)
                    .padding(horizontal = 40.dp)
            )
        }
    }
}