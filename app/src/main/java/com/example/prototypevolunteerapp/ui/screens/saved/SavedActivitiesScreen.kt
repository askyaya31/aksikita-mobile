package com.example.prototypevolunteerapp.ui.screens.saved

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.Routes
import com.example.prototypevolunteerapp.data.remote.dto.EventDto
import com.example.prototypevolunteerapp.data.remote.dto.SavedEventDto
import com.example.prototypevolunteerapp.ui.theme.OliveDark
import com.example.prototypevolunteerapp.ui.theme.TextDark
import com.example.prototypevolunteerapp.ui.theme.TextLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedActivitiesScreen(
    viewModel: SavedActivitiesViewModel = hiltViewModel()
) {
    val backStack         = LocalBackStack.current
    val uiState           by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            scope.launch { snackbarHostState.showSnackbar(msg) }
            viewModel.onToastShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Kegiatan Tersimpan",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp,
                        color      = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint               = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = Color(0xFF3D5C2A),
                    titleContentColor          = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF4F7EF)
    ) { innerPadding ->

        when {
            uiState.isLoading -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OliveDark)
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.WifiOff, null,
                            tint     = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(uiState.errorMessage!!, color = Color.Gray, fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick = { viewModel.loadSavedEvents() }) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }

            uiState.savedEvents.isEmpty() -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.BookmarkBorder, null,
                            tint     = Color(0xFFB8D8C0),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Belum ada kegiatan tersimpan",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextDark
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Simpan kegiatan yang menarik dengan menekan ikon bookmark",
                            fontSize  = 13.sp,
                            color     = TextLight,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier            = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.savedEvents,
                        key   = { it.id }
                    ) { savedEvent ->
                        SavedEventCard(
                            saved        = savedEvent,
                            onUnsave     = { viewModel.unsave(savedEvent.event_id) },
                            onOpenDetail = { event ->
                                backStack.add(
                                    Routes.ActivityDetailRoute(
                                        id       = event.id.toString(),
                                        title    = event.title,
                                        location = "${event.city ?: ""}, ${event.province ?: ""}".trim(',', ' '),
                                        desc     = event.description ?: "",
                                        imageRes = event.poster ?: "",
                                        slug     = event.slug ?: ""
                                    )
                                )
                            }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SavedEventCard(
    saved:        SavedEventDto,
    onUnsave:     () -> Unit,
    onOpenDetail: (EventDto) -> Unit
) {
    val event = saved.event ?: return

    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick   = { onOpenDetail(event) },
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            AsyncImage(
                model              = event.poster,
                contentDescription = event.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text       = event.title,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextDark,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn, null,
                        tint     = OliveDark,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text     = "${event.city ?: "-"}, ${event.province ?: ""}".trim(',', ' '),
                        fontSize = 11.sp,
                        color    = TextLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (event.start_date != null) {
                    Row(
                        verticalAlignment    = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth, null,
                            tint     = OliveDark,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text     = event.start_date,
                            fontSize = 11.sp,
                            color    = TextLight
                        )
                    }
                }
            }
            IconButton(onClick = onUnsave) {
                Icon(
                    Icons.Default.Bookmark,
                    contentDescription = "Hapus simpanan",
                    tint               = OliveDark
                )
            }
        }
    }
}