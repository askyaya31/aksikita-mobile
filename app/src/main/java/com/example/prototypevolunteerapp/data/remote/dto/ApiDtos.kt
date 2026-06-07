package com.example.prototypevolunteerapp.data.remote.dto

data class LoginRequest(
    val email:    String,
    val password: String
)

data class LoginResponse(
    val message: String? = null,
    val token:   String,
    val role:    String? = null,
    val user:    UserDto
)

data class RegisterVolunteerRequest(
    val name:                  String,
    val email:                 String,
    val password:              String,
    val password_confirmation: String,
    val phone:                 String? = null,
    val city:                  String? = null,
    val province:              String? = null
)

data class RegisterOrganizationRequest(
    val name:                  String,
    val email:                 String,
    val password:              String,
    val password_confirmation: String,
    val organization_name:     String,
    val phone:                 String? = null,
    val description:           String? = null,
    val address:               String? = null,
    val city:                  String? = null,
    val province:              String? = null
)

data class RegisterResponse(
    val message: String? = null,
    val token:   String,
    val user:    UserDto
)

data class UserDto(
    val id:                   Int,
    val name:                 String,
    val email:                String,
    val role:                 String,
    val phone:                String?  = null,
    val avatar:               String?  = null,
    val is_active:            Boolean? = null,
    val created_at:           String?  = null,
    val volunteer_profile:    VolunteerProfileDto?    = null,
    val organization_profile: OrganizationProfileDto? = null
)

data class VolunteerProfileDto(
    val id:                  Int?          = null,
    val city:                String?       = null,
    val province:            String?       = null,
    val date_of_birth:       String?       = null,
    val gender:              String?       = null,
    val bio:                 String?       = null,
    val skills:              List<String>? = null,
    val interests:           List<String>? = null,
    val avatar:              String?       = null,
    val total_events_joined: Int?          = null
)

@Deprecated("Gunakan parameter @Part di ApiService.updateVolunteerProfile()")
data class UpdateVolunteerProfileRequest(
    val name:          String?       = null,
    val phone:         String?       = null,
    val date_of_birth: String?       = null,
    val gender:        String?       = null,
    val bio:           String?       = null,
    val skills:        List<String>? = null,
    val interests:     List<String>? = null,
    val city:          String?       = null,
    val province:      String?       = null
)

data class VolunteerProfileResponse(
    val user:    UserDto,
    val message: String? = null
)

data class OrganizationProfileDto(
    val id:                  Int?    = null,
    val organization_name:   String? = null,
    val description:         String? = null,
    val address:             String? = null,
    val city:                String? = null,
    val province:            String? = null,
    val website:             String? = null,
    val logo:                String? = null,
    val verification_status: String? = null,
    val verified_at:         String? = null,
    val user:                OrgUserDto? = null
)

data class OrgProfileResponse(
    val organization: OrganizationProfileDto,
    val user:         OrgUserDto,
    val message:      String? = null
)

data class OrgUserDto(
    val id:    Int,
    val name:  String,
    val email: String,
    val phone: String? = null
)

data class EventDto(
    val id:               Int,
    val title:            String,
    val slug:             String?  = null,
    val description:      String?  = null,
    val poster:           String?  = null,
    val location_name:    String?  = null,
    val location_address: String?  = null,
    val city:             String?  = null,
    val province:         String?  = null,
    val start_date:       String?  = null,
    val end_date:         String?  = null,
    val start_time:       String?  = null,
    val end_time:         String?  = null,
    val quota:            Int?     = null,
    val registered_count: Int?     = null,
    val likes_count:      Int?     = null,
    val remaining_quota:  Int?     = null,
    val is_full:          Boolean? = null,
    val status:           String?  = null,
    val requirements:     String?  = null,
    val contact_person:   String?  = null,
    val contact_phone:    String?  = null,
    val created_at:       String?  = null,
    val categories:       List<CategoryDto>? = null,
    val organization:     OrgDto?  = null
)

data class OrgDto(
    val id:                  Int,
    val organization_name:   String,
    val description:         String?    = null,
    val address:             String?    = null,
    val city:                String?    = null,
    val province:            String?    = null,
    val website:             String?    = null,
    val logo:                String?    = null,
    val verification_status: String?    = null,
    val verified_at:         String?    = null,
    val user:                OrgUserDto? = null
)

data class CategoryDto(
    val id:   Int,
    val name: String,
    val slug: String? = null
)

data class StoreEventRequest(
    val title:            String,
    val description:      String,
    val location_name:    String,
    val location_address: String? = null,
    val city:             String,
    val province:         String? = null,
    val start_date:       String,
    val end_date:         String,
    val start_time:       String? = null,
    val end_time:         String? = null,
    val quota:            Int,
    val requirements:     String? = null,
    val contact_person:   String? = null,
    val contact_phone:    String? = null,
    val category_ids:     List<Int>? = null
)

data class EventSingleResponse(
    val event:   EventDto,
    val message: String? = null
)

data class RegistrationDto(
    val id:                  Int,
    val status:              String,
    val registered_at:       String?  = null,
    val cancelled_at:        String?  = null,
    val cancellation_reason: String?  = null,
    val notes:               String?  = null,
    val event:               EventDto? = null,
    val user:                UserDto?  = null
)

data class RegistrationSingleResponse(
    val message:      String?          = null,
    val registration: RegistrationDto? = null
)

data class RegisterEventRequest(
    val notes: String? = null
)

data class CancelRegistrationRequest(
    val reason: String? = null
)

data class NotificationDto(
    val id:               Int,
    val user_id:          Int?     = null,
    val title:            String,
    val message:          String,
    val type:             String?  = null,
    val is_read:          Boolean  = false,
    val related_event_id: Int?     = null,
    val created_at:       String?  = null
)

data class UnreadCountResponse(
    val unread_count: Int
)

data class CategoryListResponse(
    val categories: List<CategoryDto>
)

data class ApiListResponse<T>(
    val data:    List<T>,
    val message: String? = null
)

data class ApiSingleResponse<T>(
    val data:    T?,
    val message: String? = null
)

data class PaginatedResponse<T>(
    val data:         List<T> = emptyList(),
    val current_page: Int     = 1,
    val last_page:    Int     = 1,
    val per_page:     Int     = 15,
    val total:        Int     = 0
)

data class SavedEventDto(
    val id:         Int,
    val event_id:   Int,
    val created_at: String?   = null,
    val event:      EventDto? = null
)

data class SavedEventsResponse(
    val data: List<SavedEventDto> = emptyList()
)

data class SavedToggleResponse(
    val message: String?  = null,
    val saved:   Boolean  = false
)

data class LikedEventDto(
    val id:         Int,
    val event_id:   Int,
    val created_at: String?   = null,
    val event:      EventDto? = null
)

data class LikedEventsResponse(
    val data: List<LikedEventDto> = emptyList()
)

data class LikedToggleResponse(
    val message:     String?  = null,
    val liked:       Boolean  = false,
    val likes_count: Int      = 0
)

data class GoogleLoginRequest(
    val id_token: String
)