package com.afconsult.korjournal.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface TripsDataDao {
    @Query("SELECT * from trips order by id desc")
    fun getAll(): List<TripsData>

    @Query("SELECT * from trips WHERE id = :rowId")
    fun getTrip(rowId : Long): TripsData?

    @Insert(onConflict = REPLACE)
    fun insert(tripsData: TripsData) : Long

    @Query("DELETE from trips WHERE id = :rowId")
    fun delete(rowId: Long)

    @Query("DELETE from trips")
    fun deleteAll()

}