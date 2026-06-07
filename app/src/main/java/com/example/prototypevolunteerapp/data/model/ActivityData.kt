package com.example.prototypevolunteerapp.data.model

data class ActivityData(
    val id:          String  = "",
    val slug:        String  = "",
    val title:       String,
    val location:    String,
    val description: String,
    val imageRes:    String,
    val instagram:   String? = null,
    val link:        String? = null
)

fun getDummyActivities() = listOf(
    ActivityData(
        id          = "",
        slug        = "",
        title       = "Little Chef Day: Petualangan Rasa dan Cerita di Wizzmie Solo",
        location    = "Wizzmie Solo",
        description = """
Yuk, isi akhir pekanmu dengan kegiatan yang seru dan penuh makna!

Di kegiatan "Little Chef Day" kali ini, kita akan mengikuti cooking class bersama Wizzmie dan adik-adik untuk mengeksplorasi dunia kuliner dan mencicipi berbagai menu lezat.

Hari, Tanggal : Minggu, 12 April 2026
Waktu         : 10.00 – 13.00 WIB
Lokasi        : Wizzmie Solo
""".trimIndent(),
        imageRes    = "social_activity1",
        instagram   = "https://www.instagram.com/p/DWh7HgyCV54/",
        link        = "https://peduly.com/littlechefdaysolo"
    ),
    ActivityData(
        id          = "",
        slug        = "",
        title       = "Abdination Indonesia | Chapter Labuan Bajo",
        location    = "Labuan Bajo, NTT",
        description = """
OPEN VOLUNTEER GELOMBANG 2 ABDINATION MENGABDI #3

Abdination Indonesia mengajak kamu untuk mengabdi, belajar, dan menjelajahi keindahan Labuan Bajo.

Timeline:
- Pendaftaran: 5 – 30 April 2026
- Pelaksanaan: 11 – 22 Agustus 2026

Lokasi: Pulau Longos, Manggarai Barat – Labuan Bajo, NTT
""".trimIndent(),
        imageRes    = "social_activity2",
        instagram   = "https://www.instagram.com/p/DWv13w-D2NX/",
        link        = "https://linktr.ee/AbdinationIndonesiaMengabdi3"
    ),
    ActivityData(
        id          = "",
        slug        = "",
        title       = "From Heart to Paper: A Day of Kind Words",
        location    = "KGJ Ciliwung, Jakarta Timur",
        description = """
Karena kata-kata baik bisa lahir dari hati yang tulus.

Melalui kegiatan ini, kamu akan belajar menulis dengan makna, mengekspresikan perasaan, dan menyebarkan kebaikan.

Hari, Tanggal : Minggu, 12 April 2026
Waktu         : 10.00 – 13.00 WIB
Lokasi        : KGJ Ciliwung, Jakarta Timur
""".trimIndent(),
        imageRes    = "social_activity3",
        instagram   = "https://www.instagram.com/p/DW0XLLtklJF/",
        link        = "https://forms.gle/G6W3YQk3yJ3DgdaP8"
    )
)