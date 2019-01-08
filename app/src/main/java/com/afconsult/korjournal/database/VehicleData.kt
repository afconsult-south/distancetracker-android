package com.afconsult.korjournal.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class VehicleData(
    @PrimaryKey(autoGenerate = false) var reg: String,
    @ColumnInfo(name = "vehicle_type") var vehicleType: String?,
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "notes") var notes: String?,
    @ColumnInfo(name = "private") var privateVehicle: Boolean

) {
    constructor(regNr: String) : this(regNr, "", "", "", false)

}