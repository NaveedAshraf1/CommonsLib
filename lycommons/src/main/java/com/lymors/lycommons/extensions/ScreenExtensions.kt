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
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lymors.lycommons.R
import com.lymors.lycommons.utils.MyPermissionHelper
import nl.joery.animatedbottombar.AnimatedBottomBar

object ScreenExtensions {

    var AppCompatActivity.myPermissionHelper: MyPermissionHelper
        get() = MyPermissionHelper(this)
        set(value) {
            MyPermissionHelper(this)
        }

    private var _pickedImageUri: Uri? = null

    var Activity.pickedImageUri: Uri?
        get() = _pickedImageUri
        set(value) {
            _pickedImageUri = value
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
            override fun onTabSelected(lastIndex: Int, lastTab: AnimatedBottomBar.Tab?, newIndex: Int, newTab: AnimatedBottomBar.Tab) {

                supportFragmentManager.beginTransaction()
                    .replace(frameLayout.id, fragmentsList[newIndex])
                    .commit()


            }
        })
    }




    fun Activity.launchActivity(destination: Class<*>) {
        // Create an Intent to launch the target activity
        val intent = Intent(this, destination::class.java)
        // Start the activity with the created Intent
        startActivity(intent)
    }


    fun Activity.launchActivity(destination: Class<*>, key: String = "", data: Parcelable? = null) {
        // Create an Intent to launch the target activity
        val intent = Intent(this, destination::class.java)

        // Put the data into the Intent using the specified key
        if (key.isNotEmpty() && data != null) {
            intent.putExtra(key, data)
        }

        // Start the activity with the created Intent
        startActivity(intent)
    }
    fun Fragment.launchActivity(destination: Class<*>, key: String = "", data: Parcelable? = null) {
        // Create an Intent to launch the target activity
        val intent = Intent(requireContext(), destination::class.java)

        // Put the data into the Intent using the specified key
        if (key.isNotEmpty() && data != null) {
            intent.putExtra(key, data)
        }

        // Start the activity with the created Intent
        startActivity(intent)
    }


    fun Activity.launchActivity(destination: Class<*>, key: String = "", data: String = "") {
        // Create an Intent to launch the target activity
        val intent = Intent(this, destination::class.java)

        // Put the data into the Intent using the specified key
        if (key.isNotEmpty()) {
            intent.putExtra(key, data)
        }

        // Start the activity with the created Intent
        startActivity(intent)
    }

    fun Fragment.launchActivity(destination:Class<*>, key: String = "", data:String = "") {
        // Create an Intent to launch the target activity
        val intent = Intent(requireContext(), destination::class.java)

        // Put the data into the Intent using the specified key
        if (key.isNotEmpty()){
            intent.putExtra(key, data)
        }

        // Start the activity with the created Intent
        startActivity(intent)
    }




    // activity
    // . showToast(message: String)
    fun Activity.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    fun ComponentActivity.getPermissionLauncher(): ActivityResultLauncher<String> {
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


    fun Activity.statusBarColor(color:Int= R.color.blue){
        this.window.statusBarColor= ContextCompat.getColor(this,color)
    }

    fun Activity.systemBottomNavigationColor(context: Context, color: Int=android.R.color.white) {
        this.window.navigationBarColor = ContextCompat.getColor(context, color)
    }


    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun Activity.turnOnFlash(){
        val cameraManager : CameraManager =this.getSystemService(AppCompatActivity.CAMERA_SERVICE) as CameraManager
        try{
            var cameraId : String? = null
            cameraId = cameraManager.cameraIdList[0]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId,true)
            }
        }catch (e: CameraAccessException){
            Toast.makeText(this, "Something wrong", Toast.LENGTH_LONG).show()
        }
    }


    fun Activity.turnOFFFlash(){
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

    fun FragmentActivity.replaceFragment(frameLayoutId: Int, fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(frameLayoutId, fragment)
        transaction.commit()
    }


}