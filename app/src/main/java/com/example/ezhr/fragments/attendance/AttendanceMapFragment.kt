package com.example.ezhr.fragments.attendance

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.ezhr.MapsActivity
import com.example.ezhr.R
import com.example.ezhr.databinding.FragmentAttendanceMapBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.SphericalUtil

/**
 * A simple [Fragment] subclass.
 * Use the [AttendanceMapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class AttendanceMapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentAttendanceMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    // NavController variable
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        getLocationUpdates()

        binding.checkInBtn.setOnClickListener {
            navController.navigate(R.id.action_attendanceMap_to_attendanceQRCodeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getLocationUpdates() {
        Log.d(TAG, "get updates")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest.create().apply {
            interval = 100
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 100
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
//                Log.d(TAG, "location change")

                if (locationResult.locations.isNotEmpty()) {
                    // get latest location
                    val location =
                        locationResult.lastLocation
                    checkForGeoFenceEntry(
                        location,
                        MapsActivity.GEOFENCE_LAT,
                        MapsActivity.GEOFENCE_LONG,
                        MapsActivity.GEOFENCE_RADIUS
                    )
                }
            }
        }
    }

    /**
     * set up the map with the circle to indicate the perimeter around the office
     */
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a circle in Location
        mMap.addCircle(
            CircleOptions()
                .center(LatLng(MapsActivity.GEOFENCE_LAT, MapsActivity.GEOFENCE_LONG))
                .radius(MapsActivity.GEOFENCE_RADIUS)
                .strokeColor(Color.RED)
                .fillColor(0x30ff0000)
                .strokeWidth(2F)
        )
        // Add a marker in office and move the camera
        val office = LatLng(MapsActivity.GEOFENCE_LAT, MapsActivity.GEOFENCE_LONG)
//        mMap.addMarker(MarkerOptions().position(office).title("Marker in Singapore"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(office, 18F))
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(18F), 2000, null)
        requestFineAndCoarseLocationPermissions()
    }

    /**
     *  check if permissions are approved
     */
    private fun fineAndCoarseLocationPermissionApproved(): Boolean {
        val fineLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                )
        val coarsePermissionApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                )
        Log.d(TAG, fineLocationApproved.toString() + coarsePermissionApproved.toString())
        return fineLocationApproved && coarsePermissionApproved
    }

    /**
     * request fine and coarse permission required by the Gmaps
     */
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun requestFineAndCoarseLocationPermissions() {
        if (fineAndCoarseLocationPermissionApproved()) {
            mMap.isMyLocationEnabled = true
            startLocationUpdates()
            return
        }
        val permissionsArray = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        Log.d(TAG, "Request fine and coarse location permission")
        requestMultiplePermissions.launch(
            permissionsArray
        )
        if (fineAndCoarseLocationPermissionApproved()) {
            mMap.isMyLocationEnabled = true
            startLocationUpdates()
            return
        } else {
            showErrorSnackbar()
        }
    }

    /**
     * Show a snackbar to the settings if permission denied
     */
    private fun showErrorSnackbar() { //semi important error method
        view?.let {
            Snackbar.make(it, "App permission required", Snackbar.LENGTH_LONG)
                .setAction("Settings") {
                    // Responds to click on the action
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", requireActivity().packageName, null)
                    intent.data = uri
                    this.startActivity(intent)
                }.show()
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    val requestMultiplePermissions =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.entries.forEach {
                Log.e(TAG, "${it.key} = ${it.value}")
            }
        }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun startLocationUpdates() {
        Log.d(TAG, "start updates")
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper() /* Looper */
        )
    }

    /**
     * helper function to determine if the user is in the perimeter
     */
    fun checkForGeoFenceEntry(
        userLocation: Location,
        geofenceLat: Double,
        geofenceLong: Double,
        radius: Double
    ) {
        val startLatLng = LatLng(userLocation.latitude, userLocation.longitude) // User Location
        val geofenceLatLng = LatLng(geofenceLat, geofenceLong) // Center of geofence

        val distanceInMeters = SphericalUtil.computeDistanceBetween(startLatLng, geofenceLatLng)
//        Log.d(TAG, "check geo fence entry")

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
        binding.checkInBtn.show()
    }

    private fun hideClockInBtn() {
        binding.checkInBtn.hide()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         */
        @JvmStatic
        private val TAG = AttendanceMapFragment::class.simpleName
        fun newInstance(): AttendanceMapFragment {
            return AttendanceMapFragment()
        }
    }
}