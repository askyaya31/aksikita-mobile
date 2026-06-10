package com.example.prototypevolunteerapp.ui.screens.activities

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.Routes
import com.example.prototypevolunteerapp.data.model.ActivityData
import com.example.prototypevolunteerapp.data.remote.dto.CategoryDto

private val NavyDark     = Color(0xFF1E3A8A)
private val PrimaryBlue  = Color(0xFF3B82F6)
private val BgScreen     = Color(0xFFF8FAFF)
private val TextDark     = Color(0xFF0F172A)
private val TextMuted    = Color(0xFF64748B)
private val AccentOrange = Color(0xFFE8501A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    viewModel: ActivitiesViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val uiState   by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total       = listState.layoutInfo.totalItemsCount
            lastVisible >= total - 3 && !uiState.isLoadingMore && uiState.currentPage < uiState.lastPage
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) viewModel.loadMore()
    }

    val hasActiveFilter = uiState.selectedCategory != null ||
            uiState.selectedCity.isNotBlank() ||
            uiState.searchQuery.isNotBlank()

    var showCityDialog by remember { mutableStateOf(false) }
    var cityInput      by remember { mutableStateOf(uiState.selectedCity) }
    var showFilterSheet by remember { mutableStateOf(false) }

    if (showCityDialog) {
        Dialog(
            onDismissRequest = { showCityDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { showCityDialog = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(Color.White)
                        .clickable(enabled = false) {} // block dismiss on content click
                        .padding(horizontal = 24.dp)
                        .padding(top = 12.dp, bottom = 32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(Color(0xFFE2E8F0), CircleShape)
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Pilih Kota",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                "Cari kegiatan di kotamu",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFF1F5F9), CircleShape)
                                .clickable { showCityDialog = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        value = cityInput,
                        onValueChange = { cityInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Contoh: Jakarta, Yogyakarta...",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (cityInput.isNotBlank()) {
                                IconButton(onClick = { cityInput = "" }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Hapus",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFF),
                            unfocusedContainerColor = Color(0xFFF8FAFF)
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    val suggestions = listOf("Jakarta", "Surabaya", "Bandung", "Yogyakarta", "Medan", "Semarang")
                    Text(
                        "Kota populer",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        suggestions.forEach { city ->
                            val isChosen = cityInput.trim().equals(city, ignoreCase = true)
                            Surface(
                                onClick = { cityInput = city },
                                shape = RoundedCornerShape(99.dp),
                                color = if (isChosen) PrimaryBlue else Color(0xFFF1F5F9)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (isChosen) Color.White else TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        city,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isChosen) Color.White else TextDark
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.onCityChanged(cityInput.trim())
                                showCityDialog = false
                            },
                            modifier = Modifier.weight(2f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyDark)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Terapkan", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor   = Color.White,
            shape            = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "Filter Kategori",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp,
                        color      = TextDark
                    )
                    if (uiState.selectedCategory != null) {
                        TextButton(onClick = { viewModel.onCategorySelected(null) }) {
                            Text("Reset", color = Color(0xFFE53935), fontSize = 13.sp)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                CategorySheetItem(
                    label      = "Semua Kategori",
                    isSelected = uiState.selectedCategory == null,
                    onClick    = {
                        viewModel.onCategorySelected(null)
                        showFilterSheet = false
                    }
                )

                HorizontalDivider(
                    color    = Color(0xFFF1F5F9),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                uiState.categories.forEach { category ->
                    CategorySheetItem(
                        label      = category.name,
                        isSelected = uiState.selectedCategory?.id == category.id,
                        onClick    = {
                            viewModel.onCategorySelected(
                                if (uiState.selectedCategory?.id == category.id) null else category
                            )
                            showFilterSheet = false
                        }
                    )
                }
            }
        }
    }

    Scaffold(containerColor = BgScreen) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(NavyDark, PrimaryBlue)))
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 20.dp)
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
                Text(
                    "Activity Search",
                    color      = Color.White,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.align(Alignment.Center)
                )
            }

            OutlinedTextField(
                value         = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder   = { Text("Search opportunities...", color = TextMuted) },
                leadingIcon   = { Icon(Icons.Default.Search, null, tint = PrimaryBlue) },
                trailingIcon  = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, null,
                                modifier = Modifier.size(18.dp), tint = TextMuted)
                        }
                    }
                },
                singleLine  = true,
                shape       = RoundedCornerShape(50.dp),
                colors      = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = PrimaryBlue,
                    unfocusedBorderColor    = Color(0xFFE2E8F0),
                    focusedContainerColor   = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            // Filter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pill Kota
                val citySelected = uiState.selectedCity.isNotBlank()
                Surface(
                    onClick = { cityInput = uiState.selectedCity; showCityDialog = true },
                    shape   = RoundedCornerShape(99.dp),
                    color   = if (citySelected) NavyDark else Color.White,
                    border  = if (!citySelected)
                        androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    else null,
                    shadowElevation = if (citySelected) 0.dp else 1.dp
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint     = if (citySelected) Color.White else PrimaryBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text       = if (citySelected) uiState.selectedCity else "Semua Kota",
                            fontSize   = 12.sp,
                            fontWeight = if (citySelected) FontWeight.SemiBold else FontWeight.Normal,
                            color      = if (citySelected) Color.White else TextMuted,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                        if (citySelected) {
                            Spacer(Modifier.width(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                    .clickable {
                                        cityInput = ""
                                        viewModel.onCityChanged("")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Hapus kota",
                                    tint     = Color.White,
                                    modifier = Modifier.size(9.dp)
                                )
                            }
                        }
                    }
                }

                // Pill Kategori
                val categoryActive = uiState.selectedCategory != null
                Surface(
                    onClick = { showFilterSheet = true },
                    shape   = RoundedCornerShape(99.dp),
                    color   = if (categoryActive) PrimaryBlue else Color.White,
                    border  = if (!categoryActive)
                        androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    else null,
                    shadowElevation = if (categoryActive) 0.dp else 1.dp
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = null,
                            tint     = if (categoryActive) Color.White else NavyDark,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text       = if (categoryActive) uiState.selectedCategory!!.name else "Kategori",
                            fontSize   = 12.sp,
                            fontWeight = if (categoryActive) FontWeight.SemiBold else FontWeight.Medium,
                            color      = if (categoryActive) Color.White else NavyDark,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                        if (categoryActive) {
                            Spacer(Modifier.width(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                    .clickable { viewModel.onCategorySelected(null) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Hapus kategori",
                                    tint     = Color.White,
                                    modifier = Modifier.size(9.dp)
                                )
                            }
                        }
                    }
                }
            }


            if (!uiState.isLoading) {
                Text(
                    text       = "${uiState.activities.size} Opportunities Found",
                    fontSize   = 13.sp,
                    color      = TextMuted,
                    fontWeight = FontWeight.Medium,
                    modifier   = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }

                uiState.errorMessage != null -> {
                    Column(
                        modifier            = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(Icons.Default.WifiOff, null,
                            tint = Color(0xFFB0B0B0), modifier = Modifier.size(48.dp))
                        Text(uiState.errorMessage!!,
                            textAlign = TextAlign.Center, fontSize = 14.sp, color = TextMuted)
                        Button(
                            onClick = { viewModel.loadActivities() },
                            colors  = ButtonDefaults.buttonColors(containerColor = NavyDark)
                        ) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Coba Lagi")
                        }
                    }
                }

                uiState.activities.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.SearchOff, null,
                                tint = Color(0xFFB0B0B0), modifier = Modifier.size(48.dp))
                            Text("No opportunities found",
                                fontSize = 14.sp, color = TextMuted, textAlign = TextAlign.Center)
                            if (hasActiveFilter) {
                                TextButton(onClick = {
                                    cityInput = ""
                                    viewModel.onClearFilters()
                                }) {
                                    Text("Clear all filters", color = PrimaryBlue)
                                }
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        state               = listState,
                        modifier            = Modifier.fillMaxSize(),
                        contentPadding      = PaddingValues(
                            start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(
                            items = uiState.activities,
                            key   = { _, item -> item.id }
                        ) { _, activity ->
                            ActivityBigCard(
                                activity = activity,
                                isLiked  = uiState.likedIds.contains(activity.id.toIntOrNull() ?: -1),
                                isSaved  = uiState.savedIds.contains(activity.id.toIntOrNull() ?: -1),
                                onLike   = { viewModel.toggleLike(activity.id.toIntOrNull() ?: return@ActivityBigCard) },
                                onSave   = { viewModel.toggleSave(activity.id.toIntOrNull() ?: return@ActivityBigCard) },
                                onClick  = {
                                    backStack.add(
                                        Routes.ActivityDetailRoute(
                                            id       = activity.id,
                                            slug     = activity.slug,
                                            title    = activity.title,
                                            location = activity.location,
                                            desc     = activity.description,
                                            imageRes = activity.imageRes
                                        )
                                    )
                                }
                            )
                        }

                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier         = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) { CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(28.dp)) }
                            }
                        }

                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

