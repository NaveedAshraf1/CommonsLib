package com.lymors.lycommons.data.viewmodels

import com.google.maps.model.LatLng

data class LocationModel(
    var key:String = "",
    var lat:Double = 0.0,
    var lang:Double = 0.0
) {
    fun toModelLatLang(): LatLng {
        return LatLng(lat, lang)
    }
    fun toGmsLAtLang(): com.google.android.gms.maps.model.LatLng {
        return com.google.android.gms.maps.model.LatLng(lat, lang)
    }

    fun orEmpty(): LocationModel {
        return this ?: LocationModel()
    }
}
