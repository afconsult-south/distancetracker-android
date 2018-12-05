package com.afconsult.korjournal.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import java.util.*


@Database(entities = arrayOf(TripsData::class), version = 2)
abstract class TripsDataBase : RoomDatabase() {

    abstract fun tripsDataDao(): TripsDataDao

    companion object {
        private var INSTANCE: TripsDataBase? = null

        fun getInstance(context: Context): TripsDataBase? {
            if (INSTANCE == null) {
                synchronized(TripsDataBase::class) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
//                    INSTANCE = Room.inMemoryDatabaseBuilder(context.getApplicationContext(),
                        TripsDataBase::class.java, "trips.db")
//                        .allowMainThreadQueries()
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}

object DateConverter {

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return if (timestamp == null) null else Date(timestamp)
    }

    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return (if (date == null) null else date.getTime())!!.toLong()
    }
}