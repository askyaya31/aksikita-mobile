package com.example.prototypevolunteerapp.data.model

import androidx.compose.runtime.mutableStateListOf
import com.example.prototypevolunteerapp.R
import javax.inject.Inject
import javax.inject.Singleton

enum class CandidateStatus { PENDING, DITERIMA, DITOLAK }

data class Candidate(
    val id:            Int,
    val name:          String,
    val email:         String,
    val phone:         String,
    val education:     String,
    val skills:        List<String>,
    val motivation:    String,
    val activityTitle: String,
    val imageRes:      Int,
    val status:        CandidateStatus = CandidateStatus.PENDING
)
@Singleton
class CandidateRepository @Inject constructor() : ICandidateRepository {
    override val candidates = mutableStateListOf(
        Candidate(
            id            = 1,
            name          = "Budi Santoso",
            email         = "budi.santoso@gmail.com",
            phone         = "6281234567890",
            education     = "S-1 Ilmu Komunikasi, UNS",
            skills        = listOf("Public Speaking", "Event Organizing", "Social Media"),
            motivation    = "Saya ingin berkontribusi untuk komunitas dan mendapatkan pengalaman baru dalam kegiatan sosial yang bermakna.",
            activityTitle = "Little Chef Day: Petualangan Rasa dan Cerita di Wizzmie Solo",
            imageRes      = R.drawable.default_profile
        ),
        Candidate(
            id            = 2,
            name          = "Dewi Rahmawati",
            email         = "dewi.rahma@gmail.com",
            phone         = "6289876543210",
            education     = "S-1 Pendidikan, UNY",
            skills        = listOf("Teaching", "Storytelling", "Kreatif"),
            motivation    = "Sebagai calon guru, saya ingin melatih kemampuan mengajar sambil memberikan dampak positif bagi anak-anak.",
            activityTitle = "Little Chef Day: Petualangan Rasa dan Cerita di Wizzmie Solo",
            imageRes      = R.drawable.default_profile
        ),
        Candidate(
            id            = 3,
            name          = "Andi Firmansyah",
            email         = "andi.firm@gmail.com",
            phone         = "6285551234567",
            education     = "S-1 Biologi, UGM",
            skills        = listOf("Lingkungan", "Penelitian", "Dokumentasi"),
            motivation    = "Keindahan alam Labuan Bajo sangat menarik bagi saya, dan saya ingin berkontribusi dalam divisi lingkungan.",
            activityTitle = "Abdination Indonesia | Chapter Labuan Bajo",
            imageRes      = R.drawable.default_profile
        ),
        Candidate(
            id            = 4,
            name          = "Siti Nuraini",
            email         = "siti.nura@gmail.com",
            phone         = "6287778889990",
            education     = "S-1 Keperawatan, UNDIP",
            skills        = listOf("Kesehatan", "First Aid", "Empati"),
            motivation    = "Dengan latar belakang keperawatan, saya ingin menjadi relawan di divisi kesehatan untuk membantu masyarakat terpencil.",
            activityTitle = "Abdination Indonesia | Chapter Labuan Bajo",
            imageRes      = R.drawable.default_profile
        ),
        Candidate(
            id            = 5,
            name          = "Rizky Pratama",
            email         = "rizky.prat@gmail.com",
            phone         = "6282211223344",
            education     = "S-1 Sastra Indonesia, UNS",
            skills        = listOf("Menulis", "Kreativitas", "Komunikasi"),
            motivation    = "Kegiatan menulis surat ini sangat relevan dengan passion saya. Saya ingin mengajarkan cara mengekspresikan perasaan melalui tulisan.",
            activityTitle = "From Heart to Paper: A Day of Kind Words",
            imageRes      = R.drawable.default_profile
        ),
        Candidate(
            id            = 6,
            name          = "Maya Kusuma",
            email         = "maya.kus@gmail.com",
            phone         = "6281399887766",
            education     = "S-1 Psikologi, UGM",
            skills        = listOf("Konseling", "Active Listening", "Empati"),
            motivation    = "Saya percaya kata-kata baik bisa menyembuhkan. Kegiatan ini sejalan dengan studi psikologi saya.",
            activityTitle = "From Heart to Paper: A Day of Kind Words",
            imageRes      = R.drawable.default_profile
        )
    )
    override fun updateStatus(candidateId: Int, newStatus: CandidateStatus) {
        val index = candidates.indexOfFirst { it.id == candidateId }
        if (index >= 0) candidates[index] = candidates[index].copy(status = newStatus)
    }

