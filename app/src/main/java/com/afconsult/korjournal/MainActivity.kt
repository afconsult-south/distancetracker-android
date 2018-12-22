package com.afconsult.korjournal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.afconsult.korjournal.database.DbWorkerThread
import com.afconsult.korjournal.database.TripsDataBase
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val CHANNEL_ID = "com.afconsult.korjournal.running"
    //    lateinit var mainToolbar: Toolbar
    var mDb: TripsDataBase? = null

    lateinit var mDbWorkerThread: DbWorkerThread
//    private val mUiHandler = Handler()
    private var notificationManager: NotificationManager? = null


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

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, getString(R.string.notification_trip_name), NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = getString(R.string.notification_trip_desc)
        channel.enableLights(true)
        channel.lightColor = Color.BLUE
        notificationManager?.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification() {

        val notificationID = 101
        val notification = androidx.core.app.NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
            .setContentTitle("En resa har startat")
            .setContentText("Stoppa resan här:")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_vehicle)
            .setChannelId(CHANNEL_ID)
            .setOngoing(true)
            .addAction(R.drawable.ic_stop, "Stoppa", null) // #0
            .setStyle(MediaNotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0 /* #0: stop button \*/)
                .setMediaSession(null))
            .build()

        notificationManager?.notify(notificationID, notification)
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
            R.id.action_notification -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendNotification()
                }
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
