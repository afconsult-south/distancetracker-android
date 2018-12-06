package com.afconsult.korjournal.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "path",
    foreignKeys = arrayOf(
        ForeignKey(entity = TripsData::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("trip_id"),
            onDelete = ForeignKey.CASCADE)
    ))
data class PathData(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "index") var index: Long,
    @ColumnInfo(name = "x_coord") var xCoord: Double,

    @ColumnInfo(name = "y_coord") var yCoord: Double,
    @ColumnInfo(name = "trip_id") var tripId: Long

) {
    constructor(tripId : Long) : this(null, 0, 0.0, 0.0, tripId)

    constructor() : this(null, 0, 0.0, 0.0, 0)
}