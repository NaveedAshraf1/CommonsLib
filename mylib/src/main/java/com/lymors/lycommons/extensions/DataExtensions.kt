package com.lymors.lycommons.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.JsonSyntaxException
import com.lymors.lycommons.utils.MyResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

object DataExtensions {

    val gson: Gson by lazy { GsonBuilder().disableHtmlEscaping().create() }
    inline fun <reified T> typeToken(): Type = object : TypeToken<T>() {}.type
    inline fun <reified T> String.toObject(): T {
        val type = typeToken<T>()
        return gson.fromJson(this, type)
    }
    inline fun <reified T> Map<String, Any>.toObject(): T = convert()
    inline fun <T, reified R> T.convert(): R = gson.toJson(this).toObject()
    inline fun <reified T> Gson.fromJson(json: String?): T? = try {
        fromJson<T>(json, object : TypeToken<T>() {}.type)
    } catch (e: Exception) {
        null
    }
    inline fun <reified T> Gson.fromJsonList(json: String?): List<T>? = try {
        fromJson<List<T>>(json, object : TypeToken<List<T>>() {}.type)
    } catch (e: Exception) {
        null
    }

    fun JSONObject.toPrettyString(): String = toString(4)
    fun JSONArray.toPrettyString(): String = toString(4)
    fun JSONArray.toList(): List<Any> = (0 until length()).map { get(it) }
    fun JSONObject.toMap(): Map<String, Any> = keys().asSequence().associateWith { get(it) }
    fun List<Any>.toJsonArray(): JSONArray = JSONArray(this)
    fun Map<String, Any>.toJsonObject(): JSONObject = JSONObject(this)

    fun Any.shrink(): Map<String, Any> {
        val propertiesMap = mutableMapOf<String, Any>()
        this::class.memberProperties.forEach { prop ->
            prop.isAccessible = true
            if (!prop.name.startsWith("_")) {
                val value = prop.getter.call(this)
                if (value != null) {
                    when (value) {
                        is String -> if (value.isNotEmpty()) propertiesMap[prop.name] = value
                        is Int -> if (value != 0) propertiesMap[prop.name] = value
                        is Boolean -> if (value) propertiesMap[prop.name] = value
                        is Double -> if (value != 0.0) propertiesMap[prop.name] = value
                        is Long -> if (value != 0L) propertiesMap[prop.name] = value
                        is Array<*> -> if (value.isNotEmpty()) propertiesMap[prop.name] = value.map { it?.shrink() }
                        is ArrayList<*> -> if (value.isNotEmpty()) propertiesMap[prop.name] = value.map { it.shrink() }
                        is List<*> -> if (value.isNotEmpty()) propertiesMap[prop.name] = value.map { it?.shrink() }
                        is Float -> if (value != 0.0f) propertiesMap[prop.name] = value
                        is Short -> if (value != 0.toShort()) propertiesMap[prop.name] = value
                        is Byte -> if (value != 0.toByte()) propertiesMap[prop.name] = value
                        is Char -> if (value != '\u0000') propertiesMap[prop.name] = value
                        is Set<*> -> if (value.isNotEmpty()) propertiesMap[prop.name] = value.map { it?.shrink() }
                        is Map<*, *> -> if (value.isNotEmpty()) propertiesMap[prop.name] = value.mapValues { it.value?.shrink() }
                        is Enum<*> -> propertiesMap[prop.name] = value.name
                        else -> try {
                            if (value::class.java.name == "com.google.android.gms.maps.model.LatLng") {
                                val lat = value::class.java.getMethod("latitude").invoke(value)
                                val lng = value::class.java.getMethod("longitude").invoke(value)
                                if (lat != 0.0 || lng != 0.0) propertiesMap[prop.name] = mapOf("lat" to lat, "lng" to lng)
                            } else {
                                propertiesMap[prop.name] = value.shrink()
                            }
                        } catch (e: Exception) {
                            propertiesMap[prop.name] = value.shrink()
                        }
                    }
                }
            }
        }
        return propertiesMap
    }

