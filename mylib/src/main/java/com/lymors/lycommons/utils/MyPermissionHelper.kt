package com.lymors.lycommons.utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object MyPermissionHelper {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var permissionCallback: (Boolean) -> Unit = {}

    fun FragmentActivity.registerActivityForPermissionLauncher() {

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val deniedPermissions = result.filter { !it.value }.map { it.key }
            if (deniedPermissions.isEmpty()) {
                permissionCallback(true)
            } else {
                if (shouldShowRequestPermissionRationale(deniedPermissions.toTypedArray())) {
                    showRationaleForPermissions(deniedPermissions.toTypedArray())
                } else {
                    showSettingsDialog(deniedPermissions.toTypedArray())
                }
                permissionCallback(false)
            }
        }
    }

    fun FragmentActivity.checkPermissions(permissions: Array<String>): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun FragmentActivity.checkPermissionReadImages(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermissions(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
        } else {
            checkPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    fun FragmentActivity.checkPermissionReadStorage(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        return checkPermissions(permissions)
    }

    fun FragmentActivity.requestPermission(permissions: Array<String>, callback: (Boolean) -> Unit = {}) {
        permissionCallback = callback
        if (checkPermissions(permissions)) {
            callback(true)
        } else {
            if (shouldShowRequestPermissionRationale(permissions)) {
                showRationaleForPermissions(permissions)
            } else {
                requestPermissionLauncher.launch(permissions)
            }
        }
    }

    fun FragmentActivity.requestPermissionReadImages(callback: (Boolean) -> Unit = {}) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermission(permissions, callback)
    }

    fun FragmentActivity.requestPermissionReadStorage(callback: (Boolean) -> Unit = {}) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermission(permissions, callback)
    }

    fun FragmentActivity.requestPermissionReadVideos(callback: (Boolean) -> Unit = {}) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO,
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermission(permissions, callback)
    }



    fun FragmentActivity.requestPermissionReadAudios(callback: (Boolean) -> Unit = {}) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermission(permissions, callback)
    }

    fun FragmentActivity.requestPermissionImages(callback: (Boolean) -> Unit = {}) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermission(permissions, callback)
    }

    private fun FragmentActivity.showRationaleForPermissions(permissions: Array<String>) {
        val permissionNames = permissions.joinToString("\n") { getPermissionName(it) }
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app needs\n$permissionNames\npermission to use this functionality.Please grant it.")
            .setPositiveButton("OK") { _, _ ->
                requestPermissionLauncher.launch(permissions)
            }
            .setNegativeButton("Cancel") { _, _ ->
                permissionCallback(false)
            }
            .show()
    }

    private fun FragmentActivity.showSettingsDialog(permissions: Array<String>) {
        val permissionNames = permissions.joinToString("\n") { getPermissionName(it) }
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app needs\n$permissionNames\npermission to use this functionality.Please grant it in the app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { _, _ ->
                permissionCallback(false)
            }
            .show()
    }

    private fun FragmentActivity.shouldShowRequestPermissionRationale(permissions: Array<String>): Boolean {
        return permissions.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
        }
    }

    fun FragmentActivity.shouldShowStoragePermissionRationale(): Boolean {
        val shouldShowReadPermissionRationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val shouldShowWritePermissionRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        return shouldShowReadPermissionRationale || shouldShowWritePermissionRationale
    }

    private fun getPermissionName(permission: String): String {
        return when (permission) {
            Manifest.permission.READ_EXTERNAL_STORAGE -> "Read External Storage"
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> "Write External Storage"
            Manifest.permission.READ_MEDIA_IMAGES -> "Read Media Images"
            Manifest.permission.READ_MEDIA_VIDEO -> "Read Media Video"
            Manifest.permission.READ_MEDIA_AUDIO -> "Read Media Audio"
            Manifest.permission.CAMERA -> "Camera"
            Manifest.permission.RECORD_AUDIO -> "Record Audio"
            Manifest.permission.ACCESS_FINE_LOCATION -> "Access Fine Location"
            Manifest.permission.ACCESS_COARSE_LOCATION -> "Access Coarse Location"
            Manifest.permission.ACCESS_BACKGROUND_LOCATION -> "Access Background Location"
            Manifest.permission.POST_NOTIFICATIONS -> "Post Notifications"
            Manifest.permission.READ_CONTACTS -> "Read Contacts"
            Manifest.permission.READ_PHONE_STATE -> "Read Phone State"
            Manifest.permission.CALL_PHONE -> "Call Phone"
            Manifest.permission.SEND_SMS -> "Send SMS"
            Manifest.permission.RECEIVE_SMS -> "Receive SMS"
            Manifest.permission.READ_SMS -> "Read SMS"
            else -> permission
        }
    }
}
