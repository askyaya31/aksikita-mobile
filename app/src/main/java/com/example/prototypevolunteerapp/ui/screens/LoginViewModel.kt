package com.example.prototypevolunteerapp.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.core.OrganizerSession
import com.example.prototypevolunteerapp.core.UserSession
import com.example.prototypevolunteerapp.data.model.NotificationRepository
import com.example.prototypevolunteerapp.data.preferences.SessionPreferences
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.remote.dto.GoogleLoginRequest
import com.example.prototypevolunteerapp.data.remote.dto.LoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val role:            LoginRole = LoginRole.VOLUNTEER,
    val email:           String    = "",
    val password:        String    = "",
    val passwordVisible: Boolean   = false,
    val loginError:      String    = "",
    val isLoggedIn:      Boolean   = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    application:                  Application,
    private val apiService:       ApiService,
    private val sessionPrefs:     SessionPreferences,
    private val userSession:      UserSession,
    private val organizerSession: OrganizerSession,
    private val notifRepository:  NotificationRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onRoleChange(role: LoginRole) {
        _uiState.update { it.copy(role = role, email = "", password = "", loginError = "") }
    }
    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, loginError = "") }
    }
    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, loginError = "") }
    }
    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun onLogin() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(loginError = "Email dan password tidak boleh kosong.") }
            return
        }

        viewModelScope.launch {
            try {
                val response = apiService.login(
                    LoginRequest(
                        email    = state.email.trim(),
                        password = state.password.trim()
                    )
                )

                if (!response.isSuccessful || response.body() == null) {
                    val errMsg = when (response.code()) {
                        401  -> "Email atau password salah."
                        403  -> "Akun Anda telah dinonaktifkan."
                        else -> "Login gagal (${response.code()}). Coba lagi."
                    }
                    _uiState.update { it.copy(loginError = errMsg) }
                    return@launch
                }

                val body = response.body()!!
                val expectedRole = if (state.role == LoginRole.VOLUNTEER)
                    SessionPreferences.ROLE_VOLUNTEER
                else
                    SessionPreferences.ROLE_ORGANIZER

                if (body.user.role != expectedRole) {
                    _uiState.update {
                        it.copy(loginError = "Akun ini bukan akun ${expectedRole.replaceFirstChar { c -> c.uppercase() }}.")
                    }
                    return@launch
                }

                sessionPrefs.saveAuthToken(body.token)

                if (body.user.role == SessionPreferences.ROLE_VOLUNTEER) {
                    val avatarUrl = body.user.avatar
                    userSession.restoreSession(
                        email     = body.user.email,
                        name      = body.user.name,
                        volunteer = null,
                        avatarUrl = avatarUrl,
                        volunteerProfileDto = body.user.volunteer_profile
                    )
                    sessionPrefs.saveVolunteerSession(
                        email     = body.user.email,
                        name      = body.user.name,
                        avatarUrl = avatarUrl
                    )
                    notifRepository.initForUser(isDummyAccount = false)
                } else {
                    organizerSession.restoreSession(
                        email   = body.user.email,
                        name    = body.user.name,
                        logoUrl = body.user.avatar
                    )
                    sessionPrefs.saveOrganizerSession(
                        email   = body.user.email,
                        name    = body.user.name,
                        logoUrl = body.user.avatar
                    )
                    notifRepository.clear()
                }

                _uiState.update { it.copy(isLoggedIn = true, loginError = "") }

            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Koneksi ke server gagal: ${e.message}")
                _uiState.update {
                    it.copy(loginError = "Tidak dapat terhubung ke server. Periksa koneksi internet atau konfigurasi BASE_URL.")
                }
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
                    val errMsg = when (response.code()) {
                        401  -> "Akun Google tidak valid atau sudah kadaluarsa."
                        403  -> "Akun Anda telah dinonaktifkan."
                        else -> "Login Google gagal (${response.code()}). Coba lagi."
                    }
                    _uiState.update { it.copy(loginError = errMsg) }
                    return@launch
                }

                val body = response.body()!!

                sessionPrefs.saveAuthToken(body.token)

                if (body.user.role == SessionPreferences.ROLE_VOLUNTEER) {
                    userSession.restoreSession(
                        email               = body.user.email,
                        name                = body.user.name,
                        volunteer           = null,
                        avatarUrl           = body.user.avatar,
                        volunteerProfileDto = body.user.volunteer_profile
                    )
                    sessionPrefs.saveVolunteerSession(
                        email     = body.user.email,
                        name      = body.user.name,
                        avatarUrl = body.user.avatar
                    )
                    notifRepository.initForUser(isDummyAccount = false)
                } else {
                    organizerSession.restoreSession(
                        email   = body.user.email,
                        name    = body.user.name,
                        logoUrl = body.user.avatar
                    )
                    sessionPrefs.saveOrganizerSession(
                        email   = body.user.email,
                        name    = body.user.name,
                        logoUrl = body.user.avatar
                    )
                    notifRepository.clear()
                }

                _uiState.update { it.copy(isLoggedIn = true, loginError = "") }

            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Google login error: ${e.message}")
                _uiState.update {
                    it.copy(loginError = "Tidak dapat terhubung ke server.")
                }
            }
        }
    }

    fun onLoginHandled() {
        _uiState.update { it.copy(isLoggedIn = false) }
    }
}