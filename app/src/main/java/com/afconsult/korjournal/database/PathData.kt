package com.afconsult.korjournal.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.sql.Blob
import java.sql.Timestamp

@Entity(tableName = "path")
data class PathData(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "index") var index: Long,
    @ColumnInfo(name = "x_coord") var xCoord: Double,

    @ColumnInfo(name = "y_coord") var yCoord: Double,
    @ColumnInfo(name = "uuid") var uuid: String?

) {
//    constructor() : this(null, 0, 0.0, 0.0, "")
}