package com.example.prototypevolunteerapp.ui.screens.recommendations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.prototypevolunteerapp.ui.components.RecommendationCard
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.Routes
import com.example.prototypevolunteerapp.ui.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.automirrored.filled.ArrowBack

private val NavyDark    = Color(0xFF1E3A8A)
private val PrimaryBlue = Color(0xFF3B82F6)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsScreen(viewModel: RecommendationsViewModel = hiltViewModel()) {
    val backStack = LocalBackStack.current
    val state by viewModel.state.collectAsState()
    Scaffold(
        containerColor = BgScreen
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(NavyDark, PrimaryBlue)))
                    .padding(horizontal = 16.dp)
                    .padding(
                        top    = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                        bottom = 20.dp
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { backStack.removeLastOrNull() }
                        .align(Alignment.CenterStart),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint     = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(
                    modifier            = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Rekomendasi",
                        color      = Color.White,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Berdasarkan minat & riwayatmu",
                        color    = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                }
            }

            when {
                state.isLoading -> Box(
                    Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SageMedium)
                }
                state.error != null -> Column(
                    Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Gagal memuat rekomendasi", color = TextDark, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = viewModel::load,
                        colors  = ButtonDefaults.buttonColors(containerColor = SageMedium)
                    ) { Text("Coba lagi") }
                }
                state.items.isEmpty() -> Column(
                    Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Outlined.AutoAwesome, null, modifier = Modifier.size(52.dp), tint = SageSoft)
                    Spacer(Modifier.height(12.dp))
                    Text("Belum ada rekomendasi", fontWeight = FontWeight.SemiBold, color = TextDark, fontSize = 15.sp)
                    Text(
                        "Ikuti lebih banyak kegiatan\nuntuk mendapatkan rekomendasi",
                        fontSize  = 13.sp,
                        color     = TextLight,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier  = Modifier.padding(top = 4.dp)
                    )
                }
                else -> LazyVerticalGrid(
                    columns               = GridCells.Fixed(2),
                    modifier              = Modifier
                        .fillMaxSize()
                        .padding(bottom = padding.calculateBottomPadding()),
                    contentPadding        = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement   = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.items) { activity ->
                        RecommendationCard(
                            event    = activity,
                            onClick  = {
                                backStack.add(
                                    Routes.ActivityDetailRoute(
                                        id       = activity.id,
                                        title    = activity.title,
                                        location = activity.location,
                                        desc     = activity.description,
                                        imageRes = activity.imageRes,
                                        slug     = activity.slug
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}