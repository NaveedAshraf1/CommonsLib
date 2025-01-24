package com.lymors.lycommons.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.lymors.lycommons.data.database.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(private val locationRepository: LocationRepository) : ViewModel() {

    private val _location = MutableStateFlow(emptyList<LocationModel>())
    val location = _location.asStateFlow()

    suspend fun uploadLocation( child: String,key:String, latitude: Double, longitude: Double){
        locationRepository.uploadLocation(child, key, latitude, longitude)
    }
    suspend fun getLocationOfAKey(child: String,key: String, callback: (LatLng?) -> Unit){
        locationRepository.getLocationOfAKey(child, key, callback)
    }
    suspend fun collectALocation(child: String, key: String, callback: (LocationModel) -> Unit){
        locationRepository.collectALocation(child, key, callback)
    }
    fun cancelGeoQueryListener() {
        locationRepository.cancelGeoQueryListener()
    }
    suspend fun getAllLocationsInARadius(path: String, center: LatLng, radius: Double,):List<LocationModel> {
        return locationRepository.getAllLocationsInARadius(path, center, radius)

    }

    fun collectAllLocationsInARadius(path: String, center: LatLng, radius: Double, movedKey: (LocationModel) -> Unit = {}):StateFlow<List<LocationModel>> {
        viewModelScope.launch {
            locationRepository.collectAllLocationsInARadius(path, center, radius, movedKey).collect { locations ->
                _location.value = locations
            }
        }
        return location
    }



}
