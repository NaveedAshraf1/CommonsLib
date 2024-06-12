package com.lymors.lycommons.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.lymors.lycommons.utils.MyExtensions.showToast

class MyPermissionHelper(var activity: AppCompatActivity) {


    fun isReadStoragePermissionGranted():Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
        } else {
            checkPermission(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }


    fun checkWriteStoragePermission():Boolean {
      return  checkPermission(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }


    fun checkPermission(list:Array<String>):Boolean{
        for (permission in list) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }



    fun showToast(text:String){
        activity.showToast(text)
    }


    private lateinit var permissionCallback: (Boolean) ->Unit

    fun requestPermissions(
        permissions: Array<String>,
        callback: (Boolean) -> Unit
    ) {
        permissionCallback = callback
        if (arePermissionsGranted(activity, permissions)) {
            callback.invoke(true)
        } else {
            requestPermissionLauncher(activity).launch(permissions)
        }
    }

    fun requestPermissions(
        fragment: Fragment,
        permissions: Array<String>,
        callback: (Boolean)->Unit
    ) {
        permissionCallback = callback
        if (arePermissionsGranted(fragment.requireContext(), permissions)) {
            callback.invoke(true)
        } else {
            requestPermissionLauncher(fragment).launch(permissions)
        }
    }

    private fun arePermissionsGranted(context: android.content.Context, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun requestPermissionLauncher(activity: AppCompatActivity): ActivityResultLauncher<Array<String>> {
        return activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val deniedPermissions = result.filter { !it.value }.map { it.key }
            if (deniedPermissions.isEmpty()) {
                permissionCallback.invoke(true)
            } else {
                permissionCallback.invoke(false)
            }
        }
    }

    private fun requestPermissionLauncher(fragment: Fragment): ActivityResultLauncher<Array<String>> {
        return fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val deniedPermissions = result.filter { !it.value }.map { it.key }
            if (deniedPermissions.isEmpty()) {
                permissionCallback.invoke(true)
            } else {
                permissionCallback.invoke(false)
            }
        }
    }


    fun requestReadImagesPermission(callback: (Boolean)->Unit) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermissions(permissions, callback)
    }

    fun requestReadStoragePermission(callback: (Boolean)->Unit) {
        if (isReadStoragePermissionGranted()) {
            callback.invoke(true)
            return
        }

        // Else request permission
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        requestPermissions(permissions, callback)
    }



    fun requestWriteStoragePermission(callback: (Boolean)->Unit) {
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissions(permissions, callback)
    }


    fun requestCameraPermission(callback: (Boolean)->Unit) {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        requestPermissions(permissions, callback)
    }


    fun requestRecordAudioPermission(callback: (Boolean)->Unit) {
        val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
        requestPermissions(permissions, callback)
    }


    fun requestLocationPermission(callback: (Boolean)->Unit) {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        requestPermissions(permissions, callback)
    }


    fun requestLocationForegroundPermission(callback: (Boolean)->Unit) {
        val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        requestPermissions(permissions, callback)
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestNotificationPermission(callback: (Boolean)->Unit) {
        val permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        requestPermissions(permissions, callback)
    }



    fun requestContactsPermission(callback: (Boolean)->Unit) {
        val permissions = arrayOf(Manifest.permission.READ_CONTACTS)
        requestPermissions(permissions, callback)
    }


    fun requestPhoneStatePermission(callback: (Boolean)->Unit) {
        val permissions = arrayOf(Manifest.permission.READ_PHONE_STATE)
        requestPermissions(permissions, callback)
    }


    fun requestCallPhonePermission(callback: (Boolean)->Unit) {
        val permissions = arrayOf(Manifest.permission.CALL_PHONE)
        requestPermissions(permissions, callback)
    }


    fun requestSendSmsPermission(callback: (Boolean)->Unit) {
        val permissions = arrayOf(Manifest.permission.SEND_SMS)
        requestPermissions(permissions, callback)
    }


    fun requestReceiveSmsPermission(callback: (Boolean)->Unit) {
        val permissions = arrayOf(Manifest.permission.RECEIVE_SMS)
        requestPermissions(permissions, callback)
    }


    fun requestReadSmsPermission(callback: (Boolean)->Unit) {
        val permissions = arrayOf(Manifest.permission.READ_SMS)
        requestPermissions(permissions, callback)
    }




        fun shouldShowStoragePermissionRationale(activity: Activity): Boolean {

            val shouldShowReadPermissionRationale: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            val shouldShowWritePermissionRationale: Boolean =
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

            return shouldShowReadPermissionRationale || shouldShowWritePermissionRationale
        }








//        public static boolean shouldShowRequestPermissionRationale(
//        @NonNull Activity activity,
//        @NonNull String permission
//        ) {
//            return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
//        }





}
