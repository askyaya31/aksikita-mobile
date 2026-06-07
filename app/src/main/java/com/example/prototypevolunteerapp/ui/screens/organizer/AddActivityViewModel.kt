package com.example.prototypevolunteerapp.ui.screens.organizer

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.core.OrganizerSession
import com.example.prototypevolunteerapp.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import android.content.Context
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import com.example.prototypevolunteerapp.data.remote.dto.CategoryDto
import dagger.hilt.android.qualifiers.ApplicationContext

data class AddActivityFormState(
    val namaKegiatan:        String  = "",
    val lokasi:              String  = "",
    val kota:                String  = "",
    val provinsi:            String  = "",
    val tanggalMulai:        String  = "",
    val tanggalSelesai:      String  = "",
    val waktuMulai:          String  = "",
    val waktuSelesai:        String  = "",
    val deskripsi:           String  = "",
    val kuota:               String  = "",
    val persyaratan:         String  = "",
    val kontakPerson:        String  = "",
    val kontakPhone:         String  = "",
    val posterUri:           Uri?    = null,
    val categories:           List<CategoryDto> = emptyList(),
    val selectedCategoryIds:  Set<Int>          = emptySet(),
    val namaError:           Boolean = false,
    val lokasiError:         Boolean = false,
    val kotaError:           Boolean = false,
    val provinsiError:       Boolean = false,
    val tanggalMulaiError:   Boolean = false,
    val tanggalSelesaiError: Boolean = false,
    val deskError:           Boolean = false,
    val kuotaError:          Boolean = false,
    val showPreviewSheet:        Boolean = false,
    val isSubmitting:            Boolean = false,
    val showSuccessDialog:       Boolean = false,
    val showSubmitSuccessDialog: Boolean = false,
    val errorMessage:            String? = null,
    val createdEventId:          Int?    = null
)

