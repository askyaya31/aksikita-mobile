package com.example.prototypevolunteerapp.data.model

interface IActivitySubmissionRepository {
    val submissions: List<ActivitySubmission>

    fun addSubmission(
        namaKegiatan: String,
        lokasi:       String,
        tanggal:      String,
        deskripsi:    String,
        organizer:    String
    )
    fun approve(id: Int)
    fun reject(id: Int)
    fun edit(
        id:           Int,
        namaKegiatan: String,
        lokasi:       String,
        tanggal:      String,
        deskripsi:    String
    )
    fun delete(id: Int)
    fun updateActivityStatus(id: Int, activityStatus: ActivityStatus)
}