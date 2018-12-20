package com.afconsult.korjournal

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.afconsult.korjournal.database.TripsData
import com.afconsult.korjournal.database.TripsDataBase
import com.afconsult.korjournal.tasks.DeleteTripTask
import kotlinx.android.synthetic.main.fragment_previous_trips.*
import java.util.*
import kotlin.collections.ArrayList

class PreviousTripsFragment : Fragment(), TripAdapter.OnTripClickListener, DeleteTripTask.DeleteCallback {
    private val mUiHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_previous_trips, container, false)

    companion object {
        fun newInstance(): PreviousTripsFragment = PreviousTripsFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        fetchTripsDataFromDb()
    }

    private lateinit var mTripsData: List<TripsData>

    private fun fetchTripsDataFromDb() {
        val task = Runnable {
            mTripsData = (activity as? MainActivity)!!.mDb?.tripsDataDao()?.getAll()!!
            mUiHandler.post {
                if (mTripsData == null || mTripsData.size == 0) {
                    Toast.makeText(context, "Database empty!!", Toast.LENGTH_SHORT).show()
                } else {
                    recyclerView.adapter = TripAdapter(mTripsData, activity, this as TripAdapter.OnTripClickListener)
                }
            }
        }
        (activity as? MainActivity)!!.mDbWorkerThread.postTask(task)
    }

    override fun onTripClick(trip: TripsData) {
        startActivity(TripDetailsActivity.newInstance(context, trip.id))
    }

    override fun onCreateOptionsMenu(menu: Menu?, menuInflater: MenuInflater?) {
        menuInflater!!.inflate(R.menu.menu_previous_trips, menu)
        super.onCreateOptionsMenu(menu, menuInflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_export_all -> {
                TripUtils.exportToMail(activity!!, mTripsData)
                true
            }
            R.id.action_delete_all -> {
                showDeleteAllTripsDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteAllTripsDialog() {
        val ids : ArrayList<Long> = ArrayList()
        for (trip in mTripsData) {
            ids.add(trip.id!!)
        }
        val okClickListener = DialogInterface.OnClickListener { dialog, which ->
            DeleteTripTask(
                TripsDataBase.getInstance(context!!),
                this as DeleteTripTask.DeleteCallback
            ).execute(ids)
            dialog.dismiss()
        }
        TripUtils.showDeleteTripDialog(context!!, "ALLA", okClickListener)
    }

    override fun onTripLongClick(trip: TripsData) {
        val okClickListener = DialogInterface.OnClickListener { dialog, which ->
            DeleteTripTask(
                TripsDataBase.getInstance(context!!),
                this as DeleteTripTask.DeleteCallback
            ).execute(trip.id as List<Long>)
            dialog.dismiss()
        }
        TripUtils.showDeleteTripDialog(context!!, TripUtils.formatDateTime(Date(trip.start!!)), okClickListener)
    }

    override fun onDeleteComplete(success: Boolean) {
        fetchTripsDataFromDb()
    }

}