    fun Any.toMap(): Map<String, Any> {
        val propertiesMap = mutableMapOf<String, Any>()
        this::class.memberProperties.forEach { prop ->
            prop.isAccessible = true
            val value = prop.getter.call(this)
            when (value) {
                is String -> propertiesMap[prop.name] = value
                is Int -> propertiesMap[prop.name] = value
                is Boolean -> propertiesMap[prop.name] = value
                is Double -> propertiesMap[prop.name] = value
                is Long -> propertiesMap[prop.name] = value
                is List<*> -> propertiesMap[prop.name] = value
                is Float -> propertiesMap[prop.name] = value
                is Short -> propertiesMap[prop.name] = value
                is Byte -> propertiesMap[prop.name] = value
                is Char -> propertiesMap[prop.name] = value
                is Set<*> -> propertiesMap[prop.name] = value
                is Map<*, *> -> propertiesMap[prop.name] = value
                is Enum<*> -> propertiesMap[prop.name] = value.name
                is Any -> propertiesMap[prop.name] = value.shrink()
            }
        }
        return propertiesMap
    }

    fun Any.toJsonString(): String {
        val map = this.shrink()
        return gson.toJson(map)
    }

    var Calendar.year: Int
        get() = get(Calendar.YEAR)
        set(value) { set(Calendar.YEAR, value) }

    var Calendar.month: Int
        get() = get(Calendar.MONTH)
        set(value) { set(Calendar.MONTH, value) }

    var Calendar.day: Int
        get() = get(Calendar.DAY_OF_MONTH)
        set(value) { set(Calendar.DAY_OF_MONTH, value) }

    fun Calendar.previousYear() = if (get(Calendar.MONTH) == Calendar.JANUARY) get(Calendar.YEAR) - 2 else get(Calendar.YEAR) - 1
    fun Calendar.previousMonth() = if (get(Calendar.MONTH) == Calendar.JANUARY) Calendar.DECEMBER else get(Calendar.MONTH) - 1
    fun Calendar.nextMonth() = if (get(Calendar.MONTH) == Calendar.DECEMBER) Calendar.JANUARY else get(Calendar.MONTH) + 1
    fun Calendar.setLastDayOfMonth() = apply { add(Calendar.MONTH, 1); set(Calendar.DAY_OF_MONTH, 1); add(Calendar.DAY_OF_YEAR, -1) }
    fun Calendar.setLastDayOfYear() = apply { add(Calendar.YEAR, 1); set(Calendar.DAY_OF_YEAR, 1); add(Calendar.DAY_OF_YEAR, -1) }

    fun Long.toDate(pattern: String = "dd-MM-yyyy"): String {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(Date(this))
    }

    fun Long.toTime(pattern: String = "hh:mm:ss a"): String {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(Date(this))
    }

    fun Long.toDateTime(pattern: String = "dd-MM-yyyy hh:mm:ss a"): String {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(Date(this))
    }

    fun Long.isToday(): Boolean {
        val calendar = Calendar.getInstance()
        calendar.time = Date(this)
        val today = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
    }

