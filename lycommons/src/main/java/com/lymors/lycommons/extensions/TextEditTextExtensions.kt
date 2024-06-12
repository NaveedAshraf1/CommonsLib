package com.lymors.lycommons.extensions

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.CountDownTimer
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.google.android.material.textfield.TextInputEditText
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

object TextEditTextExtensions {


    fun TextView.showCountdownTimer( totalTimeInMillis: Long , onFinish: (Unit) ->Unit={} , onTicked:(Long) ->Unit = {}) {
        object : CountDownTimer(totalTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000 % 60
                val minutes = millisUntilFinished / (60 * 1000) % 60
                val hours = millisUntilFinished / (60 * 60 * 1000)
                onTicked(millisUntilFinished)

                val timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                text = timeText
            }

            override fun onFinish() {
                text = "00:00:00"
                onFinish()
            }
        }.start()
    }



    fun TextView.setTextOrInvisible(text: String) {
        this.text = text
        visibility = if (text.isNullOrEmpty()) TextView.INVISIBLE else TextView.VISIBLE
    }

    fun TextView.setTextOrGone(text: String) {
        this.text = text
        visibility = if (text.isNullOrEmpty()) TextView.GONE else TextView.VISIBLE
    }

    fun TextView.makeLinksClickable() {
        movementMethod = LinkMovementMethod.getInstance()
    }

    fun TextView.strikeThrough() {
        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun TextView.setHtmlText(html: String) {
        text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
        movementMethod = LinkMovementMethod.getInstance()
    }

    fun TextView.setTextSizeInSp( s: Float) {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, s)
    }
    fun TextView.setTextColorRes(@ColorRes colorResId: Int) {
        setTextColor(ContextCompat.getColor(context, colorResId))
    }



    private val EditText.inputMethodManager: InputMethodManager?
        get() = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

    fun EditText.onTextChange(callback: (String) -> Unit){
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                callback(s.toString())
            }
        })
    }

    fun TextInputEditText.onTextChange(callback: (String) -> Unit){
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                callback(s.toString())
            }
        })
    }





    // edit text

    fun EditText.getText(): String {
        return text.toString()
    }
    fun EditText.setMaxLength(maxLength: Int) {
        filters = arrayOf(android.text.InputFilter.LengthFilter(maxLength))
    }
    fun EditText.setSelectionToEnd() {
        setSelection(text.length)
    }



    // button
    fun Button.disable() {
        isEnabled = false
    }

    fun Button.enable() {
        isEnabled = true
    }

    fun Button.setTextColorRes(@ColorRes colorResId: Int) {
        setTextColor(ContextCompat.getColor(context, colorResId))
    }

    fun Button.setBackgroundRes(@DrawableRes drawableResId: Int) {
        background = ContextCompat.getDrawable(context, drawableResId)
    }

    fun Button.setRippleEffect() {
        val attrs = intArrayOf(R.attr.selectableItemBackground)
        val typedArray = context.obtainStyledAttributes(attrs)
        val drawable = typedArray.getDrawable(0)
        background = drawable
        typedArray.recycle()
    }



    fun Button.setCornerRadius(radius: Float) {
        val drawable = background
        if (drawable is GradientDrawable) {
            drawable.cornerRadius = radius
        }
    }

    fun Button.setElevationCompat(elevation: Float) {
        this.elevation = elevation
    }

    fun TextView.enablePinchZoom() {
        var mRatio = 1.0f
        var mBaseDist = 0
        var mBaseRatio = 0f
        val STEP = 200

        fun getDistance(event: MotionEvent): Int {
            val dx = (event.getX(0) - event.getX(1)).toInt()
            val dy = (event.getY(0) - event.getY(1)).toInt()
            return sqrt((dx * dx + dy * dy).toDouble()).toInt()
        }

        setOnTouchListener { _, event ->
            performClick()
            if (event.pointerCount == 2) {
                val action = event.action and MotionEvent.ACTION_MASK
                when (action) {
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        mBaseDist = getDistance(event)
                        mBaseRatio = mRatio
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val delta = (getDistance(event) - mBaseDist) / STEP.toFloat()
                        val multi = 2.0.pow(delta.toDouble()).toFloat()
                        mRatio = min(1024.0f, max(0.1f, mBaseRatio * multi))
                        textSize = mRatio + 13
                    }
                }
            }
            true
        }

    }



    fun EditText.isEmpty(): Boolean {

        return text.toString().isEmpty()
    }



    fun EditText.setupQuantityControl(
        incrementView: View,
        decrementView: View,
        initialValue: Int = 0
    ) {
        this.setText(initialValue.toString())
        incrementView.setOnClickListener {
            this.clearFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
            var value = this.text.toString().toIntOrNull() ?: 0
            value++
            this.setText(value.toString())
        }

        decrementView.setOnClickListener {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
            this.clearFocus()
            var value = this.text.toString().toIntOrNull() ?: 0
            if (value > 1) {
                value--
                this.setText(value.toString())
            }
        }
    }








    fun EditText.setListItems(items: List<String>) {
        fun openPopupMenu() {
            val popup = PopupMenu(this.context, this)
            items.forEach { item ->
                popup.menu.add(item)
            }
            popup.setOnMenuItemClickListener { menuItem ->
                setText(menuItem.title)
                true
            }
            popup.show()
        }


        val dropdownIcon: Drawable? =
            AppCompatResources.getDrawable(context, android.R.drawable.arrow_down_float)
        setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, dropdownIcon, null)


        inputType = InputType.TYPE_NULL
        isCursorVisible = false


        setOnClickListener {
            openPopupMenu()
        }
        onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                openPopupMenu()
            }
        }
    }


    fun EditText.value():String{
        return this.text.toString()
    }

    fun TextInputEditText.value():String{
        return this.text.toString()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun TextView.setDefaultOptions(options:List<String>) {
        var currentIndex = 0
        var startX = 0f
        var startY = 0f
        val distance = 10.0
        setText(options[currentIndex])
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val endX = event.x
                    val endY = event.y
                    val deltaX = endX - startX
                    val deltaY = endY - startY
                    if (deltaX < distance && deltaY < distance) {
                        currentIndex = (currentIndex + 1) % options.size
                        setText(options[currentIndex])
                    }
                    true
                }
                else -> false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun TextInputEditText.setDefaultOptions(options: List<String>) {
        var currentIndex = 0
        var startX = 0f
        var startY = 0f
        val distance = 10.0
        setText(options[currentIndex])
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val endX = event.x
                    val endY = event.y
                    val deltaX = endX - startX
                    val deltaY = endY - startY
                    if (deltaX < distance && deltaY < distance) {
                        currentIndex = (currentIndex + 1) % options.size
                        setText(options[currentIndex])
                    }
                    true
                }
                else -> false
            }
        }
    }

    fun TextView.onTextChange(onTextChanged: (s: String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used, implementation optional
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Call the provided lambda function with the new text
                onTextChanged(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // Not used, implementation optional
            }
        })
    }
    fun EditText.showKeyboardForce() {
        this.requestFocus()
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)

        if (!inputMethodManager.isActive(this)) {
            inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_FORCED) // Use SHOW_FORCED directly
        }

        this.post {
            val initialText = this.text.toString()
            if (initialText.isNotEmpty()) {
                val length = initialText.length
                this.setSelection(length) // Set cursor position
            }
        }
    }
}