package com.example.prototypevolunteerapp.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.core.AppConfig
import com.example.prototypevolunteerapp.core.DateUtils
import com.example.prototypevolunteerapp.core.UserSession
import com.example.prototypevolunteerapp.data.model.VolunteerDataStore
import com.example.prototypevolunteerapp.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

data class EditProfileUiState(
    val name:             String  = "",
    val phone:            String  = "",
    val about:            String  = "",
    val birthDate:        String  = "",
    val gender:           String  = "",
    val city:             String  = "",
    val province:         String  = "",
    val skillsRaw:        String  = "",
    val interestsRaw:     String  = "",
    val currentAvatarUrl: String? = null,
    val selectedImageUri: Uri?    = null,
    val isSaving:         Boolean = false,
    val isSaved:          Boolean = false,
    val isLoading:        Boolean = true,
    val errorMessage:     String? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore:   VolunteerDataStore,
    private val userSession: UserSession,
    private val apiService:  ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init { loadProfile() }

    private fun loadProfile() {
        viewModelScope.launch {
            update { copy(isLoading = true) }
            try {
                val response = apiService.getVolunteerProfile()
                if (response.isSuccessful && response.body() != null) {
                    val u  = response.body()!!.user
                    val vp = u.volunteer_profile

                    val avatarUrl = resolveAvatarUrl(u.avatar) ?: resolveAvatarUrl(vp?.avatar)

                    update {
                        copy(
                            name             = u.name,
                            phone            = u.phone ?: "",
                            about            = vp?.bio ?: "",
                            birthDate        = DateUtils.extractDatePart(vp?.date_of_birth),
                            gender           = vp?.gender ?: "",
                            city             = vp?.city ?: "",
                            province         = vp?.province ?: "",
                            skillsRaw        = vp?.skills?.joinToString(", ") ?: "",
                            interestsRaw     = vp?.interests?.joinToString(", ") ?: "",
                            currentAvatarUrl = avatarUrl,
                            isLoading        = false
                        )
                    }
                    return@launch
                }
            } catch (e: Exception) {
                android.util.Log.w("EditProfileVM", "Fetch server gagal: ${e.message}")
            }

            val saved   = dataStore.profileFlow.first()
            val current = userSession.currentVolunteerProfile
            update {
                copy(
                    name         = saved["name"]?.takeIf      { it.isNotBlank() } ?: current?.name      ?: "",
                    phone        = saved["phone"]?.takeIf     { it.isNotBlank() } ?: current?.phone     ?: "",
                    about        = saved["about"]?.takeIf     { it.isNotBlank() } ?: current?.about     ?: "",
                    birthDate    = DateUtils.extractDatePart(
                        saved["birthDate"]?.takeIf { it.isNotBlank() } ?: current?.birthDate
                    ),
                    gender       = saved["gender"]?.takeIf    { it.isNotBlank() } ?: "",
                    city         = saved["city"]?.takeIf      { it.isNotBlank() } ?: current?.birthPlace ?: "",
                    province     = saved["province"]?.takeIf  { it.isNotBlank() } ?: "",
                    skillsRaw    = current?.skills?.joinToString(", ")    ?: "",
                    interestsRaw = current?.interests?.joinToString(", ") ?: "",
                    isLoading    = false,
                    errorMessage = "Data ditampilkan dari penyimpanan lokal."
                )
            }
        }
    }
    fun onNameChange(v: String)         { update { copy(name = v,         isSaved = false, errorMessage = null) } }
    fun onPhoneChange(v: String)        { update { copy(phone = v,        isSaved = false, errorMessage = null) } }
    fun onAboutChange(v: String)        { update { copy(about = v,        isSaved = false, errorMessage = null) } }
    fun onBirthDateChange(v: String)    { update { copy(birthDate = v,    isSaved = false, errorMessage = null) } }
    fun onGenderChange(v: String)       { update { copy(gender = v,       isSaved = false, errorMessage = null) } }
    fun onCityChange(v: String)         { update { copy(city = v,         isSaved = false, errorMessage = null) } }
    fun onProvinceChange(v: String)     { update { copy(province = v,     isSaved = false, errorMessage = null) } }
    fun onSkillsRawChange(v: String)    { update { copy(skillsRaw = v,    isSaved = false, errorMessage = null) } }
    fun onInterestsRawChange(v: String) { update { copy(interestsRaw = v, isSaved = false, errorMessage = null) } }

    fun onImageSelected(uri: Uri?) {
        update { copy(selectedImageUri = uri, isSaved = false, errorMessage = null) }
    }

    fun onSave() {
        val s = _uiState.value
        val birthDateToSend = if (s.birthDate.isNotBlank()) {
            DateUtils.parseInputDate(s.birthDate) ?: run {
                update { copy(errorMessage = "Format tanggal lahir tidak valid. Gunakan YYYY-MM-DD (contoh: 2000-08-17)") }
                return
            }
        } else null

        update { copy(isSaving = true, errorMessage = null) }

        val plain         = "text/plain".toMediaType()
        val skillsList    = s.skillsRaw.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val interestsList = s.interestsRaw.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val avatarPart: MultipartBody.Part? = s.selectedImageUri?.let { uri ->
            try {
                val cr       = context.contentResolver
                val mimeType = cr.getType(uri) ?: "image/jpeg"
                val bytes    = cr.openInputStream(uri)?.readBytes() ?: return@let null
                val ext      = when (mimeType) { "image/png" -> "png"; "image/webp" -> "webp"; else -> "jpg" }
                val body     = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("avatar", "avatar.$ext", body)
            } catch (e: Exception) {
                android.util.Log.e("EditProfileVM", "Gagal baca avatar: ${e.message}")
                null
            }
        }

        viewModelScope.launch {
            try {
                val resp = apiService.updateVolunteerProfile(
                    name        = s.name.toRequestBodyOrNull(plain),
                    phone       = s.phone.toRequestBodyOrNull(plain),
                    dateOfBirth = birthDateToSend?.toRequestBody(plain),
                    gender      = s.gender.toRequestBodyOrNull(plain),
                    bio         = s.about.toRequestBodyOrNull(plain),
                    city        = s.city.toRequestBodyOrNull(plain),
                    province    = s.province.toRequestBodyOrNull(plain),
                    skills0     = skillsList.getOrNull(0)?.toRequestBody(plain),
                    skills1     = skillsList.getOrNull(1)?.toRequestBody(plain),
                    skills2     = skillsList.getOrNull(2)?.toRequestBody(plain),
                    skills3     = skillsList.getOrNull(3)?.toRequestBody(plain),
                    skills4     = skillsList.getOrNull(4)?.toRequestBody(plain),
                    interests0  = interestsList.getOrNull(0)?.toRequestBody(plain),
                    interests1  = interestsList.getOrNull(1)?.toRequestBody(plain),
                    interests2  = interestsList.getOrNull(2)?.toRequestBody(plain),
                    interests3  = interestsList.getOrNull(3)?.toRequestBody(plain),
                    avatar      = avatarPart
                )

                if (resp.isSuccessful && resp.body() != null) {
                    val updated = resp.body()!!.user
                    val vp      = updated.volunteer_profile

                    dataStore.saveProfile(
                        name       = updated.name,
                        email      = updated.email,
                        phone      = updated.phone ?: "",
                        about      = vp?.bio ?: "",
                        birthPlace = vp?.city ?: "",
                        birthDate  = DateUtils.extractDatePart(vp?.date_of_birth),
                        education  = ""
                    )

                    userSession.currentVolunteerProfile?.copy(
                        name      = updated.name,
                        phone     = updated.phone,
                        about     = vp?.bio ?: "",
                        birthDate = DateUtils.extractDatePart(vp?.date_of_birth),
                        skills    = vp?.skills ?: emptyList(),
                        interests = vp?.interests ?: emptyList()
                    )?.let { userSession.updateVolunteerProfileDto(vp) }
                    val newAvatarUrl = resolveAvatarUrl(updated.avatar)
                        ?: resolveAvatarUrl(vp?.avatar)

                    update {
                        copy(
                            isSaving         = false,
                            isSaved          = true,
                            selectedImageUri = null,
                            currentAvatarUrl = newAvatarUrl ?: currentAvatarUrl
                        )
                    }
                } else {
                    val errBody = resp.errorBody()?.string() ?: "Terjadi kesalahan"
                    update { copy(isSaving = false, errorMessage = "Gagal menyimpan (${resp.code()}): $errBody") }
                }
            } catch (e: Exception) {
                update { copy(isSaving = false, errorMessage = "Tidak dapat terhubung ke server.") }
            }
        }
    }

    fun onSavedHandled() { update { copy(isSaved = false) } }
    private inline fun update(block: EditProfileUiState.() -> EditProfileUiState) {
        _uiState.value = _uiState.value.block()
    }
    private fun resolveAvatarUrl(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return when {
            raw.startsWith("http") -> raw
                .replace("localhost", "10.0.2.2")
            else -> "${AppConfig.BASE_URL}storage/$raw"
                .replace("localhost", "10.0.2.2")
        }
    }
}

private fun String.toRequestBodyOrNull(type: MediaType): RequestBody? =
    trim().ifBlank { null }?.toRequestBody(type)