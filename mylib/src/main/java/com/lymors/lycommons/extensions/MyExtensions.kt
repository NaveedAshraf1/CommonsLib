package com.lymors.lycommons.extensions


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.graphics.pdf.PdfDocument
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.maps.model.LatLng
import com.lymors.lycommons.R
import com.lymors.lycommons.extensions.NumbersExtensions.toDoubleOrDefault
import com.lymors.lycommons.extensions.StringExtensions.toIntOrDefault
import com.lymors.lycommons.managers.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.reflect.Type
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible


object MyExtensions {


    fun TextView.appendText(text: CharSequence, size: Float = 8f,  color: Int = resources.getColor(com.lymors.lycommons.R.color.gray60)) {
        val spannable = SpannableString(text)

        // Apply color
        spannable.setSpan(
            ForegroundColorSpan(color),
            0,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Apply size
        spannable.setSpan(
            RelativeSizeSpan(size / this.textSize),  // Scaling the size relative to current text size
            0,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Append the styled text
        this.append(spannable)
    }




    fun TextView.appendSpannable(
        text: String,
        color: Int,
        textSize: Float = 25f,
        onClick: () -> Unit
    ) {
        val spannable = SpannableString(text)
        spannable.setSpan(
            object : ClickableSpan() {
                override fun onClick(view: View) {
                    onClick()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = resources.getColor(color)
                    ds.isUnderlineText = true
                    ds.textSize = textSize
                }
            },
            0,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        append(spannable)
        movementMethod = LinkMovementMethod.getInstance()
    }

    fun TextView.appendBold(
        text: String,
        textSize: Float
    ) {
        val spannable = SpannableString(text)
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            AbsoluteSizeSpan(textSize.toInt()),
            0,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        append(spannable)
    }

    fun TextView.appendItalic(
        text: String,
        textSize: Float
    ) {
        val spannable = SpannableString(text)
        spannable.setSpan(
            StyleSpan(Typeface.ITALIC),
            0,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            AbsoluteSizeSpan(textSize.toInt()),
            0,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        append(spannable)
    }

    fun TextView.appendUnderline(
        text: String,
        textSize: Float
    ) {
        val spannable = SpannableString(text)
        spannable.setSpan(
            UnderlineSpan(),
            0,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            AbsoluteSizeSpan(textSize.toInt()),
            0,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        append(spannable)
    }

    fun TextView.makeTextClickable(
        textToMakeClickable: String,
        color: Int,
        textSize: Float,
        onClick: () -> Unit
    ) {
        val text = text.toString()
        val index = text.indexOf(textToMakeClickable)
        if (index != -1) {
            val spannable = SpannableString(text)
            spannable.setSpan(
                object : ClickableSpan() {
                    override fun onClick(view: View) {
                        onClick()
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = resources.getColor(color)
                        ds.isUnderlineText = true
                        ds.textSize = textSize
                    }
                },
                index,
                index + textToMakeClickable.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            this.text = spannable
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    fun Activity.launchActivityClearNewTask(destination: Class<*>, key: String = "", data: String = "") {
        val intent = Intent(this, destination)
        if (key.isNotEmpty()) {
            intent.putExtra(key, data)
        }
        startActivity(intent)
    }



    inline fun <T> List<T>?.ifNotEmpty(callback: () -> Unit) {
        if (!this.isNullOrEmpty()) {
            callback.invoke()
        }
    }

    inline fun <T> List<T>?.ifEmpty(callback: () -> Unit) {
        if (this.isNullOrEmpty()) {
            callback.invoke()
        }
    }



    fun calculateNewAverage(previousAverage: Double, totalAmount: Int, newAmount: Double): Double {
        return ((previousAverage * totalAmount) + newAmount) / (totalAmount + 1)
    }
    fun com.google.android.gms.maps.model.LatLng.toLatLang(): LatLng {
        return LatLng(this.latitude, this.longitude)
    }




    fun Any?.ifNull(block: () -> Unit) {
        if (this != null) block.invoke()
    }
    fun LifecycleOwner.launchWhenResumed(block: suspend () -> Unit) {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                block()
            }
        }
    }

    fun LifecycleOwner.launchWhenCreated(block: suspend () -> Unit) {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                block()
            }
        }
    }

//    fun Double.roundTo(digitsAfterDecimal: Int): String {
//        return if (this % 1.0 == 0.0) {
//            this.toInt().toString()
//        } else {
//            var l =  String.format("%.${digitsAfterDecimal}f", this)
//            if (l.endsWith(".000")) l= l.removeSuffix(".000")
//            if (l.endsWith(".00")) l= l.removeSuffix(".00")
//            if (l.endsWith(".0")) l= l.removeSuffix(".0")
//
//            l
//        }
//    }
    fun Double.roundTo(digitsAfterDecimal: Int): String {
        return if (this % 1.0 == 0.0) {
            this.toInt().toString()
        } else {
            var roundedValue = String.format("%.${digitsAfterDecimal}f", this)
            // Remove trailing zeros after the decimal point
            roundedValue = roundedValue.replace(Regex("0+$"), "")
            // If it ends with a dot, remove the dot
            if (roundedValue.endsWith(".")) {
                roundedValue = roundedValue.removeSuffix(".")
            }
            roundedValue
        }
    }



    fun EditText.getTextAsInt(): Int {
        val text = text.toString().trim()
        return if (text.isEmpty()) {
            0
        } else {
            text.toIntOrDefault()
        }
    }

    fun EditText.getTextAsDouble(afterDecimal:Int): Double {
        val text = text.toString().trim()
        return if (text.isEmpty()) {
            0.0
        } else {
            text.toDoubleOrDefault().roundTo(afterDecimal).toDoubleOrDefault()
        }
    }


    fun AppCompatActivity.replaceFragment(frameLayout: FrameLayout,fragment: Fragment, addToBackStack: Boolean) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(frameLayout.id, fragment)

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null)
        }
        fragmentTransaction.commit()
    }



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
        e.printStackTrace()
        null
    }

