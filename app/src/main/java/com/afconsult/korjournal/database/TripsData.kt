package com.afconsult.korjournal.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.DecimalFormat

@Entity(tableName = "trips")
data class TripsData(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "exported") var exported: Boolean,
    @ColumnInfo(name = "distance") var distance: Double,

    @ColumnInfo(name = "start") var start: Long?,
    @ColumnInfo(name = "end") var end: Long?,

    @ColumnInfo(name = "departure") var departure: String,
    @ColumnInfo(name = "destination") var destination: String,

    @ColumnInfo(name = "vehicle_name") var vehicleName: String,
    @ColumnInfo(name = "vehicle_type") var vehicleType: String,

    @ColumnInfo(name = "notes") var notes: String?

) {
    constructor() : this(null, false, 0.0, null, null, "", "", "", "DIESEL", "")

    fun getDistanceString() :String {
        return DecimalFormat("#.##").format(distance)
    }
}