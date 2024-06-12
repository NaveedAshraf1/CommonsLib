package com.lymors.lycommons.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.annotation.DimenRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.lymors.lycommons.R
import com.lymors.lycommons.utils.MyExtensions.empty
import com.lymors.lycommons.utils.MyExtensions.logT
import com.lymors.lycommons.utils.MyExtensions.showToast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.reflect.Modifier
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Locale
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible


object Utils {


    fun getIpv4LocalHostAddress(): String {
        val ip = String.empty()
        NetworkInterface.getNetworkInterfaces()?.toList()?.map { networkInterface ->
            networkInterface.inetAddresses
                ?.toList()
                ?.find { !it.isLoopbackAddress && it is Inet4Address }
                ?.let { return it.hostAddress.orEmpty() }
        }
        return ip
    }




    fun View.setTint(color: Int) {
        backgroundTintList = ContextCompat.getColorStateList(context, color)
    }



    fun checkEditTexts(list: List<EditText>): Boolean {
        list.forEach {
            if (it.text.toString().isEmpty()) {
                if (it.tag.toString().isNotEmpty()) {
                    it.context.showToast("${it.tag}  must not be empty")
                } else if (it.hint.toString().isNotEmpty()) {
                    it.context.showToast("${it.hint}  must not be empty")
                } else {
                    it.context.showToast("${it.id}  must not be empty")
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


    @RequiresApi(Build.VERSION_CODES.O)
    fun printMethodNames(clazz: Class<*>) {
        val methods = clazz.declaredMethods
        println("Methods in class ${clazz.simpleName}:")
        for (method in methods) {
            val modifiers = Modifier.toString(method.modifiers)
            val returnType = method.returnType.simpleName
            val parameters =
                method.parameters.joinToString(", ") { "${it.type.simpleName} ${it.name}" }
            println("$modifiers $returnType ${method.name}($parameters)")
        }
    }


    fun pickImage(requestCode: Int, context: Activity) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        context.startActivityForResult(intent, requestCode)
    }

    @SuppressLint("IntentReset")
    fun pickVideo(requestCode: Int, context: Activity) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "video/*"
        context.startActivityForResult(intent, requestCode)
    }

    fun sendMessageToWhatsApp(context: Context, phoneNumber: String, message: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://wa.me/$phoneNumber/?text=${Uri.encode(message)}")
        context.startActivity(intent)
    }

    fun pickDocument(requestCode: Int, context: Activity) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/*"
        context.startActivityForResult(intent, requestCode)
    }


    fun sendMessage(number: String, message: String) {
        if (number.isEmpty() || message.isEmpty()) {
            val byteArray = message.toByteArray(charset("UTF-16"))
            val sms = String(byteArray, charset("UTF-16"))
            val smsManager: SmsManager = SmsManager.getDefault()
            val smsArray = smsManager.divideMessage(sms)
            smsManager.sendMultipartTextMessage(
                number, null, smsArray, null, null
            )
        } else {
            "number or message is empty".logT()

        }
    }


    fun View.setMargins(left: Int, top: Int, right: Int, bottom: Int) {
        val params = layoutParams as? ViewGroup.MarginLayoutParams ?: return
        params.setMargins(left, top, right, bottom)
        layoutParams = params
    }

    fun View.setWidth(width: Int) {
        val params = layoutParams ?: return
        params.width = width
        layoutParams = params
    }

    fun View.setHeight(height: Int) {
        val params = layoutParams ?: return
        params.height = height
        layoutParams = params
    }


    fun shareMyApp(context: Context, subject: String?, message: String) {
        try {
            val appUrl = "https://play.google.com/store/apps/details?id=" + context.packageName
            val i = Intent(Intent.ACTION_SEND)
            i.setType("text/plain")
            i.putExtra(Intent.EXTRA_SUBJECT, subject)
            var leadingText = """
            
            $message
            
            
            """.trimIndent()
            leadingText += appUrl + "\n\n"
            i.putExtra(Intent.EXTRA_TEXT, leadingText)
            context.startActivity(Intent.createChooser(i, "Share using"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendEmail(context: Context, sendTo: Array<String?>?, subject: String?, body: String?) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("plain/text")
        intent.putExtra(Intent.EXTRA_EMAIL, sendTo)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, body)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(intent, ""))
        }
    }


    fun showWithRevealAnimation(showView: View, hideView: View) {
        val centerX = (showView.left + showView.right) / 2
        val centerY = (showView.top + showView.bottom) / 2

        val finalRadius =
            kotlin.math.hypot(showView.width.toDouble(), showView.height.toDouble()).toFloat()
        val circularReveal =
            ViewAnimationUtils.createCircularReveal(showView, centerX, centerY, 0f, finalRadius)

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
        val initialRadius =
            Math.hypot(hideView.width.toDouble(), hideView.height.toDouble()).toFloat()
        val circularReveal =
            ViewAnimationUtils.createCircularReveal(hideView, centerX, centerY, initialRadius, 0f)
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


    fun processPhoneNumber(phoneNumber: String): String {
        val cleanedNumber = phoneNumber.replace("\\s".toRegex(), "")
        return if (phoneNumber.startsWith("03")) {
            cleanedNumber.replaceFirst("0", "")
        } else {
            cleanedNumber
        }
    }


    /**
    simple default bottom Nav
    usage in activity

    val list = listOf(BlankFragment1(), BlankFragment2(), BlankFragment3())
    setupBottomNav(this, bottomNav, frameLayout, list)
     **/
    fun AppCompatActivity.setupBottomNav(
        activity: AppCompatActivity,
        bottomNavigationView: BottomNavigationView,
        frameLayout: FrameLayout,
        fragmentsList: List<Fragment>
    ) {

        activity.supportFragmentManager.beginTransaction()
            .replace(frameLayout.id, fragmentsList.first())
            .commit()

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            val itemId = menuItem.itemId

            val menuItems = bottomNavigationView.menu
            for (index in 0 until menuItems.size()) {
                val menu = menuItems.getItem(index)
                if (menu.itemId == itemId) {
                    if (index in fragmentsList.indices) {
                        activity.supportFragmentManager.beginTransaction()
                            .replace(frameLayout.id, fragmentsList[index])
                            .commit()
                        return@setOnNavigationItemSelectedListener true
                    }
                }
            }

            return@setOnNavigationItemSelectedListener false
        }
    }


    /**
    val tabTextList = listOf("Tab 1", "Tab 2", "Tab 3")
    val fragmentsList = listOf(BlankFragment1(), BlankFragment2(), BlankFragment3())

    setupTabLayout(b.tabLayout, b.viewPager, tabTextList, fragmentsList)

     */
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


    // paging adapter
    fun <T, VB : ViewBinding> RecyclerView.setData(
        items: List<T>,
        animation: Animation? = null,
        pageSize:Int = 10,
        bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> VB,
        bindHolder: (binding: VB, item: T, position: Int) -> Unit,
        loadMore: (lastKey:Int) -> Unit,
    ) {
        val existingAdapter = this.adapter as? GenericPagingAdapter<T, VB>
        if (items.isNotEmpty()) {
            if (existingAdapter != null) {
                existingAdapter.updateData(items)
            } else {
                val adapter = GenericPagingAdapter(items, animation, bindingInflater, bindHolder, loadMore, pageSize
                )
                this.adapter = adapter
            }
        }
    }


    class DataViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)


