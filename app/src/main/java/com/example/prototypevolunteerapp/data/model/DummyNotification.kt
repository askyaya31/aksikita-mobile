package com.example.prototypevolunteerapp.data.model

import com.example.prototypevolunteerapp.R

fun getDummyNotifications(): List<NotificationItems> = listOf(
    NotificationItems(
        id = 2,
        organizationName = "Relawan Solo Raya",
        organizationLogoRes = R.drawable.peduly,
        title = "Pendaftaran Kamu Diterima!",
        message = "Selamat! Kamu terpilih menjadi relawan di program 'Aksi Kita Mengajar'. Mohon segera cek grup koordinasi untuk jadwal briefing.",
        timestamp = "10 menit yang lalu",
        isRead = false
    ),
    NotificationItems(
        id = 3,
        organizationName = "Dapur Umum Surakarta",
        organizationLogoRes = R.drawable.default_profile,
        title = "Kebutuhan Mendesak!",
        message = "Dibutuhkan 5 orang relawan tambahan untuk distribusi makanan di posko banjir Jebres sore ini. Klik untuk mendaftar cepat.",
        timestamp = "1 jam yang lalu",
        isRead = false
    ),
    NotificationItems(
        id = 4,
        organizationName = "Relawan Solo Raya",
        organizationLogoRes = R.drawable.peduly,
        title = "Peringatan: Cuaca Buruk",
        message = "Info Lapangan: Mengingat hujan deras di lokasi penanaman bibit, kegiatan hari ini diundur ke pukul 13.00 WIB demi keselamatan.",
        timestamp = "3 jam yang lalu",
        isRead = false
    ),
    NotificationItems(
        id = 5,
        organizationName = "AksiKita",
        organizationLogoRes = R.drawable.logo3,
        title = "Lengkapi Profil Kamu",
        message = "Profil yang lengkap membantu organisasi mengenalmu lebih baik. Tambahkan minat dan keahlianmu sekarang!",
        timestamp = "5 jam yang lalu",
        isRead = false
    ),
    NotificationItems(
        id = 6,
        organizationName = "Earth Hour Solo",
        organizationLogoRes = R.drawable.peduly,
        title = "E-Sertifikat Sudah Terbit",
        message = "Terima kasih sudah berkontribusi! Sertifikat kehadiran kamu untuk acara 'Global Climate Strike' kemarin sudah bisa diunduh.",
        timestamp = "Kemarin",
        isRead = false
    ),
    NotificationItems(
        id = 7,
        organizationName = "Palang Merah Indonesia",
        organizationLogoRes = R.drawable.pmi,
        title = "Update Stok Darah O",
        message = "Stok darah O saat ini sedang menipis di PMI Solo. Jika kamu bersedia melakukan donor, silakan kunjungi unit terdekat.",
        timestamp = "2 hari yang lalu",
        isRead = false
    ),
    NotificationItems(
        id = 8,
        organizationName = "Solo Mengajar",
        organizationLogoRes = R.drawable.peduly,
        title = "Undangan Briefing",
        message = "Jangan lupa hadir di pertemuan daring persiapan Kelas Inspirasi pada Sabtu malam pukul 19.00 WIB.",
        timestamp = "3 hari yang lalu",
        isRead = false
    ),
    NotificationItems(
        id = 9,
        organizationName = "AksiKita",
        organizationLogoRes = R.drawable.logo3,
        title = "Tips Relawan Pemula",
        message = "Cek artikel terbaru kami tentang cara menjaga manajemen waktu saat menjadi relawan aktif di sela-sela kesibukan kuliah.",
        timestamp = "4 hari yang lalu",
        isRead = false
    ),
    NotificationItems(
        id = 10,
        organizationName = "Green Solo",
        organizationLogoRes = R.drawable.green,
        title = "Ayo Bergabung Kembali!",
        message = "Sudah lama tidak melihatmu di lapangan. Ada 3 kegiatan baru di sekitarmu yang membutuhkan tenaga relawan lingkungan.",
        timestamp = "1 minggu yang lalu",
        isRead = false
    ),
    NotificationItems(
        id = 1,
        organizationName = "AksiKita",
        organizationLogoRes = R.drawable.logo3,
        title = "Selamat Datang di AksiKita!",
        message = "Halo Relawan! Terima kasih telah bergabung. Mari mulai langkah kecilmu untuk memberikan dampak besar bagi sesama melalui berbagai aksi sosial di sini.",
        timestamp = "2 minggu yang lalu",
        isRead = true
    )
)