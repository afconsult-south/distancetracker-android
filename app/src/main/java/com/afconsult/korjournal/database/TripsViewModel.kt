package com.afconsult.korjournal.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class TripsViewModel(application: Application) : AndroidViewModel(application) {
    private var repository: TripsRepository =
        TripsRepository(application)
    private var allVehicles: LiveData<List<VehicleData>> = repository.getAllVehicles()

    fun insertVehicle(vehicle: VehicleData) {
        repository.insertVehicle(vehicle)
    }

    fun getAllVehicles(): LiveData<List<VehicleData>> {
        return allVehicles
    }
}
