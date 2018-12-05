package com.afconsult.korjournal

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_previous_trips.*

class PreviousTripsFragment : Fragment() {

    private val mUiHandler = Handler()

    // Initializing an empty ArrayList to be filled with animals
    val animals: ArrayList<String> = ArrayList()

//    val mDb = (activity as? MainActivity).mDb
//    var mDbWorkerThread = (activity as MainActivity)!!.mDbWorkerThread?

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_previous_trips, container, false)

    companion object {
        fun newInstance(): PreviousTripsFragment = PreviousTripsFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Loads companies into the ArrayList
        addCompanies()

        // Creates a vertical Layout Manager
        recyclerView.layoutManager = LinearLayoutManager(activity)

        // You can use GridLayoutManager if you want multiple columns. Enter the number of columns as a parameter.
//        rv_animal_list.layoutManager = GridLayoutManager(this, 2)

        // Access the RecyclerView Adapter and load the data into it
//        recyclerView.adapter = TripAdapter(animals, activity)

        recyclerView.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))

        fetchTripsDataFromDb()

    }

    fun fetchTripsDataFromDb() {
        val task = Runnable {
            val tripsData = (activity as? MainActivity)!!.mDb?.tripsDataDao()?.getAll()
            mUiHandler.post({
                if (tripsData == null || tripsData?.size == 0) {
                    Toast.makeText(context, "Database empty!!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Items in db: ${tripsData.size}", Toast.LENGTH_SHORT).show()
                    recyclerView.adapter = TripAdapter(tripsData, activity)
//                    bindDataWithUi(tripsData = tripsData?.get(0))
                }
            })
        }
        (activity as? MainActivity)!!.mDbWorkerThread?.postTask(task)
    }

    //    private fun bindDataWithUi(weatherData: WeatherData?) {
//        mTempInC.text = weatherData?.tempInC.toString()
//        mTempInF.text = weatherData?.tempInF.toString()
//        mLatitude.text = weatherData?.lat.toString()
//        mLongitude.text = weatherData?.lon.toString()
//        mName.text = weatherData?.name
//        mRegion.text = weatherData?.region
//    }

    fun addCompanies() {
        animals.add("Tetra Pak")
        animals.add("Eon")
        animals.add("Apple")
        animals.add("Sony")
        animals.add("Pågen")
        animals.add("ÅF Solna")
        animals.add("Volvo")
        animals.add("P7")
    }
}