package com.lymors.lycommons.managers

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MyPermissionHelper(private val context: Activity) {
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    fun requestLocationPermissionForLocation() {
        if (isLocationPermissionGrantedForLocation()) {
            checkLocationEnabledForGps()
        } else {
            ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    fun isLocationPermissionGrantedForLocation(): Boolean {
        return ContextCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkLocationEnabledForGps():Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!gpsEnabled) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("GPS is disabled. Do you want to enable it?")
                .setPositiveButton("Yes") { _, _ ->
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    context.finishAffinity()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        return gpsEnabled
    }

}