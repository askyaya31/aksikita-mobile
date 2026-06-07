package com.example.prototypevolunteerapp.data.model

import com.example.prototypevolunteerapp.R
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

data class Volunteer(
    val name:        String,
    val birthPlace:  String,
    val birthDate:   String,
    val education:   String,
    val skills:      List<String>,
    val interests:   List<String>,
    val about:       String,
    val experiences: List<VolunteerExp>,
    val imageRes:    Int,
    val phone:       String? = null,
    val email:       String? = null,
)

data class VolunteerExp(
    val title: String,
    val role:  String,
    val year:  String
)

fun getDummyVolunteers() = listOf(
    Volunteer(
        name       = "Saskya Aliya Azizah",
        birthPlace = "Purworejo",
        birthDate  = "31 Januari 2006",
        education  = "Mahasiswa S-1 Informatika, UNS",
        skills     = listOf(
            "Machine Learning", "Data Analytics",
            "Web Development (HTML, CSS, JS)", "Cloud Computing",
            "Public Speaking", "Team Collaboration",
            "Time Management", "Creative Problem Solving"
        ),
        interests  = listOf(
            "Kegiatan sosial", "Pendidikan anak",
            "Lingkungan", "Pengembangan komunitas"
        ),
        about      = "Saya adalah seorang relawan yang memiliki semangat tinggi dalam berkontribusi untuk masyarakat. Saya percaya bahwa perubahan besar dimulai dari langkah kecil, dan melalui kegiatan sosial saya ingin memberikan dampak positif bagi lingkungan sekitar.",
        experiences = listOf(
            VolunteerExp("Aksi Bersih Pantai Parangtritis", "Relawan",  "2025"),
            VolunteerExp("Donor Darah Bersama PMI Yogyakarta", "Volunteer", "2025"),
            VolunteerExp("Penanaman Pohon di Sleman", "Kegiatan", "2024"),
            VolunteerExp("Little Chef Day", "Event", "2026"),
        ),
        imageRes   = R.drawable.volunteer1,
        phone      = "6285183163106",
        email      = "saskyaaliyaazizah06@gmail.com",
    ),
    Volunteer(
        name       = "Mumtazah Nur Hidayati",
        birthPlace = "Klaten",
        birthDate  = "20 Agustus 2006",
        education  = "Mahasiswa S-1 Informatika, UNS",
        skills     = listOf(
            "Time Management", "Creative Thinking", "Teamwork",
            "Communication Skill", "Problem Solving"
        ),
        interests  = listOf(
            "Pemberdayaan perempuan", "Kesehatan mental",
            "Literasi digital", "Bantuan kemanusiaan"
        ),
        about      = "Saya adalah pribadi yang memiliki empati tinggi dan dedikasi untuk membantu sesama. Saya sangat tertarik pada isu-isu sosial dan percaya bahwa kolaborasi antar individu dapat menciptakan solusi yang bermakna bagi permasalahan di masyarakat.",
        experiences = listOf(
            VolunteerExp("Sahabat Anak Jalanan", "Relawan",  "2025"),
            VolunteerExp("Emergency Response Team", "Volunteer", "2025"),
            VolunteerExp("Klaten Membaca", "Kegiatan", "2024"),
            VolunteerExp("Health Awareness Week", "Event", "2026"),
        ),
        imageRes   = R.drawable.volunteer2,
        phone      = "6285743192088",
        email      = "mumtatazah@gmail.com",
    ),
    Volunteer(
        name       = "Sanny Tazkiyah Fastabiqul Husna",
        birthPlace = "Boyolali",
        birthDate  = "09 Maret 2007",
        education  = "Mahasiswa S-1 Informatika, UNS",
        skills     = listOf(
            "Analisis Logika", "Pemrograman (C, C++, Java)", "Struktur Data",
            "Dasar Data Science", "Leadership", "UI/UX Design", "Social Media Management"
        ),
        interests  = listOf(
            "Statistika", "Pengembangan game interaktif",
            "Isu lingkungan", "Edukasi", "Eksplorasi alam"
        ),
        about      = "Saya adalah mahasiswa Informatika yang percaya bahwa teknologi terbaik lahir dari logika yang kuat dan empati yang dalam. Fokus saya adalah mengubah kompleksitas algoritma menjadi solusi yang berdampak nyata bagi masyarakat, baik melalui baris kode maupun aksi sosial di lapangan.",
        experiences = listOf(
            VolunteerExp("Manajemen Logistik & Penyaluran Donasi Sekolah", "Relawan",  "2023"),
            VolunteerExp("Proyek Instalasi Hidroponik & Optimalisasi Ruang Hijau", "Kegiatan", "2022"),
        ),
        imageRes   = R.drawable.volunteer3,
        phone      = "6285800379000",
        email      = "sannytazkiyah@gmail.com",
    ),
    Volunteer(
        name       = "Queen Nika Prahara Mutiara Phasya",
        birthPlace = "Ngawi",
        birthDate  = "04 Oktober 2006",
        education  = "Mahasiswa S-1 Informatika, UNS",
        skills     = listOf(
            "Leadership", "UI/UX Design", "Problem Solving",
            "Teamwork", "Communication Skill", "Public Speaking"
        ),
        interests  = listOf(
            "Teknologi informasi", "Inovasi pendidikan",
            "Pengembangan komunitas digital",
            "Pemberdayaan perempuan dalam teknologi"
        ),
        about      = "Saya adalah mahasiswa Informatika yang berdedikasi untuk memanfaatkan teknologi sebagai alat transformasi sosial. Saya percaya bahwa inovasi teknologi harus diiringi dengan empati dan kesadaran akan dampaknya bagi masyarakat. Fokus saya adalah menciptakan solusi digital yang inklusif dan berkelanjutan.",
        experiences = listOf(
            VolunteerExp("Mentor Program Coding untuk Pelajar", "Mentor",   "2023"),
            VolunteerExp("Kampanye Lingkungan Digital", "Relawan", "2022"),
        ),
        imageRes   = R.drawable.volunteer4,
        phone      = "6282139452707",
        email      = "queennikamutiara@gmail.com",
    )
)

private val Context.dataStore by preferencesDataStore(name = "volunteer_profile")

@Singleton
class VolunteerDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_PROFILE_JSON = stringPreferencesKey("profile_json")
    }

    val profileFlow: Flow<Map<String, String>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_PROFILE_JSON] ?: return@map emptyMap()
        parseProfileJson(json)
    }
    suspend fun saveProfile(
        name:       String,
        email:      String,
        phone:      String,
        about:      String,
        birthPlace: String,
        birthDate:  String,
        education:  String
    ) {
        val json = JSONObject().apply {
            put("name",       name)
            put("email",      email)
            put("phone",      phone)
            put("about",      about)
            put("birthPlace", birthPlace)
            put("birthDate",  birthDate)
            put("education",  education)
        }.toString()

        context.dataStore.edit { prefs ->
            prefs[KEY_PROFILE_JSON] = json
        }
    }

    suspend fun clearProfile() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_PROFILE_JSON)
        }
    }

    private fun parseProfileJson(json: String): Map<String, String> = try {
        val obj = JSONObject(json)
        mapOf(
            "name"       to (obj.optString("name",       "")),
            "email"      to (obj.optString("email",      "")),
            "phone"      to (obj.optString("phone",      "")),
            "about"      to (obj.optString("about",      "")),
            "birthPlace" to (obj.optString("birthPlace", "")),
            "birthDate"  to (obj.optString("birthDate",  "")),
            "education"  to (obj.optString("education",  ""))
        )
    } catch (e: Exception) {
        emptyMap()
    }
}
