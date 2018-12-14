package com.afconsult.korjournal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.afconsult.korjournal.TripAdapter.TripViewHolder
import com.afconsult.korjournal.database.TripsData
import kotlinx.android.synthetic.main.trip_list_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class TripAdapter(val items: List<TripsData>, val context: FragmentActivity?, private val listener: OnTripClickListener) :
    RecyclerView.Adapter<TripViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyy/MM/dd, HH:mm", Locale.getDefault())

    interface OnTripClickListener {
        fun onTripClick(trip: TripsData)
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
        holder.departure.text = items.get(position).departure
        holder.destination.text = items.get(position).destination
        holder.distance.text = items.get(position).getDistanceString()

        val date = Date(items.get(position).start!!)
        holder.date.text = dateFormat.format(date)

        holder.itemView.setOnClickListener { listener.onTripClick(items.get(position))}
    }

    class TripViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each animal to
        val departure = view.fromTextView
        val destination = view.toTextView
        val distance = view.distanceTextView
        val date = view.dateTextView
    }
}

