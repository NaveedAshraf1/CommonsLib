package com.lymors.lycommons.extensions

import android.R
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.TimePicker
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object ViewExtensions {




    fun TextView.setBold() {
        this.setTypeface(this.typeface, Typeface.BOLD)
    }

    // 5. Change text to italic
    fun TextView.setItalic() {
        this.setTypeface(this.typeface, Typeface.ITALIC)
    }

    // 6. Underline text
    fun TextView.underline() {
        this.paint.isUnderlineText = true
    }
    fun TextView.setTextOrDefault(text: String?, default: String = "") {
        this.text = text.takeUnless { it.isNullOrEmpty() } ?: default
    }
    fun TextView.toggleVisibilityByGONE() {
        this.visibility = if (this.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    fun TextView.toggleVisibilityByINVISIBLE() {
        this.visibility = if (this.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }


    fun View.setHeight(height: Int) {
        val params = layoutParams
        params.height = height
        layoutParams = params
    }

    fun View.setBackgroundColorRes(@ColorRes color: Int) {
        setBackgroundColor(ContextCompat.getColor(context, color))
    }

    fun View.setBackgroundDrawableRes(@DrawableRes drawable: Int) {
        background = ContextCompat.getDrawable(context, drawable)
    }





    fun View.scale(scaleX: Float, scaleY: Float, duration: Long = 300) {
        this.animate().scaleX(scaleX).scaleY(scaleY).setDuration(duration).start()
    }

    fun View.setWidth(width: Int) {
        val params = layoutParams
        params.width = width
        layoutParams = params
    }

    fun View.setMargins(left: Int, top: Int, right: Int, bottom: Int) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(left, top, right, bottom)
        layoutParams = params
    }
    fun View.setMargins(margin: Int) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(margin, margin, margin, margin)
        layoutParams = params
    }
    fun View.setMargins(margin: Float) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(margin.toInt(), margin.toInt(), margin.toInt(), margin.toInt())
        layoutParams = params
    }

    fun View.shake() {
        val shake = ObjectAnimator.ofFloat(this, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
        shake.duration = 500
        shake.start()
    }

    fun View.getActivity(): Activity? {
        var context = context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }


    fun View.setOnClickListenerWithInterval(interval: Long = 600L, action: () -> Unit) {
        this.setOnClickListener {
            this.isEnabled = false
            action()
            postDelayed({ this.isEnabled = true }, interval)
        }
    }


    fun View.getScreenshot(): Bitmap {
        val screenshot = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(screenshot)
        draw(canvas)
        return screenshot
    }

    fun View.toggleVisibility() {
        if (this.visibility == View.VISIBLE) {
            this.visibility = View.GONE
        } else {
            this.visibility = View.VISIBLE
        }
    }



    fun View.fadeOutGone(duration: Long = 2000) {
        // Create an ObjectAnimator to animate the alpha property of the view
        val alphaAnimator = ObjectAnimator.ofFloat(this, View.ALPHA, 1f, 0f)

        // Set the duration of the alpha animation
        alphaAnimator.duration = duration / 2 // Half the duration for fading out

        // Set a listener to make the view gone after the alpha animation ends
        alphaAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Create ObjectAnimator to animate the scale property of the view after fading out
                val scaleAnimatorX = ObjectAnimator.ofFloat(this@fadeOutGone, View.SCALE_X, 1f, 0f)
                val scaleAnimatorY = ObjectAnimator.ofFloat(this@fadeOutGone, View.SCALE_Y, 1f, 0f)

                // Set the duration of the scale animation
                scaleAnimatorX.duration = duration / 2 // Half the duration for shrinking
                scaleAnimatorY.duration = duration / 2 // Half the duration for shrinking

                // Set a listener to make the view gone after the scale animation ends
                scaleAnimatorX.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        this@fadeOutGone.visibility = View.GONE
                    }
                })

                // Start the scale animations
                scaleAnimatorX.start()
                scaleAnimatorY.start()
            }
        })

        // Start the alpha animation
        alphaAnimator.start()
    }

    fun View.fadeOutInvisible(duration: Long = 5000) {
        // Create an ObjectAnimator to animate the alpha property of the view
        val animator = ObjectAnimator.ofFloat(this, View.ALPHA, 1f, 0f)

        // Set the duration of the animation
        animator.duration = duration

        // Set a listener to make the view invisible after the animation ends
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                this@fadeOutInvisible.visibility = View.INVISIBLE
            }
        })

        // Start the animation
        animator.start()
    }

    fun View.zoomOutVisibleFadeIn(duration: Long = 300L) {
        // Set initial alpha to 0 to ensure it fades in
        alpha = 0f

        // Set initial scale to 0 to ensure it zooms out
        scaleX = 0f
        scaleY = 0f

        // Set visibility to VISIBLE before starting the animation
        visibility = View.VISIBLE

        // Animate zooming out and fading in
        animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }


    fun View.zoomOutVisibleFadeOut(duration: Long = 300L) {
        val scaleX = 0f
        val scaleY = 0f
        val alpha = 0f
        this.animate()
            .scaleX(scaleX)
            .scaleY(scaleY)
            .alpha(alpha)
            .setDuration(duration)
            .setStartDelay(0)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                this.visibility = View.GONE  // Set visibility to GONE after animation
            }
            .start()
    }

    fun View.slideInFromLeft(duration: Long = 300L) {
        val translationX = -width.toFloat()
        animate().translationX(0f).setDuration(duration).setInterpolator(
            AccelerateDecelerateInterpolator()
        ).start()
        visibility = View.VISIBLE
    }


    fun View.slideInFromRight(duration: Long = 300L) {
        val translationX = width.toFloat()
        animate().translationX(0f).setDuration(duration).setInterpolator(
            AccelerateDecelerateInterpolator()
        ).start()
        visibility = View.VISIBLE
    }



    fun View.slideInFromBottom(duration: Long = 300L) {
        translationY = height.toFloat()
        animate().translationY(0f).setDuration(duration).setInterpolator(AccelerateDecelerateInterpolator()).start()
        visibility = View.VISIBLE
    }

    fun View.slideOutToRightGone(duration: Long = 300L){
        val translationX = width.toFloat()
        animate().translationX(0f).setDuration(duration).setInterpolator(AccelerateDecelerateInterpolator()).start()
        visibility = View.GONE
    }

    fun View.slideOutToRightInvisible(duration: Long = 300L){
        val translationX = width.toFloat()
        animate().translationX(0f).setDuration(duration).setInterpolator(AccelerateDecelerateInterpolator()).start()
        visibility = View.INVISIBLE
    }


    fun View.slideInFromBottomFadeIn(duration: Long = 300L) {
        translationY = this.height.toFloat()
        this.alpha = 0f
        this.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(duration)
            .setStartDelay(0)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    fun View.fadeOut(duration: Long = 5000) {
        val fadeOut = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
        fadeOut.duration = duration
        fadeOut.start()
    }

    fun View.slideInFromTopFadeInVisible(duration: Long = 300L) {
        // Set initial alpha to 0 to ensure it fades in
        alpha = 0f
        // Set visibility to VISIBLE before starting the animation
        visibility = View.VISIBLE
        // Animate sliding in from top and fading in
        animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }



    fun View.fadeInVisible(duration: Long = 300) {
        this.alpha = 0f
        this.visibility = View.VISIBLE
        this.animate().alpha(1f).setDuration(duration).start()
    }

    fun View.fadeIn(duration: Long = 5000) {
        val fadeOut = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f)
        fadeOut.duration = duration
        fadeOut.start()
    }



    fun View.slideUpVisibleFadeIn(duration: Long = 500) {
        // Calculate the height of the view
        val originalHeight = height

        // Set initial translationY to the height of the view to make it start from below
        translationY = originalHeight.toFloat()

        // Animate sliding up
        val slideAnimator = ObjectAnimator.ofFloat(this, "translationY", originalHeight.toFloat(), 0f)
        slideAnimator.interpolator = DecelerateInterpolator()
        slideAnimator.duration = duration

        // Animate fading in
        val fadeInAnimator = ObjectAnimator.ofFloat(this, View.ALPHA, 0f, 1f)
        fadeInAnimator.duration = duration

        // Combine the animations
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(slideAnimator, fadeInAnimator)

        // Set a listener to make the view visible after the animation ends
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                visibility = View.VISIBLE
            }
        })

        // Start the animation
        animatorSet.start()
    }
    fun View.slideDownGoneFadeOut(duration: Long = 500) {
        // Calculate the height of the view
        val originalHeight = height

        // Animate sliding down
        val slideAnimator = ObjectAnimator.ofFloat(this, "translationY", 0f, originalHeight.toFloat())
        slideAnimator.interpolator = AccelerateInterpolator()
        slideAnimator.duration = duration

        // Animate fading out
        val fadeOutAnimator = ObjectAnimator.ofFloat(this, View.ALPHA, 1f, 0f)
        fadeOutAnimator.duration = duration

        // Combine the animations
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(slideAnimator, fadeOutAnimator)

        // Set a listener to make the view gone after the animation ends
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                visibility = View.GONE
                translationY = 0f // Reset translation for future use
            }
        })

        // Start the animation
        animatorSet.start()
    }





    fun View.slideLeft(duration: Long = 1000) {
        val slideLeft = ObjectAnimator.ofFloat(this, "translationX", 0f, -this.width.toFloat())
        slideLeft.duration = duration
        slideLeft.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                this@slideLeft.translationX = 0f
                val slideBack = ObjectAnimator.ofFloat(
                    this@slideLeft,
                    "translationX",
                    -this@slideLeft.width.toFloat(),
                    0f
                )
                slideBack.duration = duration
                slideBack.start()
            }

            override fun onAnimationCancel(animation: Animator) {
                this@slideLeft.translationX = 0f
                Log.i("TAG", "onAnimationCancel: ")
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
        slideLeft.start()
    }

    fun View.slideRight(duration: Long = 1000) {
        val slideIn = ObjectAnimator.ofFloat(this, "translationX", 0f, this.width.toFloat())
        slideIn.duration = duration
        slideIn.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                this@slideRight.translationX = 0f
                val slideBack = ObjectAnimator.ofFloat(
                    this@slideRight,
                    "translationX",
                    this@slideRight.width.toFloat(),
                    0f
                )
                slideBack.duration = duration
                slideBack.start()
            }

            override fun onAnimationCancel(animation: Animator) {
                this@slideRight.translationX = 0f
                Log.i("TAG", "onAnimationCancel: ");
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
        slideIn.start()
    }

    fun View.bounce(duration: Long = 1000) {
        var d = duration
        val scaleXDown = ObjectAnimator.ofFloat(this, "scaleX", 1f, 0.8f)
        val scaleYDown = ObjectAnimator.ofFloat(this, "scaleY", 1f, 0.8f)
        val scaleXUp = ObjectAnimator.ofFloat(this, "scaleX", 0.8f, 1.2f)
        val scaleYUp = ObjectAnimator.ofFloat(this, "scaleY", 0.8f, 1.2f)
        val scaleXBack = ObjectAnimator.ofFloat(this, "scaleX", 1.2f, 1f)
        val scaleYBack = ObjectAnimator.ofFloat(this, "scaleY", 1.2f, 1f)

        val downSet = AnimatorSet().apply {
            playTogether(scaleXDown, scaleYDown)
            d = duration / 3
        }

        val upSet = AnimatorSet().apply {
            playTogether(scaleXUp, scaleYUp)
            d = duration / 3
        }

        val backSet = AnimatorSet().apply {
            playTogether(scaleXBack, scaleYBack)
            d = duration / 3
        }

        val bounceSet = AnimatorSet().apply {
            playSequentially(downSet, upSet, backSet)
        }

        bounceSet.start()
    }
    fun View.makeVisible() {
        this.visibility = View.VISIBLE
    }

    fun View.makeInVisible() {
        this.visibility = View.INVISIBLE
    }
    fun View.makeGone() {
        this.visibility = View.GONE
    }
    fun View.makeEnabled() {
        this.isEnabled = true
    }
    fun View.makeDisabled() {
        this.isEnabled = false
    }

    fun View.makeSelected() {
        this.isSelected = true
    }
    fun View.makeUnselected() {
        this.isSelected = false
    }



    fun View.attachDatePicker(pattern:String = "dd-MM-yyyy",callback: (Date) -> Unit = {}) {

        fun openDatePickerDialog() {
            val context = this.context
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(
                context,
                R.style.Theme_Holo_Light_Dialog_NoActionBar,
                { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(selectedYear, selectedMonth, selectedDay)

                    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                    val formattedDate = sdf.format(selectedDate.time)

                    (this as TextView).text = formattedDate
                    callback(selectedDate.time)
                }, year, month, day
            )

            datePickerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            datePickerDialog.setTitle("Select Date")

            datePickerDialog.show()
        }

        if (this is EditText) {
            this.inputType = InputType.TYPE_NULL
            this.isCursorVisible = false
            this.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    openDatePickerDialog()
                }
            }
            this.setOnClickListener {
                openDatePickerDialog()
            }
        } else {
            this.setOnClickListener {
                openDatePickerDialog()
            }
        }


    }


    fun View.attachTimePicker() {
        fun openTimePickerDialog() {
            val context = this.context
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(
                context,
                R.style.Theme_Holo_Light_Dialog_NoActionBar,
                { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                    val selectedTime = Calendar.getInstance()
                    selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour)
                    selectedTime.set(Calendar.MINUTE, selectedMinute)

                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val formattedTime = sdf.format(selectedTime.time)
                    (this as TextView).text = formattedTime
                }, hour, minute, false // Set is24HourView to false
            )

            timePickerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            timePickerDialog.setTitle("Select Time")
            timePickerDialog.show()
        }

        if (this is EditText) {
            this.inputType = InputType.TYPE_NULL
            this.isCursorVisible = false
            this.setOnClickListener {
                openTimePickerDialog()
            }
            this.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    openTimePickerDialog()
                }
            }
        } else {
            this.setOnClickListener {
                openTimePickerDialog()
            }
        }


    }

    fun View.showSnackbar(message: String) {
        Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
    }

    fun View.animateToLeft(duration: Long = 300) {
        val slideLeftAnimation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, -1.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f
        )
        slideLeftAnimation.duration = duration
        this.startAnimation(slideLeftAnimation)
    }

    fun View.animateToRight(duration: Long = 300) {
        val slideRightAnimation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f
        )
        slideRightAnimation.duration = duration
        this.startAnimation(slideRightAnimation)
    }

    fun View.animateToDown(duration: Long = 300) {
        val slideDownAnimation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 1.0f
        )
        slideDownAnimation.duration = duration
        this.startAnimation(slideDownAnimation)
    }

    fun View.animateFromDown(duration: Long = 300) {
        val slideUpAnimation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.0f
        )
        slideUpAnimation.duration = duration
        this.startAnimation(slideUpAnimation)
    }


    fun View.animateFromLeft( fromX: Float = 0.0f, toX: Float = 1.0f , duration: Long = 300) {
        val slideLeftAnimation = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, fromX,
            Animation.RELATIVE_TO_PARENT, toX,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f
        )
        slideLeftAnimation.duration = duration
        this.startAnimation(slideLeftAnimation)
    }

    fun View.animateFromRight(fromX: Float = 1.0f, toX: Float = 0.0f) {
        val slideRightAnimation = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, fromX,
            Animation.RELATIVE_TO_PARENT, toX,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f
        )
        slideRightAnimation.duration = 300
        this.startAnimation(slideRightAnimation)
    }


    // Extension function to fade out a view
    fun View.fadeOutAnimation(fromAlpha: Float = 1f, toAlpha: Float = 0f, duration: Long = 300) {
        val fadeOutAnimation = AlphaAnimation(fromAlpha, toAlpha)
        fadeOutAnimation.duration = duration
        this.startAnimation(fadeOutAnimation)
    }

    // Extension function to rotate a view clockwise
    fun View.animateRotateClockwise(fromDegrees: Float = 0f, toDegrees: Float = 360f, duration: Long = 300, pivotX: Float = 0.5f, pivotY: Float = 0.5f) {
        val rotateAnimation = RotateAnimation(fromDegrees, toDegrees, Animation.RELATIVE_TO_SELF, pivotX, Animation.RELATIVE_TO_SELF, pivotY)
        rotateAnimation.duration = duration
        this.startAnimation(rotateAnimation)
    }

    // Extension function to rotate a view anticlockwise
    fun View.animateRotateAntiClockwise(fromDegrees: Float = 0f, toDegrees: Float = -360f, duration: Long = 300, pivotX: Float = 0.5f, pivotY: Float = 0.5f) {
        val rotateAnimation = RotateAnimation(fromDegrees, toDegrees, Animation.RELATIVE_TO_SELF, pivotX, Animation.RELATIVE_TO_SELF, pivotY)
        rotateAnimation.duration = duration
        this.startAnimation(rotateAnimation)
    }

    // Extension function to scale in a view
    fun View.animateScaleIn(fromX: Float = 0f, toX: Float = 1f, fromY: Float = 0f, toY: Float = 1f, duration: Long = 300, pivotX: Float = 0.5f, pivotY: Float = 0.5f) {
        val scaleAnimation = ScaleAnimation(fromX, toX, fromY, toY, Animation.RELATIVE_TO_SELF, pivotX, Animation.RELATIVE_TO_SELF, pivotY)
        scaleAnimation.duration = duration
        this.startAnimation(scaleAnimation)
    }

    // Extension function to scale out a view
    fun View.animateScaleOut(fromX: Float = 1f, toX: Float = 0f, fromY: Float = 1f, toY: Float = 0f, duration: Long = 300, pivotX: Float = 0.5f, pivotY: Float = 0.5f) {
        val scaleAnimation = ScaleAnimation(fromX, toX, fromY, toY, Animation.RELATIVE_TO_SELF, pivotX, Animation.RELATIVE_TO_SELF, pivotY)
        scaleAnimation.duration = duration
        this.startAnimation(scaleAnimation)
    }


    // Extension function to bounce a view
    fun View.animateBounce(fromX: Float = 0.9f, toX: Float = 1.1f, fromY: Float = 0.9f, toY: Float = 1.1f, duration: Long = 300, pivotX: Float = 0.5f, pivotY: Float = 0.5f, repeatCount: Int = 1, repeatMode: Int = Animation.REVERSE) {
        val bounceAnimation = ScaleAnimation(fromX, toX, fromY, toY, Animation.RELATIVE_TO_SELF, pivotX, Animation.RELATIVE_TO_SELF, pivotY)
        bounceAnimation.duration = duration
        bounceAnimation.repeatCount = repeatCount
        bounceAnimation.repeatMode = repeatMode
        this.startAnimation(bounceAnimation)
    }

    // Extension function to shake a view
    fun View.animateShake(offset: Float = 10f, duration: Long = 300) {
        val shakeAnimation = TranslateAnimation(0f, offset, 0f, 0f)
        shakeAnimation.duration = duration / 6
        shakeAnimation.repeatCount = 5
        shakeAnimation.repeatMode = Animation.REVERSE
        this.startAnimation(shakeAnimation)
    }

    // Extension function to flip a view
    fun View.animateFlip(fromDegrees: Float = 0f, toDegrees: Float = 360f, duration: Long = 300, pivotX: Float = 0.5f, pivotY: Float = 0.5f) {
        val flipAnimation = RotateAnimation(fromDegrees, toDegrees, Animation.RELATIVE_TO_SELF, pivotX, Animation.RELATIVE_TO_SELF, pivotY)
        flipAnimation.duration = duration
        this.startAnimation(flipAnimation)
    }

    // Extension function to zoom in a view
    fun View.animateZoomIn(fromX: Float = 0.5f, toX: Float = 1f, fromY: Float = 0.5f, toY: Float = 1f, duration: Long = 300, pivotX: Float = 0.5f, pivotY: Float = 0.5f) {
        val zoomInAnimation = ScaleAnimation(fromX, toX, fromY, toY, Animation.RELATIVE_TO_SELF, pivotX, Animation.RELATIVE_TO_SELF, pivotY)
        zoomInAnimation.duration = duration
        this.startAnimation(zoomInAnimation)
    }

    // Extension function to zoom out a view

    // Extension function to slide in a view from left
    fun View.slideInFromLeft(fromX: Float = -1f, toX: Float = 0f, fromY: Float = 0f, toY: Float = 0f, duration: Long = 300) {
        val slideIn = TranslateAnimation(Animation.RELATIVE_TO_PARENT, fromX, Animation.RELATIVE_TO_PARENT, toX, Animation.RELATIVE_TO_PARENT, fromY, Animation.RELATIVE_TO_PARENT, toY)
        slideIn.duration = duration
        this.startAnimation(slideIn)
        this.visibility = View.VISIBLE
    }

    // Extension function to slide out a view to left
    fun View.slideOutToLeft(fromX: Float = 0f, toX: Float = -1f, fromY: Float = 0f, toY: Float = 0f, duration: Long = 300) {
        val slideOut = TranslateAnimation(Animation.RELATIVE_TO_PARENT, fromX, Animation.RELATIVE_TO_PARENT, toX, Animation.RELATIVE_TO_PARENT, fromY, Animation.RELATIVE_TO_PARENT, toY)
        slideOut.duration = duration
        this.startAnimation(slideOut)
        this.visibility = View.GONE
    }

    // Extension function to slide in a view from right
    fun View.slideInFromRight(fromX: Float = 1f, toX: Float = 0f, fromY: Float = 0f, toY: Float = 0f, duration: Long = 300) {
        val slideIn = TranslateAnimation(Animation.RELATIVE_TO_PARENT, fromX, Animation.RELATIVE_TO_PARENT, toX, Animation.RELATIVE_TO_PARENT, fromY, Animation.RELATIVE_TO_PARENT, toY)
        slideIn.duration = duration
        this.startAnimation(slideIn)
        this.visibility = View.VISIBLE
    }

    // Extension function to slide out a view to right
    fun View.slideOutToRight(fromX: Float = 0f, toX: Float = 1f, fromY: Float = 0f, toY: Float = 0f, duration: Long = 300) {
        val slideOut = TranslateAnimation(Animation.RELATIVE_TO_PARENT, fromX, Animation.RELATIVE_TO_PARENT, toX, Animation.RELATIVE_TO_PARENT, fromY, Animation.RELATIVE_TO_PARENT, toY)
        slideOut.duration = duration
        this.startAnimation(slideOut)
        this.visibility = View.GONE
    }

    // Extension function to slide in a view from top

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





    fun View.setVisibleOrInvisible(visible: Boolean) {
        visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }
    fun View.setVisibleOrGone(visible: Boolean) {
        visibility = if (visible) View.VISIBLE else View.GONE
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


}