    inline fun <reified T : Any> Gson.fromJsonList(json: String?): List<T>? = try {
        fromJson<List<T>>(json, object : TypeToken<List<T>>() {}.type)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }


    var Calendar.year: Int
        get() = get(Calendar.YEAR)
        set(value) {
            set(Calendar.YEAR, value)
        }

    var Calendar.month: Int
        get() = get(Calendar.MONTH)
        set(value) {
            set(Calendar.MONTH, value)
        }

    var Calendar.day: Int
        get() = get(Calendar.DAY_OF_MONTH)
        set(value) {
            set(Calendar.DAY_OF_MONTH, value)
        }

    fun Calendar.previousYear() = if (get(Calendar.MONTH) == Calendar.JANUARY) {
        get(Calendar.YEAR) - 2
    } else get(Calendar.YEAR) - 1

    fun Calendar.previousMonth() = if (get(Calendar.MONTH) == Calendar.JANUARY) {
        Calendar.DECEMBER
    } else get(Calendar.MONTH) - 1

    fun Calendar.nextMonth() = if (get(Calendar.MONTH) == Calendar.DECEMBER) {
        Calendar.JANUARY
    } else get(Calendar.MONTH) + 1

    fun Calendar.setLastDayOfMonth() = apply {
        add(Calendar.MONTH, 1)
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.DAY_OF_YEAR, -1)
    }

    fun Calendar.setLastDayOfYear() = apply {
        add(Calendar.YEAR, 1)
        set(Calendar.DAY_OF_YEAR, 1)
        add(Calendar.DAY_OF_YEAR, -1)
    }


    fun Any.ifNotNull(callback: () -> Unit){
        if (this != null){
            callback.invoke()
        }
    }

    fun Any?.isNull() = this == null
    fun Any?.isNotNull() = this != null

    val Any.className: String
        get() = this::class.java.simpleName



    inline fun <T> tryOrDefault(default: T?, block: () -> T) =
        try {
            block()
        } catch (_: Throwable) {
            default
        }



    fun Int.Companion.empty() = 0
    fun Long.Companion.empty() = 0L
    fun Float.Companion.empty() = 0f
    fun String.Companion.empty() = ""
    fun Double.Companion.empty() = 0.0
    fun Bitmap.toUri(context: Context): Uri? {
        // Get the external storage directory
        val imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        // Create a temporary file to save the bitmap
        val imageFile = File.createTempFile(
            "image_${System.currentTimeMillis()}",
            ".jpg",
            imagesDir
        )

        // Save the bitmap to the temporary file
        return try {
            FileOutputStream(imageFile).use { outputStream ->
                this.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            // Create a Uri from the file
            Uri.fromFile(imageFile)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }




    fun WebView.hideElementByClassName(className: String) {
        this.evaluateJavascript(
            """
        (function() {
            var elements = document.getElementsByClassName('$className');
            for (var i = 0; i < elements.length; i++) {
                elements[i].style.display = 'none';
            }
        })();
        """.trimIndent(), null
        )
    }

    fun WebView.hideElementByTagName(tagName: String) {
        this.evaluateJavascript(
            """
        (function() {
            var elements = document.getElementsByTagName('$tagName');
            for (var i = 0; i < elements.length; i++) {
                elements[i].style.display = 'none';
            }
        })();
        """.trimIndent(), null
        )
    }

    fun WebView.hideElementById(id: String) {
        this.evaluateJavascript(
            """
        (function() {
            var element = document.getElementById('$id');
            if (element) {
                element.style.display = 'none';
            }
        })();
        """.trimIndent(), null
        )
    }




    fun createTextView(
        context: Context,
        text: String,
        gravity: Int,
        width: Int,
        height: Int,
        textSize: Float = 16f,  // Default text size in sp
        marginLeft: Int = 0,   // Default margin
        marginTop: Int = 0,
        marginRight: Int = 0,
        marginBottom: Int = 0,
        isBold: Boolean = false  // Default not bold
    ): TextView {
        val textView = TextView(context)

        val layoutParams = LinearLayout.LayoutParams(width, height)
        layoutParams.setMargins(marginLeft, marginTop, marginRight, marginBottom)
        textView.layoutParams = layoutParams

        textView.gravity = gravity
        textView.text = text
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)

        if (isBold) {
            textView.setTypeface(null, Typeface.BOLD)  // Set typeface to bold
        }

        return textView
    }


    fun createButton(
        context: Context,
        text: String,
        gravity: Int,
        width: Int,
        height: Int,
        textColorRes: Int = com.lymors.lycommons.R.color.cement  // Color resource for the text color
    ): Button {
        val button = Button(context)
        val layoutParams = LinearLayout.LayoutParams(width, height)
        button.layoutParams = layoutParams
        button.gravity = gravity
        button.text = text
        button.text = text.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        button.isAllCaps = false
        button.setTextColor(ContextCompat.getColor(context, textColorRes))  // Set text color
        return button
    }


    fun createLinearLayout(context: Context,orientation: Int = LinearLayout.VERTICAL, gravity: Int = Gravity.CENTER, left:Int = 15, top:Int=15, right:Int=15, bottom:Int=15): LinearLayout {
        val linearLayout = LinearLayout(context)
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(left,top,right,bottom)
        linearLayout.layoutParams = layoutParams
        linearLayout.orientation = orientation
        linearLayout.gravity = gravity
        return linearLayout
    }















    fun RecyclerView.convertRecyclerViewToPdf(
        context: Activity,
        pdfFileName: String,
        itemBottomMargin: Int = 15
    ): String? {
        val adapter = this.adapter ?: return null

        // Create a PdfDocument
        val pdfDocument = PdfDocument()

        // Get the dimensions of the view
        val recyclerViewWidth = this.width
        var totalHeight = 0

        // Calculate the margins (20% on each side)
        val margin = (recyclerViewWidth * 0.12).toInt()
        val contentWidth = recyclerViewWidth - 2 * margin

        // Measure and layout each item view
        for (i in 0 until adapter.itemCount) {
            val holder = adapter.createViewHolder(this, adapter.getItemViewType(i))
            adapter.bindViewHolder(holder, i)
            val itemView = holder.itemView

            itemView.measure(
                View.MeasureSpec.makeMeasureSpec(contentWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            itemView.layout(0, 0, itemView.measuredWidth, itemView.measuredHeight)
            totalHeight += itemView.measuredHeight + itemBottomMargin
        }

        // Define page height (you can adjust this as needed)
        val pageHeight = 297 * 72 / 25 // A4 page height in points

        // Variables to keep track of the current page and its height
        var currentPageHeight = 0
        var currentPage: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var pageNumber = 1

        // Create pages and draw each item view onto the pages
        for (i in 0 until adapter.itemCount) {
            val holder = adapter.createViewHolder(this, adapter.getItemViewType(i))
            adapter.bindViewHolder(holder, i)
            val itemView = holder.itemView

            itemView.measure(
                View.MeasureSpec.makeMeasureSpec(contentWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            itemView.layout(0, 0, itemView.measuredWidth, itemView.measuredHeight)

            if (currentPage == null || currentPageHeight + itemView.measuredHeight + itemBottomMargin > pageHeight) {
                if (currentPage != null) {
                    pdfDocument.finishPage(currentPage)
                }
                val pageInfo = PdfDocument.PageInfo.Builder(recyclerViewWidth, pageHeight, pageNumber).create()
                currentPage = pdfDocument.startPage(pageInfo)
                canvas = currentPage.canvas
                currentPageHeight = 0
                pageNumber++
            }

            canvas?.let {
                it.save()
                // Translate to the margin position
                it.translate(margin.toFloat(), currentPageHeight.toFloat())
                itemView.draw(it)
                it.restore()
                currentPageHeight += itemView.measuredHeight + itemBottomMargin
            }
        }

        currentPage?.let {
            pdfDocument.finishPage(it)
        }

        // Save the PDF to a file
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), pdfFileName)
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        // Close the PdfDocument
        pdfDocument.close()

        val uri = addPdfToMediaStore(context, file, pdfFileName)

        // Return the URI of the saved PDF file
        return uri.toString()
    }


    fun RecyclerView.convertRecyclerViewToPdf(
        context: Activity,
        pdfFileName: String,
        headerView: View? = null,
        footerView: View? = null,
        itemBottomMargin: Int = 15
    ): String? {

        val adapter = this.adapter ?: return null
        // Create a PdfDocument
        val pdfDocument = PdfDocument()
        val recyclerViewWidth = this.width

        // Calculate the margins (12% on each side)
        val margin = (recyclerViewWidth * 0.12).toInt()
        val contentWidth = recyclerViewWidth - 2 * margin
        val pageHeight = 297 * 72 / 25 // A4 page height in points

        // Measure and layout a view
        fun measureAndLayoutView(view: View) {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(contentWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        }

        // Draw a view on the canvas
        fun drawViewOnCanvas(view: View, canvas: Canvas, yOffset: Int): Int {
            canvas.save()
            canvas.translate(margin.toFloat(), yOffset.toFloat())
            view.draw(canvas)
            canvas.restore()
            return yOffset + view.measuredHeight + itemBottomMargin
        }

        // Variables to keep track of the current page and its height
        var currentPageHeight = 0
        var pageNumber = 1

        // Function to start a new page
        fun startNewPage(): Pair<PdfDocument.Page, Canvas?> {
            val pageInfo = PdfDocument.PageInfo.Builder(recyclerViewWidth, pageHeight, pageNumber).create()
            val newPage = pdfDocument.startPage(pageInfo)
            pageNumber++
            currentPageHeight = 0
            return newPage to newPage.canvas
        }

        // Initialize the first page
        var (currentPage, canvas) = startNewPage()

        // Draw the header if present
        headerView?.let {
            measureAndLayoutView(it)
            if (currentPageHeight + it.measuredHeight + itemBottomMargin > pageHeight) {
                pdfDocument.finishPage(currentPage)
                val newPage = startNewPage()
                currentPage = newPage.first
                canvas = newPage.second
            }
            canvas?.let { c -> currentPageHeight = drawViewOnCanvas(it, c, currentPageHeight) }
        }

        // Draw each RecyclerView item
        for (i in 0 until adapter.itemCount) {
            val holder = adapter.createViewHolder(this, adapter.getItemViewType(i))
            adapter.bindViewHolder(holder, i)
            val itemView = holder.itemView

            measureAndLayoutView(itemView)

            if (currentPageHeight + itemView.measuredHeight + itemBottomMargin > pageHeight) {
                pdfDocument.finishPage(currentPage)
                val newPage = startNewPage()
                currentPage = newPage.first
                canvas = newPage.second
            }

            canvas?.let { c -> currentPageHeight = drawViewOnCanvas(itemView, c, currentPageHeight) }
        }

        // Draw the footer if present
        footerView?.let {
            measureAndLayoutView(it)
            if (currentPageHeight + it.measuredHeight + itemBottomMargin > pageHeight) {
                pdfDocument.finishPage(currentPage)
                val newPage = startNewPage()
                currentPage = newPage.first
                canvas = newPage.second
            }
            canvas?.let { c -> currentPageHeight = drawViewOnCanvas(it, c, currentPageHeight) }
        }

        pdfDocument.finishPage(currentPage)

        // Save the PDF to a file
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), pdfFileName)
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        // Close the PdfDocument
        pdfDocument.close()

        val uri = addPdfToMediaStore(context, file, pdfFileName)

        // Return the URI of the saved PDF file
        return uri.toString()
    }





    private fun addPdfToMediaStore(context: Activity,pdfFile: File, displayName: String): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, displayName)
            put(MediaStore.Files.FileColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), values)

        uri?.let {
            try {
                resolver.openOutputStream(uri, "w")?.use { outputStream ->
                    pdfFile.inputStream().copyTo(outputStream)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }

        return uri
    }








    @RequiresApi(Build.VERSION_CODES.O)
    fun TextView.enableAutoSizingWithPresetSizes(
        presetSizes: IntArray,
        unit: Int = TypedValue.COMPLEX_UNIT_SP
    ) {
        setAutoSizeTextTypeUniformWithPresetSizes(presetSizes, unit)
    }






    fun Long.toDate(pattern: String = "dd-MM-yyyy"): String {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(Date(this))
    }
    fun Long.toTime(pattern: String = "hh:mm a"): String {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(Date(this))
    }
    fun Long.toDateTime(pattern: String = "dd-MM-yyyy hh:mm a"): String {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(Date(this))
    }






    fun Boolean.toggle(): Boolean {
        return !this
    }

    enum class TextStyle(
        val style: Int
    ) {
        BOLD(Typeface.BOLD),
        NORMAL(Typeface.NORMAL),
        ITALIC(Typeface.ITALIC),
        BOLD_ITALIC(Typeface.BOLD_ITALIC)
    }

    fun TextView.spannableTextFormat(
        wordToFormat: String,
        colorResId: Int = android.R.color.black,
        size: Float = 1f,
        style: TextStyle = TextStyle.NORMAL
    ) {
        val fullText = text.toString()
        val spannable = SpannableStringBuilder(fullText)
        val pattern = "\\b${Regex.escape(wordToFormat)}\\b".toRegex()
        val color = ContextCompat.getColor(context, colorResId)
        pattern.findAll(fullText).forEach { result ->
            val startIndex = result.range.first
            val endIndex = result.range.last + 1

            spannable.setSpan(
                ForegroundColorSpan(color),
                startIndex,
                endIndex,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                RelativeSizeSpan(size),
                startIndex,
                endIndex,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                StyleSpan(style.style),
                startIndex,
                endIndex,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )

        }

        text = spannable
    }


    fun View.applyRippleEffect(color: Int = android.R.color.holo_red_dark) {
        val rippleColor =
            ColorStateList.valueOf(ContextCompat.getColor(context, color))
        val rippleDrawable = RippleDrawable(rippleColor, null, null)
        foreground = rippleDrawable
    }

    fun Double.rounded(): String {
        return if (this == this.roundToInt().toDouble()) {
            this.roundToInt().toString()
        } else {
            String.format("%.1f", this)
        }
    }


    inline fun <reified T : Any> T.deepCopy(): T {
        val jsonString = Gson().toJson(this)
        return Gson().fromJson(jsonString, T::class.java)
    }

    fun Any.logT(append:String = "" , tag:String = "TAG"){
        if (this == null){
            Log.i(tag, "$append:null")
        }else{
        Log.i(tag, "$append:$this")
        }
    }


    inline fun <reified T : ViewBinding> Activity.viewBinding(
        crossinline bindingInflater: (LayoutInflater) -> T
    ): Lazy<T> {
        return lazy(LazyThreadSafetyMode.NONE) {
            bindingInflater.invoke(layoutInflater).also {
                setContentView(it.root)
            }
        }
    }


    fun JSONObject.toPrettyString(): String {
        return this.toString(4) // Indent by 4 spaces for pretty printing
    }
    fun JSONArray.toPrettyString(): String {
        return this.toString(4) // Indent by 4 spaces for pretty printing
    }

    fun JSONArray.toList(): List<Any> {
        val list = mutableListOf<Any>()
        for (i in 0 until this.length()) {
            list.add(this[i])
        }
        return list
    }


    fun JSONObject.toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val keys = this.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = this[key]
        }
        return map
    }

    fun List<Any>.toJsonArray(): JSONArray {
        return JSONArray(this)
    }

    fun Map<String, Any>.toJsonObject(): JSONObject {
        return JSONObject(this)
    }


    //start<NextActivity>()
    inline fun <reified T> Activity.start() {
        this.startActivity(Intent(this, T::class.java))
    }


    fun AppCompatActivity.setUpFragmentSlider(fragments:List<Fragment> , viewPager2: ViewPager2){
        viewPager2.adapter = object : androidx.viewpager2.adapter.FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size

            override fun createFragment(position: Int): Fragment {
                return fragments[position]
            }
        }

    }

    fun AppCompatActivity.setupTabLayout(
        tabLayout: TabLayout,
        viewPager2: ViewPager2,
        tabTextList: List<String>,
        fragments: List<Fragment>,
        initialPosition: Int = 0
    ) {
        viewPager2.adapter = object : androidx.viewpager2.adapter.FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size

            override fun createFragment(position: Int): Fragment {
                return fragments[position]
            }
        }
        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            tab.text = tabTextList[position]
        }.attach()
        viewPager2.setCurrentItem(initialPosition, false)
    }



    fun Long.getFormattedDateAndTime(pattern:String = "hh:mm:ss a"): String {
        return try {
            // Assuming currentTimeMillis is a string representation of a Long

            val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
            val date = Date(this)
            dateFormat.format(date)
        } catch (e: NumberFormatException) {
            "Invalid timestamp" // or handle the error in another way
        }
    }



