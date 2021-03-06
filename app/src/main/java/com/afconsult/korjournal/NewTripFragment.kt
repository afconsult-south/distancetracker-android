package com.afconsult.korjournal

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.afconsult.korjournal.database.TripsData
import com.afconsult.korjournal.database.TripsDataBase
import com.afconsult.korjournal.tasks.InsertTripTask
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_new_trip.*
import java.util.*
import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import com.afconsult.korjournal.database.TripsViewModel
import com.afconsult.korjournal.utils.TripUtils

class NewTripFragment : Fragment(), OnMapReadyCallback, InsertTripTask.InsertCallback, MyLocationService.CallBack, ServiceConnection {
    lateinit var locationService: MyLocationService
    var bound: Boolean = false

    private val TAG = "NewTripFragment"

    private val LOCATION = "LOCATION"
    private val PERMISSIONS = "PERMISSIONS"

    val PERMISSIONS_REQUEST_ALL = 1

    val timeS = 1000.0;
    val timeM = timeS * 60;
    val timeH = timeM * 60;

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

    private var mVehicleName : String? = null
    private lateinit var mo : MarkerOptions
    private lateinit var marker : Marker
    private var points : MutableList<LatLng> = ArrayList<LatLng>()
    private var timestamps : MutableList<Long> = ArrayList<Long>()

    private lateinit var locationManager : LocationManager

    private lateinit var tripsViewModel: TripsViewModel
    private lateinit var vehicleSpinnerItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_new_trip, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startService();

        tripsViewModel = ViewModelProviders.of(this).get(TripsViewModel::class.java)

        startButton.setOnClickListener {
            resetButton.isEnabled = true

            if (!running && points.isEmpty()) {
                // START
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions()
                } else {
                    startTracking()
                }

                if (!isLocationEnabled()) {
                    showAlert(LOCATION)
                }
            } else {
                // STOP and SAVE
                duration += millisecondTime
                handler.removeCallbacks(updateStatsRunnable)

                stopTracking()
                onServiceLocationUpdate(locationService.getCoordinates(), locationService.getTimeStamps())
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

    override fun onStart() {
        super.onStart()

        doBindService()
        if (running) {
            handler.postDelayed(updateStatsRunnable, 500)
        }
    }

    override fun onStop() {
        if (running) {
            handler.removeCallbacks(updateStatsRunnable)
        }
        doUnbindService()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(!running) {
            stopService()
        }
    }

    private fun setRunning(time: Long) {
        running = true
        banner.visibility = View.VISIBLE
        startButton.text = getString(R.string.btn_stop)
        startButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stop, 0, 0, 0)

        startTime = time
        handler.postDelayed(updateStatsRunnable, 500)
    }

    private fun reset() {
        millisecondTime = 0
        startTime = 0
        duration = 0L
        updateTime = 0L
        handler = Handler()
        seconds = 0
        minutes = 0
        hours = 0

        distance = 0.0

        durationTextView.text = "00:00:00"
        distanceTextView.text = "00.00"
        speedTextView.text = "0"

        points.clear();
        timestamps.clear();
        mPolyline?.remove()

        startButton.text = getString(R.string.btn_start)
        startButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_circle_outline, 0, 0, 0)

