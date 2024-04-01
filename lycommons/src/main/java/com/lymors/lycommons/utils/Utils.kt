package com.lymors.lycommons.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.DimenRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.lymors.lycommons.R


object Utils {


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



    fun hideWithRevealAnimation(hideView: View, showView: View){
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


    fun String.showToast(context: Context, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, this, duration).show()
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


    fun processPhoneNumber(phoneNumber: String): String {
        val cleanedNumber = phoneNumber.replace("\\s".toRegex(), "")
        return if (phoneNumber.startsWith("03")) {
            cleanedNumber.replaceFirst("0","")
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









    inline fun <T, VB : ViewBinding> RecyclerView.setData(
        items: List<T>,
        crossinline bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> VB,
        crossinline bindHolder: (binding: VB, item: T, position: Int) -> Unit
    ) {
        val adapter = object : RecyclerView.Adapter<DataViewHolder<VB>>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder<VB> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = bindingInflater(layoutInflater, parent, false)
                return DataViewHolder(binding)
            }
            override fun onBindViewHolder(holder: DataViewHolder<VB>, position: Int) {
                bindHolder(holder.binding, items[position], position)
            }
            override fun getItemCount(): Int {
              return  items.size
            }
        }
        this.adapter = adapter
    }
    class DataViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)









    fun statusBarColor(context: Activity, color:Int= R.color.tool_bar_color){
        context.window.statusBarColor= ContextCompat.getColor(context,color)
    }



    fun View.setVisible(isVisible: Boolean) {
        visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun Context.isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun Context.openActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
    }

    fun View.showSnackbar(message: String) {
        // Implement your Snackbar logic here
    }

    fun Activity.hideSoftKeyboard() {
        currentFocus?.let {
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    fun String.isValidEmail(): Boolean {
        // Implement email validation logic
        return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }


    fun Activity.startNewTaskActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun View.setClickListener(onClick: () -> Unit) {
        setOnClickListener { onClick.invoke() }
    }

    fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { it.capitalize() }
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

    @SuppressLint("NewApi")
    fun Activity.setTransparentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = Color.TRANSPARENT
        }
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
