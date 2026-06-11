package com.example.prototypevolunteerapp.core

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
object Routes {

    @Serializable
    data object SplashRoute : NavKey
    @Serializable
    data object WelcomeRoute : NavKey
    @Serializable
    data object HomeRoute : NavKey
    @Serializable
    data object LoginRoute : NavKey

    @Serializable
    data object ActivitiesRoute : NavKey

    @Serializable
    data class ActivityDetailRoute(
        val id:        String = "",
        val title:     String,
        val location:  String,
        val desc:      String,
        val imageRes:  String,
        val instagram: String = "",
        val link:      String = "",
        val slug:      String = ""
    ) : NavKey

    @Serializable
    data object VolunteersRoute : NavKey

    @Serializable
    data class VolunteerDetailRoute(
        val volunteerIndex: Int
    ) : NavKey

    @Serializable
    data object ActivityHistoryRoute : NavKey
    @Serializable
    data object ProfileRoute : NavKey
    @Serializable
    data object NotificationsRoute : NavKey

    @Serializable
    data object OrganizerLoginRoute : NavKey

    @Serializable
    data object OrgDashboardRoute : NavKey

    @Serializable
    data object AddActivityRoute : NavKey

    @Serializable
    data class CandidateListRoute(val eventId: Int? = null, val initialFilter: String = "Semua") : NavKey

    @Serializable
    data class CandidateDetailRoute(
        val candidateId: Int
    ) : NavKey

    @Serializable
    data class EditActivityRoute(val eventId: Int) : NavKey
    @Serializable
    data class NotificationDetailRoute(
        val notificationId: Int
    ) : NavKey

    @Serializable
    data object OrgProfileRoute : NavKey

    @Serializable
    data object EditOrgProfileRoute : NavKey

    @Serializable
    data object EditProfileRoute : NavKey
    @Serializable
    data object SavedActivitiesRoute : NavKey

    @Serializable
    data object LikedActivitiesRoute : NavKey
    @Serializable
    data object RegisterRoute : NavKey
}