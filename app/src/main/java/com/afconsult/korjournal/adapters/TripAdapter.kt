package com.afconsult.korjournal.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.afconsult.korjournal.R
import com.afconsult.korjournal.utils.TripUtils
import com.afconsult.korjournal.adapters.TripAdapter.TripViewHolder
import com.afconsult.korjournal.database.TripsData
import kotlinx.android.synthetic.main.trip_list_item.view.*
import java.util.*

class TripAdapter(val items: List<TripsData>, val context: FragmentActivity?, private val listener: OnTripClickListener) :
    RecyclerView.Adapter<TripViewHolder>() {

    interface OnTripClickListener {
        fun onTripClick(trip: TripsData)
        fun onTripLongClick(trip: TripsData)
    }

    // Gets the number of trips in the list
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): TripViewHolder {
        return TripViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.trip_list_item, parent, false))
    }

    // Binds each trip in the ArrayList to a view
    override fun onBindViewHolder(holder: TripAdapter.TripViewHolder, position: Int) {
        holder.date.text = TripUtils.formatDate(Date(items.get(position).start!!))
        holder.regNumber.text = items.get(position).vehicleReg


        holder.departure.text = items.get(position).departure
        holder.destination.text = items.get(position).destination
        holder.distance.text = items.get(position).getDistanceString()


        holder.fromTime.text = TripUtils.formatTime(Date(items.get(position).start!!))
        holder.toTime.text = TripUtils.formatTime(Date(items.get(position).end!!))

        holder.itemView.setOnClickListener { listener.onTripClick(items.get(position))}
        holder.itemView.setOnLongClickListener { listener.onTripLongClick(items.get(position))
        return@setOnLongClickListener true }
    }

    class TripViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each animal to
        val departure = view.fromTextView
        val destination = view.toTextView
        val distance = view.distanceTextView
        val date = view.dateTextView
        val fromTime = view.fromTimeTextView
        val toTime = view.toTimeTextView
        val regNumber = view.regNumberTextView
    }
}

