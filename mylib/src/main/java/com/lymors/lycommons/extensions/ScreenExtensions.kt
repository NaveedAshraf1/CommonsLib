package com.lymors.lycommons.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.window.Window
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lymors.lycommons.R
import com.lymors.lycommons.utils.MyPermissionHelper
import nl.joery.animatedbottombar.AnimatedBottomBar
import java.util.WeakHashMap
import android.view.Window
import kotlin.jvm.java

object ScreenExtensions {


    fun Activity.refresh() {
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }


    inline fun <reified MB : ViewBinding, DB : ViewBinding> Activity.setUpDrawer(
        mainActivityBinding: MB,
        crossinline drawerContentInflater: (LayoutInflater) -> DB,
        openDrawerButton: View,
        crossinline setupDrawerContent: (DrawerLayout, DB) -> Unit
    ) {
        val inflater = LayoutInflater.from(this)
        val drawerContentBinding = drawerContentInflater.invoke(inflater)

        val drawerLayout = DrawerLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        val mainActivityView = mainActivityBinding.root
        val parentViewGroup = mainActivityView.parent as? ViewGroup
        parentViewGroup?.removeView(mainActivityView)
        drawerLayout.addView(mainActivityView)
        val gravity = GravityCompat.START
        drawerLayout.addView(
            drawerContentBinding.root,
            DrawerLayout.LayoutParams(
                DrawerLayout.LayoutParams.MATCH_PARENT,
                DrawerLayout.LayoutParams.MATCH_PARENT,
                gravity
            )
        )
        setContentView(drawerLayout)
        openDrawerButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        setupDrawerContent(drawerLayout, drawerContentBinding)

        // Ensure drawer content is interactive and not clicking through
        drawerContentBinding.root.isClickable = true
        drawerContentBinding.root.isFocusable = true
        drawerContentBinding.root.isFocusableInTouchMode = true
    }


