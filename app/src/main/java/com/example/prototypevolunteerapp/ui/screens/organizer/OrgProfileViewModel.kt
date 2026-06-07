package com.example.prototypevolunteerapp.ui.screens.organizer

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.core.AppConfig
import com.example.prototypevolunteerapp.core.OrganizerSession
import com.example.prototypevolunteerapp.data.model.OrgPortfolioItem
import com.example.prototypevolunteerapp.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

data class OrgProfileState(
    val email:              String  = "",
    val orgName:            String  = "",
    val description:        String  = "",
    val website:            String  = "",
    val phone:              String  = "",
    val address:            String  = "",
    val city:               String  = "",
    val province:           String  = "",
    val verificationStatus: String  = "",
    val logoUrl:            String? = null,
    val logoUri:            Uri?    = null,
    val portfolio:          List<OrgPortfolioItem> = emptyList(),
    val isLoading:          Boolean = false,
    val isSaving:           Boolean = false,
    val isSaved:            Boolean = false,
    val errorMessage:       String? = null
)

@HiltViewModel
class OrgProfileViewModel @Inject constructor(
    private val apiService:       ApiService,
    private val organizerSession: OrganizerSession
) : ViewModel() {

    private val _profileState = MutableStateFlow(OrgProfileState())
    val profileState: StateFlow<OrgProfileState> = _profileState.asStateFlow()

    val currentOrgEmail get() = organizerSession.currentOrg?.email ?: ""

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true, errorMessage = null)
            try {
                val resp = apiService.getOrgProfile()
                if (!resp.isSuccessful || resp.body() == null) {
                    _profileState.value = _profileState.value.copy(
                        isLoading    = false,
                        errorMessage = "Gagal memuat profil (${resp.code()})"
                    )
                    return@launch
                }
                val body = resp.body()!!
                val org  = body.organization
                val user = body.user

                val logoUrl = org.logo?.let { path ->
                    if (path.startsWith("http")) path
                    else "${AppConfig.BASE_URL}storage/$path"
                }

                _profileState.value = OrgProfileState(
                    email               = user.email,
                    orgName             = org.organization_name ?: "",
                    description         = org.description ?: "",
                    website             = org.website ?: "",
                    phone               = user.phone ?: "",
                    address             = org.address ?: "",
                    city                = org.city ?: "",
                    province            = org.province ?: "",
                    verificationStatus  = org.verification_status ?: "pending",
                    logoUrl             = logoUrl,
                    isLoading           = false
                )
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(
                    isLoading    = false,
                    errorMessage = "Koneksi gagal: ${e.localizedMessage}"
                )
            }
        }
    }

    fun updateOrgName(v: String)     { _profileState.value = _profileState.value.copy(orgName = v) }
    fun updateDescription(v: String) { _profileState.value = _profileState.value.copy(description = v) }
    fun updateWebsite(v: String)     { _profileState.value = _profileState.value.copy(website = v) }
    fun updatePhone(v: String)       { _profileState.value = _profileState.value.copy(phone = v) }
    fun updateAddress(v: String)     { _profileState.value = _profileState.value.copy(address = v) }
    fun updateCity(v: String)        { _profileState.value = _profileState.value.copy(city = v) }
    fun updateProvince(v: String)    { _profileState.value = _profileState.value.copy(province = v) }

    fun updateLogoUri(uri: Uri?) {
        _profileState.value = _profileState.value.copy(logoUri = uri)
    }
    fun saveProfile() {
        val s = _profileState.value
        viewModelScope.launch {
            _profileState.value = s.copy(isSaving = true, errorMessage = null)
            try {
                val plain = "text/plain".toMediaType()
                val logoPart: MultipartBody.Part? = null
                val resp = apiService.updateOrgProfile(
                    name             = s.orgName.trim().toRequestBody(plain),
                    phone            = s.phone.trimOrNull()?.toRequestBody(plain),
                    organizationName = s.orgName.trim().toRequestBody(plain),
                    description      = s.description.trimOrNull()?.toRequestBody(plain),
                    address          = s.address.trimOrNull()?.toRequestBody(plain),
                    city             = s.city.trimOrNull()?.toRequestBody(plain),
                    province         = s.province.trimOrNull()?.toRequestBody(plain),
                    website          = s.website.trimOrNull()?.toRequestBody(plain),
                    logo             = logoPart,
                    document         = null
                )

                if (resp.isSuccessful) {
                    _profileState.value = _profileState.value.copy(
                        isSaving = false,
                        isSaved  = true,
                        logoUri  = null
                    )
                } else {
                    val errBody = resp.errorBody()?.string()
                    _profileState.value = _profileState.value.copy(
                        isSaving     = false,
                        errorMessage = "Gagal menyimpan (${resp.code()}): $errBody"
                    )
                }
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(
                    isSaving     = false,
                    errorMessage = "Koneksi gagal: ${e.localizedMessage}"
                )
            }
        }
    }
    fun addPortfolio(title: String, year: String, description: String) {
        val updated = _profileState.value.portfolio + OrgPortfolioItem(title, year, description)
        _profileState.value = _profileState.value.copy(portfolio = updated)
    }

    fun removePortfolio(index: Int) {
        val updated = _profileState.value.portfolio.toMutableList().also { it.removeAt(index) }
        _profileState.value = _profileState.value.copy(portfolio = updated)
    }

    fun onSavedHandled()   { _profileState.value = _profileState.value.copy(isSaved = false) }
    fun onErrorDismissed() { _profileState.value = _profileState.value.copy(errorMessage = null) }
}

private fun String.trimOrNull(): String? = trim().ifBlank { null }