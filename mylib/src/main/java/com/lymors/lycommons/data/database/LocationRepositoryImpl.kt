package com.lymors.lycommons.data.database

import android.annotation.SuppressLint
import android.util.Log
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.FirebaseDatabase
import com.lymors.lycommons.data.viewmodels.LocationModel
import com.lymors.lycommons.extensions.StringExtensions.child
import com.lymors.lycommons.utils.MyExtensions.logT
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationRepositoryImpl(private val mainRepository: MainRepository):LocationRepository {


    private var geoFire = GeoFire(FirebaseDatabase.getInstance().reference)

    override suspend fun uploadLocation( child: String,key:String, latitude: Double, longitude: Double) {
        child.child(key).logT("uploadLocation->path", "path")
        geoFire.setLocation(child.child(key), GeoLocation(latitude, longitude))
    }

    override suspend fun getLocationOfAKey(child: String,key: String, callback: (LatLng?) -> Unit) {
        child.child(key).logT("getLocationOfAKey->path", "path")
        geoFire.getLocation(child.child(key), object : com.firebase.geofire.LocationCallback {
            override fun onLocationResult(key: String?, location: GeoLocation?) {
                if (location != null) {
                    // Convert GeoLocation to LatLng and pass it to the callback
                    val latLng = LatLng(location.latitude, location.longitude)
                    callback(latLng)
                } else {
                    // Location is not available for this key
                    callback(null)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors if any and pass null to the callback
                Log.e("GeoFireError", "Error getting location: ${databaseError.message}")
                callback(null)
            }
        })
    }

    override suspend fun collectALocation(child: String, key: String, callback: (LocationModel) -> Unit) {
        child.child(key).logT("collectALocation->path", "path")
        val path = "$child/$key/l"
        mainRepository.collectAnyModel(path = path, clazz = Double::class.java).collect{
            it.whenSuccess {
                if (it.isNotEmpty()){
                    callback.invoke(LocationModel(key,it[0],it[1]))
                }
            }
        }
    }

    override suspend fun getAllLocationsInARadius(path: String, center: LatLng, radius: Double): List<LocationModel> {
        path.logT("getAllLocationsInARadius->path", "path")
        val geoF = GeoFire(FirebaseDatabase.getInstance().reference.child(path))
        val geoQuery = geoF.queryAtLocation(GeoLocation(center.latitude, center.longitude), radius)

        locations.clear() // Clear previous locations

        return suspendCancellableCoroutine { continuation ->
            val geoQueryListener = object : GeoQueryEventListener {
                override fun onKeyEntered(key: String, location: GeoLocation) {
                    key.logT("onKeyEntered")
                    locations[key] = LocationModel(key, location.latitude, location.longitude)
                }

                override fun onKeyExited(key: String) {
                    key.logT("onKeyExited")
                    locations.remove(key)
                }

                override fun onKeyMoved(key: String, location: GeoLocation) {
                    // Handle key movement if needed

                    locations[key] = LocationModel(key, location.latitude, location.longitude)
                }

                override fun onGeoQueryReady() {
                    continuation.resume(locations.values.toList())
                    geoQuery.removeGeoQueryEventListener(this)
                }

                @SuppressLint("RestrictedApi")
                override fun onGeoQueryError(error: DatabaseError) {
                    continuation.resumeWithException(DatabaseException(error.message))
                    locations.clear() // Clear locations on error
                }
            }
            geoQuery.addGeoQueryEventListener(geoQueryListener)
            continuation.invokeOnCancellation {
                geoQuery.removeGeoQueryEventListener(geoQueryListener)
            }
        }
    }



    val locations = mutableMapOf<String , LocationModel>()
    var geoQuery: GeoQuery? = null
    var geoQueryListener: GeoQueryEventListener? = null

    override fun collectAllLocationsInARadius(path: String, center: LatLng, radius: Double, movedKey: (LocationModel) -> Unit): Flow<List<LocationModel>> = callbackFlow {
       path.logT("collectAllLocationsInARadius->path", "path")

        val geoFire = GeoFire(FirebaseDatabase.getInstance().reference.child(path))
        val geoQuery = geoFire.queryAtLocation(GeoLocation(center.latitude, center.longitude), radius)

        val geoQueryListener = object : GeoQueryEventListener {
            override fun onKeyEntered(key: String, location: GeoLocation) {
                key.logT("onKeyEntered")
                if (!locations.containsKey(key)) {
                    locations[key] = LocationModel(key, location.latitude, location.longitude)
                    trySend(locations.values.toList()).isSuccess
                }
            }

            override fun onKeyExited(key: String) {
                key.logT("onKeyExited")
                val target = locations[key]
                if (target != null) {
                    locations.remove(key)
                    trySend(locations.values.toList()).isSuccess
                }
            }

            override fun onKeyMoved(key: String, location: GeoLocation) {
                "onKeyMoved".logT("collectAllLocationsInARadius->onKeyMoved -> key: $key")
                movedKey(LocationModel(key, location.latitude, location.longitude))
            }

            override fun onGeoQueryReady() {
                "onGeoQueryReady".logT("", "firebase")
                // You could emit the initial state of locations here if needed
                trySend(locations.values.toList()).isSuccess
            }

            override fun onGeoQueryError(error: DatabaseError?) {
                error?.toException()?.printStackTrace()
                close(error?.toException()) // Close the flow on error
            }
        }

        geoQuery.addGeoQueryEventListener(geoQueryListener)

        // Ensure to clean up the listener when the flow is cancelled
        awaitClose { geoQuery.removeGeoQueryEventListener(geoQueryListener) }
    }

    override fun cancelGeoQueryListener() {
        if (geoQuery != null) {
            geoQuery?.removeGeoQueryEventListener(geoQueryListener)
        }
    }

}