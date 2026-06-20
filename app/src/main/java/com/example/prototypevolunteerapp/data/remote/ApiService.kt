package com.example.prototypevolunteerapp.data.remote

import com.example.prototypevolunteerapp.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface ApiService {
    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/v1/auth/register/volunteer")
    suspend fun registerVolunteer(
        @Body request: RegisterVolunteerRequest
    ): Response<RegisterResponse>

    @POST("api/v1/auth/register/organization")
    suspend fun registerOrganization(
        @Body request: RegisterOrganizationRequest
    ): Response<RegisterResponse>

    @POST("api/v1/auth/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleLoginRequest
    ): Response<LoginResponse>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("api/v1/auth/me")
    suspend fun getMe(): Response<ApiSingleResponse<UserDto>>

    @GET("api/v1/events")
    suspend fun getPublicEvents(
        @Query("search")     search:    String? = null,
        @Query("city")       city:      String? = null,
        @Query("province")   province:  String? = null,
        @Query("category")   category:  String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("page")       page:      Int?    = null
    ): Response<PaginatedResponse<EventDto>>

    @GET("api/v1/events/{slug}")
    suspend fun getPublicEventDetail(
        @Path("slug") slug: String
    ): Response<EventSingleResponse>

    @GET("api/v1/categories")
    suspend fun getCategories(): Response<CategoryListResponse>

    @GET("api/v1/categories/{slug}/events")
    suspend fun getEventsByCategory(
        @Path("slug") slug: String,
        @Query("page")      page: Int? = null
    ): Response<PaginatedResponse<EventDto>>

    @GET("api/v1/volunteer/events")
    suspend fun getVolunteerEvents(
        @Query("search")   search:   String? = null,
        @Query("city")     city:     String? = null,
        @Query("category") category: String? = null,
        @Query("page")     page:     Int?    = null
    ): Response<PaginatedResponse<EventDto>>

    @GET("api/v1/volunteer/events/{slug}")
    suspend fun getVolunteerEventDetail(
        @Path("slug") slug: String
    ): Response<EventSingleResponse>

    @POST("api/v1/volunteer/events/{id}/register")
    suspend fun registerToEvent(
        @Path("id") eventId: Int,
        @Body request: RegisterEventRequest = RegisterEventRequest()
    ): Response<RegistrationSingleResponse>

    @DELETE("api/v1/volunteer/events/{id}/cancel")
    suspend fun cancelRegistration(
        @Path("id") eventId: Int
    ): Response<Unit>

    @GET("api/v1/volunteer/registrations")
    suspend fun getVolunteerRegistrations(
        @Query("status") status: String? = null,
        @Query("page")   page:   Int?    = null
    ): Response<PaginatedResponse<RegistrationDto>>

    @GET("api/v1/volunteer/registrations/{id}")
    suspend fun getVolunteerRegistrationDetail(
        @Path("id") id: Int
    ): Response<RegistrationSingleResponse>

    @GET("api/v1/volunteer/profile")
    suspend fun getVolunteerProfile(): Response<VolunteerProfileResponse>

    @Multipart
    @POST("api/v1/volunteer/profile?_method=PUT")
    suspend fun updateVolunteerProfile(
        @Part("name")          name:         RequestBody?       = null,
        @Part("phone")         phone:        RequestBody?       = null,
        @Part("date_of_birth") dateOfBirth:  RequestBody?       = null,
        @Part("gender")        gender:       RequestBody?       = null,
        @Part("bio")           bio:          RequestBody?       = null,
        @Part("city")          city:         RequestBody?       = null,
        @Part("province")      province:     RequestBody?       = null,
        @Part("skills[]")      skills0:      RequestBody? = null,
        @Part("skills[]")      skills1:      RequestBody? = null,
        @Part("skills[]")      skills2:      RequestBody? = null,
        @Part("skills[]")      skills3:      RequestBody? = null,
        @Part("skills[]")      skills4:      RequestBody? = null,
        @Part("interests[]")   interests0:   RequestBody? = null,
        @Part("interests[]")   interests1:   RequestBody? = null,
        @Part("interests[]")   interests2:   RequestBody? = null,
        @Part("interests[]")   interests3:   RequestBody? = null,
        @Part                  avatar:       MultipartBody.Part? = null
    ): Response<VolunteerProfileResponse>

    @GET("api/v1/volunteer/notifications")
    suspend fun getVolunteerNotifications(
        @Query("page") page: Int? = null
    ): Response<PaginatedResponse<NotificationDto>>

    @GET("api/v1/volunteer/notifications/unread-count")
    suspend fun getVolunteerUnreadCount(): Response<UnreadCountResponse>

    @PUT("api/v1/volunteer/notifications/{id}/read")
    suspend fun markVolunteerNotificationRead(
        @Path("id") id: Int
    ): Response<Unit>

    @PUT("api/v1/volunteer/notifications/mark-all-read")
    suspend fun markAllVolunteerNotificationsRead(): Response<Unit>

    @GET("api/v1/volunteer/saved-events")
    suspend fun getSavedEvents(): Response<SavedEventsResponse>

    @POST("api/v1/volunteer/events/{id}/save")
    suspend fun toggleSaveEvent(
        @Path("id") eventId: Int
    ): Response<SavedToggleResponse>

    @DELETE("api/v1/volunteer/events/{id}/save")
    suspend fun unsaveEvent(
        @Path("id") eventId: Int
    ): Response<SavedToggleResponse>

    @GET("api/v1/volunteer/liked-events")
    suspend fun getLikedEvents(): Response<LikedEventsResponse>

    @POST("api/v1/volunteer/events/{id}/like")
    suspend fun toggleLikeEvent(
        @Path("id") eventId: Int
    ): Response<LikedToggleResponse>

    @GET("api/v1/organization/profile")
    suspend fun getOrgProfile(): Response<OrgProfileResponse>

    @Multipart
    @POST("api/v1/organization/profile?_method=PUT")
    suspend fun updateOrgProfile(
        @Part("name")              name:             RequestBody?       = null,
        @Part("phone")             phone:            RequestBody?       = null,
        @Part("organization_name") organizationName: RequestBody?       = null,
        @Part("description")       description:      RequestBody?       = null,
        @Part("address")           address:          RequestBody?       = null,
        @Part("city")              city:             RequestBody?       = null,
        @Part("province")          province:         RequestBody?       = null,
        @Part("website")           website:          RequestBody?       = null,
        @Part                      logo:             MultipartBody.Part? = null,
        @Part                      document:         MultipartBody.Part? = null
    ): Response<OrgProfileResponse>

    @GET("api/v1/organization/events")
    suspend fun getOrgEvents(
        @Query("status") status: String? = null,
        @Query("page")   page:   Int?    = null
    ): Response<PaginatedResponse<EventDto>>

    @Multipart
    @POST("api/v1/organization/events")
    suspend fun storeOrgEvent(
        @Part("title")            title:         RequestBody,
        @Part("description")      description:   RequestBody,
        @Part("location_name")    locationName:  RequestBody,
        @Part("city")             city:          RequestBody,
        @Part("province")         province:      RequestBody? = null,
        @Part("start_date")       startDate:     RequestBody,
        @Part("end_date")         endDate:       RequestBody,
        @Part("start_time")       startTime:     RequestBody? = null,
        @Part("end_time")         endTime:       RequestBody? = null,
        @Part("quota")            quota:         RequestBody,
        @Part("requirements")     requirements:  RequestBody? = null,
        @Part("contact_person")   contactPerson: RequestBody? = null,
        @Part("contact_phone")    contactPhone:  RequestBody? = null,
        @Part("category_ids[0]")  categoryId0:   RequestBody? = null,
        @Part("category_ids[1]")  categoryId1:   RequestBody? = null,
        @Part("category_ids[2]")  categoryId2:   RequestBody? = null,
        @Part("category_ids[3]")  categoryId3:   RequestBody? = null,
        @Part("category_ids[4]")  categoryId4:   RequestBody? = null,
        @Part                     poster:        MultipartBody.Part? = null
    ): Response<EventSingleResponse>

    @GET("api/v1/organization/events/{id}")
    suspend fun getOrgEventDetail(
        @Path("id") id: Int
    ): Response<EventSingleResponse>

    @Multipart
    @POST("api/v1/organization/events/{id}?_method=PUT")
    suspend fun updateOrgEvent(
        @Path("id")            id:            Int,
        @Part("title")         title:         RequestBody,
        @Part("description")   description:   RequestBody,
        @Part("location_name") locationName:  RequestBody,
        @Part("city")          city:          RequestBody,
        @Part("start_date")    startDate:     RequestBody,
        @Part("end_date")      endDate:       RequestBody,
        @Part("start_time")    startTime:     RequestBody? = null,
        @Part("end_time")      endTime:       RequestBody? = null,
        @Part("quota")         quota:         RequestBody,
        @Part("requirements")  requirements:  RequestBody? = null,
        @Part("contact_person") contactPerson: RequestBody? = null,
        @Part("contact_phone") contactPhone:  RequestBody? = null,
        @Part               poster:        MultipartBody.Part? = null
    ): Response<EventSingleResponse>

    @DELETE("api/v1/organization/events/{id}")
    suspend fun deleteOrgEvent(
        @Path("id") id: Int
    ): Response<Unit>

    @PUT("api/v1/organization/events/{id}/submit")
    suspend fun submitOrgEvent(
        @Path("id") id: Int
    ): Response<EventSingleResponse>

    @PUT("api/v1/organization/events/{id}/complete")
    suspend fun completeOrgEvent(
        @Path("id") id: Int
    ): Response<EventSingleResponse>

    @PUT("api/v1/organization/events/{id}/cancel")
    suspend fun cancelOrgEvent(
        @Path("id") id: Int,
        @Body reason: Map<String, String?> = emptyMap()
    ): Response<EventSingleResponse>
    @GET("api/v1/volunteer/chats/unread-count")
    suspend fun getChatUnreadCount(): Response<UnreadCountResponse>

    @GET("api/v1/organization/events/{id}/registrations")
    suspend fun getEventRegistrations(
        @Path("id")      eventId: Int,
        @Query("status") status:  String? = null,
        @Query("page")   page:    Int?    = null
    ): Response<PaginatedResponse<RegistrationDto>>

    @GET("api/v1/organization/registrations/{id}")
    suspend fun getOrgRegistrationDetail(
        @Path("id") id: Int
    ): Response<RegistrationSingleResponse>

    @PUT("api/v1/organization/registrations/{id}/confirm")
    suspend fun confirmRegistration(
        @Path("id") registrationId: Int
    ): Response<Unit>

    @PUT("api/v1/organization/registrations/{id}/reject")
    suspend fun rejectRegistration(
        @Path("id")   registrationId: Int,
        @Body reason: Map<String, String?> = emptyMap()
    ): Response<Unit>

    @PUT("api/v1/organization/registrations/{id}/attend")
    suspend fun attendRegistration(
        @Path("id") registrationId: Int
    ): Response<Unit>

    @GET("api/v1/organization/notifications")
    suspend fun getOrgNotifications(
        @Query("page") page: Int? = null
    ): Response<PaginatedResponse<NotificationDto>>

    @GET("api/v1/organization/notifications/unread-count")
    suspend fun getOrgUnreadCount(): Response<UnreadCountResponse>

    @PUT("api/v1/organization/notifications/{id}/read")
    suspend fun markOrgNotificationRead(
        @Path("id") id: Int
    ): Response<Unit>

    @PUT("api/v1/organization/notifications/mark-all-read")
    suspend fun markAllOrgNotificationsRead(): Response<Unit>

    @GET("api/v1/volunteer/chats")
    suspend fun getChatRooms(): List<ChatRoomDto>

    @GET("api/v1/volunteer/chats/{roomId}/messages")
    suspend fun getChatMessages(@Path("roomId") roomId: Int): List<ChatMessageDto>

    @POST("api/v1/volunteer/chats/{roomId}/messages")
    suspend fun sendMessage(
        @Path("roomId") roomId: Int,
        @Body body: SendMessageRequest
    ): ChatMessageDto

    @GET("api/v1/volunteer/chats/{roomId}/poll")
    suspend fun pollMessages(
        @Path("roomId") roomId: Int,
        @Query("after") afterId: Int
    ): ChatPollResponse

    @GET("api/v1/volunteer/schedule")
    suspend fun getSchedule(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): ScheduleResponse

    @GET("api/v1/volunteer/recommendations")
    suspend fun getRecommendations(): List<ActivityDto>
    @GET("api/v1/organization/chats")
    suspend fun getOrgChatRooms(): List<ChatRoomDto>

    @GET("api/v1/organization/chats/{roomId}/messages")
    suspend fun getOrgChatMessages(@Path("roomId") roomId: Int): List<ChatMessageDto>

    @POST("api/v1/organization/chats/{roomId}/messages")
    suspend fun sendOrgMessage(
        @Path("roomId") roomId: Int,
        @Body body: SendMessageRequest
    ): ChatMessageDto

    @GET("api/v1/organization/chats/{roomId}/poll")
    suspend fun pollOrgMessages(
        @Path("roomId") roomId: Int,
        @Query("after") afterId: Int
    ): ChatPollResponse
}