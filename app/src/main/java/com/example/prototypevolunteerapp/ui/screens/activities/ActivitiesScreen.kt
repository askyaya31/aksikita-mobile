package com.example.prototypevolunteerapp.ui.screens.activities

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.Routes
import com.example.prototypevolunteerapp.data.model.ActivityData
import com.example.prototypevolunteerapp.data.remote.dto.CategoryDto
import com.example.prototypevolunteerapp.ui.components.ActivityCard
import com.example.prototypevolunteerapp.ui.components.AppFooter
import com.example.prototypevolunteerapp.ui.components.LoadingIndicator

private val NavyDark   = Color(0xFF1E3A8A)
private val PrimaryBlue= Color(0xFF3B82F6)
private val BgScreen   = Color(0xFFF8FAFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    viewModel: ActivitiesViewModel = hiltViewModel()
) {
    val backStack          = LocalBackStack.current
    val searchQuery        by viewModel.searchQuery.collectAsState()
    val isLoading          by viewModel.isLoading.collectAsState()
    val filteredActivities by viewModel.filteredActivities.collectAsState()
    val errorMessage       by viewModel.errorMessage.collectAsState()
    val categories         by viewModel.categories.collectAsState()
    val selectedCategory   by viewModel.selectedCategory.collectAsState()
    val selectedCity       by viewModel.selectedCity.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cari Kegiatan",
                        color      = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
            )
        },
        containerColor = BgScreen
    ) { innerPadding ->
        ActivitiesContent(
            modifier           = Modifier.padding(innerPadding),
            searchQuery        = searchQuery,
            isLoading          = isLoading,
            filteredActivities = filteredActivities,
            errorMessage       = errorMessage,
            categories         = categories,
            selectedCategory   = selectedCategory,
            selectedCity       = selectedCity,
            onSearchChange     = viewModel::onSearchQueryChange,
            onCategorySelected = viewModel::onCategorySelected,
            onCityChanged      = viewModel::onCityChanged,
            onClearFilters     = viewModel::onClearFilters,
            onRetry            = { viewModel.loadActivities() },
            onViewDetail       = { activity ->
                backStack.add(
                    Routes.ActivityDetailRoute(
                        id        = activity.id,
                        slug      = activity.slug,
                        title     = activity.title,
                        location  = activity.location,
                        desc      = activity.description,
                        imageRes  = activity.imageRes,
                        instagram = activity.instagram ?: "",
                        link      = activity.link ?: ""
                    )
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesContent(
    modifier:           Modifier = Modifier,
    searchQuery:        String,
    isLoading:          Boolean,
    filteredActivities: List<ActivityData>,
    errorMessage:       String?,
    categories:         List<CategoryDto>,
    selectedCategory:   CategoryDto?,
    selectedCity:       String,
    onSearchChange:     (String) -> Unit,
    onCategorySelected: (CategoryDto?) -> Unit,
    onCityChanged:      (String) -> Unit,
    onClearFilters:     () -> Unit,
    onRetry:            () -> Unit,
    onViewDetail:       (ActivityData) -> Unit
) {
    var showCityDialog by remember { mutableStateOf(false) }
    var cityInput      by remember { mutableStateOf(selectedCity) }

    if (showCityDialog) {
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title   = { Text("Filter Kota", fontWeight = FontWeight.Bold) },
            text    = {
                OutlinedTextField(
                    value         = cityInput,
                    onValueChange = { cityInput = it },
                    placeholder   = { Text("Contoh: Yogyakarta, Jakarta...") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        focusedLabelColor  = PrimaryBlue
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { onCityChanged(cityInput.trim()); showCityDialog = false },
                    colors  = ButtonDefaults.buttonColors(containerColor = NavyDark)
                ) { Text("Terapkan") }
            },
            dismissButton = {
                TextButton(onClick = {
                    cityInput = ""
                    onCityChanged("")
                    showCityDialog = false
                }) { Text("Hapus Filter", color = PrimaryBlue) }
            }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value         = searchQuery,
            onValueChange = onSearchChange,
            modifier      = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder   = { Text("Cari nama kegiatan atau organisasi...") },
            leadingIcon   = {
                Icon(Icons.Default.Search, null, tint = PrimaryBlue)
            },
            trailingIcon  = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            Icons.Default.Close,
                            "Hapus pencarian",
                            modifier = Modifier.size(18.dp),
                            tint     = Color(0xFF64748B)
                        )
                    }
                }
            },
            singleLine    = true,
            shape         = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedLabelColor = PrimaryBlue,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        if (categories.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedCategory == null && selectedCity.isBlank(),
                    onClick  = { cityInput = ""; onClearFilters() },
                    label    = { Text("Semua", fontSize = 12.sp) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyDark,
                        selectedLabelColor     = Color.White
                    )
                )
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory?.id == category.id,
                        onClick  = {
                            onCategorySelected(
                                if (selectedCategory?.id == category.id) null else category
                            )
                        },
                        label  = { Text(category.name, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor     = Color.White
                        )
                    )
                }
                FilterChip(
                    selected     = selectedCity.isNotBlank(),
                    onClick      = { cityInput = selectedCity; showCityDialog = true },
                    label        = {
                        Text(
                            if (selectedCity.isBlank()) "Kota" else selectedCity,
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon  = {
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor   = PrimaryBlue,
                        selectedLabelColor       = Color.White,
                        selectedLeadingIconColor = Color.White
                    )
                )
            }
        }
        val hasActiveFilter = selectedCategory != null || selectedCity.isNotBlank()
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            if (!isLoading) {
                Text(
                    text       = "${filteredActivities.size} kegiatan ditemukan",
                    fontSize   = 12.sp,
                    color      = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            } else {
                Spacer(Modifier.height(16.dp))
            }
            if (hasActiveFilter || searchQuery.isNotBlank()) {
                TextButton(
                    onClick        = { cityInput = ""; onClearFilters() },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("Reset", fontSize = 12.sp, color = PrimaryBlue)
                }
            }
        }
        when {
            isLoading -> {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { LoadingIndicator() }
            }

            errorMessage != null -> {
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.WifiOff, null,
                        tint     = Color(0xFFB0B0B0),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text      = errorMessage,
                        textAlign = TextAlign.Center,
                        fontSize  = 14.sp,
                        color     = Color(0xFF64748B)
                    )
                    Button(
                        onClick = onRetry,
                        colors  = ButtonDefaults.buttonColors(containerColor = NavyDark)
                    ) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Coba Lagi")
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier            = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding      = PaddingValues(bottom = 24.dp)
                ) {
                    if (filteredActivities.isEmpty()) {
                        item {
                            Column(
                                modifier            = Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.SearchOff, null,
                                    tint     = Color(0xFFB0B0B0),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = when {
                                        hasActiveFilter && searchQuery.isBlank() ->
                                            "Tidak ada kegiatan dengan filter ini."
                                        searchQuery.isNotBlank() ->
                                            "Kegiatan \"$searchQuery\" tidak ditemukan."
                                        else ->
                                            "Belum ada kegiatan tersedia saat ini."
                                    },
                                    textAlign  = TextAlign.Center,
                                    fontSize   = 14.sp,
                                    color      = Color(0xFF64748B)
                                )
                                if (hasActiveFilter || searchQuery.isNotBlank()) {
                                    TextButton(onClick = { cityInput = ""; onClearFilters() }) {
                                        Text("Hapus semua filter", color = PrimaryBlue)
                                    }
                                }
                            }
                        }
                    } else {
                        items(filteredActivities, key = { it.id }) { activity ->
                            ActivityCard(
                                activity     = activity,
                                onViewDetail = { onViewDetail(activity) }
                            )
                        }
                    }
                    item { AppFooter() }
                }
            }
        }
    }
}