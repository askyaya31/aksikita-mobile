package com.example.prototypevolunteerapp.ui.screens.organizer

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.core.AppConfig
import com.example.prototypevolunteerapp.core.OrganizerSession
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.remote.dto.EventDto
import com.example.prototypevolunteerapp.data.remote.dto.StoreEventRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class EditActivityFormState(
    val eventId:          Int            = -1,
    val namaKegiatan:     String         = "",
    val lokasi:           String         = "",
    val kota:             String         = "",
    val tanggalMulai:     String         = "",
    val tanggalSelesai:   String         = "",
    val waktuMulai:       String         = "",
    val waktuSelesai:     String         = "",
    val deskripsi:        String         = "",
    val kuota:            String         = "",
    val persyaratan:      String         = "",
    val kontakPerson:     String         = "",
    val kontakPhone:      String         = "",
    val statusFromApi:    String         = "",
    val namaError:        Boolean        = false,
    val lokasiError:      Boolean        = false,
    val kotaError:        Boolean        = false,
    val tanggalMulaiError:Boolean        = false,
    val tanggalSelesaiError: Boolean     = false,
    val deskError:        Boolean        = false,
    val kuotaError:       Boolean        = false,
    val showPreviewSheet:        Boolean = false,
    val showDeleteDialog:        Boolean = false,
    val showStatusSheet:         Boolean = false,
    val isLoading:               Boolean = false,
    val isSubmitting:            Boolean = false,
    val notFound:                Boolean = false,
    val isReadOnly:              Boolean = false,
    val errorMessage:            String? = null,
    val successMessage:          String? = null,
    val posterUri:               Uri?    = null,
    val existingPosterUrl:       String? = null,
    val showSubmitToReviewDialog:Boolean = false,
    val showSubmitSuccessDialog: Boolean = false
)

