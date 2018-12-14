package com.afconsult.korjournal

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.afconsult.korjournal.database.TripsData
import kotlinx.android.synthetic.main.fragment_previous_trips.*

class PreviousTripsFragment : Fragment(), TripAdapter.OnTripClickListener {

    private val mUiHandler = Handler()

//    val mDb = (activity as? MainActivity).mDb
//    var mDbWorkerThread = (activity as MainActivity)!!.mDbWorkerThread?

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_previous_trips, container, false)

    companion object {
        fun newInstance(): PreviousTripsFragment = PreviousTripsFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Creates a vertical Layout Manager
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))

        fetchTripsDataFromDb()
    }

    private fun fetchTripsDataFromDb() {
        val task = Runnable {
            val tripsData = (activity as? MainActivity)!!.mDb?.tripsDataDao()?.getAll()
            mUiHandler.post {
                if (tripsData == null || tripsData.size == 0) {
                    Toast.makeText(context, "Database empty!!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Items in db: ${tripsData.size}", Toast.LENGTH_SHORT).show()
                    recyclerView.adapter = TripAdapter(tripsData, activity, this as TripAdapter.OnTripClickListener)
                }
            }
        }
        (activity as? MainActivity)!!.mDbWorkerThread.postTask(task)
    }

    override fun onTripClick(trip: TripsData) {
        Toast.makeText(context, trip.departure + " " + trip.destination,  Toast.LENGTH_LONG).show()
        startActivity(TripDetailsActivity.newInstance(context, trip.id))
    }

}