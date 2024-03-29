package com.lymors.lycommons.managers

import android.app.Activity
//import com.an.deviceinfo.location.LocationInfo

class MyLocationManager(private val context: Activity) {

//    suspend fun getUserCurrentLocationInfo(): LocationData?{
//        val myPermissionHelper= MyPermissionHelper(context)
//        return if (myPermissionHelper.isLocationPermissionGrantedForLocation()){
//            val bol=myPermissionHelper.checkLocationEnabledForGps()
//            return if (bol){
//                val locationInfo = LocationInfo(context)
//                val location=locationInfo.location
//                LocationData(location.addressLine1,location.city,location.countryCode,location.latitude,location.longitude,location.state)
//            }else{
//                null
//            }
//        }else{
//            myPermissionHelper.requestLocationPermissionForLocation()
//            null
//        }
//    }

}


data class LocationData(
    var address:String?="",
    var city:String?="",
    var countryCode:String?="",
    var latitude:Double?=0.0,
    var longitude:Double?=0.0,
    var state:String?=""
)