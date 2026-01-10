package com.lymors.lycommons.extensions

import android.R.attr.dialogLayout
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.EditText
import android.widget.Button
import android.widget.TextView
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.Gravity
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.Window
import android.widget.DatePicker
import android.widget.Space
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.lymors.lycommons.R
import com.lymors.lycommons.databinding.BottomsheetSimpleListBinding
import com.lymors.lycommons.databinding.ItemviewSimpleListBinding
import com.lymors.lycommons.utils.MyExtensions.hideSoftKeyboard
import com.lymors.lycommons.utils.Utils.setData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object UIExtensions {
    fun View.setBottomSheetList(optionsList: List<String>, selected: (String) -> Unit = {}) {
        if (this is EditText) inputType = InputType.TYPE_NULL
        isFocusable = false
        isFocusableInTouchMode = false

        val dialogBinding = BottomsheetSimpleListBinding.inflate(LayoutInflater.from(context))
        val dialog = BottomSheetDialog(context)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        dialog.setContentView(dialogBinding.root)

        setOnClickListener {
            if (this is EditText) hideSoftKeyboard()
            dialog.show()
        }

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                val shapeAppearanceModel = ShapeAppearanceModel.builder()
                    .setTopLeftCornerSize(50f)
                    .setTopRightCornerSize(50f)
                    .build()
                it.parent.requestLayout()
                val materialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel).apply {
                    setTint(Color.WHITE)
                    elevation = ViewCompat.getElevation(it)
                }
                ViewCompat.setBackground(it, materialShapeDrawable)
            }
        }

        dialogBinding.recyclerview.setData(optionsList, ItemviewSimpleListBinding::inflate) { itemview, item, _ ->
            itemview.textview.text = item
            itemview.root.setOnClickListener {
                selected.invoke(item)
                if (this is TextView) this.text = item
                dialog.dismiss()
            }
        }
    }

    fun View.attachValueEditor(onTextChanged: (String) -> Unit) {
        val dialog = AlertDialog.Builder(context)
            .setView(dialogLayout)
            .create()
        // Ensure this view is a TextView
        if (this !is TextView) return

        setOnClickListener {
            val dialogLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(40, 40, 40, 40)
            }

            val editText = EditText(context).apply {
                // If current text is "0", show empty input
                val currentText = this@attachValueEditor.text.toString().trim()
                setText(if (currentText == "0") "" else currentText)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            dialogLayout.addView(editText)

            val buttonLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            val buttonCancel = Button(context).apply {
                text = "Cancel"
                setOnClickListener { dialog.dismiss() }
            }

            val buttonOk = Button(context).apply {
                text = "OK"
                setOnClickListener {
                    val newText = editText.text.toString().trim()
                    this@attachValueEditor.text = newText
                    onTextChanged.invoke(newText)
                    dialog.dismiss()
                }
            }

            // Add buttons with space in between
            buttonLayout.addView(buttonCancel)
            buttonLayout.addView(Space(context), LinearLayout.LayoutParams(0, 0, 1f))
            buttonLayout.addView(buttonOk)

            dialogLayout.addView(buttonLayout)



            dialog.show()
        }
    }


    fun showWithRevealAnimation(showView: View, hideView: View) {
        val centerX = (showView.left + showView.right) / 2
        val centerY = (showView.top + showView.bottom) / 2
        val finalRadius = kotlin.math.hypot(showView.width.toDouble(), showView.height.toDouble()).toFloat()
        val circularReveal = ViewAnimationUtils.createCircularReveal(showView, centerX, centerY, 0f, finalRadius)
        circularReveal.duration = 700
        circularReveal.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                showView.visibility = View.VISIBLE
                hideView.visibility = View.INVISIBLE
            }
        })
        circularReveal.start()
    }

    fun hideWithRevealAnimation(hideView: View, showView: View) {
        val centerX = (hideView.left + hideView.right) / 2
        val centerY = (hideView.top + hideView.bottom) / 2
        val initialRadius = Math.hypot(hideView.width.toDouble(), hideView.height.toDouble()).toFloat()
        val circularReveal = ViewAnimationUtils.createCircularReveal(hideView, centerX, centerY, initialRadius, 0f)
        circularReveal.duration = 700
        circularReveal.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                hideView.visibility = View.INVISIBLE
                showView.visibility = View.VISIBLE
            }
        })
        circularReveal.start()
    }

    inline fun <reified T : ViewBinding> AppCompatActivity.createBottomDialog(
        crossinline bindingInflater: (LayoutInflater) -> T,
        callback: (T, Dialog) -> Unit = { _, _ -> }
    ): Dialog {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val binding = bindingInflater(LayoutInflater.from(this))
        dialog.setContentView(binding.root)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val dialogWidth = (screenWidth * 0.92).toInt()

        val windowParams = WindowManager.LayoutParams().apply {
            copyFrom(dialog.window!!.attributes)
            width = dialogWidth
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            gravity = Gravity.BOTTOM
        }
        dialog.window!!.attributes = windowParams
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.show()
        callback(binding, dialog)
        return dialog
    }

    inline fun <reified T : ViewBinding> Fragment.createBottomDialog(
        crossinline bindingInflater: (LayoutInflater) -> T,
        callback: (T, Dialog) -> Unit = { _, _ -> }
    ): Dialog {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val binding = bindingInflater(LayoutInflater.from(requireActivity()))
        dialog.setContentView(binding.root)
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val dialogWidth = (screenWidth * 0.92).toInt()

        val windowParams = WindowManager.LayoutParams().apply {
            copyFrom(dialog.window!!.attributes)
            width = dialogWidth
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            gravity = Gravity.BOTTOM
        }
        dialog.window!!.attributes = windowParams
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.show()
        callback(binding, dialog)
        return dialog
    }

    fun View.attachDateTimePicker(callback: (Calendar) -> Unit = {}) {
        fun openDateTimePickerDialog() {
            val context = this.context
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val datePickerDialog = DatePickerDialog(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, { _, selectedYear, selectedMonth, selectedDay ->
                val timePickerDialog = TimePickerDialog(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, { _, selectedHour, selectedMinute ->
                    val selectedDateTime = Calendar.getInstance()
                    selectedDateTime.set(selectedYear, selectedMonth, selectedDay)
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, selectedHour)
                    selectedDateTime.set(Calendar.MINUTE, selectedMinute)
                    callback(selectedDateTime)
                }, hour, minute, false)
                timePickerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                timePickerDialog.setTitle("Select Time")
                timePickerDialog.show()
            }, year, month, day)

            datePickerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            datePickerDialog.setTitle("Select Date")
            datePickerDialog.show()
        }

        if (this is EditText) {
            inputType = InputType.TYPE_NULL
            isCursorVisible = false
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) openDateTimePickerDialog()
            }
            setOnClickListener { openDateTimePickerDialog() }
        } else {
            setOnClickListener { openDateTimePickerDialog() }
        }
    }

    fun View.attachDatePicker(pattern: String = "dd-MM-yyyy", callback: (Date) -> Unit = {}) {
        fun openDatePickerDialog() {
            val context = this.context
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                val formattedDate = sdf.format(selectedDate.time)
                (this as TextView).text = formattedDate
                callback(selectedDate.time)
            }, year, month, day)

            datePickerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            datePickerDialog.setTitle("Select Date")
            datePickerDialog.show()
        }

        if (this is EditText) {
            inputType = InputType.TYPE_NULL
            isCursorVisible = false
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) openDatePickerDialog()
            }
            setOnClickListener { openDatePickerDialog() }
        } else {
            setOnClickListener { openDatePickerDialog() }
        }
    }

    fun View.attachTimePicker() {
        fun openTimePickerDialog() {
            val context = this.context
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                val selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour)
                selectedTime.set(Calendar.MINUTE, selectedMinute)
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val formattedTime = sdf.format(selectedTime.time)
                (this as TextView).text = formattedTime
            }, hour, minute, false)

            timePickerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            timePickerDialog.setTitle("Select Time")
            timePickerDialog.show()
        }

        if (this is EditText) {
            inputType = InputType.TYPE_NULL
            isCursorVisible = false
            setOnClickListener { openTimePickerDialog() }
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) openTimePickerDialog()
            }
        } else {
            setOnClickListener { openTimePickerDialog() }
        }
    }
}