//    val Fragment.dialogUtil: DialogUtil
//        get() = DialogUtil()
//    val Activity.dialogUtil: DialogUtil
//        get() = DialogUtil()

    val Activity.dataStore: DataStoreManager
        get() = DataStoreManager(this)


    @SuppressLint("ClickableViewAccessibility")
    fun TextInputEditText.setOptions(options: List<String>) {
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
    fun MaterialAutoCompleteTextView.setOptions(options: List<String>) {
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






    fun showWithRevealAnimation(showView: View, hideView:View) {
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



    fun hideWithRevealAnimation(hideView:View,showView:View){
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

    fun Context.showToast(message: Any, duration: Int = Toast.LENGTH_SHORT) {
        CoroutineScope(Dispatchers.Main).launch {
            val toast = Toast.makeText(this@showToast, message.toString(), duration)
            toast.show()
        }
    }

    fun Context.showLongTextToast(text: String, gravity: Int = Gravity.BOTTOM) {
        val inflater = LayoutInflater.from(this)
        val layout = inflater.inflate(R.layout.custom_toast, null)
        val textView = layout.findViewById<TextView>(R.id.toast_text)
        textView.text = text

        val toast = Toast(this)
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.setGravity(gravity, 0, 100) // Set gravity and offsets (x, y)
        toast.show()
    }




    // Extension function to format Long as date string
    fun Long.asDate(pattern: String = "yyyy-MM-dd HH:mm:ss a"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        val date = Date(this)
        return sdf.format(date)
    }

    fun EditText.showSoftKeyboard() {
        this.requestFocus()
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)

        this.post{
            val initialText = this.text.toString()
            if (initialText.isNotEmpty()){
                val length = initialText.length
                this.setSelection(length)
            }
        }
    }

    fun EditText.setCursorToEndOnFocus(){
        this.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus){
                this.post{
                    val initialText = this.text.toString()
                    if (initialText.isNotEmpty()){
                        val length = initialText.length
                        this.setSelection(length)
                    }
                }
            }
        }

        this.setOnClickListener {
            this.post{
                val initialText = this.text.toString()
                if (initialText.isNotEmpty()){
                    val length = initialText.length
                    this.setSelection(length)
                }
            }
        }
    }

    fun EditText.onFocus(callback: () -> Unit){
        this.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus){
                callback.invoke()
            }
        }
    }





    fun EditText.showSoftKeyboardForce() {
        runDelay {
            this.requestFocus()
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)

            if (!inputMethodManager.isActive(this)) {
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            }

            this.post{
                val initialText = this.text.toString()
                if (initialText.isNotEmpty()){
                    val length = initialText.length
                    this.setSelection(length)
                }
            }
        }
    }


