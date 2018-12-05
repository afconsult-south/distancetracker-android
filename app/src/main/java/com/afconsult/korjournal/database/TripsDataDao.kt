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

    @Insert(onConflict = REPLACE)
    fun insert(tripsData: TripsData)

    @Query("DELETE from trips")
    fun deleteAll()
}