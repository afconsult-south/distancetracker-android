package com.afconsult.korjournal.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.afconsult.korjournal.database.TripsData

@Dao
interface PathDataDao {
    @Insert(onConflict = REPLACE)
    fun insert(pathData: PathData) : Long

    // Trip
    @Query("SELECT * from path WHERE trip_id = :tripId")
    fun getAllPaths(tripId: Long): List<PathData>
}