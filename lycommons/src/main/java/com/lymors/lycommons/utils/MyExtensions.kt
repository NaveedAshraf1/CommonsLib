package com.lymors.lycommons.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.Html
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.lymors.lycommons.R
import com.lymors.lycommons.managers.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.joery.animatedbottombar.AnimatedBottomBar
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.lang.Error
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.full.memberProperties


object MyExtensions {

    suspend inline fun <T, R> T.onMain(crossinline block: (T) -> R): R = withContext(Dispatchers.Main) { this@onMain.let(block) }
    suspend inline fun <T> onMain(crossinline block: CoroutineScope.() -> T): T = withContext(Dispatchers.Main) { block.invoke(this@withContext) }


    suspend inline fun <T, R> T.onIO(crossinline block: (T) -> R): R = withContext(Dispatchers.IO) { this@onIO.let(block) }
    suspend inline fun <T> onIO(crossinline block: CoroutineScope.() -> T): T = withContext(Dispatchers.IO) { block.invoke(this@withContext) }


    fun EditText.isEmpty(): Boolean {
        return text.toString().isEmpty()
    }
    fun String.openUrlInBrowser(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(this))
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
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


    fun String.toJsonArray(): JSONArray {
        return try {
            JSONArray(this)
        } catch (e: Exception) {
            JSONArray()
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



    //start<NextActivity>()
    inline fun <reified T> Activity.start() {
        this.startActivity(Intent(this, T::class.java))
    }

    suspend fun runDelayed(delay: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) {
        return suspendCoroutine { continuation ->
            GlobalScope.launch(Dispatchers.Main) {
                delay(timeUnit.toMillis(delay))
                continuation.resume(Unit)
            }
        }
    }


    fun AppCompatActivity.setupTabLayout(
        tabLayout: TabLayout,
        viewPager2: ViewPager2,
        tabTextList: List<String>,
        fragments: List<Fragment>
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

    fun View.showSnackbar(message: String) {
        Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
    }

    val Activity.dialogUtil: DialogUtil
        get() = DialogUtil(this)

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

    fun Context.showToast(message: String , duration:Int = Toast.LENGTH_SHORT) {
        var v = this
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(v, message, duration).show()
        }
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


    fun View.animateFromLeft() {
        val slideLeftAnimation = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 1.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f
        )
        slideLeftAnimation.duration = 300
        this.startAnimation(slideLeftAnimation)
    }

    fun View.animateFromRight() {
        val slideRightAnimation = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, -1.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f
        )
        slideRightAnimation.duration = 300
        this.startAnimation(slideRightAnimation)
    }

    fun View.fadeInAnimation(duration: Long = 300) {
        val fadeInAnimation = AlphaAnimation(0f, 1f)
        fadeInAnimation.duration = duration
        this.startAnimation(fadeInAnimation)
    }

    fun View.fadeOutAnimation(duration: Long = 300) {
        val fadeOutAnimation = AlphaAnimation(1f, 0f)
        fadeOutAnimation.duration = duration
        this.startAnimation(fadeOutAnimation)
    }

    fun View.animateRotateClockwise(duration: Long = 300) {
        val rotateAnimation = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotateAnimation.duration = duration
        this.startAnimation(rotateAnimation)
    }

    fun View.animateRotateAntiClockwise(duration: Long = 300) {
        val rotateAnimation = RotateAnimation(0f, -360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotateAnimation.duration = duration
        this.startAnimation(rotateAnimation)
    }

    fun View.animateScaleIn(duration: Long = 300) {
        val scaleAnimation = ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scaleAnimation.duration = duration
        this.startAnimation(scaleAnimation)
    }

    fun View.animateScaleOut(duration: Long = 300) {
        val scaleAnimation = ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scaleAnimation.duration = duration
        this.startAnimation(scaleAnimation)
    }

    fun View.animateBounce(duration: Long = 300) {
        val bounceAnimation = ScaleAnimation(0.9f, 1.1f, 0.9f, 1.1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        bounceAnimation.duration = duration
        bounceAnimation.repeatCount = 1
        bounceAnimation.repeatMode = Animation.REVERSE
        this.startAnimation(bounceAnimation)
    }

    fun View.animateShake(duration: Long = 300) {
        val shakeAnimation = TranslateAnimation(0f, 10f, 0f, 0f)
        shakeAnimation.duration = duration / 6
        shakeAnimation.repeatCount = 5
        shakeAnimation.repeatMode = Animation.REVERSE
        this.startAnimation(shakeAnimation)
    }

    fun View.animateFlip(duration: Long = 300) {
        val flipAnimation = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        flipAnimation.duration = duration
        this.startAnimation(flipAnimation)
    }


    fun View.animateZoomIn(duration: Long = 300) {
        val zoomInAnimation = ScaleAnimation(0.5f, 1f, 0.5f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        zoomInAnimation.duration = duration
        this.startAnimation(zoomInAnimation)
    }

    fun View.animateZoomOut(duration: Long = 300) {
        val zoomOutAnimation = ScaleAnimation(1f, 0.5f, 1f, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        zoomOutAnimation.duration = duration
        this.startAnimation(zoomOutAnimation)
    }


    fun Long.asDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss a", Locale.getDefault())
        val date = Date(this)
        return sdf.format(date)
    }



    fun EditText.showSoftKeyboard() {
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


    fun EditText.hideSoftKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }



    inline fun <reified T : ViewBinding> Fragment.createBottomSheet(
        crossinline bindingInflater: (LayoutInflater) -> T,
        callback: (Dialog) -> Unit = {}
    ): T  {
        var dialogBinding = bindingInflater.invoke(layoutInflater)
        val dialog = BottomSheetDialog(requireActivity())
        dialog.setContentView(dialogBinding.root)
        dialog.show()
        callback(dialog)
        return dialogBinding
    }

    inline fun <reified T : ViewBinding> AppCompatActivity.createBottomSheet(
        crossinline bindingInflater: (LayoutInflater) -> T,
        callback: (Dialog) -> Unit = {}

    ): T {
        var dialogBinding = bindingInflater.invoke(layoutInflater)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogBinding.root)
        dialog.show()
        callback(dialog)
        return dialogBinding
    }





    inline fun <reified T : ViewBinding> Fragment.createBottomDialog(
        crossinline bindingInflater: (LayoutInflater) -> T,
        callback: (Dialog) -> Unit = {}
    ): T {

        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = bindingInflater(LayoutInflater.from(requireContext()))
        dialog.setContentView(binding.root)

        dialog.show()
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)

        callback(dialog)
        return binding
    }

    inline fun <reified T : ViewBinding> AppCompatActivity.createBottomDialog(
        crossinline bindingInflater: (LayoutInflater) -> T
    ): T {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = bindingInflater(LayoutInflater.from(this))
        dialog.setContentView(binding.root)

        // Add horizontal line as close icon
        val closeLine = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                2 // Adjust the height as needed
            )
            setBackgroundColor(Color.BLACK)
            setOnClickListener { dialog.dismiss() }
        }

        dialog.addContentView(closeLine, closeLine.layoutParams)

        dialog.show()
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
        return binding
    }





