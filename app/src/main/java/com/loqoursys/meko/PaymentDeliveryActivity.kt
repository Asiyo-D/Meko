package com.loqoursys.meko

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.loqoursys.meko.data.SavedLocations

import kotlinx.android.synthetic.main.activity_payment_delivery.*
import kotlinx.android.synthetic.main.content_payment_delivery.*
import utils.*
import utils.location.GetAddress

class PaymentDeliveryActivity : AppCompatActivity() {
    private var locationTitle = ""
    var locationDescription = ""
    private var isNewLocation: Boolean = true

    private var uid = ""
    lateinit var savedLocationsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_delivery)
        setSupportActionBar(toolbar)

        uid = mAuth.currentUser!!.uid

        savedLocationsRef = mDatabase.getReference(FirebaseUtil.SAVED_LOCATIONS)

        fab.setOnClickListener { view ->
            if (selectedLocation != null) {
                if (isNewLocation) {
                    locationDescription = location_description.text.toString()
                }
                if (locationDescription.isNotEmpty()) {
                    startActivity(Intent(this, OrderConfirmationActivity::class.java)
                            .putExtra("location_des", locationDescription)
                            .putExtra("location_title", locationTitle))
                } else {
                    Snackbar.make(view, "Please add a description of the delivery location",
                            Snackbar.LENGTH_LONG).show()
                }
            } else {
                Snackbar.make(view, "Select your preferred delivery location",
                        Snackbar.LENGTH_LONG).show()
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        add_location.setOnClickListener {
            startActivityForResult(Intent(this, MapActivity::class.java)
                    .putExtra("subtitle", "Pick a delivery location"),
                    RC_LOCATION)
        }

        spinner_delivery_location.adapter = SpinnerAdapter(this,
                arrayOf("New location", "Home", "Work"))

        spinner_payment.adapter = SpinnerAdapter(
                toolbar.context,
                arrayOf("Cash on delivery"))

        spinner_payment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                togglePayment(position)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        spinner_delivery_location.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                toggleLocation(position)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
    }

    override fun onResume() {
        super.onResume()
        Preferences.loadPreferences(this)
    }

    private fun togglePayment(pos: Int) {
        logData("$pos")
    }

    private fun toggleLocation(pos: Int) {
        when (pos) {
            0 -> {
                isNewLocation = true
                selectedLocation = null
                locationTitle = ""
                locationDescription = ""

                add_location.visibility = View.VISIBLE
                location_description.visibility = View.VISIBLE
            }
            1 -> {
                isNewLocation = false
                if (Preferences.isHomeSet()) {
                    selectDeliveryHome()
                } else {
                    fab.snackBar("Home location not set. Go to locations to change your home location")
                }
            }
            2 -> {
                isNewLocation = false
                if (Preferences.isWorkSet()) {
                    selectDeliveryWork()
                } else {
                    fab.snackBar("Work location not set. Go to locations to change your work location")
                }
            }
        }
    }

    private fun selectDeliveryHome() {
        savedLocationsRef.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(snapshot: DataSnapshot?) {
                if (snapshot != null) {
                    val savedLocation = snapshot.getValue(SavedLocations::class.java)
                    if (savedLocation != null) {
                        val home = savedLocation.home

                        locationTitle = home.location_name
                        locationDescription = home.description

                        selectedLocation = Location("")
                        selectedLocation!!.latitude = home.latitude
                        selectedLocation!!.longitude = home.longitutde

                        txt_location.text = locationTitle
                    }
                }
            }
        })
    }

    private fun selectDeliveryWork() {
        savedLocationsRef.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(snapshot: DataSnapshot?) {
                if (snapshot != null) {
                    val savedLocation = snapshot.getValue(SavedLocations::class.java)
                    if (savedLocation != null) {
                        val work = savedLocation.work

                        locationTitle = work.location_name
                        locationDescription = work.description

                        selectedLocation = Location("")
                        selectedLocation!!.latitude = work.latitude
                        selectedLocation!!.longitude = work.longitutde

                        txt_location.text = locationTitle
                    }
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RC_LOCATION -> {
                    if (data != null) {
                        val locationName = data.getStringExtra("location name")
                        val location = data.getParcelableExtra<Location>("location")
                        handleMapData(locationName, location)
                    }
                }
            }
        }
    }

    private fun handleMapData(name: String, location: Location) {
        locationTitle = if (name == getString(R.string.no_location_selected)) {
            GetAddress(this).fetchCurrentAddress(location)
        } else {
            name
        }

        txt_location.text = locationTitle
        location_description.visibility = View.VISIBLE
    }

    companion object {
        const val RC_LOCATION = 101
    }
}