        resetButton.isEnabled = false
        banner.visibility = View.INVISIBLE
    }

    companion object {
        fun newInstance(): NewTripFragment = NewTripFragment()
    }

    private var updateStatsRunnable: Runnable = object : Runnable {

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
        if (mVehicleName != null) {
            val spinner = vehicleSpinnerItem.actionView as Spinner
            if (spinner.adapter != null && !spinner.adapter.isEmpty) {
                vehicleSpinnerItem.isVisible = true
            }

            val tripsData = TripsData()
            if (points.isNotEmpty()) {
                tripsData.departure = TripUtils.getAddress(context!!, points.first())
                tripsData.destination = TripUtils.getAddress(context!!, points.last())
            }
            tripsData.start = startTime
            tripsData.end = startTime + duration
            tripsData.distance = distance
            tripsData.vehicleReg = mVehicleName!!

            val okClickListener = DialogInterface.OnClickListener { dialog, _ ->
                InsertTripTask(TripsDataBase.getInstance(context!!), tripsData, points, this)
                    .execute()
                dialog.dismiss()
            }
            TripUtils.showSaveTripDialog(context!!, tripsData, okClickListener)
        } else {
            TripUtils.showAddEditVehicleDialog(context!!, null, tripsViewModel)
        }
    }

    override fun onInsertComplete(success: Boolean) {
        reset()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showAlert(PERMISSIONS)
        } else {
            startTracking()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_ALL -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startTracking()
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

    }

    private fun isLocationEnabled() : Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showAlert(type : String) {
        val title : CharSequence
        val message : String
        val btnText : String

        if (type == LOCATION) {
            title = getString(R.string.dialog_location_title)
            message = getString(R.string.dialog_location_desc)
            btnText = getString(R.string.dialog_location_btn)
        } else {
            title = getString(R.string.dialog_permissions_title)
            message = getString(R.string.dialog_permissions_desc)
            btnText = getString(R.string.dialog_permissions_btn)
        }

        val dialog = AlertDialog.Builder(context!!)
        dialog.setCancelable(false)
        dialog.setTitle(title).setMessage(message).setPositiveButton(btnText) { _, _ ->
            if (type == LOCATION) {
                val myIntent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                startActivity(myIntent)
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ALL)
            }
        }
        dialog.show()
    }

    private fun getDistanceBetween(p1 : LatLng, p2 : LatLng) : Double {
        val results = FloatArray(1)
        Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, results)

        return results[0].toDouble()/1000
    }

    override fun onCreateOptionsMenu(menu: Menu?, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_new_trip, menu)

        val HANDLE_VEHICLES = getString(R.string.handle_vehicles)

        vehicleSpinnerItem = menu!!.findItem(R.id.spinner)
        val spinner = vehicleSpinnerItem.actionView as Spinner

        tripsViewModel.getAllVehicles().observe(this, Observer { vehicleItems ->
            var vehicles = vehicleItems.map { it.reg }
            if (vehicles.isNotEmpty()) {
                if(!running && points.isNotEmpty() && vehicles.size == 1) {
                    mVehicleName = vehicles.get(0)

                    // SAVE
                    showSaveTripDialog()
                }
                vehicles += HANDLE_VEHICLES
                val adapter = ArrayAdapter<String>(context, R.layout.vehicle_spinner, vehicles)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
                vehicleSpinnerItem.isVisible = true

                // TODO Restore vehicle from SharedPrefs
//                val storedVehicleReg = "asdasd"
//                spinner.setSelection(adapter.getPosition(mVehicleName))

            } else {
                mVehicleName = null
                vehicleSpinnerItem.isVisible = false
            }
        })

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val itemString = (view as TextView).text.toString()
                if (itemString == HANDLE_VEHICLES) {

                    startActivity(Intent(context, VehiclesActivity::class.java))

                } else {
                    mVehicleName = itemString
                    // TODO Save vehicle to SharedPrefs
                }
            }
        }
        super.onCreateOptionsMenu(menu, menuInflater)
    }

    override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
        locationService = (iBinder as MyLocationService.MyBinder).service
        locationService.setCallBack(this)
        if (locationService.isTracking()) {
            setRunning(locationService.getStartTime())
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        locationService.setCallBack(null)
        bound = false
    }

    override fun onServiceLocationUpdate(coordinates: MutableList<LatLng>, timestamps: MutableList<Long>) {
        Log.e(TAG, "onLocationChanged: ${coordinates.last()}")
        points = coordinates;

        banner.visibility = View.INVISIBLE

        val lastPosition = points.last()

        if (::marker.isInitialized) {
            marker.position = lastPosition
        } else {
            mo = MarkerOptions().position(lastPosition).title("My position").icon(TripUtils.bitmapDescriptorFromVector(context!!, R.drawable.ic_map_marker_circle)).anchor(0.5f, 0.5f)
            marker = mMap.addMarker(mo)
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPosition, 14.5f))
        mPolyline?.remove()
        if (points.size > 1) {
            mPolyline = mMap.addPolyline(PolylineOptions().addAll(points))
            distance += getDistanceBetween(points.get(points.size - 2), points.get(points.size - 1));
            distanceTextView.text = String.format("%.2f", distance)

            var timeMS = 0.0
            var distanceKM = 0.0
            if (points.size > 5) {
                for (i in (points.size - 5)..(points.size - 1) ) {
                    timeMS += timestamps.get(i) - timestamps.get(i - 1)
                    distanceKM += getDistanceBetween(points.get(i), points.get(i - 1))
                }

                val speed = distanceKM/(timeMS/timeH);
                speedTextView.text = speed.toInt().toString()
            }
        }
    }

    private fun startService() {
        val intent = Intent(context, MyLocationService::class.java)
        activity!!.startService(intent)
    }

    private fun startTracking() {
        vehicleSpinnerItem.isVisible = false
        setRunning(System.currentTimeMillis())
        locationService.setCallBack(this)
        locationService.startLocationTracking()
    }

    private fun stopService() {
        if (bound) {
            doUnbindService()
        }
        val intent = Intent(context, MyLocationService::class.java)
        activity!!.stopService(intent)
    }

    private fun stopTracking() {
        handler.removeCallbacks(updateStatsRunnable)
        running = false
        locationService.stopLocationTracking()

        startButton.text = getString(R.string.btn_save)
    }

    private fun doBindService() {
        activity!!.bindService(Intent(context, MyLocationService::class.java), this@NewTripFragment, Context.BIND_AUTO_CREATE)
        bound = true
    }

    private fun doUnbindService() {
        if (bound) {
            // Detach our existing connection.
            locationService.setCallBack(null)
            activity!!.unbindService(this@NewTripFragment)
            bound = false
        }
    }

}