// 4. Create MonthYear.kt in com.example.expensetrackerapp.data.model package
package com.example.expensetrackerapp.data.model

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class MonthYear(val month: Int, val year: Int) {

    fun toFormattedString(): String {
        return "$year-${month.toString().padStart(2, '0')}"
    }

    fun toDisplayString(): String {
        val yearMonth = YearMonth.of(year, month)
        return yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }

    fun next(): MonthYear {
        val yearMonth = YearMonth.of(year, month).plusMonths(1)
        return MonthYear(yearMonth.monthValue, yearMonth.year)
    }

    fun previous(): MonthYear {
        val yearMonth = YearMonth.of(year, month).minusMonths(1)
        return MonthYear(yearMonth.monthValue, yearMonth.year)
    }

    companion object {
        fun current(): MonthYear {
            val now = LocalDate.now()
            return MonthYear(now.monthValue, now.year)
        }

        fun fromLocalDate(date: LocalDate): MonthYear {
            return MonthYear(date.monthValue, date.year)
        }

        fun fromFormattedString(formatted: String): MonthYear {
            val parts = formatted.split("-")
            return MonthYear(parts[1].toInt(), parts[0].toInt())
        }
    }
}