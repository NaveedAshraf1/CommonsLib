package com.lymors.lycommons.data.database

import com.google.android.gms.maps.model.LatLng
import com.lymors.lycommons.data.viewmodels.LocationModel
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun uploadLocation( child: String,key:String, latitude: Double, longitude: Double)
    suspend fun getLocationOfAKey(child: String,key: String, callback: (LatLng?) -> Unit)
    suspend fun collectALocation(child: String, key: String, callback: (LocationModel) -> Unit)
    fun collectAllLocationsInARadius(path:String, center: LatLng, radius: Double, movedKey: (LocationModel) -> Unit = {}):Flow<List<LocationModel>>
    fun cancelGeoQueryListener()
    suspend fun getAllLocationsInARadius(path:String, center: LatLng, radius: Double, callback: (List<LocationModel>) -> Unit)

}