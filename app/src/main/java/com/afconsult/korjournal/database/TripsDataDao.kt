package com.afconsult.korjournal.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.afconsult.korjournal.database.TripsData

@Dao
interface TripsDataDao {
    @Query("SELECT * from trips")
    fun getAll(): List<TripsData>

    @Query("SELECT * from trips WHERE id = :rowId")
    fun getTrip(rowId : Long): TripsData?

    @Insert(onConflict = REPLACE)
    fun insert(tripsData: TripsData) : Long


    @Query("DELETE from trips")
    fun deleteAll()

}