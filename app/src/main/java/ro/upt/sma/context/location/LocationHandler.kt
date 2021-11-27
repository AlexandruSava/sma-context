package ro.upt.sma.context.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

@SuppressLint("MissingPermission")
class LocationHandler(context: Context) {

    private val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    fun registerLocationListener(locationCallback: LocationCallback) {
        // Create a LocationRequest with PRIORITY_HIGH_ACCURACY and smallest displacement to 10m.
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setSmallestDisplacement(10f)

        // Register request and callback with the fused location service client.
        client.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun unregisterLocationListener(locationCallback: LocationCallback) {
        client.removeLocationUpdates(locationCallback)
    }

}
