package com.afconsult.korjournal

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.LatLng



class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {
    val PERMISSIONS_REQUEST_ALL = 1
    private lateinit var mMap: GoogleMap

    private lateinit var mo : MarkerOptions
    private lateinit var marker : Marker
    private var points : MutableList<LatLng> = ArrayList<LatLng>()

    private lateinit var locationManager : LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mo = MarkerOptions().position(LatLng(0.0,0.0)).title("My position")

//        points = MutableList<LatLng>()
//        points.add(LatLng(55.016, 12.321))
//        points.add(LatLng(-34.747, 145.592))
//        points.add(LatLng(-34.364, 147.891))
//        points.add(LatLng(-33.501, 150.217))
//        points.add(LatLng(-32.306, 149.248))
//        points.add(LatLng(-32.491, 147.309))


        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions()
        } else {
            requestLocation()
        }

        if (!isLocationEnabled()) {
            showAlert("plats")
        }

    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                showAlert("permissions")
//            }
        } else {
            requestLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_ALL -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    requestLocation()
                } else {
                    finish()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        marker = mMap.addMarker(mo)

    }

    override fun onProviderDisabled(provider: String?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(provider: String?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLocationChanged(location: Location?) {
        println("zzz: " + location!!.latitude +  " " + location.longitude)
        val myCoords = LatLng(location!!.latitude, location.longitude)
        marker.position = myCoords

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myCoords, 16.0f))
        points.add(myCoords)

        mMap.addPolyline(PolylineOptions().addAll(points))

    }

    @SuppressLint("MissingPermission")
    fun requestLocation() {
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.powerRequirement = Criteria.POWER_HIGH
        val provider = locationManager.getBestProvider(criteria, true)
        locationManager.requestLocationUpdates(provider, 2000L, 20F, this)
    }

    fun isLocationEnabled() : Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun showAlert(type : String) {
        val title : CharSequence
        val message : String
        val btnText : String

        if (type == "plats") {
            title = "Platsuppgifter"
            message = "Din platsdelning är satt till 'AV'.\n Var god slå på detta."
            btnText = "Platsinställningar"
        } else {
            title = "Rättigheter"
            message = "Den här appen behöver tillgång till din plats."
            btnText = "Tillåt"
        }

        val dialog = AlertDialog.Builder(this)
        dialog.setCancelable(false)
        dialog.setTitle(title).setMessage(message).setPositiveButton(btnText) { _, which ->
            if (type == "plats") {
                val myIntent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                startActivity(myIntent)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ALL)
            }
        }.setNegativeButton("Avbryt") { _, which ->  finish()}
        dialog.show()

    }

}