fun runDelay(delay:Long =400 , callback: () -> Unit){
    Handler(Looper.getMainLooper()).postDelayed({
        callback.invoke()
    }, delay)
}



    fun EditText.hideSoftKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }


    inline fun <reified T : ViewBinding> AppCompatActivity.createBottomDialog(
        crossinline bindingInflater: (LayoutInflater) -> T,
        callback: (T , Dialog) -> Unit = {_,_ ->}
    ): Dialog {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val binding = bindingInflater(LayoutInflater.from(this))
        dialog.setContentView(binding.root)

        // Calculate 90% of the screen width
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
        dialog.window!!.attributes.windowAnimations = com.lymors.lycommons.R.style.DialogAnimation
        dialog.show()
        callback(binding , dialog)
        return dialog
    }



    inline fun <reified T : ViewBinding> Fragment.createBottomDialog(
        crossinline bindingInflater: (LayoutInflater) -> T,
        callback: (T , Dialog) -> Unit = {_,_ ->}
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
        dialog.window!!.attributes.windowAnimations = com.lymors.lycommons.R.style.DialogAnimation
        dialog.show()
        callback(binding , dialog)
        return dialog
    }





    fun <T> List<T>.toArrayList(): ArrayList<T> {
        val arrayList = ArrayList<T>()
        arrayList.addAll(this)
        return arrayList
    }


    fun Any.shrink(): Map<String, Any> {
        val propertiesMap = mutableMapOf<String, Any>()
        this::class.memberProperties.forEach { prop ->
            prop.isAccessible = true
            if (!prop.name.startsWith("_")) { // Filter out properties starting with "_"
                val value = prop.getter.call(this)
                if (value!=null)  {
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
                        is Char -> if (value != '\u0000') propertiesMap[prop.name] = value // '\u0000' is the null char
                        is Set<*> -> if (value.isNotEmpty()) propertiesMap[prop.name] = value.map { it?.shrink() }
                        is Map<*, *> -> if (value.isNotEmpty()) propertiesMap[prop.name] = value.mapValues { it.value?.shrink() }
                        is Enum<*> -> propertiesMap[prop.name] = value.name
                        is LatLng -> if (value.lat != 0.0 || value.lng != 0.0) propertiesMap[prop.name] = LatLng(value.lat , value.lng)
                        else -> propertiesMap[prop.name] = value.shrink()
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
                is Int ->  propertiesMap[prop.name] = value
                is Boolean ->  propertiesMap[prop.name] = value
                is Double ->  propertiesMap[prop.name] = value
                is Long ->  propertiesMap[prop.name] = value
                is List<*> ->  propertiesMap[prop.name] = value
                is Float ->  propertiesMap[prop.name] = value
                is Short ->  propertiesMap[prop.name] = value
                is Byte ->  propertiesMap[prop.name] = value
                is Char ->  propertiesMap[prop.name] = value
                is Set<*> ->  propertiesMap[prop.name] = value
                is Map<*, *> -> propertiesMap[prop.name] = value
                is Enum<*> -> propertiesMap[prop.name] = value.name
                is Any -> propertiesMap[prop.name] = value.shrink()
            }
        }
        return propertiesMap
    }



    fun View.setOnNetCheckClickListener(callback: (Boolean) -> Unit) {
        this.setOnClickListener {
            checkInternetConnection(context) { isInternetAvailable ->
                callback(isInternetAvailable)
            }
        }
    }

    fun  Any.toJsonString():String{
        val gson= Gson()
        val map=this.shrink()
        val jsonString=gson.toJson(map)
        return jsonString
    }



    @SuppressLint("ObsoleteSdkInt")
    private fun checkInternetConnection(context: Context, callback: (Boolean) -> Unit) { // make it suspend network working .
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return
            val activeNetwork =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return

            callback(
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            )
        } else {
            // For devices below Android M
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            callback(activeNetworkInfo != null && activeNetworkInfo.isConnected)
        }
    }

    suspend fun Context.isInternetAccessible(): Boolean {
        return try {
            val url = URL("https://www.google.com")
            val connection = withContext(Dispatchers.IO) { url.openConnection() }
            connection.connectTimeout = 5000 // 5 seconds
            withContext(Dispatchers.IO) { connection.connect() }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun Context.isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        } catch (e: NullPointerException) {
            // Handle NullPointerException
            Log.e("NetworkAvailability", "NullPointerException occurred", e)
            false
        } catch (e: SecurityException) {
            // Handle SecurityException
            Log.e("NetworkAvailability", "SecurityException occurred", e)
            false
        } catch (e: Exception) {
            // Handle any other exceptions
            Log.e("NetworkAvailability", "Exception occurred", e)
            false
        }
    }

    // Extension function to start an activity with a delay
    fun Context.startActivityWithDelay(delayMillis: Long, targetActivity: Class<out Activity>) {
        val intent = Intent(this, targetActivity)
        if (this is Activity) {
            this.window.decorView.postDelayed({
                startActivity(intent)
            }, delayMillis)
        } else {
            this.applicationContext.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }



    // Extension function to convert a Drawable to a Bitmap
    fun Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable) {
            return this.bitmap
        }
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }





//    check button


    fun CheckBox.setCheckedColor(color: Int) {
        buttonTintList = context.getColorStateList(color)
    }

    fun CheckBox.setUncheckedColor(color: Int) {
        buttonTintList = context.getColorStateList(color)
    }

//    fun CheckBox.setCheckDrawable(drawableResId: Int) {
//        buttonDrawable = drawableResId
//    }

//    fun CheckBox.setUncheckDrawable(drawableResId: Int) {
//        buttonDrawable = drawableResId
//    }

    fun CheckBox.setCheckedTintColor(color: Int) {
        buttonDrawable?.let { drawable ->
            val wrappedDrawable = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(wrappedDrawable, color)
            buttonDrawable = wrappedDrawable
        }
    }

    fun CheckBox.setUncheckedTintColor(color: Int) {
        buttonDrawable?.let { drawable ->
            val wrappedDrawable = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(wrappedDrawable, color)
            buttonDrawable = wrappedDrawable
        }
    }


//    fun CheckBox.setCheckedText(text: CharSequence) {
//        // implement later
//
//    }
//
//    fun CheckBox.setUncheckedText(text: CharSequence) {
//        // implement later
//    }

    fun CheckBox.setOnCheckedChangeListenerCompat(listener: (Boolean) -> Unit) {
        setOnCheckedChangeListener { _, isChecked -> listener(isChecked) }
    }

    fun CheckBox.setCheckedWithAnimation(checked: Boolean) {
        if (isChecked != checked) {
            toggle()
        }
    }

    fun CheckBox.toggle() {
        isChecked = !isChecked
    }


    // radio button

    fun RadioButton.setCheckedColor(color: Int) {
        buttonTintList = context.getColorStateList(color)
    }

    fun RadioButton.setUncheckedColor(color: Int) {
        buttonTintList = context.getColorStateList(color)
    }

    fun RadioButton.setCheckedTintColor(color: Int) {
        buttonDrawable?.let { drawable ->
            DrawableCompat.setTint(drawable, color)
        }
    }

    fun RadioButton.setUncheckedTintColor(color: Int) {
        buttonDrawable?.let { drawable ->
            DrawableCompat.setTint(drawable, color)
        }
    }