    inline fun <T : ViewBinding> Fragment.viewBinding(
        crossinline bindingInflater: (LayoutInflater) -> T
    ) = lazy(LazyThreadSafetyMode.NONE) {
        bindingInflater.invoke(layoutInflater)
    }


    /*
    simple default bottom Nav
    usage in activity

      val list = listOf(BlankFragment1(), BlankFragment2(), BlankFragment3())
        setupBottomNav(this, bottomNav, frameLayout, list)
     */
    fun AppCompatActivity.setupBottomNav(
        bottomNavigationView: BottomNavigationView,
        frameLayout: FrameLayout,
        fragmentsList: List<Fragment>
    ) {

        supportFragmentManager.beginTransaction()
            .replace(frameLayout.id, fragmentsList.first())
            .commit()

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            val itemId = menuItem.itemId

            val menuItems = bottomNavigationView.menu
            for (index in 0 until menuItems.size()) {
                val menu = menuItems.getItem(index)
                if (menu.itemId == itemId) {
                    val position = index
                    if (position in fragmentsList.indices) {
                        supportFragmentManager.beginTransaction()
                            .replace(frameLayout.id, fragmentsList[position])
                            .commit()
                        return@setOnNavigationItemSelectedListener true
                    }
                }
            }

            return@setOnNavigationItemSelectedListener false
        }
    }




    fun AppCompatActivity.setupBottomNav(
        bottomNavigationView: AnimatedBottomBar,
        frameLayout: FrameLayout,
        fragmentsList: List<Fragment>
    ) {
        supportFragmentManager.beginTransaction()
            .replace(frameLayout.id, fragmentsList.first())
            .commit()

        bottomNavigationView.setOnTabSelectListener(object : AnimatedBottomBar.OnTabSelectListener {
            override fun onTabSelected(lastIndex: Int, lastTab: AnimatedBottomBar.Tab?, newIndex: Int, newTab: AnimatedBottomBar.Tab) {

                supportFragmentManager.beginTransaction()
                    .replace(frameLayout.id, fragmentsList[newIndex])
                    .commit()


            }
        })
    }

















    fun EditText.value():String{
        return this.text.toString()
    }

    fun TextInputEditText.value():String{
        return this.text.toString()
    }


















    fun Any.shrink(): Map<String, Any> {
        val propertiesMap = mutableMapOf<String, Any>()
        this::class.memberProperties.forEach { prop ->
            val value = prop.getter.call(this)
            when (prop.returnType.toString()) {
                "kotlin.Boolean" -> if (value as Boolean) propertiesMap[prop.name] = value
                "kotlin.collections.List<kotlin.Any>" -> if ((value as List<*>).isNotEmpty()) propertiesMap[prop.name] = value
                "kotlin.Int" -> if (value as Int != 0) propertiesMap[prop.name] = value
                "kotlin.Double" -> if (value as Double != 0.0) propertiesMap[prop.name] = value
                "kotlin.Float" -> if (value as Float != 0.0f) propertiesMap[prop.name] = value
                "kotlin.Long" -> if (value as Long != 0L) propertiesMap[prop.name] = value
                "kotlin.collections.ArrayList<kotlin.Any>" -> if ((value as ArrayList<*>).isNotEmpty()) propertiesMap[prop.name] = value
                "kotlin.String" -> if (value as String != "") propertiesMap[prop.name] = value
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

    fun String.fromJson(clazz: Class<*>): Any? {
        val gson = Gson()
        return try {
            gson.fromJson(this, clazz)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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
    fun String.toIntOrDefault(defaultValue: Int): Int {
        return toIntOrNull() ?: defaultValue
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



//    textview
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
        val attrs = intArrayOf(android.R.attr.selectableItemBackground)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.elevation = elevation
        }
    }





    // imageview
    fun ImageView.loadImageFromUrl(url: String , placeHolder:Int = R.drawable.ic_launcher_background , error: Int = R.drawable.ic_launcher_background) {
        Glide.with(this.context)
            .load(url)
            .placeholder(placeHolder)
            .error(error)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(this)

    }

    fun ImageView.loadImageFromResource(resourceId: Int) {
        Glide.with(this.context)
            .load(resourceId)
            .into(this)
    }


    fun ImageView.makeCircular() {
        Glide.with(this.context)
            .load(this.drawable)
            .apply(RequestOptions.circleCropTransform())
            .into(this)
    }

    fun ImageView.setRoundedCorner(cornerRadius: Float) {
        Glide.with(this.context)
            .load(this.drawable)
            .apply(RequestOptions().transform(RoundedCorners(cornerRadius.toInt())))
            .into(this)
    }


    fun ImageView.setTintColor(color: Int) {
        setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    fun ImageView.setGrayscale() {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)
        val filter = ColorMatrixColorFilter(matrix)
        colorFilter = filter
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
        buttonDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    fun CheckBox.setUncheckedTintColor(color: Int) {
        buttonDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
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
        buttonDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    fun RadioButton.setUncheckedTintColor(color: Int) {
        buttonDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

//    fun RadioButton.setCheckedText(text: CharSequence) {
//
//    }

//    fun RadioButton.setUncheckedText(text: CharSequence) {
//
//    }

    fun RadioButton.setCheckedWithAnimation(checked: Boolean) {
        if (isChecked != checked) {
            isChecked = checked
        }
    }





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

    fun AutoCompleteTextView.setDropdownBackgroundResource(resourceId: Int) {
        setDropDownBackgroundResource(resourceId)
    }

    fun AutoCompleteTextView.setOnItemSelectedListener(listener: AdapterView.OnItemSelectedListener) {
        onItemSelectedListener = listener
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





    // activity
    // . showToast(message: String)
    fun Activity.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    fun ComponentActivity.getPermissionLauncher():ActivityResultLauncher<String>{
        return this.registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
    }

    fun Activity.startActivity(clazz: Class<*>) {
        startActivity(Intent(this, clazz))
    }

    fun Activity.startActivity(clazz: Class<*>, key: String, data: String) {
        var i = Intent(this, clazz)
        i.putExtra(key, data)
        startActivity(i)
    }

    // . setStatusBarColor(color: Int)
    fun Activity.setStatusBarColor(color: Int) {
        window.statusBarColor = color
    }

    // . setActionBarTitle(title: String)
    fun Activity.setActionBarTitle(title: String) {
        actionBar?.title = title
    }

    // . requestPermission(permission: String, requestCode: Int)
    fun Activity.requestPermission(permission: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(this, permission, requestCode)
    }

    // . checkPermission(permission: String)
    fun Activity.checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun Activity.openAppSettings() {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
    }

    fun Activity.vibrate(milliseconds: Long) {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(milliseconds)
        }
    }

    // . startActivityWithAnimation(clazz: Class<*>, enterAnim: Int, exitAnim: Int)
    fun Activity.startActivityWithAnimation(clazz: Class<*>, enterAnim: Int, exitAnim: Int) {
        startActivity(Intent(this, clazz))
        overridePendingTransition(enterAnim, exitAnim)
    }


    fun Activity.setTransparentStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
    }


    fun Activity.takeScreenshot() {
        val rootView = window.decorView.rootView
        rootView.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(rootView.drawingCache)
        rootView.isDrawingCacheEnabled = false
        // Save or share the bitmap as needed
    }


    fun Activity.restart() {
        startActivity(Intent(this, this::class.java))
        finish()
    }

    inline fun <reified T> Activity.getBinding(): T {
        val bindingClass = T::class.java
        val inflateMethod = bindingClass.getMethod("inflate", LayoutInflater::class.java)
        val inflater = LayoutInflater.from(this)
        @Suppress("UNCHECKED_CAST")
        return inflateMethod.invoke(null, inflater) as T
    }

    fun FragmentActivity.replaceFragment(frameLayoutId: Int,fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(frameLayoutId, fragment)
        transaction.commit()
    }

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

    // fragments
    fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(requireContext(), message, duration).show()
    }

    fun Fragment.navigateToFragment(frameLayoutId:Int ,fragment: Fragment, addToBackStack: Boolean = true) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(frameLayoutId, fragment)
        if (addToBackStack) transaction.addToBackStack(null)
        transaction.commit()
    }

    fun Fragment.openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

       fun Fragment.shareText(content: String, title: String = "Share via") {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, content)
        startActivity(Intent.createChooser(intent, title))
    }

    fun Fragment.hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
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




    fun String.shareImage(context: Context) {
        var uri = Uri.parse(this)
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/*"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share image via"))
    }




    fun Uri.shareImage(context: Context, imgUri: Uri) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, imgUri)
            type = "image/*"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share image via"))
    }






    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun Activity.onFlash(){
        val cameraManager : CameraManager =this.getSystemService(AppCompatActivity.CAMERA_SERVICE) as CameraManager
        try{
            var cameraId : String? = null
            cameraId = cameraManager.cameraIdList[0]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager?.setTorchMode(cameraId,true)
            }
        }catch (e: CameraAccessException){
            Toast.makeText(this, "Something wrong", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    fun Activity.offFlash(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val cameraManage = this.getSystemService(AppCompatActivity.CAMERA_SERVICE) as CameraManager
            try {
                val cameraId = cameraManage.cameraIdList[0]
                cameraManage.setTorchMode(cameraId,false)
            }catch (e: CameraAccessException){
                Toast.makeText(this, "Something wrong", Toast.LENGTH_LONG).show()
            }
        }
    }




}

