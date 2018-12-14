package com.afconsult.korjournal

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.afconsult.korjournal.database.TripsData
import com.afconsult.korjournal.database.TripsDataBase
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_new_trip.*
import java.io.IOException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*


class NewTripFragment : Fragment(), OnMapReadyCallback, LocationListener, InsertTripTask.InsertCallback {


    val PERMISSIONS_REQUEST_ALL = 1

    val timeS = 1000.0;
    val timeM = timeS * 60;
    val timeH = timeM * 60;

    // Database
//    lateinit var mDb : TripsDataBase

    // Calculations
    var millisecondTime: Long = 0
    var startTime: Long = 0
    var duration: Long = 0
    var updateTime = 0L
    var handler: Handler = Handler()
    var seconds: Int = 0
    var minutes: Int = 0
    var hours: Int = 0

    var distance: Double = 0.0

    var running: Boolean = false

    // Maps
    private val mapFragment: SupportMapFragment = SupportMapFragment.newInstance()
    private lateinit var mMap: GoogleMap
    private var mPolyline : Polyline? = null

    private lateinit var mo : MarkerOptions
    private lateinit var marker : Marker
    private var points : MutableList<LatLng> = ArrayList<LatLng>()
    private var timestamps : MutableList<Long> = ArrayList<Long>()

    private lateinit var locationManager : LocationManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_new_trip, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        mDb = TripsDataBase.getInstance(context!!)!!

        startButton.setOnClickListener {
            resetButton.isEnabled = true

            if (!running) {
                // START
                running = true

                checkPermissionsIfNeeded()

//                showBanner(true)
                banner.visibility = View.VISIBLE
                startButton.text = "Stoppa resa"
                startButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stop, 0, 0, 0)

                startTime = System.currentTimeMillis()
                handler.postDelayed(runnable, 0)
            } else {
                // STOP
                locationManager.removeUpdates(this)

                duration += millisecondTime

                handler.removeCallbacks(runnable)

                showSaveTripDialog()
            }
        }

        resetButton.setOnClickListener {
            reset()
        }

        mapFragment.getMapAsync(this)
        childFragmentManager.beginTransaction().replace(R.id.map, mapFragment as Fragment).commit()
        locationManager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private fun reset() {
        handler.removeCallbacks(runnable)

        millisecondTime = 0
        startTime = 0
        duration = 0L
        updateTime = 0L
        handler = Handler()
        seconds = 0
        minutes = 0
        hours = 0

        distance = 0.0

        running = false

        durationTextView.text = "00:00:00"
        distanceTextView.text = "00.00"

        points.clear();
        timestamps.clear();
        mPolyline?.remove()

        locationManager.removeUpdates(this)
        handler.removeCallbacks(runnable)

        startButton.text = "Starta resa"
        startButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_circle_outline, 0, 0, 0)

        resetButton.isEnabled = false
        banner.visibility = View.INVISIBLE