@HiltViewModel
class AddActivityViewModel @Inject constructor(
    private val apiService:       ApiService,
    private val organizerSession: OrganizerSession,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val currentOrg get() = organizerSession.currentOrg
    val hasAccess  get() = organizerSession.hasAccess()

    private val _formState = MutableStateFlow(AddActivityFormState())
    val formState: StateFlow<AddActivityFormState> = _formState.asStateFlow()

    fun onNamaKegiatanChange(v: String)   { _formState.value = _formState.value.copy(namaKegiatan = v,   namaError = false) }
    fun onLokasiChange(v: String)         { _formState.value = _formState.value.copy(lokasi = v,         lokasiError = false) }
    fun onKotaChange(v: String)           { _formState.value = _formState.value.copy(kota = v,           kotaError = false) }
    fun onProvinsiChange(v: String)       { _formState.value = _formState.value.copy(provinsi = v,       provinsiError = false) }
    fun onTanggalMulaiChange(v: String)   { _formState.value = _formState.value.copy(tanggalMulai = v,   tanggalMulaiError = false) }
    fun onTanggalSelesaiChange(v: String) { _formState.value = _formState.value.copy(tanggalSelesai = v, tanggalSelesaiError = false) }
    fun onWaktuMulaiChange(v: String)     { _formState.value = _formState.value.copy(waktuMulai = v) }
    fun onWaktuSelesaiChange(v: String)   { _formState.value = _formState.value.copy(waktuSelesai = v) }
    fun onDeskripsiChange(v: String)      { _formState.value = _formState.value.copy(deskripsi = v,      deskError = false) }
    fun onKuotaChange(v: String)          { _formState.value = _formState.value.copy(kuota = v,          kuotaError = false) }
    fun onPersyaratanChange(v: String)    { _formState.value = _formState.value.copy(persyaratan = v) }
    fun onKontakPersonChange(v: String)   { _formState.value = _formState.value.copy(kontakPerson = v) }
    fun onKontakPhoneChange(v: String)    { _formState.value = _formState.value.copy(kontakPhone = v) }

    fun onPosterSelected(uri: Uri?) {
        _formState.value = _formState.value.copy(posterUri = uri)
    }

    fun onCategoryToggled(id: Int) {
        val current = _formState.value.selectedCategoryIds.toMutableSet()
        if (current.contains(id)) current.remove(id) else if (current.size < 5) current.add(id)
        _formState.value = _formState.value.copy(selectedCategoryIds = current)
    }

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val resp = apiService.getCategories()
                if (resp.isSuccessful) {
                    _formState.value = _formState.value.copy(
                        categories = resp.body()?.categories ?: emptyList()
                    )
                }
            } catch (e: Exception) {
                android.util.Log.w("AddActivityVM", "Gagal load kategori: ${e.message}")
            }
        }
    }

    fun onPreviewRequested(): Boolean {
        val s = _formState.value
        val namaError           = s.namaKegiatan.isBlank()
        val lokasiError         = s.lokasi.isBlank()
        val kotaError           = s.kota.isBlank()
        val provinsiError       = s.provinsi.isBlank()
        val tanggalMulaiError   = s.tanggalMulai.isBlank()
        val tanggalSelesaiError = s.tanggalSelesai.isBlank()
        val deskError           = s.deskripsi.isBlank()
        val kuotaError          = s.kuota.isBlank() || s.kuota.toIntOrNull() == null

        _formState.value = s.copy(
            namaError           = namaError,
            lokasiError         = lokasiError,
            kotaError           = kotaError,
            provinsiError       = provinsiError,
            tanggalMulaiError   = tanggalMulaiError,
            tanggalSelesaiError = tanggalSelesaiError,
            deskError           = deskError,
            kuotaError          = kuotaError,
            showPreviewSheet    = !namaError && !lokasiError && !kotaError && !provinsiError
                    && !tanggalMulaiError && !tanggalSelesaiError
                    && !deskError && !kuotaError
        )
        return _formState.value.showPreviewSheet
    }

    fun onPreviewDismissed() {
        _formState.value = _formState.value.copy(showPreviewSheet = false)
    }

    fun onSubmitConfirmed() {
        val s = _formState.value
        viewModelScope.launch {
            _formState.value = s.copy(isSubmitting = true, errorMessage = null)
            try {
                val plain = "text/plain".toMediaType()
                val posterPart: MultipartBody.Part? = s.posterUri?.let { uri ->
                    runCatching {
                        val stream  = context.contentResolver.openInputStream(uri)!!
                        val bytes   = stream.readBytes()
                        stream.close()
                        val reqBody = bytes.toRequestBody("image/*".toMediaType())
                        MultipartBody.Part.createFormData("poster", "poster.jpg", reqBody)
                    }.getOrNull()
                }

                val resp = apiService.storeOrgEvent(
                    title          = s.namaKegiatan.trim().toRequestBody(plain),
                    description    = s.deskripsi.trim().toRequestBody(plain),
                    locationName   = s.lokasi.trim().toRequestBody(plain),
                    city           = s.kota.trim().toRequestBody(plain),
                    province       = s.provinsi.trim().toRequestBody(plain),
                    startDate      = s.tanggalMulai.trim().toRequestBody(plain),
                    endDate        = s.tanggalSelesai.trim().toRequestBody(plain),
                    startTime      = s.waktuMulai.trimOrNull()?.toRequestBody(plain),
                    endTime        = s.waktuSelesai.trimOrNull()?.toRequestBody(plain),
                    quota          = s.kuota.trim().toRequestBody(plain),
                    requirements   = s.persyaratan.trimOrNull()?.toRequestBody(plain),
                    contactPerson  = s.kontakPerson.trimOrNull()?.toRequestBody(plain),
                    contactPhone   = s.kontakPhone.trimOrNull()?.toRequestBody(plain),
                    categoryId0    = s.selectedCategoryIds.elementAtOrNull(0)?.toString()?.toRequestBody(plain),
                    categoryId1    = s.selectedCategoryIds.elementAtOrNull(1)?.toString()?.toRequestBody(plain),
                    categoryId2    = s.selectedCategoryIds.elementAtOrNull(2)?.toString()?.toRequestBody(plain),
                    categoryId3    = s.selectedCategoryIds.elementAtOrNull(3)?.toString()?.toRequestBody(plain),
                    categoryId4    = s.selectedCategoryIds.elementAtOrNull(4)?.toString()?.toRequestBody(plain),
                    poster         = posterPart
                )

                if (resp.isSuccessful) {
                    val eventId = resp.body()?.event?.id
                    _formState.value = _formState.value.copy(
                        isSubmitting      = false,
                        showPreviewSheet  = false,
                        showSuccessDialog = true,
                        createdEventId    = eventId
                    )
                } else {
                    val errBody = resp.errorBody()?.string()
                    _formState.value = _formState.value.copy(
                        isSubmitting = false,
                        errorMessage = "Gagal menyimpan (${resp.code()}): $errBody"
                    )
                }
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    isSubmitting = false,
                    errorMessage = "Koneksi gagal: ${e.localizedMessage}"
                )
            }
        }
    }

    fun onSubmitToReview() {
        val eventId = _formState.value.createdEventId ?: return
        viewModelScope.launch {
            _formState.value = _formState.value.copy(isSubmitting = true)
            try {
                val resp = apiService.submitOrgEvent(eventId)
                _formState.value = _formState.value.copy(
                    isSubmitting            = false,
                    showSuccessDialog       = false,
                    showSubmitSuccessDialog = resp.isSuccessful
                )
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    isSubmitting = false,
                    errorMessage = "Gagal submit ke review: ${e.localizedMessage}"
                )
            }
        }
    }

    fun onSuccessDialogDismissed()       { _formState.value = _formState.value.copy(showSuccessDialog = false) }
    fun onSubmitSuccessDialogDismissed() { _formState.value = _formState.value.copy(showSubmitSuccessDialog = false) }
    fun onErrorDismissed()               { _formState.value = _formState.value.copy(errorMessage = null) }
}

private fun String.trimOrNull(): String? = trim().ifBlank { null }