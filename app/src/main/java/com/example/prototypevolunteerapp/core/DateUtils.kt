package com.example.prototypevolunteerapp.core

import java.text.SimpleDateFormat
import java.util.Locale

object DateUtils {

    private val inputFormats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    )

    private val dateOutput     = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
    private val dateTimeOutput = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("id", "ID"))
    private val shortOutput    = SimpleDateFormat("d MMM yyyy", Locale("id", "ID"))

    fun formatDate(raw: String?): String {
        if (raw.isNullOrBlank()) return "-"
        if (!raw.contains('T') && !raw.contains('Z') && raw.length == 10) {
            return tryParse(raw, "yyyy-MM-dd", dateOutput) ?: raw
        }
        for (fmt in inputFormats) {
            val result = tryParse(raw, fmt, dateOutput)
            if (result != null) return result
        }
        return raw
    }

    fun formatDateShort(raw: String?): String {
        if (raw.isNullOrBlank()) return "-"
        for (fmt in inputFormats) {
            val result = tryParse(raw, fmt, shortOutput)
            if (result != null) return result
        }
        return raw
    }

    fun formatDateTime(raw: String?): String {
        if (raw.isNullOrBlank()) return "-"
        for (fmt in inputFormats) {
            val result = tryParse(raw, fmt, dateTimeOutput)
            if (result != null) return result
        }
        return raw
    }

    fun parseInputDate(input: String): String? {
        val trimmed = input.trim()
        return tryParse(trimmed, "yyyy-MM-dd", SimpleDateFormat("yyyy-MM-dd", Locale.US))
    }

    fun extractDatePart(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        if (raw.length >= 10 && raw[4] == '-' && raw[7] == '-') {
            return raw.substring(0, 10)
        }
        return raw
    }

    private fun tryParse(
        raw:    String,
        format: String,
        output: SimpleDateFormat
    ): String? {
        return try {
            val sdf = SimpleDateFormat(format, Locale.US)
            sdf.isLenient = false
            val date = sdf.parse(raw) ?: return null
            output.format(date)
        } catch (_: Exception) {
            null
        }
    }
}