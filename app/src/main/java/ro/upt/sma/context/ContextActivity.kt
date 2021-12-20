package ro.upt.sma.context

import android.Manifest.permission
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ro.upt.sma.context.activity.ActivityRecognitionHandler
import ro.upt.sma.context.activity.ActivityRecognitionService
import ro.upt.sma.context.location.LocationHandler
import java.text.MessageFormat

class ContextActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fMap: SupportMapFragment
    private lateinit var tvLocation: TextView
    private lateinit var tvActivity: TextView

    private var googleMap: GoogleMap? = null

    private lateinit var locationHandler: LocationHandler
    private lateinit var activityRecognitionHandler: ActivityRecognitionHandler
    private var locationCallback: LocationCallback? = null
    private var activityPendingIntent: PendingIntent? = null
    private var activityRecognitionReceiver: BroadcastReceiver? = null

    private val isLocationPermissionGranted: Boolean
        get() = ContextCompat.checkSelfPermission(
            this,
            permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private val isActivityPermissionGranted: Boolean
        get() = ContextCompat.checkSelfPermission(
            this,
            permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.fMap = supportFragmentManager.findFragmentById(R.id.f_map) as SupportMapFragment
        this.tvLocation = findViewById(R.id.tv_location)
        this.tvActivity = findViewById(R.id.tv_activity)

        this.locationHandler = LocationHandler(this)
        this.activityRecognitionHandler = ActivityRecognitionHandler(this)

        if (!isLocationPermissionGranted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_ID
            )
        }

        if (!isActivityPermissionGranted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission.ACTIVITY_RECOGNITION),
                ACTIVITY_PERMISSION_REQUEST_ID
            )
        }
    }

    override fun onResume() {
        super.onResume()

        if (isLocationPermissionGranted) {
            fMap.getMapAsync(this)
            setupLocation()
            setupActivityRecognition()
        }
    }

    override fun onPause() {
        super.onPause()

        if (locationCallback != null) {
            locationHandler.unregisterLocationListener(locationCallback!!)
        }
        if (activityPendingIntent != null) {
            activityRecognitionHandler.unregisterPendingIntent(activityPendingIntent!!)
        }
        if (activityRecognitionReceiver != null) {
            unregisterReceiver(activityRecognitionReceiver)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_ID -> {
                checkAndShowToast(grantResults, R.string.toast_location_permission)
            }
            ACTIVITY_PERMISSION_REQUEST_ID -> {
                checkAndShowToast(grantResults, R.string.toast_activity_permission)
            }
        }
    }

    private fun checkAndShowToast(grantResults: IntArray, @StringRes toastResId: Int) {
        if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, toastResId, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }

    private fun setupLocation() {
        this.locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                val location = locationResult!!.lastLocation
                updateMap(location)
                updateLocationCard(location)
            }
        }
        locationHandler.registerLocationListener(locationCallback!!)
    }

    private fun setupActivityRecognition() {
        this.activityPendingIntent = activityRecognitionHandler.registerPendingIntent()

        this.activityRecognitionReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // Extract activity type from intent extras and pass it to updateActivityCard method.
                // Take a look at ActivityRecognitionService to see how intent extras are formed.

                val activityType : String? = intent.getStringExtra("activity")

                if (activityType != null) {
                    updateActivityCard(activityType.toInt())
                }
            }
        }

        // Register created receiver only for ActivityRecognitionService.INTENT_ACTION.
        val intentFilter = IntentFilter()
        intentFilter.addAction(ActivityRecognitionService.INTENT_ACTION)
        registerReceiver(activityRecognitionReceiver, intentFilter)
    }

    private fun updateMap(location: Location) {
        googleMap?.let {
            // Clear current marker and create a new marker based on the received location object.
            googleMap?.clear()
            val marker = MarkerOptions()
                .position(LatLng(location.latitude, location.longitude))

            val coordinates = "${location.latitude} " + "${location.longitude}"
            marker.title(coordinates)

            googleMap?.addMarker(marker)
            // Use CameraUpdateFactory to perform a zoom in.
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 12f))

        }
    }

    private fun updateLocationCard(location: Location) {
        tvLocation.text = MessageFormat.format(
            "Latitude: {0}\nLongitude: {1}\nAltitude: {2}",
            location.latitude, location.longitude, location.altitude
        )
    }

    private fun updateActivityCard(activityType: Int) {
        val activityResId: Int = when (activityType) {
            DetectedActivity.IN_VEHICLE -> R.string.activity_in_vehicle
            DetectedActivity.ON_BICYCLE -> R.string.activity_on_bicycle
            DetectedActivity.ON_FOOT -> R.string.activity_on_foot
            DetectedActivity.RUNNING -> R.string.activity_running
            DetectedActivity.WALKING -> R.string.activity_walking
            DetectedActivity.TILTING -> R.string.activity_tilting
            DetectedActivity.STILL -> R.string.activity_still
            else -> R.string.activity_unknown
        }

        tvActivity.text =
            String.format("%s: %s", getString(R.string.activity_title), getString(activityResId))
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_ID = 111
        private const val ACTIVITY_PERMISSION_REQUEST_ID = 113
    }

}