    class GenericPagingAdapter<T, VB : ViewBinding>(
        items: List<T>,
        private val animation: Animation?,
        private val bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> VB,
        private val bindHolder: (binding: VB, item: T, position: Int) -> Unit,
        private val loadMore: (moreItems: Int) -> Unit,
        private val pageSize: Int
    ) : RecyclerView.Adapter<DataViewHolder<VB>>() {
        private var adapterList: List<T> = items

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder<VB> {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = bindingInflater(layoutInflater, parent, false)
            return DataViewHolder(binding)
        }

        override fun getItemCount(): Int = adapterList.size

        override fun onBindViewHolder(holder: DataViewHolder<VB>, position: Int) {
            position.logT("position" , "position")

            bindHolder(holder.binding, adapterList[position], position)
            animation?.let {
                holder.itemView.startAnimation(it)
            }

            if (position == adapterList.lastIndex) {
                    loadMore(adapterList.size+pageSize)
            }
        }

        private val handler = Handler(Looper.getMainLooper())
        fun updateData(newData: List<T>) {
            newData.logT("updateData")
            handler.post {
                adapterList.lastIndex.logT("lastIndex//")
                if (adapterList.size!= newData.size && newData.size>adapterList.size){
                    adapterList = newData
                    notifyDataSetChanged()
                }
                adapterList.size.logT("listSize//")
            }
        }

    }






