package com.jcarrasco96.socialnet.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Time {

    private fun currentDate(): Date {
        val calendar = Calendar.getInstance()
        return calendar.time
    }

    fun checkDate(date: String): Boolean {
        if (date.length != 10) {
            return false
        }

        return try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            simpleDateFormat.isLenient = false

            simpleDateFormat.parse(date)

            true
        } catch (e: ParseException) {
            false
        } catch (e: NullPointerException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun timeAgo(date: Date?): String {
        if (date == null) {
            return ""
        }

        if (date.time < 1000000000000L) {
            date.time *= 1000
        }

        val calendar = Calendar.getInstance()
        calendar.time = date

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val currentCalendar = Calendar.getInstance()

        val currentYear = currentCalendar.get(Calendar.YEAR)
        val currentMonth = currentCalendar.get(Calendar.MONTH)
        val currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH)
        val currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentCalendar.get(Calendar.MINUTE)

        return if (year < currentYear) {
            val interval = currentYear - year
            if (interval == 1) "hace $interval año" else "hace $interval años"
        } else if (month < currentMonth) {
            val interval = currentMonth - month
            if (interval == 1) "hace $interval mes" else "hace $interval meses"
        } else if (day < currentDay) {
            val interval = currentDay - day
            if (interval == 1) "hace $interval día" else "hace $interval días"
        } else if (hour < currentHour) {
            val interval = currentHour - hour
            if (interval == 1) "hace $interval hora" else "hace $interval horas"
        } else if (minute < currentMinute) {
            val interval = currentMinute - minute
            if (interval == 1) "hace $interval minuto" else "hace $interval minutos"
        } else {
            "hace unos momentos"
        }
    }

}