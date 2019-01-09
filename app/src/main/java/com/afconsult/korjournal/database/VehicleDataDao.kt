package com.afconsult.korjournal.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface VehicleDataDao {
    @Insert(onConflict = REPLACE)
    fun insert(vehicleData: VehicleData) : Long

    @Query("SELECT * from vehicles")
    fun getAllVehicles(): LiveData<List<VehicleData>>

    @Query("SELECT * from vehicles WHERE reg = :regNr")
    fun getVehicle(regNr: String): VehicleData
}