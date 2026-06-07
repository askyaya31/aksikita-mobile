package com.example.prototypevolunteerapp.ui.screens.volunteers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.Routes
import com.example.prototypevolunteerapp.data.model.Volunteer
import com.example.prototypevolunteerapp.ui.components.AppFooter
import com.example.prototypevolunteerapp.ui.components.LoadingIndicator
import com.example.prototypevolunteerapp.ui.components.VolunteerCard

private val ScreenBg = Color(0xFFF4F7EF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteersScreen(
    viewModel: VolunteersViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val volunteers by viewModel.volunteers.collectAsState()
    val isLoading  by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "Meet the Volunteers!",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 20.sp,
                        color      = Color(0xFF1E2D1E)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(
                            imageVector    = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint           = Color(0xFF1E2D1E)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ScreenBg)
            )
        },
        containerColor = ScreenBg
    ) { innerPadding ->
        VolunteersContent(
            modifier   = Modifier.padding(innerPadding),
            isLoading  = isLoading,
            volunteers = volunteers,
            onViewProfile = { index ->
                backStack.add(Routes.VolunteerDetailRoute(volunteerIndex = index))
            }
        )
    }
}
@Composable
fun VolunteersContent(
    modifier:     Modifier = Modifier,
    isLoading:    Boolean,
    volunteers:   List<Volunteer>,
    onViewProfile: (Int) -> Unit
) {
    if (isLoading) {
        LoadingIndicator(modifier = modifier)
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            itemsIndexed(volunteers) { index, volunteer ->
                VolunteerCard(
                    volunteer     = volunteer,
                    onViewProfile = { onViewProfile(index) }
                )
            }
            item { AppFooter() }
        }
    }
}