    override fun getCandidatesByActivity(activityTitle: String): List<Candidate> =
        candidates.filter { it.activityTitle == activityTitle }
}

enum class ActivitySubmissionStatus { MENUNGGU_VERIFIKASI, DISETUJUI, DITOLAK }

enum class ActivityStatus { AKTIF, SELESAI, DIBATALKAN }

data class ActivitySubmission(
    val id:             Int,
    val namaKegiatan:   String,
    val lokasi:         String,
    val tanggal:        String,
    val deskripsi:      String,
    val organizer:      String,
    val status:         ActivitySubmissionStatus = ActivitySubmissionStatus.MENUNGGU_VERIFIKASI,
    val activityStatus: ActivityStatus           = ActivityStatus.AKTIF
)

data class OrgPortfolioItem(
    val title:       String,
    val year:        String,
    val description: String
)

data class OrgProfile(
    val name:        String,
    val email:       String,
    val description: String              = "",
    val instagram:   String              = "",
    val website:     String              = "",
    val portfolio:   List<OrgPortfolioItem> = emptyList()
)

@Singleton
class OrgProfileRepository @Inject constructor(){
    private val profiles = mutableMapOf<String, OrgProfile>()
    fun getProfile(email: String): OrgProfile? = profiles[email]
    fun saveProfile(email: String, profile: OrgProfile) {
        profiles[email] = profile
    }
}

@Singleton
class ActivitySubmissionRepository @Inject constructor() : IActivitySubmissionRepository {
    override val submissions = mutableStateListOf<ActivitySubmission>()

    private var nextId = 100
    override fun addSubmission(
        namaKegiatan: String,
        lokasi:       String,
        tanggal:      String,
        deskripsi:    String,
        organizer:    String
    ) {
        submissions.add(
            ActivitySubmission(
                id           = nextId++,
                namaKegiatan = namaKegiatan,
                lokasi       = lokasi,
                tanggal      = tanggal,
                deskripsi    = deskripsi,
                organizer    = organizer
            )
        )
    }

    override fun approve(id: Int) {
        val i = submissions.indexOfFirst { it.id == id }
        if (i >= 0) submissions[i] = submissions[i].copy(status = ActivitySubmissionStatus.DISETUJUI)
    }

    override fun reject(id: Int) {
        val i = submissions.indexOfFirst { it.id == id }
        if (i >= 0) submissions[i] = submissions[i].copy(status = ActivitySubmissionStatus.DITOLAK)
    }
    override fun edit(
        id:           Int,
        namaKegiatan: String,
        lokasi:       String,
        tanggal:      String,
        deskripsi:    String
    ) {
        val i = submissions.indexOfFirst { it.id == id }
        if (i >= 0) submissions[i] = submissions[i].copy(
            namaKegiatan = namaKegiatan,
            lokasi       = lokasi,
            tanggal      = tanggal,
            deskripsi    = deskripsi
        )
    }
    override fun delete(id: Int) {
        val i = submissions.indexOfFirst { it.id == id }
        if (i >= 0) submissions.removeAt(i)
    }
    override fun updateActivityStatus(id: Int, activityStatus: ActivityStatus) {
        val i = submissions.indexOfFirst { it.id == id }
        if (i >= 0) submissions[i] = submissions[i].copy(activityStatus = activityStatus)
    }
}