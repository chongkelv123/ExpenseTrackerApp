package com.example.expensetrackerapp.data.model

import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit

/**
 * Represents a date range period, typically used for budget cycles
 */
data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("d MMM")
        private val MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy")

        /**
         * Creates a default mid-month to mid-month range (13th to 12th)
         */
        fun customDefault(): DateRange {
            // By default, create a mid-month to mid-month range (13th to 12th)
            val now = LocalDate.now()
            val startDay = 13
            val endDay = 12

            // Calculate start date (13th of previous month or current month)
            val currentMonth = YearMonth.from(now)
            val startDate = if (now.dayOfMonth <= endDay) {
                currentMonth.minusMonths(1).atDay(startDay)
            } else {
                currentMonth.atDay(startDay)
            }

            // Calculate end date (12th of current month or next month)
            val endDate = if (now.dayOfMonth <= endDay) {
                currentMonth.atDay(endDay)
            } else {
                currentMonth.plusMonths(1).atDay(endDay)
            }

            return DateRange(startDate, endDate)
        }

        /**
         * Creates a date range for a calendar month
         */
        fun forCalendarMonth(yearMonth: YearMonth): DateRange {
            return DateRange(
                yearMonth.atDay(1),
                yearMonth.atEndOfMonth()
            )
        }

        /**
         * Creates a date range for the current calendar month
         */
        fun currentCalendarMonth(): DateRange {
            val yearMonth = YearMonth.now()
            return forCalendarMonth(yearMonth)
        }

        /**
         * Parse a date range from a formatted string
         */
        fun fromFormattedString(formatted: String): DateRange {
            val parts = formatted.split(",")
            return DateRange(
                LocalDate.parse(parts[0], DATE_FORMATTER),
                LocalDate.parse(parts[1], DATE_FORMATTER)
            )
        }
    }

    /**
     * Returns the next date range with the same duration
     */
    fun next(): DateRange {
        val duration = ChronoUnit.DAYS.between(startDate, endDate)
        return DateRange(endDate.plusDays(1), endDate.plusDays(duration + 1))
    }

    /**
     * Returns the previous date range with the same duration
     */
    fun previous(): DateRange {
        val duration = ChronoUnit.DAYS.between(startDate, endDate)
        return DateRange(startDate.minusDays(duration + 1), startDate.minusDays(1))
    }

    /**
     * Returns the next budget cycle (usually mid-month to mid-month)
     */
    fun nextBudgetCycle(): DateRange {
        // For 13th to 12th budget cycle
        val nextStartDate = startDate.plusMonths(1)
        val nextEndDate = endDate.plusMonths(1)
        return DateRange(nextStartDate, nextEndDate)
    }

    /**
     * Returns the previous budget cycle (usually mid-month to mid-month)
     */
    fun previousBudgetCycle(): DateRange {
        // For 13th to 12th budget cycle
        val prevStartDate = startDate.minusMonths(1)
        val prevEndDate = endDate.minusMonths(1)
        return DateRange(prevStartDate, prevEndDate)
    }

    /**
     * Convert to a standardized string format for storage
     */
    fun toFormattedString(): String {
        return "${startDate.format(DATE_FORMATTER)},${endDate.format(DATE_FORMATTER)}"
    }

    /**
     * User-friendly display format
     */
    fun toDisplayString(): String {
        // Check if this is a calendar month
        if (isCalendarMonth()) {
            return startDate.format(MONTH_FORMATTER)
        }
        return "${startDate.format(DISPLAY_FORMATTER)} - ${endDate.format(DISPLAY_FORMATTER)} ${endDate.year}"
    }

    /**
     * Checks if this date range represents a full calendar month
     */
    fun isCalendarMonth(): Boolean {
        return startDate.dayOfMonth == 1 &&
                endDate.dayOfMonth == YearMonth.from(endDate).lengthOfMonth() &&
                startDate.year == endDate.year &&
                startDate.monthValue == endDate.monthValue
    }

    /**
     * Checks if this date range is a budget cycle (13th to 12th)
     */
    fun isBudgetCycle(): Boolean {
        return startDate.dayOfMonth == 13 &&
                endDate.dayOfMonth == 12 &&
                ChronoUnit.MONTHS.between(startDate, endDate) == 0.toLong()
    }

    /**
     * Returns the name of the period type (Budget Cycle, Calendar Month, or Custom)
     */
    fun getPeriodTypeName(): String {
        return when {
            isBudgetCycle() -> "Budget Cycle"
            isCalendarMonth() -> "Calendar Month"
            else -> "Custom Period"
        }
    }
}