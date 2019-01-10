package com.afconsult.korjournal.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.afconsult.korjournal.R
import com.afconsult.korjournal.database.VehicleData


class VehicleListAdapter internal constructor(
        context: Context, private val listener: OnVehicleClickListener
) : RecyclerView.Adapter<VehicleListAdapter.VehicleViewHolder>() {

    interface OnVehicleClickListener {
        fun onVehicleEdit(vehicle: VehicleData)
        fun onVehicleRemove(vehicle: VehicleData)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var vehicles = emptyList<VehicleData>() // Cached copy of vehicles

    inner class VehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val vehicleItemView: View = itemView
        val vehicleRegView: TextView = itemView.findViewById(R.id.regNumberTextView)
        val removeItemView: ImageView = itemView.findViewById(R.id.remove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val itemView = inflater.inflate(R.layout.vehicle_item, parent, false)
        return VehicleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val current = vehicles[position]
        holder.vehicleItemView.setOnClickListener { listener.onVehicleEdit(current) }
        holder.vehicleRegView.text = current.reg
        holder.removeItemView.setOnClickListener { listener.onVehicleRemove(current) }
    }

    internal fun setVehicles(vehicles: List<VehicleData>) {
        this.vehicles = vehicles
        notifyDataSetChanged()
    }

    override fun getItemCount() = vehicles.size
}


