package com.example.prototypevolunteerapp.data.model

import com.example.prototypevolunteerapp.data.remote.dto.ActivityDto

fun ActivityDto.toActivityData(): ActivityData = ActivityData(
    id          = this.id.toString(),
    slug        = this.slug ?: "",
    title       = this.title,
    location    = listOfNotNull(this.location_name, this.city)
        .filter { it.isNotBlank() }
        .joinToString(", "),
    description = this.description ?: "",
    imageRes    = this.poster ?: "",
    organizationName = this.organization?.organization_name
        ?: this.organization?.user?.name,
    startDate   = this.start_date,
    remainingQuota = this.remaining_quota,
    category    = this.categories?.firstOrNull()?.name
)