// Category Sheet Item
@Composable
private fun CategorySheetItem(
    label:      String,
    isSelected: Boolean,
    onClick:    () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) PrimaryBlue.copy(alpha = 0.08f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text       = label,
            fontSize   = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (isSelected) PrimaryBlue else TextDark
        )
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint     = PrimaryBlue,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Activity Card
@Composable
private fun ActivityBigCard(
    activity: ActivityData,
    isLiked:  Boolean,
    isSaved:  Boolean,
    onLike:   () -> Unit,
    onSave:   () -> Unit,
    onClick:  () -> Unit
) {
    val context    = LocalContext.current
    val localResId = remember(activity.imageRes) {
        context.resources.getIdentifier(activity.imageRes, "drawable", context.packageName)
    }

    Card(
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        modifier  = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model              = if (localResId != 0) localResId
                    else activity.imageRes.ifBlank { null },
                    contentDescription = activity.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.3f)))
                        )
                )
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        .clickable { onLike() }
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint     = if (isLiked) Color(0xFFE53935) else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (!activity.category.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
                        shape    = RoundedCornerShape(99.dp),
                        color    = PrimaryBlue
                    ) {
                        Text(
                            activity.category,
                            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color.White
                        )
                    }
                }
            }

            Column(
                modifier            = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Text(
                        activity.title,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextDark,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f),
                        lineHeight = 19.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                if (isSaved) PrimaryBlue.copy(alpha = 0.1f) else Color(0xFFF1F5F9),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSave() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save",
                            tint     = if (isSaved) PrimaryBlue else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (!activity.organizationName.isNullOrBlank()) {
                    Text(activity.organizationName, fontSize = 12.sp,
                        color = PrimaryBlue, fontWeight = FontWeight.Medium)
                }

                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.LocationOn, null,
                        tint = TextMuted, modifier = Modifier.size(13.dp))
                    Text(activity.location, fontSize = 11.sp, color = TextMuted,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                if (!activity.duration.isNullOrBlank()) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Schedule, null,
                            tint = TextMuted, modifier = Modifier.size(13.dp))
                        Text(activity.duration, fontSize = 11.sp, color = TextMuted)
                    }
                }

                if (activity.remainingQuota != null) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Group, null,
                                tint = AccentOrange, modifier = Modifier.size(13.dp))
                            Text("${activity.remainingQuota} spots left",
                                fontSize = 11.sp, color = AccentOrange,
                                fontWeight = FontWeight.SemiBold)
                        }
                        Surface(
                            shape    = RoundedCornerShape(99.dp),
                            color    = NavyDark,
                            modifier = Modifier.clickable { onClick() }
                        ) {
                            Text("View Detail",
                                modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = Color.White)
                        }
                    }
                }
            }
        }
    }
}