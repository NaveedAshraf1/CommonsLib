package com.lymors.lycommons.managers
//
//import android.app.Activity
//import android.app.DatePickerDialog
//import android.content.Context
//import androidx.fragment.app.FragmentManager
//import com.example.commonsuper.R
//import com.google.android.material.timepicker.MaterialTimePicker
//import com.google.android.material.timepicker.TimeFormat
//import java.time.Instant
//import java.time.LocalDate
//import java.time.LocalDateTime
//import java.time.LocalTime
//import java.time.ZoneOffset
//import java.time.format.DateTimeFormatter
//import java.time.temporal.ChronoUnit
//import java.util.Calendar
//
//
//class DateAndTimeManager() {
//
//    var timeInMillis: Long = System.currentTimeMillis()
//    var date: LocalDate = LocalDate.now()
//    var time: LocalTime = LocalTime.now()
//    var currentDateTime: LocalDateTime = LocalDateTime.now()
//
//    fun create(millis: Long): LocalDateTime {
//        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC)
//    }
//
//    fun create(date:String , format: String = "yyyy-MM-dd HH:mm:ss"): LocalDateTime {
//        val formatter = DateTimeFormatter.ofPattern(format)
//        return LocalDateTime.parse(date, formatter)
//    }
//
//    fun create(year: Int = date.year, month: Int = date.monthValue, day: Int = date.dayOfMonth) {
//        date = LocalDate.of(year, month, day)
//    }
//
//    fun create(year: Int = date.year, month: Int = date.monthValue, day: Int = date.dayOfMonth, hour: Int = currentDateTime.hour, minutes: Int = currentDateTime.minute, seconds: Int = currentDateTime.second
//    ) {
//        currentDateTime = LocalDateTime.of(year, month, day, hour, minutes, seconds)
//        date = currentDateTime.toLocalDate()
//        timeInMillis = currentDateTime.toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
//    }
//
//    fun getCurrentDate(format: String): String {
//        val formatter = DateTimeFormatter.ofPattern(format)
//        return date.format(formatter)
//    }
//
//    fun addDays(days: Long) {
//        date = date.plusDays(days)
//    }
//
//
//
//
//    fun minusDays(days: Long) {
//        date = date.minusDays(days)
//    }
//
//    fun minus(days: Long) {
//        timeInMillis -= days * 24 * 60 * 60 * 1000
//        date = LocalDate.ofEpochDay(timeInMillis / (24 * 60 * 60 * 1000))
//    }
//
//    fun getCurrentWeek():Pair<LocalDate,LocalDate> {
//        return Pair(this.date,this.date)
//    }
//
//    fun getCurrentMonth(): Int {
//        return date.monthValue
//    }
//
//    fun getCurrentYear(): Int {
//        return date.year
//    }
//
//    fun getCurrentHour(): Int {
//        return currentDateTime.hour
//    }
//}
//
//// calculate the days from start date to end date
//fun calculateDaysUntilEvent(currentDate: LocalDate, futureDate: LocalDate): Long {
//    return ChronoUnit.DAYS.between(currentDate, futureDate)
//}
//
//fun isEventWithinTimeframe(startDate: LocalDate, endDate: LocalDate, timeframeDays: Long): Boolean {
//    val limitDate = startDate.plusDays(timeframeDays)
//    return endDate.isBefore(limitDate) || endDate.isEqual(limitDate)
//}
//
//fun getDaysBetween(start:DateAndTimeManager , end:DateAndTimeManager):Int {
//    return 0
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//    fun showDatePickerDialog(context: Context , text:(String, Calendar) -> Unit) {
//        val calendar = Calendar.getInstance()
//        val year = calendar.get(Calendar.YEAR)
//        val month = calendar.get(Calendar.MONTH)
//        val day = calendar.get(Calendar.DAY_OF_MONTH)
//
//        val datePickerDialog = DatePickerDialog(context, R.style.MyDatePickerDialogStyle, { _, selectedYear, selectedMonth, selectedDay ->
//                text("$selectedDay-$selectedMonth-$selectedYear",calendar)
//            },
//            year, month, day
//        )
//
//        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
//        datePickerDialog.show()
//    }
//    fun showTimePickerDialog(context: Activity, fragmentManager: FragmentManager, formattedTime: (Calendar, String) -> Unit) {
//        var formattedTimeStr = ""
//        val calendar = Calendar.getInstance()
//        val picker = MaterialTimePicker.Builder()
//            .setTimeFormat(TimeFormat.CLOCK_12H)
//            .setHour(12)
//            .setMinute(0)
//            .setTitleText("Select Time")
//            .build()
//
//        picker.show(fragmentManager, "timePickerTag")
//
//        picker.addOnPositiveButtonClickListener {
//            formattedTimeStr = if (picker.hour > 12) {
//                String.format("%02d", picker.hour - 12) + " : " + String.format("%02d", picker.minute) + " PM "
//            } else {
//                String.format("%02d", picker.hour) + " : " + String.format("%02d", picker.minute) + " AM "
//            }
//            calendar[Calendar.HOUR_OF_DAY] = picker.hour
//            calendar[Calendar.MINUTE] = picker.minute
//            calendar[Calendar.SECOND] = 0
//            calendar[Calendar.MILLISECOND] = 0
//            formattedTime(calendar, formattedTimeStr)
//        }
//    }
//
//
//
//}









