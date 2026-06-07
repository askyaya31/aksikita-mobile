package com.example.prototypevolunteerapp.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.prototypevolunteerapp.ui.screens.LoginScreen
import com.example.prototypevolunteerapp.ui.screens.activities.ActivitiesScreen
import com.example.prototypevolunteerapp.ui.screens.activities.ActivityDetailScreen
import com.example.prototypevolunteerapp.ui.screens.dashboard.HomeScreen
import com.example.prototypevolunteerapp.ui.screens.history.ActivityHistoryScreen
import com.example.prototypevolunteerapp.ui.screens.notifications.NotificationsScreen
import com.example.prototypevolunteerapp.ui.screens.notifications.NotificationsDetailScreen
import com.example.prototypevolunteerapp.ui.screens.organizer.AddActivityScreen
import com.example.prototypevolunteerapp.ui.screens.organizer.CandidateDetailScreen
import com.example.prototypevolunteerapp.ui.screens.organizer.CandidateListScreen
import com.example.prototypevolunteerapp.ui.screens.organizer.EditActivityScreen
import com.example.prototypevolunteerapp.ui.screens.organizer.OrgDashboardScreen
import com.example.prototypevolunteerapp.ui.screens.organizer.EditOrgProfileScreen
import com.example.prototypevolunteerapp.ui.screens.organizer.OrgProfileScreen
import com.example.prototypevolunteerapp.ui.screens.saved.SavedActivitiesScreen
import com.example.prototypevolunteerapp.ui.screens.liked.LikedActivitiesScreen
import com.example.prototypevolunteerapp.ui.screens.profile.ProfileScreen
import com.example.prototypevolunteerapp.ui.screens.profile.EditProfileScreen
import com.example.prototypevolunteerapp.ui.screens.register.RegisterScreen
import com.example.prototypevolunteerapp.ui.screens.welcome.WelcomeScreen
import com.example.prototypevolunteerapp.ui.screens.splash.SplashScreen
import com.example.prototypevolunteerapp.ui.theme.AksiKitaTheme

@Composable
fun ComposeApp() {
    val backStack = rememberNavBackStack(Routes.SplashRoute)

    CompositionLocalProvider(LocalBackStack provides backStack) {
        AksiKitaTheme {
            NavDisplay(
                backStack = backStack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    entry<Routes.SplashRoute>    { SplashScreen() }
                    entry<Routes.WelcomeRoute>   { WelcomeScreen() }
                    entry<Routes.LoginRoute>     { LoginScreen() }
                    entry<Routes.RegisterRoute>  { RegisterScreen() }
                    entry<Routes.HomeRoute>      { HomeScreen() }
                    entry<Routes.ActivitiesRoute>{ ActivitiesScreen() }

                    entry<Routes.ActivityDetailRoute> {
                        ActivityDetailScreen(
                            id = it.id,
                            title = it.title,
                            location = it.location,
                            desc = it.desc,
                            imageRes = it.imageRes,
                            instagram = it.instagram.ifBlank { null },
                            link = it.link.ifBlank { null },
                            slug = it.slug
                        )
                    }

                    entry<Routes.ActivityHistoryRoute>  { ActivityHistoryScreen() }
                    entry<Routes.NotificationsRoute>    { NotificationsScreen() }
                    entry<Routes.SavedActivitiesRoute> {
                        SavedActivitiesScreen()
                    }
                    entry<Routes.LikedActivitiesRoute> {
                        LikedActivitiesScreen()
                    }
                    entry<Routes.ProfileRoute>          { ProfileScreen() }
                    entry<Routes.EditProfileRoute>      { EditProfileScreen() }

                    entry<Routes.NotificationDetailRoute> {
                        NotificationsDetailScreen(notificationId = it.notificationId)
                    }

                    entry<Routes.OrgDashboardRoute>    { OrgDashboardScreen() }
                    entry<Routes.AddActivityRoute>     { AddActivityScreen() }

                    entry<Routes.CandidateListRoute> {
                        CandidateListScreen(eventId = it.eventId)
                    }
                    entry<Routes.CandidateDetailRoute> {
                        CandidateDetailScreen(candidateId = it.candidateId)
                    }
                    entry<Routes.EditActivityRoute> {
                        EditActivityScreen(eventId = it.eventId)
                    }
                    entry<Routes.OrgProfileRoute> { OrgProfileScreen() }
                    entry<Routes.EditOrgProfileRoute> { EditOrgProfileScreen() }
                }
            )
        }
    }
}