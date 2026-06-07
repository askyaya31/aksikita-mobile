package com.example.prototypevolunteerapp.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.core.UserSession
import com.example.prototypevolunteerapp.data.preferences.AccountDataStore
import com.example.prototypevolunteerapp.data.preferences.SessionPreferences
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.remote.dto.GoogleLoginRequest
import com.example.prototypevolunteerapp.data.remote.dto.RegisterOrganizationRequest
import com.example.prototypevolunteerapp.data.remote.dto.RegisterVolunteerRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RegisterRole { VOLUNTEER, ORGANISASI }

data class RegisterUiState(
    val role:             RegisterRole = RegisterRole.VOLUNTEER,
    val name:             String       = "",
    val organizationName: String       = "",
    val email:            String       = "",
    val password:         String       = "",
    val confirmPassword:  String       = "",
    val passwordVisible:  Boolean      = false,
    val confirmVisible:   Boolean      = false,
    val isLoading:        Boolean      = false,
    val errorMessage:     String       = "",
    val isRegistered:     Boolean      = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val accountDataStore: AccountDataStore,
    private val apiService:       ApiService,
    private val sessionPrefs:     SessionPreferences,
    private val userSession:      UserSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onRoleChange(role: RegisterRole) {
        _uiState.update {
            it.copy(
                role             = role,
                errorMessage     = "",
                name             = "",
                organizationName = "",
                email            = "",
                password         = "",
                confirmPassword  = ""
            )
        }
    }

    fun onNameChange(v: String)             { _uiState.update { it.copy(name = v,             errorMessage = "") } }
    fun onOrganizationNameChange(v: String) { _uiState.update { it.copy(organizationName = v, errorMessage = "") } }
    fun onEmailChange(v: String)            { _uiState.update { it.copy(email = v,             errorMessage = "") } }
    fun onPasswordChange(v: String)         { _uiState.update { it.copy(password = v,          errorMessage = "") } }
    fun onConfirmChange(v: String)          { _uiState.update { it.copy(confirmPassword = v,   errorMessage = "") } }
    fun onTogglePassword()                  { _uiState.update { it.copy(passwordVisible = !it.passwordVisible) } }
    fun onToggleConfirm()                   { _uiState.update { it.copy(confirmVisible  = !it.confirmVisible)  } }

    fun onRegister() {
        val s = _uiState.value

        val error = when {
            s.name.isBlank()                                                  -> "Nama tidak boleh kosong"
            s.role == RegisterRole.ORGANISASI && s.organizationName.isBlank() -> "Nama organisasi tidak boleh kosong"
            s.email.isBlank()                                                 -> "Email tidak boleh kosong"
            !s.email.contains("@")                                            -> "Format email tidak valid"
            s.password.length < 8                                             -> "Password minimal 8 karakter"
            s.password != s.confirmPassword                                   -> "Konfirmasi password tidak cocok"
            else                                                              -> ""
        }
        if (error.isNotEmpty()) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = "") }

        viewModelScope.launch {
            try {
                val response = if (s.role == RegisterRole.VOLUNTEER) {
                    apiService.registerVolunteer(
                        RegisterVolunteerRequest(
                            name                  = s.name.trim(),
                            email                 = s.email.trim(),
                            password              = s.password,
                            password_confirmation = s.confirmPassword
                        )
                    )
                } else {
                    apiService.registerOrganization(
                        RegisterOrganizationRequest(
                            name                  = s.name.trim(),
                            email                 = s.email.trim(),
                            password              = s.password,
                            password_confirmation = s.confirmPassword,
                            organization_name     = s.organizationName.trim()
                        )
                    )
                }

                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, isRegistered = true) }
                } else {
                    val errorMsg = when (response.code()) {
                        422  -> "Email sudah terdaftar atau data tidak valid"
                        else -> "Registrasi gagal (${response.code()})"
                    }
                    _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Tidak dapat terhubung ke server") }
            }
        }
    }

    fun onGoogleLogin(idToken: String) {
        viewModelScope.launch {
            try {
                val response = apiService.loginWithGoogle(
                    GoogleLoginRequest(id_token = idToken)
                )

                if (!response.isSuccessful || response.body() == null) {
                    _uiState.update {
                        it.copy(errorMessage = "Register dengan Google gagal. Coba lagi.")
                    }
                    return@launch
                }

                val body = response.body()!!

                sessionPrefs.saveAuthToken(body.token)
                userSession.restoreSession(
                    email     = body.user.email,
                    name      = body.user.name,
                    volunteer = null,
                    avatarUrl = body.user.avatar
                )
                sessionPrefs.saveVolunteerSession(
                    email     = body.user.email,
                    name      = body.user.name,
                    avatarUrl = body.user.avatar
                )

                _uiState.update { it.copy(isRegistered = true) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Tidak dapat terhubung ke server.")
                }
            }
        }
    }

    fun onRegisteredHandled() {
        _uiState.update { it.copy(isRegistered = false) }
    }
}