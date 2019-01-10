package com.afconsult.korjournal

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afconsult.korjournal.adapters.VehicleListAdapter
import com.afconsult.korjournal.database.TripsViewModel
import com.afconsult.korjournal.database.VehicleData
import com.afconsult.korjournal.utils.TripUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton

class VehiclesActivity : AppCompatActivity(), VehicleListAdapter.OnVehicleClickListener {

    override fun onVehicleEdit(vehicle: VehicleData) {
        Toast.makeText(applicationContext, "Ändra", Toast.LENGTH_LONG).show()
        TripUtils.showAddEditVehicleDialog(this, vehicle, tripsViewModel)
    }

    override fun onVehicleRemove(vehicle: VehicleData) {
        TripUtils.showDeleteVehicleDialog(this, vehicle, tripsViewModel)
    }

    //    private val newWordActivityRequestCode = 1
    private lateinit var tripsViewModel: TripsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicles)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = VehicleListAdapter(
            this,
            this as VehicleListAdapter.OnVehicleClickListener
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL))

        tripsViewModel = ViewModelProviders.of(this).get(TripsViewModel::class.java)

        tripsViewModel.getAllVehicles().observe(this, androidx.lifecycle.Observer { vehicleItems ->
            vehicleItems?.let { adapter.setVehicles(it) }
        })

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            TripUtils.showAddEditVehicleDialog(this, null, tripsViewModel)
        }
    }

}