//    fun RadioButton.setCheckedText(text: CharSequence) {
//
//    }

//    fun RadioButton.setUncheckedText(text: CharSequence) {
//
//    }


    // seek bar
    fun SeekBar.setOnSeekBarChangeListenerCompat(listener: (Int) -> Unit) {
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                listener(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not implemented
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not implemented
            }
        })
    }


    // spinner

    fun Spinner.setItems(items: List<String>) {
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        this.adapter = adapter
    }

    fun Spinner.setItemsWithSelection(items: List<String>, selection: String?) {
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        this.adapter = adapter
        selection?.let { setSelectedItem(it) }
    }

    fun Spinner.setOnItemSelectedListenerCompat(listener: (Int) -> Unit) {
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                listener(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Not implemented
            }
        }
    }

    fun Spinner.setSelectedItem(item: String) {
        val position = (adapter as? ArrayAdapter<String>)?.getPosition(item)
        position?.let { setSelection(it) }
    }



    //  tablayout
    fun TabLayout.addTabWithText(text: String) {
        this.addTab(this.newTab().setText(text))
    }

    fun TabLayout.addTabWithIcon(iconResId: Int) {
        this.addTab(this.newTab().setIcon(iconResId))
    }

    fun TabLayout.addTabWithTextAndIcon(text: String, iconResId: Int) {
        this.addTab(this.newTab().setText(text).setIcon(iconResId))
    }

    fun TabLayout.addTabsWithText(textList: List<String>) {
        for (text in textList) {
            this.addTabWithText(text)
        }
    }

    fun TabLayout.addTabsWithIcons(iconList: List<Int>) {
        for (iconResId in iconList) {
            this.addTabWithIcon(iconResId)
        }
    }

    fun TabLayout.addTabsWithTextAndIcons(textIconList: List<Pair<String, Int>>) {
        for ((text, iconResId) in textIconList) {
            this.addTabWithTextAndIcon(text, iconResId)
        }
    }



    // viewpager

    fun ViewPager.addOnPageChangeListenerCompat(listener: (position: Int) -> Unit) {
        this.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // Not implemented
            }

            override fun onPageSelected(position: Int) {
                listener(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                // Not implemented
            }
        })
    }

    fun ViewPager.setCurrentPage(position: Int) {
        this.setCurrentItem(position, true)
    }

    fun ViewPager.getPageAt(position: Int): androidx.fragment.app.Fragment? {
        val adapter = this.adapter as? androidx.fragment.app.FragmentPagerAdapter
        return adapter?.getItem(position)
    }

    fun ViewPager.getTotalPages(): Int {
        val adapter = this.adapter
        return adapter?.count ?: 0
    }



    // webview
    fun WebView.enableJavaScript() {
        settings.javaScriptEnabled = true
    }


    // autoCompleteTextView

    fun AutoCompleteTextView.setAdapter(list: List<String>) {
        val myAdapter = ArrayAdapter(this.context, android.R.layout.simple_dropdown_item_1line, list)
        this.setAdapter(myAdapter)
    }



    fun AutoCompleteTextView.setOnItemSelectedAction(action: (position: Int) -> Unit) {
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                action(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    fun AutoCompleteTextView.selectItem(position: Int) {
        setSelection(position)
    }


// scrollview
fun ScrollView.scrollToView(view: View) {
    this.post { this.scrollTo(0, view.top) }
}

    // spinner
    fun Spinner.setAdapter(list: List<String>) {
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        this.adapter = adapter
    }

    fun Spinner.setOnItemSelectedListener(listener: AdapterView.OnItemSelectedListener) {
        onItemSelectedListener = listener
    }

    fun Spinner.setOnItemSelectedAction(action: (position: Int) -> Unit) {
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                action(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    fun Spinner.selectItem(position: Int) {
        setSelection(position)
    }

    fun Spinner.getSelectedItemPosition(): Int {
        return selectedItemPosition
    }


    fun CoroutineScope.launchSafe(
        block: suspend CoroutineScope.() -> Unit,
        onError: (Throwable) -> Unit = {}
    ): Job {
        return launch {
            try {
                block()
            } catch (e: Throwable) {
                onError(e)
            }
        }
    }


    fun CoroutineScope.repeatWithDelay(
        intervalMillis: Long,
        action: suspend () -> Unit
    ): Job {
        return launch {
            while (isActive) {
                action()
                delay(intervalMillis)
            }
        }
    }


    private fun <T : AppCompatActivity> startActivityWithDelay(activity:Activity,delayMillis: Long, destination: Class<T>) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(delayMillis)
           activity.startActivity(Intent(activity, destination))

        }
    }


    fun NotificationManager.showNotification(
        channelId: String,
        notificationId: Int,
        contentTitle: String,
        contentText: String,
        context: Context,
        smallIcon: Int= android.R.drawable.dialog_frame
    ) {

        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSmallIcon(smallIcon)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel Name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            createNotificationChannel(channel)
        }

        notify(notificationId, builder.build())
    }


    // bitmap

    fun Bitmap.getUri(context: Context):Uri{
        val bytes = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, this, "Image", null)
        return Uri.parse(path)
    }

    fun Uri.shareImage(context: Context, imgUri: Uri) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, imgUri)
            type = "image/*"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share image via"))
    }


}

