package com.lymors.lycommons.extensions

import android.content.Context
import android.util.TypedValue
import kotlin.math.roundToInt
import kotlin.random.Random

object NumbersExtensions {


    fun Double?.orEmpty(): Double {
        return this ?: 0.0
    }

    fun Double?.ifEmpty(callback: () -> Double): Double {
        return if (this == null || this == 0.0) callback.invoke() else this
    }


    fun Int.addRandomNumber(from:Int , to:Int){
        var random = Random.nextInt(from,to)
    }


    private val indexMap = mutableMapOf<List<*>, Int>()

    private var List<*>.currentIndex: Int
        get() = indexMap[this] ?: 0
        set(value) {
            indexMap[this] = value
        }

    fun List<Int>.next(): Int {
        if (isEmpty()) throw NoSuchElementException("List is empty")
        val value = this[currentIndex % size]
        currentIndex++
        return value
    }

    fun String.toDoubleOrDefault(defaultValue: Double =0.0): Double {
        return try {
            this.toDouble()
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }


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