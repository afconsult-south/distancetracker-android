package com.afconsult.korjournal

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.net.Uri
import android.view.LayoutInflater
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.afconsult.korjournal.database.TripsData
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object TripUtils {

    fun showEditTripDialog(context: Context, tripsData: TripsData, okClickListener: DialogInterface.OnClickListener) {
        showSaveEditTripDialog(context, tripsData, "Ändra resa", okClickListener)
    }

    fun showSaveTripDialog(context: Context, tripsData: TripsData, okClickListener: DialogInterface.OnClickListener) {
        showSaveEditTripDialog(context, tripsData, "Spara resa", okClickListener)
    }

    private fun showSaveEditTripDialog(context: Context, tripsData: TripsData, title: String, okClickListener: DialogInterface.OnClickListener) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_save_trip, null)
        val departureEditText = view.findViewById(R.id.departureEditText) as EditText
        val destinationEditText = view.findViewById(R.id.destinationEditText) as EditText

        val noteEditText = view.findViewById(R.id.noteEditText) as EditText
        val builder = androidx.appcompat.app.AlertDialog.Builder(context!!)
        builder.setTitle(title)
        builder.setView(view)

        departureEditText.setText(tripsData.departure)
        destinationEditText.setText(tripsData.destination)
        noteEditText.setText(tripsData.notes)

        val ok = DialogInterface.OnClickListener { dialog, which ->
            tripsData.departure = departureEditText.text.toString()
            tripsData.destination = destinationEditText.text.toString()
            tripsData.notes = noteEditText.text.toString()

            okClickListener.onClick(dialog, which)
            dialog.dismiss()
        }

        builder.setPositiveButton(android.R.string.ok, ok)
        builder.setNegativeButton(android.R.string.cancel) { dialog, p1 ->
            dialog.cancel()
        }
        builder.show()
    }

    fun showDeleteTripDialog(context: Context, date: String, okClickListener: DialogInterface.OnClickListener) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_save_trip, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(context!!)
        builder.setTitle("Radera resa")
        builder.setMessage("Vill du radera resan från $date")

        builder.setPositiveButton(android.R.string.ok, okClickListener)
        builder.setNegativeButton(android.R.string.cancel) { dialog, p1 ->
            dialog.cancel()
        }
        builder.show()
    }

    fun exportToMail(activity: FragmentActivity, trips: List<TripsData>) {
        val intent = Intent(Intent.ACTION_SENDTO)
        var outputString: String = String()
        for (trip in trips) {
            outputString += trip.exportString()
        }
        intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, "ÅFs Körjournal - export")
        intent.putExtra(Intent.EXTRA_TEXT, outputString)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
    * Address lookup of coords
    *
    * @return Address string, or empty string if null
    */
    fun getAddress(context: Context, latLan : LatLng) : String {
        val geocoder = Geocoder(context, Locale.getDefault());
        var retVal = ""

        try {

            val addresses = geocoder.getFromLocation(latLan.latitude, latLan.longitude, 1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            val firstAddress = addresses.get(0)

            val knownName = firstAddress.getFeatureName(); // Only if available else return NULL
            val address = firstAddress.getAddressLine(0)// If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            val city = firstAddress.getLocality();
            //String state = addresses.get(0).getAdminArea();
            //String country = addresses.get(0).getCountryName();
            //String postalCode = addresses.get(0).getPostalCode();

//        if (knownName != null) {
//            return "$knownName, $city"
//        }
            retVal = address.substringBeforeLast(",").substringBeforeLast(",") + ", " + firstAddress.locality
        } catch (e: IOException) {

        }

        return retVal
    }

    fun formatDateTime(date: Date) : String {
        val dateFormat = SimpleDateFormat("yyy-MM-dd, HH:mm", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun formatDate(date: Date) : String {
        val dateFormat = SimpleDateFormat("yyy-MM-dd", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun formatTime(date: Date) : String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap =
            Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
