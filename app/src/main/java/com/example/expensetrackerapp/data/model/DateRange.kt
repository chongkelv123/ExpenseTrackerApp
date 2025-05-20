package com.example.expensetrackerapp.data.model

import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit

data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    fun toFormattedString(): String {
        return "${startDate.format(DATE_FORMATTER)},${endDate.format(DATE_FORMATTER)}"
    }

    fun toDisplayString(): String {
        return "${startDate.format(DISPLAY_FORMATTER)} to ${endDate.format(DISPLAY_FORMATTER)}"
    }

    fun next(): DateRange {
        val duration = ChronoUnit.DAYS.between(startDate, endDate)
        return DateRange(endDate.plusDays(1), endDate.plusDays(duration + 1))
    }

    fun previous(): DateRange {
        val duration = ChronoUnit.DAYS.between(startDate, endDate)
        return DateRange(startDate.minusDays(duration + 1), startDate.minusDays(1))
    }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy")

        fun customDefault(): DateRange {
            // By default, create a mid-month to mid-month range (13th to 12th)
            val now = LocalDate.now()
            val startDate = now.withDayOfMonth(13).minusMonths(1)
            val endDate = now.withDayOfMonth(12)

            // Adjust if endDate is in the future
            return if (endDate.isAfter(now)) {
                DateRange(startDate.minusMonths(1), endDate.minusMonths(1))
            } else {
                DateRange(startDate, endDate)
            }
        }

        fun fromFormattedString(formatted: String): DateRange {
            val parts = formatted.split(",")
            return DateRange(
                LocalDate.parse(parts[0], DATE_FORMATTER),
                LocalDate.parse(parts[1], DATE_FORMATTER)
            )
        }
    }
}