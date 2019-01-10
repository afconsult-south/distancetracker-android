package com.afconsult.korjournal

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.afconsult.korjournal.database.TripsData
import com.afconsult.korjournal.database.TripsDataBase
import com.afconsult.korjournal.tasks.DeleteTripTask
import com.afconsult.korjournal.tasks.GetTripTask
import com.afconsult.korjournal.tasks.InsertTripTask
import com.afconsult.korjournal.utils.TripUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_trip_details.*
import java.util.*

class TripDetailsActivity : AppCompatActivity(), OnMapReadyCallback, GetTripTask.TripCallback, InsertTripTask.InsertCallback, DeleteTripTask.DeleteCallback  {
    private lateinit var mMap: GoogleMap
    private lateinit var mTripData: TripsData
    private lateinit var mPoints: List<LatLng>

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
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true)

        fab.setOnClickListener { _ ->
            showEditTripDialog()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun showEditTripDialog() {
        val okClickListener = DialogInterface.OnClickListener { dialog, _ ->
            InsertTripTask(
                TripsDataBase.getInstance(this),
                mTripData,
                mPoints as MutableList<LatLng>,
                this
            ).execute()
            dialog.dismiss()
        }

        TripUtils.showEditTripDialog(this, mTripData, okClickListener)
    }

    override fun onInsertComplete(success: Boolean) {
        if (success) {
            Snackbar.make(fab, "Resan uppdaterad", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            GetTripTask(this, intent.getLongExtra(TRIP_ID, -1)).execute()

        }
    }

    override fun onGetTripComplete(tripData: TripsData, points: List<LatLng>) {
        mTripData = tripData
        mPoints = points
        fromTextView.text = tripData.departure
        toTextView.text = tripData.destination
        distanceTextView.text = tripData.getDistanceString()
        dateTextView.text = TripUtils.formatDateTime(Date(tripData.start!!))

        if (points.isNotEmpty()) {
            mMap.addPolyline(PolylineOptions().addAll(points))

            val builder = LatLngBounds.Builder()
            for (p in points) {
                builder.include(p)
            }

            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150))
            mMap.addMarker(
                MarkerOptions().position(points.first()).title("Departure").icon(
                    TripUtils.bitmapDescriptorFromVector(
                        this,
                        R.drawable.ic_flag
                    )
                ).anchor(0.25f, 0.9f)
            )
            mMap.addMarker(
                MarkerOptions().position(points.last()).title("Destination").icon(
                    TripUtils.bitmapDescriptorFromVector(
                        this,
                        R.drawable.ic_flag_checkered
                    )
                ).anchor(0.25f, 0.9f)
            )
        } else {
            noPointsView.visibility = View.VISIBLE
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        GetTripTask(this, intent.getLongExtra(TRIP_ID, -1)).execute()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export -> {
                TripUtils.exportToMail(this, listOf(mTripData))
                true
            }
            R.id.action_delete -> {
                showDeleteTripDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun showDeleteTripDialog() {
        val okClickListener = DialogInterface.OnClickListener { dialog, _ ->
            DeleteTripTask(
                TripsDataBase.getInstance(this),
                this as DeleteTripTask.DeleteCallback
            ).execute(listOf(mTripData.id!!))
            dialog.dismiss()
        }
        TripUtils.showDeleteTripDialog(this, TripUtils.formatDateTime(Date(mTripData.start!!)), okClickListener)
    }

    override fun onDeleteComplete(success: Boolean) {
        finish()
    }
}
