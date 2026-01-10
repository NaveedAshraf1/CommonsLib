package com.lymors.lycommons.extensions

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.lymors.lycommons.R
import com.lymors.lycommons.extensions.ContextExtensions.showToast
import com.lymors.lycommons.extensions.DataExtensions.toTime
import com.lymors.lycommons.utils.MyExtensions.empty
import kotlinx.coroutines.tasks.await
import java.net.Inet4Address
import java.net.NetworkInterface
import kotlin.random.Random


object UtilityExtensions {

    fun Any?.logT(append: String = "", tag: String = "TAG") {
        if (this == null) {
            Log.i(tag, "$append:null")
        } else {
            Log.i(tag, "${System.currentTimeMillis().toTime()}:$append:$this")
        }
    }

    fun dialACode(context: Context, code: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$code")
        context.startActivity(intent)
    }

    fun String.sendInMessage(number: String) {
        if (number.isEmpty() || this.isEmpty()) {
            "number or message is empty".logT()
        } else {
            val byteArray = this.toByteArray(charset("UTF-16"))
            val sms = String(byteArray, charset("UTF-16"))
            val smsManager: SmsManager = SmsManager.getDefault()
            val smsArray = smsManager.divideMessage(sms)
            smsManager.sendMultipartTextMessage(number, null, smsArray, null, null)
        }
    }

    fun Context.sendEmail(sendTo: Array<String?>?, subject: String?, body: String?) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("plain/text")
        intent.putExtra(Intent.EXTRA_EMAIL, sendTo)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, body)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(intent, ""))
        }
    }

    fun Context.shareMyApp(subject: String?, message: String) {
        try {
            val appUrl = "https://play.google.com/store/apps/details?id=" + packageName
            val i = Intent(Intent.ACTION_SEND)
            i.setType("text/plain")
            i.putExtra(Intent.EXTRA_SUBJECT, subject)
            var leadingText = "\n$message\n\n"
            leadingText += appUrl + "\n\n"
            i.putExtra(Intent.EXTRA_TEXT, leadingText)
            startActivity(Intent.createChooser(i, "Share using"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun Activity.shareText(text: String, subject: String = "") {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        startActivity(Intent.createChooser(intent, "Share"))
    }

    suspend fun getFcmToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getRandomNumber(from: Int, to: Int): Int = Random.nextInt(from, to)

    fun paste(context: Context): String {
        val clipboardManager: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboardManager.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val item = clipData.getItemAt(0)
            return item.text.toString()
        }
        return ""
    }

    fun sendMessageToWhatsApp(context: Context, phoneNumber: String, message: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://wa.me/$phoneNumber/?text=${Uri.encode(message)}")
        context.startActivity(intent)
    }

    fun Activity.setUpBottomNavigationColor(color: Int = R.color.gray10) {
        window.navigationBarColor = ContextCompat.getColor(this, color)
    }

    fun getIpv4LocalHostAddress(): String {
        val ip = String.empty()
        NetworkInterface.getNetworkInterfaces()?.toList()?.map { networkInterface ->
            networkInterface.inetAddresses?.toList()?.find { !it.isLoopbackAddress && it is Inet4Address }?.let { return it.hostAddress.orEmpty() }
        }
        return ip
    }

    fun checkEditTexts(list: List<EditText>): Boolean {
        list.forEach {
            if (it.text.toString().isEmpty()) {
                if (it.tag.toString().isNotEmpty()) {
                    it.context.showToast("${it.tag} must not be empty")
                } else if (it.hint.toString().isNotEmpty()) {
                    it.context.showToast("${it.hint} must not be empty")
                } else {
                    it.context.showToast("${it.id} must not be empty")
                }
                return false
            }
        }
        return true
    }

    fun printClassInfo(clazz: Class<*>) {
        println("Methods:")
        clazz.declaredMethods.forEach { method ->
            println(method.name)
        }
        println("\nProperties:")
        clazz.declaredFields.forEach { field ->
            println(field.name)
        }
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    fun printMethodNames(clazz: Class<*>) {
//        val methods = clazz.declaredMethods
//        println("Methods in class ${clazz.simpleName}:")
//        for (method in methods) {
//            val modifiers = Modifier.toString(method.modifiers)
//            val returnType = method.returnType.simpleName
//            val parameters = method.parameters.joinToString(", ") { "${it.type.simpleName} ${it.name}" }
//            println("$modifiers $returnType ${method.name}($parameters)")
//        }
//    }

//    fun View.convertToPdf(context: Activity, pdfFileName: String): String? {
//        val pdfDocument = PdfDocument()
//        val width = this.width
//        val height = this.height
//        val pageInfo = PdfDocument.PageInfo.Builder(width, height, 1).create()
//        val page = pdfDocument.startPage(pageInfo)
//        val canvas = page.canvas
//        this.draw(canvas)
//        pdfDocument.finishPage(page)
//        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), pdfFileName)
//        try {
//            pdfDocument.writeTo(FileOutputStream(file))
//        } catch (e: IOException) {
//            e.printStackTrace()
//            return null
//        }
//        pdfDocument.close()
//        val uri = Utils.addPdfToMediaStore(context, file, pdfFileName)
//        return uri.toString()
//    }

//    fun View.convertToPdfA4(context: Activity, pdfFileName: String): String? {
//        val pdfDocument = PdfDocument()
//        val a4Width = 595
//        val a4Height = 842
//        val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(bitmap)
//        this.draw(canvas)
//        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, a4Width, a4Height, true)
//        val pageInfo = PdfDocument.PageInfo.Builder(a4Width, a4Height, 1).create()
//        val page = pdfDocument.startPage(pageInfo)
//        val pdfCanvas = page.canvas
//        pdfCanvas.drawBitmap(scaledBitmap, 0f, 0f, null)
//        pdfDocument.finishPage(page)
//        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), pdfFileName)
//        try {
//            pdfDocument.writeTo(FileOutputStream(file))
//        } catch (e: IOException) {
//            e.printStackTrace()
//            return null
//        }
//        pdfDocument.close()
//        val uri = Utils.addPdfToMediaStore(context, file, pdfFileName)
//        return uri.toString()
//    }

    fun calculateNewAverage(previousAverage: Double, totalAmount: Int, newAmount: Double): Double {
        return ((previousAverage * totalAmount) + newAmount) / (totalAmount + 1)
    }

//    fun runAfterAttempts(repeatInterval: Int, block: () -> Unit) {
//        attemptCount++
//        if (attemptCount % repeatInterval == 0) {
//            block()
//        }
//    }
}