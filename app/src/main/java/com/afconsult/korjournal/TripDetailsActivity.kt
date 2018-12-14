package com.afconsult.korjournal

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.afconsult.korjournal.database.TripsData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_trip_details.*
import java.text.SimpleDateFormat
import java.util.*

class TripDetailsActivity : AppCompatActivity(), OnMapReadyCallback, GetTripTask.TripCallback {
    private lateinit var mMap: GoogleMap
    private val dateFormat = SimpleDateFormat("yyy/MM/dd, HH:mm", Locale.getDefault())

    companion object {
        private const val TRIP_ID = "id"

        fun newInstance(context: Context?, tripId: Long?): Intent {
            val intent = Intent(context, TripDetailsActivity::class.java)
            intent.putExtra(TRIP_ID, tripId)

            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_details)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "TODO Edit", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        GetTripTask(this, intent.getLongExtra(TRIP_ID, -1)).execute()
    }

    override fun onGetTripComplete(tripData: TripsData, points: List<LatLng>) {
        fromTextView.text = tripData.departure
        toTextView.text = tripData.destination
        distanceTextView.text = tripData.getDistanceString()
        dateTextView.text = dateFormat.format(Date(tripData.start!!))

        var polyline = mMap.addPolyline(PolylineOptions().addAll(points))

        val builder = LatLngBounds.Builder()
        for (p in points) {
            builder.include(p)
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150))
        mMap.addMarker(MarkerOptions().position(points.first()).title("Departure").icon(bitmapDescriptorFromVector(this, R.drawable.ic_flag)))
        mMap.addMarker(MarkerOptions().position(points.last()).title("Destination").icon(bitmapDescriptorFromVector(this, R.drawable.ic_flag_checkered)))

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap =
            Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

}