//        showBanner(false)
    }

    companion object {
        fun newInstance(): NewTripFragment = NewTripFragment()
    }

    private var runnable: Runnable = object : Runnable {

        override fun run() {
            millisecondTime = System.currentTimeMillis() - startTime
            updateTime = duration + millisecondTime
            seconds = (updateTime.toInt() / 1000)
            minutes = seconds / 60
            seconds = seconds % 60
            minutes = minutes % 60
            hours = minutes / 60

            durationTextView.text = String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format(
                "%02d", seconds
            )

            handler.postDelayed(this, 0)
        }

    }

    private fun showSaveTripDialog() {

        startButton.text = "Spara resa"
        val builder = AlertDialog.Builder(this.context!!)
        builder.setTitle("Spara resa")

        val view = layoutInflater.inflate(R.layout.dialog_save_trip, null)

        val departureEditText = view.findViewById(R.id.departureEditText) as EditText
        val destinationEditText = view.findViewById(R.id.destinationEditText) as EditText
        val noteEditText = view.findViewById(R.id.noteEditText) as EditText

        if (points.size > 0) {
            departureEditText.setText(getAddress(points[0]))
            destinationEditText.setText(getAddress(points[points.size - 1]))
        }

        builder.setView(view)

        // set up the ok button
        builder.setPositiveButton(android.R.string.ok) { dialog, p1 ->
            val tripsData = TripsData()
            tripsData.departure = departureEditText.text.toString()
            tripsData.destination = destinationEditText.text.toString()
            tripsData.notes = noteEditText.text.toString()

            tripsData.start = startTime
            tripsData.end = startTime + duration

            tripsData.distance = distance

//            val nf = NumberFormat.getNumberInstance(Locale.US)
//            val formatter = nf as DecimalFormat
//            formatter.applyPattern("#.###")
//            tripsData.distance = formatter.format(distance).toDouble()

            InsertTripTask(TripsDataBase.getInstance(context!!), tripsData, points, this).execute()

            dialog.dismiss()
        }

        builder.setNegativeButton(android.R.string.cancel) { dialog, p1 ->
            dialog.cancel()
        }
        builder.show()
    }

    override fun onInsertComplete(success: Boolean) {
        reset()
    }


    private fun checkPermissionsIfNeeded() {
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
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showAlert("permissions")
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
        mo = MarkerOptions().position(LatLng(0.0,0.0)).title("My position")
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
        banner.visibility = View.INVISIBLE

        val myCoords = LatLng(location!!.latitude, location.longitude)
        marker.position = myCoords

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myCoords, 16.0f))
        mPolyline?.remove()
        points.add(myCoords)
        timestamps.add(System.currentTimeMillis())
        if (points.size > 1) {
            mPolyline = mMap.addPolyline(PolylineOptions().addAll(points))
            distance += getDistanceBetween(points.get(points.size - 2), points.get(points.size - 1));
            distanceTextView.text = String.format("%.2f", distance)

//            Toast.makeText(context, "" + (timestamps.get(points.size - 1) - timestamps.get(points.size - 2)) + " ms", Toast.LENGTH_SHORT).show()

            var timeMS = 0.0
            var distanceKM = 0.0
            if (points.size > 5) {
                for (i in (points.size - 5)..(points.size - 1) ) {
                    timeMS += timestamps.get(i) - timestamps.get(i - 1)
                    distanceKM += getDistanceBetween(points.get(i), points.get(i - 1))

                }

                val speed = distanceKM/(timeMS/timeH);
                if (speed >= 20.0) {
                    speedTextView.text = speed.toInt().toString()
                } else {
                    speedTextView.text = String.format("%.1f", speed)
                }
            }

//            mPolyline = mMap.addPolyline(PolylineOptions().add(points[points.size - 2]).add(points[points.size - 1]))
        }

    }

    @SuppressLint("MissingPermission")
    fun requestLocation() {
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.powerRequirement = Criteria.POWER_HIGH
        val provider = locationManager.getBestProvider(criteria, true)

        locationManager.requestLocationUpdates(provider, 1000L, 10F, this)
    }

    private fun isLocationEnabled() : Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showAlert(type : String) {
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

        val dialog = AlertDialog.Builder(context!!)
        dialog.setCancelable(false)
        dialog.setTitle(title).setMessage(message).setPositiveButton(btnText) { _, which ->
            if (type == "plats") {
                val myIntent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                startActivity(myIntent)
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ALL)
            }
        }
        dialog.show()

    }

    private fun getAddress(latLan :LatLng) : String {
        val geocoder = Geocoder(context, Locale.getDefault());
        var retVal = ""

        try {

        val addresses = geocoder.getFromLocation(latLan.latitude, latLan.longitude, 1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        val firstAddress = addresses.get(0)

        val knownName = firstAddress.getFeatureName(); // Only if available else return NULL
        val address = firstAddress.getAddressLine(0)// If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        val city = firstAddress.getLocality();
        //String state = addresses.get(0).getAdminArea();
        //String country = addresses.get(0).getCountryName();
        //String postalCode = addresses.get(0).getPostalCode();

//        if (knownName != null) {
//            return "$knownName, $city"
//        }
            retVal = address.substringBeforeLast(",").substringBeforeLast(",") + ", " + firstAddress.locality
        } catch (e: IOException) {

        }

        return retVal
    }

    private fun getDistanceBetween(p1 : LatLng, p2 : LatLng) : Double {
        val results = FloatArray(1)
        Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, results)

        return results[0].toDouble()/1000
    }

//    private fun showBanner(show: Boolean) {
//        if (show) {
//            val height = getResources().getDimensionPixelSize(R.dimen.banner_height);
//            banner.layoutParams.height = height
//        } else {
//            banner.text = ""
//            banner.layoutParams.height = 0
//        }
//    }
}