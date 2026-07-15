package com.remindme.app.utils

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object ComputedFields {

    fun calculateAge(birthdate: LocalDate, targetDate: LocalDate = LocalDate.now()): Int {
        var age = targetDate.year - birthdate.year
        if (targetDate.monthValue < birthdate.monthValue ||
            (targetDate.monthValue == birthdate.monthValue && targetDate.dayOfMonth < birthdate.dayOfMonth)
        ) {
            age--
        }
        return age
    }

    fun calculateDaysToBirthday(birthdate: LocalDate, targetDate: LocalDate = LocalDate.now()): Int {
        var nextBirthday = LocalDate.of(targetDate.year, birthdate.monthValue, birthdate.dayOfMonth)

        if (birthdate.monthValue == 2 && birthdate.dayOfMonth == 29) {
            if (!targetDate.isLeapYear) {
                nextBirthday = LocalDate.of(targetDate.year, 2, 28)
            }
        }

        if (nextBirthday.isBefore(targetDate)) {
            val nextYear = targetDate.year + 1
            if (birthdate.monthValue == 2 && birthdate.dayOfMonth == 29) {
                nextBirthday = LocalDate.of(nextYear, 2, if (LocalDate.of(nextYear, 1, 1).isLeapYear) 29 else 28)
            } else {
                nextBirthday = LocalDate.of(nextYear, birthdate.monthValue, birthdate.dayOfMonth)
            }
        }

        return ChronoUnit.DAYS.between(targetDate, nextBirthday).toInt()
    }

    fun getZodiacSign(birthdate: LocalDate): String {
        val month = birthdate.monthValue
        val day = birthdate.dayOfMonth

        if ((month == 3 && day >= 21) || (month == 4 && day <= 19)) return "Aries"
        if ((month == 4 && day >= 20) || (month == 5 && day <= 20)) return "Taurus"
        if ((month == 5 && day >= 21) || (month == 6 && day <= 20)) return "Gemini"
        if ((month == 6 && day >= 21) || (month == 7 && day <= 22)) return "Cancer"
        if ((month == 7 && day >= 23) || (month == 8 && day <= 22)) return "Leo"
        if ((month == 8 && day >= 23) || (month == 9 && day <= 22)) return "Virgo"
        if ((month == 9 && day >= 23) || (month == 10 && day <= 22)) return "Libra"
        if ((month == 10 && day >= 23) || (month == 11 && day <= 21)) return "Scorpio"
        if ((month == 11 && day >= 22) || (month == 12 && day <= 21)) return "Sagittarius"
        if ((month == 12 && day >= 22) || (month == 1 && day <= 19)) return "Capricorn"
        if ((month == 1 && day >= 20) || (month == 2 && day <= 18)) return "Aquarius"
        if ((month == 2 && day >= 19) || (month == 3 && day <= 20)) return "Pisces"

        return "Unknown"
    }
}
