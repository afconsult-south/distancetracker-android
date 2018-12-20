package com.afconsult.korjournal.tasks

import android.os.AsyncTask
import com.afconsult.korjournal.database.PathData
import com.afconsult.korjournal.database.TripsData
import com.afconsult.korjournal.database.TripsDataBase
import com.google.android.gms.maps.model.LatLng

class InsertTripTask(database: TripsDataBase, tripsData: TripsData, tripsPoints: MutableList<LatLng>, insertCallback: InsertCallback) : AsyncTask<Void, Void, Boolean>() {
    private val db : TripsDataBase? = database
    private val data : TripsData? = tripsData
    private val points : MutableList<LatLng>? = tripsPoints
    private val listener : InsertCallback = insertCallback

    interface InsertCallback {
        fun onInsertComplete(success: Boolean)
    }

    override fun doInBackground(vararg params: Void?): Boolean? {
        val tripID = db?.tripsDataDao()?.insert(data!!)
        var success : Boolean = tripID != -1L

        println("aaa "+ points!!.size)
        if (points.size > 0) {
            for (i in 0..(points.size - 1)) {
                println("aaa $tripID + , $i")
                success = db!!.pathDataDao().insert(PathData(tripID!!, i.toLong(), points[i].latitude, points[i].longitude)) > 0
            }
        }

        return success
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)

        listener.onInsertComplete(result)
    }
}