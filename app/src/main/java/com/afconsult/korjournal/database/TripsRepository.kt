package com.afconsult.korjournal.database


import android.app.Application
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.afconsult.korjournal.database.TripsDataBase
import com.afconsult.korjournal.database.VehicleData
import com.afconsult.korjournal.database.VehicleDataDao

/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
class TripsRepository(application: Application) {

    private var vehicleDataDao: VehicleDataDao
    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    private var allVehicles: LiveData<List<VehicleData>>

    init {
        val database: TripsDataBase = TripsDataBase.getInstance(
            application.applicationContext
        )
        vehicleDataDao = database.vehicleDataDao()
        allVehicles = vehicleDataDao.getAllVehicles()
    }

    // You must call this on a non-UI thread or your app will crash. So we're making this a
    // suspend function so the caller methods know this.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    // TODO run on workethread
//    @Suppress("RedundantSuspendModifier")
//    @WorkerThread
    fun insertVehicle(vehicleData: VehicleData) {
        vehicleDataDao.insert(vehicleData)
    }

    fun getAllVehicles(): LiveData<List<VehicleData>> {
        return allVehicles
    }
}
