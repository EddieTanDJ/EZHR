package com.example.ezhr

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ezhr.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.SphericalUtil


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        const val GEOFENCE_LAT = 1.377411770441811
        const val GEOFENCE_LONG = 103.84883027746038
        const val GEOFENCE_RADIUS = 50.0
        const val CHANNEL_ID = "222"
    }

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    // NavController variable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        getLocationUpdates()

    }

    private fun getLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 100
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 100
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                Log.d(TAG, "location change")

                if (locationResult.locations.isNotEmpty()) {
                    // get latest location
                    val location =
                        locationResult.lastLocation
                    checkForGeoFenceEntry(
                        location,
                        GEOFENCE_LAT,
                        GEOFENCE_LONG,
                        GEOFENCE_RADIUS
                    )
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a circle in Location
        mMap.addCircle(
            CircleOptions()
                .center(LatLng(GEOFENCE_LAT, GEOFENCE_LONG))
                .radius(GEOFENCE_RADIUS)
                .strokeColor(Color.RED)
                .fillColor(0x30ff0000)
                .strokeWidth(2F)
        )
        // Add a marker in office and move the camera
        val office = LatLng(GEOFENCE_LAT, GEOFENCE_LONG)
        mMap.addMarker(MarkerOptions().position(office).title("Marker in Singapore"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(office, 18F))
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(18F), 2000, null)
        requestFineAndCoarseLocationPermissions()
    }

    private fun fineAndCoarseLocationPermissionApproved(): Boolean {
        val fineLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                )
        val coarsePermissionApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                )

        return fineLocationApproved && coarsePermissionApproved
    }


    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun requestFineAndCoarseLocationPermissions() {
        if (fineAndCoarseLocationPermissionApproved()) {
            mMap.isMyLocationEnabled = true
            return
        }
        val permissionsArray = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val resultCode = REQUEST_FINE_AND_COARSE_PERMISSION_RESULT_CODE
        Log.d(TAG, "Request fine and coarse location permission")
        ActivityCompat.requestPermissions(
            this,
            permissionsArray,
            resultCode
        )
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionResult")
        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FINE_AND_COARSE_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)
        ) {
            // TODO message for tell user why we need to permission
        } else {
            mMap.isMyLocationEnabled = true
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper() /* Looper */
        )
    }

    fun checkForGeoFenceEntry(
        userLocation: Location,
        geofenceLat: Double,
        geofenceLong: Double,
        radius: Double
    ) {
        val startLatLng = LatLng(userLocation.latitude, userLocation.longitude) // User Location
        val geofenceLatLng = LatLng(geofenceLat, geofenceLong) // Center of geofence

        val distanceInMeters = SphericalUtil.computeDistanceBetween(startLatLng, geofenceLatLng)
        Log.d(TAG, "check geo fence entry")

        if (distanceInMeters < radius) {
            // User is inside the Geo-fence
            showClockInBtn()
        } else {
            hideClockInBtn()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    private fun showClockInBtn() {
        binding.checkInBtn.visibility = View.VISIBLE
    }

    private fun hideClockInBtn() {
        binding.checkInBtn.visibility = View.INVISIBLE
    }

}

const val REQUEST_FINE_AND_COARSE_PERMISSION_RESULT_CODE = 33
const val TAG = "MapsMainActivity"
const val LOCATION_PERMISSION_INDEX = 0
const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1