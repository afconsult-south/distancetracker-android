package com.afconsult.korjournal

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.afconsult.korjournal.database.TripsData
import kotlinx.android.synthetic.main.fragment_new_trip.*


class NewTripFragment : Fragment() {

    var millisecondTime: Long = 0
    var startTime: Long = 0
    var duration: Long = 0
    var updateTime = 0L
    var handler: Handler = Handler()
    var seconds: Int = 0
    var minutes: Int = 0
    var hours: Int = 0

    var distance: Double = 12.4

    var running: Boolean = false;

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_new_trip, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startButton.setOnClickListener {
            resetButton.isEnabled = true

            if (!running) {
                running = true
//                Toast.makeText(context, "Resan startas!", Toast.LENGTH_LONG).show()
                startButton.setText("Stoppa resa")
                startButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stop, 0, 0, 0)

                startTime = System.currentTimeMillis()
                handler.postDelayed(runnable, 0)
            } else {
                duration += millisecondTime;

                handler.removeCallbacks(runnable);

                showCreateCategoryDialog()
            }

//            openMaps()
        }

        resetButton.setOnClickListener {
            reset()
        }
    }

    private fun reset() {
        handler.removeCallbacks(runnable);

        millisecondTime = 0
        startTime = 0
        duration = 0
        updateTime = 0L
        handler = Handler()
        seconds = 0
        minutes = 0
        hours = 0

        running = false

        durationTextView.text = "00:00:00"
        distanceTextView.text = "00.00"

        startButton.setText("Starta resa")
        startButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_circle_outline, 0, 0, 0)

        resetButton.isEnabled = false

    }



    companion object {
        fun newInstance(): NewTripFragment = NewTripFragment()
    }

    var runnable: Runnable = object : Runnable {

        override fun run() {

            millisecondTime = System.currentTimeMillis() - startTime

            updateTime = duration + millisecondTime

            seconds = (updateTime.toInt() / 1000)

            minutes = seconds / 60

            seconds = seconds % 60

            minutes = minutes % 60

            hours = minutes / 60

            durationTextView.setText(
                String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format(
                    "%02d",
                    seconds
                )
            )

            handler.postDelayed(this, 0)
        }

    }

    fun showCreateCategoryDialog() {
        val builder = AlertDialog.Builder(this.context!!)
        builder.setTitle("Spara resa")

        val view = layoutInflater.inflate(R.layout.dialog_save_trip, null)

        val departureEditText = view.findViewById(R.id.departureEditText) as EditText
        val destinationEditText = view.findViewById(R.id.destinationEditText) as EditText
        val noteEditText = view.findViewById(R.id.noteEditText) as EditText

        builder.setView(view);

        // set up the ok button
        builder.setPositiveButton(android.R.string.ok) { dialog, p1 ->

            val tripsData = TripsData()
            tripsData.departure = departureEditText.text.toString()
            tripsData.destination = destinationEditText.text.toString()
            tripsData.notes = noteEditText.text.toString()

            tripsData.start = startTime
            tripsData.end = startTime + duration
            tripsData.distance = distance

            (activity as MainActivity).insertTripsDataInDb(tripsData)
            var isValid = false
            if (departureEditText.text.isBlank()) {
                departureEditText.error = "Invalid input"
                isValid = false
            }

            if (isValid) {
                // do something
            }

            if (isValid) {
                dialog.dismiss()
            }

            reset()
        }

        builder.setNegativeButton(android.R.string.cancel) { dialog, p1 ->
            dialog.cancel()
        }

        builder.show();
    }
}