package com.lymors.lycommons.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.speech.tts.TextToSpeech
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.lymors.lycommons.utils.MyExtensions.empty
import org.json.JSONArray
import org.json.JSONObject
import org.mariuszgromada.math.mxparser.Expression
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


object StringExtensions {

    fun String.isNumber() =
        String.empty() != this && Pattern.compile("^[0-9]*$").matcher(this).matches()


    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val ITERATION_COUNT = 65536
    private const val KEY_LENGTH = 256

    @RequiresApi(Build.VERSION_CODES.O)
    fun String.encrypt(key: String): String {
        val salt = ByteArray(16).apply {
            SecureRandom().nextBytes(this)
        }
        val iv = ByteArray(16).apply {
            SecureRandom().nextBytes(this)
        }

        val secretKey = generateSecretKey(key, salt)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        }
        val encryptedBytes = cipher.doFinal(this.toByteArray(Charsets.UTF_8))

        val saltIvAndEncrypted = salt + iv + encryptedBytes
        return java.util.Base64.getEncoder().encodeToString(saltIvAndEncrypted)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun String.decrypt(key: String): String {
        val decodedBytes = java.util.Base64.getDecoder().decode(this)
        val salt = decodedBytes.sliceArray(0 until 16)
        val iv = decodedBytes.sliceArray(16 until 32)
        val encryptedBytes = decodedBytes.sliceArray(32 until decodedBytes.size)

        val secretKey = generateSecretKey(key, salt)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        }
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    private fun generateSecretKey(key: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(key.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        return SecretKeySpec(factory.generateSecret(spec).encoded, ALGORITHM)
    }


    fun String.encryptShort( key: Int): String {
        val offset = key % 26
        if (offset == 0) return this
        var d: Char
        val chars = CharArray(this.length)
        for ((index, c) in this.withIndex()) {
            if (c in 'A'..'Z') {
                d = c + offset
                if (d > 'Z') d -= 26
            } else if (c in 'a'..'z') {
                d = c + offset
                if (d > 'z') d -= 26
            } else
                d = c
            chars[index] = d
        }
        return chars.joinToString("")
    }

    fun String.decryptShort( key: Int): String = this.encryptShort( 26 - key)



    fun String.capitalizeString(): String {
        return split(" ").joinToString(" ") { it ->
            it.replaceFirstChar {char-> if (char.isLowerCase()) char.titlecase(Locale.ROOT) else it
            } }
    }

    fun String.getStringDate(initialFormat: String, requiredFormat: String, locale: Locale = Locale.getDefault()): String {
        return this.toDate(initialFormat, locale).toString(requiredFormat, locale)
    }

    fun String.toDate(format: String, locale: Locale = Locale.getDefault()): Date = SimpleDateFormat(format, locale).parse(this)!!

    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    // string
    //  Capitalize the first letter of the string


    //   // qr code generater
    //    implementation 'com.google.zxing:core:3.4.0'
    //    implementation 'com.journeyapps:zxing-android-embedded:3.6.0'
    fun String.generateQrCode(): Bitmap? {
        val bitMatrix: BitMatrix
        try {
            bitMatrix = MultiFormatWriter().encode(this, BarcodeFormat.QR_CODE, 300, 300, null)
        } catch (illegalArgumentException: WriterException) {
            return null
        }
        val bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.RGB_565)
        for (x in 0 until 300) {
            for (y in 0 until 300) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }



    fun String.capitalize(): String {
        return if (isNotEmpty()) {
            this[0].uppercase() + substring(1)
        } else {
            this
        }
    }

    fun String.isValidEmail(): Boolean {
        val emailRegex = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return matches(emailRegex)
    }

    //Convert a string to an integer (or return a default value if conversion fails)
    fun String.toIntOrDefault(defaultValue: Int = 0): Int {
        return toIntOrNull() ?: defaultValue
    }
    fun String.toLongOrDefault(defaultValue: Long = 0): Long {
        return toLongOrNull() ?: defaultValue
    }

    fun String.isValidUrl(): Boolean {
        return try {
            java.net.URL(this)
            true
        } catch (e: Exception) {
            false
        }
    }



    //Truncate the string to a specified length and append "..." if it exceeds
    fun String.truncate(length: Int): String {
        return if (length >= length) {
            substring(0, length) + "..."
        } else {
            this
        }
    }

    fun String.isNumeric(): Boolean {
        return matches(Regex("-?\\d+(\\.\\d+)?"))
    }

    fun String.isAlphaString(): Boolean {
        return all { it.isLetter() }
    }

    fun String.isValidPhoneNumber(): Boolean {
        return matches(Regex("^\\+(?:[0-9] ?){6,14}[0-9]$"))
    }

    fun String.splitIntoWords(): List<String> {
        return split("\\s+".toRegex())
    }

    fun String.removeSpaces(): String {
        return this.replace("\\s".toRegex(), "")
    }


    fun String.keepOnlyAlphanumeric(): String {
        return replace(Regex("[^A-Za-z0-9]"), "")
    }




    fun String.toJsonArray(): JSONArray {
        return try {
            JSONArray(this)
        } catch (e: Exception) {
            JSONArray()
        }
    }
    fun String.calculateExpression(exp:String):Double{
        return Expression(exp).calculate()
    }

    // Extension function to convert dp to pixels
    fun String.convertDpToPx(context: Context): Float {
        val density = context.resources.displayMetrics.density
        return this.toFloat() * density
    }

    fun String.openUrlInBrowser(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(this))
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    fun String.toJson(): JSONObject {
        return try {
            JSONObject(this)
        } catch (e: Exception) {
            // Handle JSON parsing exception here
            JSONObject()
        }
    }

    fun String.isDigitsOnly(): Boolean {
        return all { it.isDigit() }
    }

    fun String.parseAsHtml(
        @SuppressLint("InlinedApi")
        flag: Int = Html.FROM_HTML_MODE_LEGACY,
        imageGetter: Html.ImageGetter? = null,
        tagHandler: Html.TagHandler? = null
    ): Spanned {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(this, flag, imageGetter, tagHandler)
        }

        @Suppress("Deprecation")
        return Html.fromHtml(this, imageGetter, tagHandler)
    }





    inline fun <reified T> String.fromJson(): T {
        return Gson().fromJson(this, T::class.java)
    }

    fun String.shareImage(context: Context) {
        var uri = Uri.parse(this)
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/*"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share image via"))
    }

    fun String.showInToast(context: Context, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, this, duration).show()
    }

    lateinit var tts: TextToSpeech
    fun String.textToSpeak(context: Context) {
        val text = this

        // Check if the TTS engine is available
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Set the TTS language
                val result = tts.setLanguage(Locale.getDefault())

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // TTS language not supported
                    Log.e("TTS", "Language not supported")
                } else {
                    // Speak the text
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            } else {
                // TTS engine not available
                Log.e("TTS", "TTS engine not available")
            }
        }
    }

}