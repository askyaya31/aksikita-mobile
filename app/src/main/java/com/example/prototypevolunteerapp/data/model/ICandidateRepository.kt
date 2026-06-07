package com.example.prototypevolunteerapp.data.model

interface ICandidateRepository {
    val candidates: List<Candidate>
    fun getCandidatesByActivity(activityTitle: String): List<Candidate>
    fun updateStatus(candidateId: Int, newStatus: CandidateStatus)
}