@HiltViewModel
class EditActivityViewModel @Inject constructor(
    private val apiService:       ApiService,
    private val organizerSession: OrganizerSession,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val hasAccess: Boolean get() = organizerSession.hasAccess()

    private val _formState = MutableStateFlow(EditActivityFormState())
    val formState: StateFlow<EditActivityFormState> = _formState.asStateFlow()
    fun loadEvent(eventId: Int) {
        if (eventId == -1) { _formState.update { it.copy(notFound = true) }; return }
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            try {
                val resp = apiService.getOrgEventDetail(eventId)
                if (!resp.isSuccessful || resp.body() == null) {
                    _formState.update { it.copy(isLoading = false, notFound = true) }
                    return@launch
                }
                val event = resp.body()!!.event
                val canEdit = event.status == "draft" || event.status == "rejected"
                _formState.update { _ ->
                    EditActivityFormState(
                        eventId          = event.id,
                        namaKegiatan     = event.title,
                        lokasi           = event.location_name ?: "",
                        kota             = event.city ?: "",
                        tanggalMulai     = event.start_date ?: "",
                        tanggalSelesai   = event.end_date ?: "",
                        waktuMulai       = event.start_time ?: "",
                        waktuSelesai     = event.end_time ?: "",
                        deskripsi        = event.description ?: "",
                        kuota            = event.quota?.toString() ?: "",
                        persyaratan      = event.requirements ?: "",
                        kontakPerson     = event.contact_person ?: "",
                        kontakPhone      = event.contact_phone ?: "",
                        statusFromApi       = event.status ?: "",
                        existingPosterUrl   = event.poster?.let { buildPosterUrl(it) },
                        isLoading           = false,
                        isReadOnly          = !canEdit
                    )
                }
            } catch (e: Exception) {
                _formState.update { it.copy(isLoading = false, errorMessage = "Gagal memuat: ${e.localizedMessage}") }
            }
        }
    }

    fun onNamaKegiatanChange(v: String)   { _formState.update { it.copy(namaKegiatan = v,   namaError = false) } }
    fun onLokasiChange(v: String)         { _formState.update { it.copy(lokasi = v,         lokasiError = false) } }
    fun onKotaChange(v: String)           { _formState.update { it.copy(kota = v,           kotaError = false) } }
    fun onTanggalMulaiChange(v: String)   { _formState.update { it.copy(tanggalMulai = v,   tanggalMulaiError = false) } }
    fun onTanggalSelesaiChange(v: String) { _formState.update { it.copy(tanggalSelesai = v, tanggalSelesaiError = false) } }
    fun onWaktuMulaiChange(v: String)     { _formState.update { it.copy(waktuMulai = v) } }
    fun onWaktuSelesaiChange(v: String)   { _formState.update { it.copy(waktuSelesai = v) } }
    fun onDeskripsiChange(v: String)      { _formState.update { it.copy(deskripsi = v,      deskError = false) } }
    fun onKuotaChange(v: String)          { _formState.update { it.copy(kuota = v,          kuotaError = false) } }
    fun onPersyaratanChange(v: String)    { _formState.update { it.copy(persyaratan = v) } }
    fun onKontakPersonChange(v: String)   { _formState.update { it.copy(kontakPerson = v) } }
    fun onKontakPhoneChange(v: String)    { _formState.update { it.copy(kontakPhone = v) } }

    fun onPreviewRequested() {
        val s = _formState.value
        val namaError         = s.namaKegiatan.isBlank()
        val lokasiError       = s.lokasi.isBlank()
        val kotaError         = s.kota.isBlank()
        val tanggalMulaiError = s.tanggalMulai.isBlank()
        val tanggalSelesaiError = s.tanggalSelesai.isBlank()
        val deskError         = s.deskripsi.isBlank()
        val kuotaError        = s.kuota.isBlank() || s.kuota.toIntOrNull() == null

        _formState.update {
            it.copy(
                namaError = namaError, lokasiError = lokasiError,
                kotaError = kotaError, tanggalMulaiError = tanggalMulaiError,
                tanggalSelesaiError = tanggalSelesaiError,
                deskError = deskError, kuotaError = kuotaError,
                showPreviewSheet = !namaError && !lokasiError && !kotaError
                        && !tanggalMulaiError && !tanggalSelesaiError
                        && !deskError && !kuotaError
            )
        }
    }

    fun onPreviewDismissed() { _formState.update { it.copy(showPreviewSheet = false) } }
    fun onSaveConfirmed() {
        val s = _formState.value
        viewModelScope.launch {
            _formState.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                val plain = "text/plain".toMediaType()
                fun String.rb() = trim().toRequestBody(plain)
                fun String?.optRb() = takeIf { !it.isNullOrBlank() }?.trim()?.toRequestBody(plain)

                val posterPart: MultipartBody.Part? = s.posterUri?.let { uri ->
                    uriToMultipartPart(uri, "poster")
                }

                val resp = apiService.updateOrgEvent(
                    id            = s.eventId,
                    title         = s.namaKegiatan.rb(),
                    description   = s.deskripsi.rb(),
                    locationName  = s.lokasi.rb(),
                    city          = s.kota.rb(),
                    startDate     = s.tanggalMulai.rb(),
                    endDate       = s.tanggalSelesai.rb(),
                    startTime     = s.waktuMulai.optRb(),
                    endTime       = s.waktuSelesai.optRb(),
                    quota         = s.kuota.trim().toRequestBody(plain),
                    requirements  = s.persyaratan.optRb(),
                    contactPerson = s.kontakPerson.optRb(),
                    contactPhone  = s.kontakPhone.optRb(),
                    poster        = posterPart
                )
                if (resp.isSuccessful) {
                    _formState.update { it.copy(
                        isSubmitting     = false,
                        showPreviewSheet = false,
                        posterUri        = null,
                        existingPosterUrl = resp.body()?.event?.poster?.let { buildPosterUrl(it) }
                            ?: s.existingPosterUrl,
                        successMessage   = "Kegiatan berhasil diperbarui"
                    )}
                } else {
                    val errBody = resp.errorBody()?.string()
                    _formState.update { it.copy(
                        isSubmitting = false,
                        errorMessage = "Gagal menyimpan (${resp.code()}): $errBody"
                    )}
                }
            } catch (e: Exception) {
                _formState.update { it.copy(
                    isSubmitting = false,
                    errorMessage = "Koneksi gagal: ${e.localizedMessage}"
                )}
            }
        }
    }

    fun onPosterSelected(uri: Uri?) { _formState.update { it.copy(posterUri = uri) } }
    private fun uriToMultipartPart(uri: Uri, fieldName: String): MultipartBody.Part? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val mimeType    = context.contentResolver.getType(uri) ?: "image/jpeg"
            val extension   = when (mimeType) {
                "image/png"  -> "png"
                "image/webp" -> "webp"
                else         -> "jpg"
            }
            val tempFile = File(context.cacheDir, "upload_${fieldName}_${System.currentTimeMillis()}.$extension")
            FileOutputStream(tempFile).use { out -> inputStream.copyTo(out) }
            val requestBody = tempFile.asRequestBody(mimeType.toMediaType())
            MultipartBody.Part.createFormData(fieldName, tempFile.name, requestBody)
        } catch (e: Exception) {
            android.util.Log.e("EditActivityVM", "Gagal konversi URI ke Part: ${e.message}")
            null
        }
    }

    private fun buildPosterUrl(path: String): String {
        if (path.startsWith("http")) return path
        val base = AppConfig.BASE_URL.trimEnd('/')
        return "$base/storage/$path"
    }
    fun onSubmitToReviewConfirmed() {
        val eventId = _formState.value.eventId
        viewModelScope.launch {
            _formState.update { it.copy(isSubmitting = true, showSubmitToReviewDialog = false) }
            try {
                val resp = apiService.submitOrgEvent(eventId)
                _formState.update { it.copy(
                    isSubmitting           = false,
                    showSubmitSuccessDialog = resp.isSuccessful,
                    errorMessage           = if (!resp.isSuccessful) "Gagal submit (${resp.code()})" else null
                )}
            } catch (e: Exception) {
                _formState.update { it.copy(
                    isSubmitting = false,
                    errorMessage = "Koneksi gagal: ${e.localizedMessage}"
                )}
            }
        }
    }
    fun onDeleteConfirmed(onSuccess: () -> Unit) {
        val eventId = _formState.value.eventId
        viewModelScope.launch {
            try {
                apiService.deleteOrgEvent(eventId)
                onSuccess()
            } catch (e: Exception) {
                _formState.update { it.copy(errorMessage = "Gagal menghapus: ${e.localizedMessage}") }
            }
        }
    }
    fun onDeleteDialogShow()           { _formState.update { it.copy(showDeleteDialog = true) } }
    fun onDeleteDialogDismiss()        { _formState.update { it.copy(showDeleteDialog = false) } }
    fun onStatusSheetShow()            { _formState.update { it.copy(showStatusSheet = true) } }
    fun onStatusSheetDismiss()         { _formState.update { it.copy(showStatusSheet = false) } }
    fun onSubmitToReviewDialogShow()   { _formState.update { it.copy(showSubmitToReviewDialog = true) } }
    fun onSubmitToReviewDialogDismiss(){ _formState.update { it.copy(showSubmitToReviewDialog = false) } }
    fun onSubmitSuccessDialogDismissed(){ _formState.update { it.copy(showSubmitSuccessDialog = false) } }
    fun onErrorDismissed()             { _formState.update { it.copy(errorMessage = null) } }
    fun onSuccessHandled()             { _formState.update { it.copy(successMessage = null) } }
}

private fun String.trimOrNull(): String? = trim().ifBlank { null }