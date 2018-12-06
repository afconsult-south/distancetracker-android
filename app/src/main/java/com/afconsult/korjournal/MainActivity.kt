package com.afconsult.korjournal

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.afconsult.korjournal.database.DbWorkerThread
import com.afconsult.korjournal.database.PathData
import com.afconsult.korjournal.database.TripsData
import com.afconsult.korjournal.database.TripsDataBase
import com.google.android.material.bottomnavigation.BottomNavigationView

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    //    lateinit var mainToolbar: Toolbar
    var mDb: TripsDataBase? = null

    lateinit var mDbWorkerThread: DbWorkerThread
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
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    fun openMaps() {
        startActivity(Intent(this, MapsActivity::class.java))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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

    fun insertTripsDataInDb(tripsData: TripsData) {
        val task = Runnable {
            val tripID = mDb?.tripsDataDao()?.insert(tripsData)

            val id = mDb?.pathDataDao()?.insert(PathData(tripID!!))

            println(id)
        }
        mDbWorkerThread.postTask(task)
    }

    override fun onDestroy() {
        TripsDataBase.destroyInstance()
        mDbWorkerThread.quit()
        super.onDestroy()
    }
}
