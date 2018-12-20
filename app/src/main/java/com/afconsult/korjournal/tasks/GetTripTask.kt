package com.afconsult.korjournal.tasks

import android.content.Context
import android.os.AsyncTask
import com.afconsult.korjournal.database.PathData
import com.afconsult.korjournal.database.TripsData
import com.afconsult.korjournal.database.TripsDataBase
import com.google.android.gms.maps.model.LatLng

class GetTripTask(c: Context, tripId: Long) : AsyncTask<Void, Void, Boolean>() {
    private val db = TripsDataBase.getInstance(c)
    private val id = tripId
    private val listener = c as TripCallback
    private lateinit var trip: TripsData
    private lateinit var points: List<LatLng>

    interface TripCallback {
        fun onGetTripComplete(
            tripsData: TripsData,
            points: List<LatLng>
        )
    }

    override fun doInBackground(vararg params: Void?): Boolean? {
        var success : Boolean = false

        trip = db.tripsDataDao().getTrip(id)!!
        points = getPoints(db.pathDataDao().getAllPaths(id))


        return success
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)
        listener.onGetTripComplete(trip, points)

    }

    private fun getPoints(pathDataList: List<PathData>) : List<LatLng> {
        val dataList = ArrayList<LatLng>()

        for (data in pathDataList) {
            dataList.add(LatLng(data.latitude, data.longitude))
        }
        return dataList
    }
}