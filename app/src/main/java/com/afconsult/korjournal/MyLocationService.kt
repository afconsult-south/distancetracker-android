package com.afconsult.korjournal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.IBinder
import android.util.Log
import android.os.Bundle
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.google.android.gms.maps.model.LatLng
import java.util.ArrayList

class MyLocationService : Service() {
    private val TAG = "ServiceExample"

    val CHANNEL_ID = "com.afconsult.korjournal.running"
    val NOTIFICATION_ID = 101

    private var notificationManager: NotificationManager? = null

    private lateinit var mLocationManager: LocationManager
    private val LOCATION_INTERVAL = 1000L
    private val LOCATION_DISTANCE = 10f

    private val mLocalbinder = MyBinder()

    // Custom interface Callback which is declared in this Service
    private var mCallBack: CallBack? = null

    private var startTime : Long = 0L
    private var coordinates : MutableList<LatLng> = ArrayList()
    private var timestamps : MutableList<Long> = ArrayList<Long>()

    private var mLocationListeners =
        arrayOf(LocationListener(LocationManager.GPS_PROVIDER) /*, LocationListener(LocationManager.NETWORK_PROVIDER) */
        )

    // Callback interface
    interface CallBack {
        fun onServiceLocationUpdate(
            locationPoints: MutableList<LatLng>,
            timestamps: MutableList<Long>
        )
    }

    override fun onCreate() {
        Log.i(TAG, "Service onCreate")
        mLocationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onDestroy() {
        Log.i(TAG, "Service onDestroy")

        super.onDestroy()

        Toast.makeText(this, "MyService Completed or Stopped.", Toast.LENGTH_SHORT).show()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return mLocalbinder;
    }

    fun setCallBack(callBack: CallBack?) {
        mCallBack = callBack
    }

    //Custom Binder class
    inner class MyBinder : Binder() {
        val service: MyLocationService
            get() = this@MyLocationService
    }

    fun getStartTime() : Long {
        return startTime
    }

    fun isTracking() : Boolean {
        return startTime != 0L
    }

    fun startLocationTracking() {
        startTime = System.currentTimeMillis()

        try {
            mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                mLocationListeners[0]
            )
        } catch (ex: java.lang.SecurityException) {
            Log.i(TAG, "Failed to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "gps provider does not exist " + ex.message)
        }

//        try {
//            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[1])
//        } catch (ex: java.lang.SecurityException) {
//            Log.i(TAG, "fail to request location update, ignore", ex)
//        } catch (ex: IllegalArgumentException) {
//            Log.d(TAG, "network provider does not exist, " + ex.message)
//        }
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
            showNotification()
        }
    }

    fun stopLocationTracking() {
        startTime = 0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            cancelNotification()
        }
        mCallBack = null
        for (i in 0..mLocationListeners.lastIndex) {
            try {
                Log.d(TAG, "Shutting down: " + mLocationListeners[i])
                mLocationManager.removeUpdates(mLocationListeners[i])
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to remove location listeners, ignore", ex)
            }
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
    private fun showNotification() {
        // Create an Intent for the activity you want to start
        val resultIntent = Intent(this, MainActivity::class.java)
        // Create the TaskStackBuilder
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(this@MyLocationService, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_vehicle)
            .setChannelId(CHANNEL_ID)
            .setOngoing(true)
            .setContentIntent(resultPendingIntent)
//            .addAction(R.drawable.ic_stop, "Stoppa", null) // #0
//            .setStyle(
//                androidx.media.app.NotificationCompat.MediaStyle()
//                    .setShowActionsInCompactView(0 /* #0: stop button \*/)
//                    .setMediaSession(null))
            .build()

        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun cancelNotification() {
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    fun getCoordinates(): MutableList<LatLng> {
        return coordinates
    }

    fun getTimeStamps(): MutableList<Long> {
        return timestamps
    }

    private inner class LocationListener(provider: String) : android.location.LocationListener {
        internal var mLastLocation: Location

        init {
            Log.e(TAG, "LocationListener $provider")
            mLastLocation = Location(provider)
        }

        override fun onLocationChanged(location: Location) {
            Log.e(TAG, "onLocationChanged: $location")

            coordinates.add(LatLng(location.latitude, location.longitude))
            timestamps.add(System.currentTimeMillis())
            mLastLocation.set(location)

            mCallBack?.onServiceLocationUpdate(coordinates, timestamps)
        }

        override fun onProviderDisabled(provider: String) {
            Log.e(TAG, "onProviderDisabled: $provider")
        }

        override fun onProviderEnabled(provider: String) {
            Log.e(TAG, "onProviderEnabled: $provider")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.e(TAG, "onStatusChanged: $provider")
        }
    }
}