    class GenericAdapter<T, VB : ViewBinding>(
        private var items: List<T>,
        private val bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> VB,
        private val bindHolder: (binding: VB, item: T, position: Int) -> Unit,
        private val animation: Animation?,
    ) : RecyclerView.Adapter<DataViewHolder<VB>>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder<VB> {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = bindingInflater(layoutInflater, parent, false)
            return DataViewHolder(binding)
        }
        override fun onBindViewHolder(holder: DataViewHolder<VB>, position: Int) {
            bindHolder(holder.binding, items[position], position)
            animation?.let {
                holder.itemView.startAnimation(it)
            }
        }
        override fun getItemCount(): Int = items.size
        fun updateData(newItems: List<T>) {
            newItems.logT("newItems")
            items = newItems
            notifyDataSetChanged()
        }
    }


    fun <T, VB : ViewBinding> RecyclerView.setData(
        items: List<T>,
        bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> VB,
        bindHolder: (binding: VB, item: T, position: Int) -> Unit,
    ) {
        val existingAdapter = this.adapter as? GenericAdapter<T, VB>
        if (existingAdapter != null) {
            existingAdapter.updateData(items)
        } else {
            val adapter = GenericAdapter(
                items,
                bindingInflater,
                bindHolder,
                animation
            )
            this.adapter = adapter
        }
    }


    fun <T, VB : ViewBinding> RecyclerView.setData(
        animation: Animation? = null,
        items: List<T>,
        bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> VB,
        bindHolder: (binding: VB, item: T, position: Int) -> Unit,

    ) {
        val existingAdapter = this.adapter as? GenericAdapter<T, VB>
        if (existingAdapter != null) {
            existingAdapter.updateData(items)
        } else {
            val adapter = GenericAdapter(items, bindingInflater, bindHolder, animation,)
            this.adapter = adapter
        }
    }



//    inline fun <T, VB : ViewBinding> RecyclerView.setData(
//        items: List<T>,
//        crossinline bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> VB,
//        crossinline bindHolder: (binding: VB, item: T, position: Int , adapter1: RecyclerView.Adapter<DataViewHolder<VB>>) -> Unit,
//        animation: Animation? = null
//    ) {
//        val adapter = object : RecyclerView.Adapter<DataViewHolder<VB>>() {
//            var adapterList = items
//            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder<VB> {
//                val layoutInflater = LayoutInflater.from(parent.context)
//                val binding = bindingInflater(layoutInflater, parent, false)
//                return DataViewHolder(binding)
//            }
//
//            override fun onBindViewHolder(holder: DataViewHolder<VB>, position: Int) {
//                bindHolder(holder.binding, adapterList[position], position , this)
//                if (animation != null) {
//                    holder.itemView.startAnimation(animation)
//                }
//            }
//            override fun getItemCount(): Int = adapterList.size
//        }
//        this.adapter = adapter
//    }
//    class DataViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)









    fun statusBarColor(context: Activity, color:Int= R.color.blue){
        context.window.statusBarColor= ContextCompat.getColor(context,color)
    }




    fun Context.openActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
    }

    fun View.convertToPdf(context: Context, pdfFileName: String): String? {
        val pdfDocument = PdfDocument()

        val width = this.width
        val height = this.height

        val pageInfo = PdfDocument.PageInfo.Builder(width, height, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val canvas = page.canvas
        this.draw(canvas)

        pdfDocument.finishPage(page)

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), pdfFileName)
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        pdfDocument.close()
//        val uri = addPdfToMediaStore(context, file.absolutePath, pdfFileName)
        val uri = file.toUri()
        return uri.toString()
    }


    fun Activity.hideSoftKeyboard() {
        currentFocus?.let {
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }



    fun Activity.startNewTaskActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun View.setClickListener(onClick: () -> Unit) {
        setOnClickListener { onClick.invoke() }
    }



    fun Context.dpToPx(dp: Float): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun View.fadeIn(duration: Long = 300) {
        alpha = 0f
        visibility = View.VISIBLE
        animate().alpha(1f).setDuration(duration).start()
    }

    fun View.fadeOut(duration: Long = 300) {
        animate().alpha(0f).setDuration(duration).withEndAction { visibility = View.GONE }.start()
    }

    fun Context.getVersionName(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
    }

    fun View.setPaddingRes(@DimenRes paddingRes: Int) {
        val padding = resources.getDimensionPixelSize(paddingRes)
        setPadding(padding, padding, padding, padding)
    }

    fun Activity.setTransparentStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)  // Recommended for new APIs
        window.statusBarColor = Color.TRANSPARENT
    }


    fun View.rotate(degrees: Float) {
        animate().rotation(degrees).setDuration(300).setInterpolator(
            AccelerateDecelerateInterpolator()
        )
            .withEndAction { rotation = degrees }.start()
    }

    fun Activity.shareText(text: String, subject: String = "") {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        startActivity(Intent.createChooser(intent, "Share"))
    }

    fun Activity.launchDialer(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivity(intent)
    }

    fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

}
