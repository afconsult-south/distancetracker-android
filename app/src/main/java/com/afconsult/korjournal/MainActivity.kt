package com.afconsult.korjournal

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.afconsult.korjournal.database.DbWorkerThread
import com.afconsult.korjournal.database.TripsDataBase
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import androidx.media.app.NotificationCompat as MediaNotificationCompat


class MainActivity : AppCompatActivity() {
    lateinit var mDbWorkerThread: DbWorkerThread
    var mDb: TripsDataBase? = null
//    private val mUiHandler = Handler()

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_start -> {
                mainToolbar.title = getString(R.string.title_new_trip)
                val trackFragment = NewTripFragment.newInstance()
                openFragment(trackFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_log -> {
                mainToolbar.title = getString(R.string.title_previous_trips)
                val logFragment = PreviousTripsFragment.newInstance()
                openFragment(logFragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(mainToolbar)

        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()

        mDb = TripsDataBase.getInstance(this)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        openFragment(NewTripFragment.newInstance())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun openMaps() {
        startActivity(Intent(this, MapsActivity::class.java))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_map -> {
                openMaps()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }

    override fun onDestroy() {
        TripsDataBase.destroyInstance()
        mDbWorkerThread.quit()
        super.onDestroy()
    }

}