    // WeakHashMap to hold the picked image URI for each Activity instance
    private val activityPickedImageUriMap = WeakHashMap<Activity, Uri?>()
    var Activity.pickedImageUri: Uri?
        get() = activityPickedImageUriMap[this]
        set(value) {
            activityPickedImageUriMap[this] = value
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
                    if (index in fragmentsList.indices) {
                        supportFragmentManager.beginTransaction()
                            .replace(frameLayout.id, fragmentsList[index])
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
            override fun onTabSelected(
                lastIndex: Int,
                lastTab: AnimatedBottomBar.Tab?,
                newIndex: Int,
                newTab: AnimatedBottomBar.Tab
            ) {

                supportFragmentManager.beginTransaction()
                    .replace(frameLayout.id, fragmentsList[newIndex])
                    .commit()


            }
        })
    }


    fun Activity.launchActivityClearNewTask(
        destination: Class<*>,
        key: String = "",
        data: String = ""
    ) {
        val intent = Intent(this, destination)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        if (key.isNotEmpty()) {
            intent.putExtra(key, data)
        }
        startActivity(intent)
    }


    fun Activity.launchActivity(destination: Class<*>, key: String, data: Parcelable? = null) {
        val intent = Intent(this, destination)
        if (key.isNotEmpty() && data != null) {
            intent.putExtra(key, data)
        }
        startActivity(intent)
    }

    fun Activity.launchActivity(destination: Class<*>, key: String = "", data: String = "") {
        val intent = Intent(this, destination)
        if (key.isNotEmpty()) {
            intent.putExtra(key, data)
        }
        startActivity(intent)
    }


    fun CheckBox.toggle() {
        isChecked = !this.isChecked
    }


    // . setStatusBarColor(color: Int)
    fun Activity.setStatusBarColor(
        backgroundColor: Int = R.color.white,
        darkTextColor: Boolean = true
    ) {
        // Set the status bar background color
        this.window.statusBarColor = ContextCompat.getColor(this, backgroundColor)

        // Set the status bar text color to light or dark
        val decor = window.decorView
        if (darkTextColor) {
            // If lightTextColor is true, set the text color to dark
            decor.systemUiVisibility =
                decor.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // Otherwise, set the text color to light
            decor.systemUiVisibility =
                decor.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }


    // . setActionBarTitle(title: String)
    fun Activity.setActionBarTitle(title: String) {
        actionBar?.title = title
    }

    // . startActivityWithAnimation(clazz: Class<*>, enterAnim: Int, exitAnim: Int)
    fun Activity.startActivityWithAnimation(clazz: Class<*>, enterAnim: Int, exitAnim: Int) {
        startActivity(Intent(this, clazz))
        overridePendingTransition(enterAnim, exitAnim)
    }


    fun Activity.setStatusBarTransparent() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
    }


    fun Activity.statusBarColor(color: Int = R.color.blue) {
        this.window.statusBarColor = ContextCompat.getColor(this, color)
    }

    fun Activity.systemBottomNavigationColor(context: Context, color: Int = android.R.color.white) {
        this.window.navigationBarColor = ContextCompat.getColor(context, color)
    }


    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun Activity.turnOnFlash() {
        val cameraManager: CameraManager =
            this.getSystemService(AppCompatActivity.CAMERA_SERVICE) as CameraManager
        try {
            var cameraId: String? = null
            cameraId = cameraManager.cameraIdList[0]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, true)
            }
        } catch (e: CameraAccessException) {
            Toast.makeText(this, "Something wrong", Toast.LENGTH_LONG).show()
        }
    }


    fun Activity.turnOFFFlash() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cameraManage =
                this.getSystemService(AppCompatActivity.CAMERA_SERVICE) as CameraManager
            try {
                val cameraId = cameraManage.cameraIdList[0]
                cameraManage.setTorchMode(cameraId, false)
            } catch (e: CameraAccessException) {
                Toast.makeText(this, "Something wrong", Toast.LENGTH_LONG).show()
            }
        }
    }


    // fragments
    fun Fragment.showToast(message: Any, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(requireContext(), message.toString(), duration).show()
    }

    fun Fragment.navigateToFragment(
        frameLayoutId: Int,
        fragment: Fragment,
        addToBackStack: Boolean = true
    ) {
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
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }


    fun Activity.takeScreenshot() {
        val rootView = window.decorView.rootView
        rootView.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(rootView.drawingCache)
        rootView.isDrawingCacheEnabled = false
        // Save or share the bitmap as needed
    }

    fun Activity.setStatusBarTextColor(activity: Activity, isLight: Boolean) {
        val window = activity.window

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // API 30+ (Android 11 and later)
                window.insetsController?.setSystemBarsAppearance(
                    if (isLight) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                // API 23-29 (Android 6.0 to Android 10)
                val decorView = window.decorView
                decorView.systemUiVisibility = if (isLight) {
                    decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
            }
        }
    }

    /**
     * Sets the color of the status bar icons (text and icons) to light or dark.
     *
     * This method ensures compatibility across various Android versions (API 23 and above).
     *
     * @param isLight If true, status bar icons will be dark (suitable for a light status bar background).
     * If false, status bar icons will be light (suitable for a dark status bar background).
     */
    fun Activity.setStatusBarIconColor(isLight: Boolean) {
        val window: Window = this.window
        val decorView: View = window.decorView

        // 1. Ensure the window is edge-to-edge for consistent system bar behavior.
        // This allows the content to extend behind the system bars, and it's good practice
        // when manipulating system bar appearance.

        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 2. Get the WindowInsetsControllerCompat for cross-version compatibility.
        // This is the primary mechanism for controlling system bar appearance from API 23 onwards.
        val insetsController: WindowInsetsControllerCompat? =
            WindowCompat.getInsetsController(window, decorView)

        // Apply the light/dark appearance for status bar icons
        insetsController?.isAppearanceLightStatusBars = isLight

        // Optional: Set a default status bar background color.
        // This is often handled by your app's theme (e.g., in themes.xml),
        // but you might want to explicitly set it here if you need dynamic colors
        // or to ensure visibility against the icon color.
        // Example: If isLight=true, the icons are dark, so a light background like WHITE is suitable.
        // If isLight=false, the icons are light, so a dark background like BLACK is suitable.
        // window.statusBarColor = if (isLight) Color.WHITE else Color.BLACK

        fun Activity.restart() {
            startActivity(Intent(this, this::class.java))
            finish()
        }

        fun <T> Activity.getBinding(clazz: Class<T>): T {
            val inflateMethod = clazz.getMethod("inflate", LayoutInflater::class.java)
            val inflater = LayoutInflater.from(this)
            @Suppress("UNCHECKED_CAST")
            return inflateMethod.invoke(null, inflater) as T
        }

        fun FragmentActivity.replaceFragment(frameLayoutId: Int, fragment: Fragment) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(frameLayoutId, fragment)
            transaction.commit()
        }


    }
}