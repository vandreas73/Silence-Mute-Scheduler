package hu.vandreas73.silencescheduler

import java.time.DayOfWeek
import java.util.*

class CalculatorNextWeekDay {

    /**
     * Example if you want to calculate next Tuesday 15:00
     */
    fun getCalendar(dayOfWeek: DayOfWeek, hourOfDay: Int, minute: Int): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek.value+1)       // I don't totally understand why do I have to add 1 to the value, but it works so
        val rightNow = Calendar.getInstance()
        if (calendar.timeInMillis < rightNow.timeInMillis)
            calendar.add(Calendar.WEEK_OF_YEAR, 1)

        return calendar
    }
}