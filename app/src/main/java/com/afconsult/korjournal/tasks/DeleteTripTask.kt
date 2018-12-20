package com.afconsult.korjournal.tasks

import android.os.AsyncTask
import com.afconsult.korjournal.database.TripsDataBase

class DeleteTripTask(database: TripsDataBase, deleteCallback: DeleteCallback) : AsyncTask<List<Long>, Void, Boolean>() {
    private val db : TripsDataBase? = database
    private val listener : DeleteCallback = deleteCallback

    interface DeleteCallback {
        fun onDeleteComplete(success: Boolean)
    }

    override fun doInBackground(vararg params: List<Long>?): Boolean? {
        val ids = params[0]
        if (ids != null) {
            for (id in ids) {
                db?.tripsDataDao()?.delete(id)
            }
        }
        return true
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)
        listener.onDeleteComplete(result)
    }
}