    fun Long.isThisMonth(): Boolean {
        val calendar = Calendar.getInstance()
        calendar.time = Date(this)
        val today = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)
    }

    fun Long.isThisYear(): Boolean {
        val calendar = Calendar.getInstance()
        calendar.time = Date(this)
        val today = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
    }

    fun Long.isFuture(): Boolean = this > System.currentTimeMillis()
    fun Long.isPast(): Boolean = this < System.currentTimeMillis()

    fun Double.rounded(): String = if (this == roundToInt().toDouble()) roundToInt().toString() else String.format("%.1f", this)

    fun Double.roundTo(digitsAfterDecimal: Int): String {
        return if (this % 1.0 == 0.0) {
            this.toInt().toString()
        } else {
            var roundedValue = String.format(Locale.US, "%.${digitsAfterDecimal}f", this)
            roundedValue = roundedValue.replace(Regex("0+$"), "")
            if (roundedValue.endsWith(".")) roundedValue = roundedValue.removeSuffix(".")
            roundedValue
        }
    }

    fun <T> List<T>.toArrayList(): ArrayList<T> = ArrayList<T>().apply { addAll(this@toArrayList) }

    fun Any?.ifNull(block: () -> Unit) { if (this == null) block() }
    fun Any?.isNull() = this == null
    fun Any?.isNotNull() = this != null
    val Any.className: String get() = this::class.java.simpleName

    inline fun <T> tryOrDefault(default: T?, block: () -> T) = try { block() } catch (_: Throwable) { default }

    val Int.Companion.empty get() = 0
    val Long.Companion.empty get() = 0L
    val Float.Companion.empty get() = 0f
    val String.Companion.empty get() = ""
    val Double.Companion.empty get() = 0.0

    fun Bitmap.toUri(context: Context): Uri? {
        val imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile("image_${System.currentTimeMillis()}", ".jpg", imagesDir)
        return try {
            FileOutputStream(imageFile).use { outputStream ->
                compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            Uri.fromFile(imageFile)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable) return bitmap
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }

    fun com.google.android.gms.maps.model.LatLng.toLatLang(): LatLng = LatLng(latitude, longitude)

    fun Boolean.toggle(): Boolean = !this

    // Improved JSON pretty print with custom indentation
    fun JSONObject.toPrettyString(indentSpaces: Int = 4): String {
        return try {
            this.toString(indentSpaces)
        } catch (e: Exception) {
            this.toString() // Fallback
        }
    }

    fun JSONArray.toPrettyString(indentSpaces: Int = 4): String {
        return try {
            this.toString(indentSpaces)
        } catch (e: Exception) {
            this.toString()
        }
    }

    // Merge JSON objects safely
    fun JSONObject.merge(other: JSONObject): JSONObject {
        val result = JSONObject(this.toString())
        other.keys().forEach { key ->
            result.put(key, other.get(key))
        }
        return result
    }

    // Validate JSON string
    fun String.isValidJson(): Boolean {
        return try {
            JSONObject(this)
            true
        } catch (e: Exception) {
            try {
                JSONArray(this)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }

    // Calendar improvements with overflow handling
    fun Calendar.addDays(days: Int): Calendar = apply { add(Calendar.DAY_OF_MONTH, days) }
    fun Calendar.addMonths(months: Int): Calendar = apply { add(Calendar.MONTH, months) }
    fun Calendar.addYears(years: Int): Calendar = apply { add(Calendar.YEAR, years) }

    fun Calendar.subtractDays(days: Int): Calendar = addDays(-days)
    fun Calendar.subtractMonths(months: Int): Calendar = addMonths(-months)
    fun Calendar.subtractYears(years: Int): Calendar = addYears(-years)

    fun Calendar.isLeapYear(): Boolean {
        val year = get(Calendar.YEAR)
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    fun Calendar.daysInMonth(): Int {
        return getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun Calendar.daysInYear(): Int {
        return if (isLeapYear()) 366 else 365
    }

    // Safe date parsing with fallback
    fun String.toDateSafe(pattern: String = "yyyy-MM-dd", fallback: Date? = null): Date? {
        return try {
            SimpleDateFormat(pattern, Locale.getDefault()).parse(this)
        } catch (e: Exception) {
            fallback
        }
    }

    fun Date.formatSafe(pattern: String = "yyyy-MM-dd", fallback: String = ""): String {
        return try {
            SimpleDateFormat(pattern, Locale.getDefault()).format(this)
        } catch (e: Exception) {
            fallback
        }
    }

    // Enhanced Any extensions
    inline fun <reified T : Any> Any.castOrNull(): T? = this as? T

    fun Any.toHexString(): String = Integer.toHexString(hashCode())

    fun Any.simpleClassName(): String = this::class.java.simpleName

    inline fun <reified T> Any.isInstanceOf(): Boolean = this is T

    fun <T> List<T>.randomOrNull(): T? = if (isEmpty()) null else random()

    fun <T> List<T>.shuffleSafe(): List<T> = toMutableList().apply { shuffle() }

    fun <T, K> List<T>.groupBySafe(keySelector: (T) -> K): Map<K, List<T>> = groupBy(keySelector)

    fun <T> List<T>.distinctBySafe(selector: (T) -> Any?): List<T> = distinctBy(selector)

    // String utilities with edge cases
    fun String.toTitleCase(): String {
        return if (isEmpty()) "" else lowercase().replaceFirstChar { it.titlecase() }
    }

    fun String.removeAccents(): String {
        return java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }

    fun String.base64Encode(): String {
        return try {
            android.util.Base64.encodeToString(toByteArray(), android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            ""
        }
    }

    fun String.base64Decode(): String {
        return try {
            String(android.util.Base64.decode(this, android.util.Base64.DEFAULT))
        } catch (e: Exception) {
            ""
        }
    }

    fun String.isValidEmail(): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return matches(emailRegex)
    }

    fun String.isValidPhone(): Boolean {
        val phoneRegex = "^[+]?[0-9]{10,15}$".toRegex()
        return matches(phoneRegex)
    }

    // Number extensions with bounds
    fun Int.clamp(min: Int, max: Int): Int = coerceIn(min, max)
    fun Float.clamp(min: Float, max: Float): Float = coerceIn(min, max)
    fun Double.clamp(min: Double, max: Double): Double = coerceIn(min, max)

    fun Int.toCurrency(symbol: String = "$"): String = "$symbol$this"
    fun Double.toPercentage(): String = "${(this * 100).toInt()}%"

    fun Int.toOrdinal(): String {
        return when {
            this % 100 in 11..13 -> "${this}th"
            this % 10 == 1 -> "${this}st"
            this % 10 == 2 -> "${this}nd"
            this % 10 == 3 -> "${this}rd"
            else -> "${this}th"
        }
    }

    // Bitmap enhancements with quality control
    fun Bitmap.resize(width: Int, height: Int, filter: Boolean = true): Bitmap {
        return Bitmap.createScaledBitmap(this, width, height, filter)
    }

    fun Bitmap.resizeByFactor(factor: Float, filter: Boolean = true): Bitmap {
        val newWidth = (width * factor).toInt()
        val newHeight = (height * factor).toInt()
        return resize(newWidth, newHeight, filter)
    }

    fun Bitmap.crop(x: Int, y: Int, width: Int, height: Int): Bitmap {
        return Bitmap.createBitmap(this, x, y, width, height)
    }

    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = android.graphics.Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    // File/URI utilities
    fun Uri.getFileSize(context: Context): Long {
        return try {
            context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getLong(cursor.getColumnIndexOrThrow(android.provider.OpenableColumns.SIZE))
                } else 0L
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun Uri.getFileExtension(): String {
        return lastPathSegment?.substringAfterLast('.') ?: ""
    }

    fun Uri.isImage(): Boolean = getFileExtension().lowercase() in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
    fun Uri.isVideo(): Boolean = getFileExtension().lowercase() in listOf("mp4", "avi", "mkv", "mov", "wmv")

    // Random utilities
    fun randomString(length: Int = 10): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length).map { chars.random() }.joinToString("")
    }

    fun randomUUID(): String = java.util.UUID.randomUUID().toString()

    // Hash functions (basic, for demo)
    fun String.md5(): String {
        return try {
            val md = java.security.MessageDigest.getInstance("MD5")
            val bytes = md.digest(toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }

    fun String.sha256(): String {
        return try {
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val bytes = md.digest(toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }

    // Add more as needed
}