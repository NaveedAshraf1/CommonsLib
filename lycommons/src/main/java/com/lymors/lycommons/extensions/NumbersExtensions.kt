package com.lymors.lycommons.extensions

import android.content.Context
import android.util.TypedValue
import kotlin.math.roundToInt

object NumbersExtensions {


    fun Int?.roundUpToNearestTen(): Int = ((((this ?: 0) + 5) / 10.0).roundToInt() * 10)


    fun Int.dpToPx(context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }


    fun Int.isEven(): Boolean {
        return this % 2 == 0
    }

    fun Int.isOdd(): Boolean {
        return !isEven()
    }
    fun Int.isPositive(): Boolean {
        return this > 0
    }
    fun Int.isNegative(): Boolean {
        return this < 0
    }
    fun Int.isZero(): Boolean {
        return this == 0
    }
    fun Int.isPositiveOrZero(): Boolean {
        return this >= 0
    }
    fun Int.isNegativeOrZero(): Boolean {
        return this <= 0
    }
    fun Int.absValue(): Int {
        return Math.abs(this)
    }

    fun Long.isEven(): Boolean {
        return this % 2 == 0L
    }
    fun Long.isOdd(): Boolean {
        return !isEven()
    }
    fun Long.isPositive(): Boolean {
        return this > 0
    }
    fun Long.isNegative(): Boolean {
        return this < 0
    }
    fun Long.isZero(): Boolean {
        return this == 0L
    }
    fun Long.isPositiveOrZero(): Boolean {
        return this >= 0
    }
    fun Long.isNegativeOrZero(): Boolean {
        return this <= 0
    }
    fun Long.absValue(): Long {
        return Math.abs(this)
    }
    fun Int.square(): Int {
        return this * this
    }

    fun Long.square(): Long {
        return this * this
    }

    fun Int.isWithinRange(min: Int, max: Int): Boolean {
        return this in min..max
    }


    fun Long.isWithinRange(min: Long, max: Long): Boolean {
        return this in min..max
    }
}