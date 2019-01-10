package com.afconsult.korjournal

import android.app.Application
import androidx.room.Room
import com.afconsult.korjournal.database.DbWorkerThread
import com.afconsult.korjournal.database.TripsDataBase


class MyApplication : Application() {


    private lateinit var mDbWorkerThread: DbWorkerThread

    //    companion object DatabaseSetup {
//        var database: AppDatabase? = null
//    }
//
    override fun onCreate() {
        super.onCreate()
//        MyApplication.database = Room.databaseBuilder(this, TripsDataBase::class.java, "MyDatabase").build()

        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()
//
//        mDb = TripsDataBase.getInstance(this)
    }
}
