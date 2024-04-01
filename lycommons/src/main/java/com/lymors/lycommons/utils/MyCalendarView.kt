package com.lymors.lycommons.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.lymors.lycommons.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MyCalendarView(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    // Initialize calendar instance
    private val calendar = Calendar.getInstance()
    var dateTextView:TextView = createTextView()

    // List to hold the text views representing the calendar dates
    val dateTextViews = ArrayList<TextView>()
    var onMonthChangeListener: ((List<TextView>) -> Unit)? = null


    init {
        // Set the orientation of the LinearLayout
        orientation = VERTICAL
        // Populate the calendar grid
        populateCalendar()
    }

    private fun createImageView(src: Int):ImageView {
        return ImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(10, 20, 10, 0)
            }
            gravity = Gravity.CENTER
            setImageResource(src)

        }
    }

    private fun createTextView():TextView {
        return  TextView(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(10, 10, 10, 10)
                setPadding(1, 15, 1, 15)
                elevation = 10f
            }
            gravity = Gravity.CENTER
            textSize = 22f
        }

    }


    // Function to populate the calendar grid
    @SuppressLint("SetTextI18n")
    private fun populateCalendar() {
        // Clear any existing views
        removeAllViews()
        dateTextViews.clear()


        var switchLinear = LinearLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(30,30,30,50)
            }

        }

        var backWardButton = createImageView(R.drawable.baseline_arrow_back_ios_24)
        var forwardButton = createImageView(R.drawable.baseline_arrow_forward_ios_24)
        backWardButton.setOnClickListener {
            calendar.add(Calendar.MONTH,-1)
            updateCalendar()
            onMonthChangeListener?.invoke(dateTextViews)
        }

        forwardButton.setOnClickListener {
            calendar.add(Calendar.MONTH,+1)
            updateCalendar()
            onMonthChangeListener?.invoke(dateTextViews)
        }

        switchLinear.addView(backWardButton )
        switchLinear.addView( dateTextView)
        switchLinear.addView( forwardButton)
        addView(switchLinear)
        addView(daysLinear())





        val calendarCopy = calendar.clone() as Calendar

        // Set the calendar to the first day of the month
        calendarCopy.set(Calendar.DAY_OF_MONTH, 1)
        dateTextView.text = getMonth(calendarCopy)

        val firstDayOfWeek = calendarCopy.get(Calendar.DAY_OF_WEEK)
        calendarCopy.add(Calendar.DAY_OF_MONTH, Calendar.SUNDAY - firstDayOfWeek)
        // Loop through each row in the calendar
        repeat(6) { _ ->
            // Create a new horizontal LinearLayout for each row
            val rowLayout = createLinearLayout()
            // Loop through each day in the row
            repeat(7) { _ ->
                // Create a TextView for the day
                val textView = createTextView()
                // Add the TextView to the list
                textView.text = calendarCopy.get(Calendar.DAY_OF_MONTH).toString()
                dateTextViews.add(textView)

                // Add the TextView to the row layout
                rowLayout.addView(textView)

                // Move to the next day
                calendarCopy.add(Calendar.DAY_OF_MONTH, 1)
            }

            // Add the row layout to the calendar view
            addView(rowLayout)
        }
    }

    private fun daysLinear(): LinearLayout {
        var dL = createLinearLayout()
        var list = listOf("S","M","T","W","T","F","S")
        list.forEach {
            var tv = createTextView()
            tv.text = it
            dL.addView(tv)
        }
        return dL
    }

    private fun createLinearLayout(): LinearLayout {
       return LinearLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            }
            orientation = HORIZONTAL

        }
    }

    // Function to update the calendar with a new month
    fun updateCalendar() {
        val calendarCopy = calendar.clone() as Calendar
        calendarCopy.set(Calendar.DAY_OF_MONTH, 1)
        dateTextView.text = getMonth(calendarCopy)
        val firstDayOfWeek = calendarCopy.get(Calendar.DAY_OF_WEEK)
        calendarCopy.add(Calendar.DAY_OF_MONTH, Calendar.SUNDAY - firstDayOfWeek)

        dateTextViews.forEach {
            it.text = calendarCopy.get(Calendar.DAY_OF_MONTH).toString()
            calendarCopy.add(Calendar.DAY_OF_MONTH, 1)
        }

    }

    private fun getMonth(calendar: Calendar):String {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
       return dateFormat.format(calendar.time)
    }

    // Function to set the onMonthSwitch listener
    fun setOnMonthSwitchListener(listener: (List<TextView>) -> Unit) {
        onMonthChangeListener = listener
    }



}
