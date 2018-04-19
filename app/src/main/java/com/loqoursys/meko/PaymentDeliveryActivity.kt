package com.loqoursys.meko

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import kotlinx.android.synthetic.main.activity_payment_delivery.*
import kotlinx.android.synthetic.main.content_payment_delivery.*
import utils.SpinnerAdapter
import utils.location.GetAddress
import utils.logData
import utils.selectedLocation

class PaymentDeliveryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_delivery)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            if (selectedLocation != null) {
                startActivity(Intent(this, OrderConfirmationActivity::class.java))
            } else {
                Snackbar.make(view, "Select your preferred delivery location",
                        Snackbar.LENGTH_LONG).show()
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        add_location.setOnClickListener {
            startActivityForResult(
                    Intent(this, MapActivity::class.java), RC_LOCATION)
        }

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
    }

    private fun togglePayment(pos: Int) {
//        showToast(this, "Selected $pos")
        logData("$pos")
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
        val localName = if (name == getString(R.string.no_location_selected)) {
            GetAddress(this).fetchCurrentAddress(location)
        } else {
            name
        }

        txt_location.text = localName
    }

    companion object {
        const val RC_LOCATION = 101
    }
}
