package com.lymors.lycommons.extensions

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.Activity
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.ContextCompat
import android.Manifest
import android.graphics.Color
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.telephony.SmsManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.lymors.lycommons.R

import java.net.URL


object ContextExtensions {

    fun Context.showToast(message: Any, duration: Int = Toast.LENGTH_SHORT) {
        CoroutineScope(Dispatchers.Main).launch {
            val toast = Toast.makeText(this@showToast, message.toString(), duration)
            toast.show()
        }
    }

    fun Context.openActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
    }

    fun Context.dpToPx(dp: Float): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun Context.getVersionName(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
    }

    fun Activity.setTransparentStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
    }

    fun Context.shareText(text: String, subject: String = "") {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        startActivity(Intent.createChooser(intent, "Share"))
    }

    fun Context.launchDialer(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivity(intent)
    }

    fun Context.startActivityWithDelay(delayMillis: Long, targetActivity: Class<out Activity>) {
        val intent = Intent(this, targetActivity)
        if (this is Activity) {
            window.decorView.postDelayed({
                startActivity(intent)
            }, delayMillis)
        } else {
            applicationContext.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
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
            Log.e("NetworkAvailability", "NullPointerException occurred", e)
            false
        } catch (e: SecurityException) {
            Log.e("NetworkAvailability", "SecurityException occurred", e)
            false
        } catch (e: Exception) {
            Log.e("NetworkAvailability", "Exception occurred", e)
            false
        }
    }

    suspend fun Context.isInternetAccessible(): Boolean {
        return try {
            val url = URL("https://www.google.com")
            val connection = withContext(Dispatchers.IO) { url.openConnection() }
            connection.connectTimeout = 5000
            withContext(Dispatchers.IO) { connection.connect() }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun Activity.hideSoftKeyboard() {
        currentFocus?.let {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    fun Activity.startNewTaskActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun Activity.setUpBottomNavigationColor(color: Int = R.color.gray10) {
        window.navigationBarColor = ContextCompat.getColor(this, color)
    }

    fun AppCompatActivity.setUpFragmentSlider(fragments: List<Fragment>, viewPager2: ViewPager2) {
        viewPager2.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }
    }

    fun AppCompatActivity.setupTabLayout(
        tabLayout: TabLayout,
        viewPager2: ViewPager2,
        tabTextList: List<String>,
        fragments: List<Fragment>,
        initialPosition: Int = 0
    ) {
        viewPager2.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }
        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            tab.text = tabTextList[position]
        }.attach()
        viewPager2.setCurrentItem(initialPosition, false)
    }

    fun LifecycleOwner.launchWhenResumed(block: suspend () -> Unit) {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                block()
            }
        }
    }

    // Improved showToast with position and null safety
    fun Context?.showToastSafe(message: Any, duration: Int = Toast.LENGTH_SHORT, gravity: Int = Gravity.BOTTOM) {
        this?.let {
            CoroutineScope(Dispatchers.Main).launch {
                val toast = Toast.makeText(it, message.toString(), duration).apply {
                    setGravity(gravity, 0, if (gravity == Gravity.TOP) 100 else -100)
                }
                toast.show()
            }
        }
    }

    // Copy text to clipboard with fallback
    fun Context.copyToClipboard(text: String, label: String = "Copied Text") {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
    }

    // Get screen dimensions safely
    fun Context.getScreenWidth(): Int = resources.displayMetrics.widthPixels
    fun Context.getScreenHeight(): Int = resources.displayMetrics.heightPixels

    // Check if app is in foreground
    fun Context.isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = this.packageName
        return appProcesses.any { it.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && it.processName == packageName }
    }

    // Vibrate device with permission check
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun Context.vibrate(milliseconds: Long = 100) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(milliseconds)
        }
    }

    // Open app settings with edge case for API levels
    fun Context.openAppSettings() {
        val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    // Check single permission safely
    fun Context.hasPermission(permission: String): Boolean {
        return android.content.pm.PackageManager.PERMISSION_GRANTED == checkSelfPermission(permission)
    }

    // Open URL in browser with intent chooser
    fun Context.openUrl(url: String) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
            startActivity(android.content.Intent.createChooser(intent, "Open with"))
        } catch (e: Exception) {
            showToastSafe("Invalid URL")
        }
    }

    // Share image with URI validation
    fun Context.shareImage(imageUri: android.net.Uri, title: String = "Share Image") {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(android.content.Intent.EXTRA_STREAM, imageUri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(android.content.Intent.createChooser(intent, title))
        } catch (e: Exception) {
            showToastSafe("Unable to share image")
        }
    }

    // Get current locale safely
    fun Context.getCurrentLocale(): java.util.Locale {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            resources.configuration.locale
        }
    }

    // Check if GPS is enabled with location service check
    fun Context.isGpsEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
        return locationManager?.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ?: false
    }

    // Get available memory in MB
    fun Context.getAvailableMemory(): Long {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem / (1024 * 1024)
    }

    // Open dialer with number and validation
    fun Context.openDialer(phoneNumber: String) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:$phoneNumber"))
            startActivity(intent)
        } catch (e: Exception) {
            showToastSafe("Invalid phone number")
        }
    }

    // Send SMS with permission check
    fun Context.sendSms(phoneNumber: String, message: String) {
        if (hasPermission(android.Manifest.permission.SEND_SMS)) {
            try {
                val smsManager = android.telephony.SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                showToastSafe("SMS sent")
            } catch (e: Exception) {
                showToastSafe("Failed to send SMS")
            }
        } else {
            showToastSafe("SMS permission not granted")
        }
    }

    // Open email app with validation
    fun Context.openEmail(to: Array<String>, subject: String = "", body: String = "") {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:")
                putExtra(android.content.Intent.EXTRA_EMAIL, to)
                putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
                putExtra(android.content.Intent.EXTRA_TEXT, body)
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                showToastSafe("No email app found")
            }
        } catch (e: Exception) {
            showToastSafe("Failed to open email")
        }
    }

    // Open map with coordinates
    fun Context.openMap(latitude: Double, longitude: Double, label: String = "Location") {
        try {
            val uri = android.net.Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (e: Exception) {
            showToastSafe("No map app found")
        }
    }

    // Open camera with intent
    fun Context.openCamera() {
        try {
            val intent = android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            startActivity(intent)
        } catch (e: Exception) {
            showToastSafe("No camera app found")
        }
    }

    // Check battery level
    fun Context.getBatteryLevel(): Int {
        val intent = registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level != -1 && scale != -1) (level * 100) / scale else -1
    }

    // Get network type
    fun Context.getNetworkType(): String {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "No Network"
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "Unknown"
        return when {
            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Other"
        }
    }

    // Open settings for specific permission
    fun Context.openPermissionSettings(permission: String) {
        val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    // Check if notification permission granted (API 33+)
    fun Context.hasNotificationPermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            hasPermission(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true // Granted by default on lower APIs
        }
    }

    // Open app in Play Store
    fun Context.openInPlayStore() {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=$packageName"))
            startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            openUrl("https://play.google.com/store/apps/details?id=$packageName")
        }
    }

    // Get app version code safely
    fun Context.getVersionCode(): Long {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
            0L
        }
    }

    // Check if app is installed
    fun Context.isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
            false
        }
    }

    // Open file with intent
    fun Context.openFile(uri: android.net.Uri, mimeType: String? = null) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType ?: "*/*")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(android.content.Intent.createChooser(intent, "Open with"))
        } catch (e: Exception) {
            showToastSafe("Unable to open file")
        }
    }

    // Get external storage directory path
    fun Context.getExternalStoragePath(): String? = getExternalFilesDir(null)?.absolutePath

    // Check if device is rooted (basic check)
    fun Context.isDeviceRooted(): Boolean {
        val paths = arrayOf("/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su")
        return paths.any { java.io.File(it).exists() }
    }

    // Add more as needed
}