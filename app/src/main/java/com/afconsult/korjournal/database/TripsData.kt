package com.afconsult.korjournal.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.afconsult.korjournal.utils.TripUtils
import java.text.DecimalFormat
import java.util.*

@Entity(tableName = "trips")
data class TripsData(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "exported") var exported: Boolean,
    @ColumnInfo(name = "distance") var distance: Double,

    @ColumnInfo(name = "start") var start: Long?,
    @ColumnInfo(name = "end") var end: Long?,

    @ColumnInfo(name = "departure") var departure: String,
    @ColumnInfo(name = "destination") var destination: String,

    @ColumnInfo(name = "vehicle_reg") var vehicleReg: String,
    @ColumnInfo(name = "vehicle_type") var vehicleType: String,

    @ColumnInfo(name = "notes") var notes: String?

) {
    constructor() : this(null, false, 0.0, null, null, "", "", "FRY091", "F01", "")

    fun getDistanceString(): String {
        return DecimalFormat("#.#").format(distance)
    }

    fun exportString(): String {
        return TripUtils.formatDate(Date(this.start!!)) + ";" +
                TripUtils.formatTime(Date(this.start!!)) + ";" +
                TripUtils.formatDate(Date(this.end!!)) + ";" +
                TripUtils.formatTime(Date(this.end!!)) + ";" +
                "$departure;" +
                "$destination;" +
                "$vehicleType;" +
                getDistanceString() + ";" +
                "$vehicleReg;" +
                "$notes"
    }
}