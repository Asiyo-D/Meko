package com.loqoursys.meko

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.loqoursys.meko.data.Home
import com.loqoursys.meko.data.MekoUser
import com.loqoursys.meko.data.Work
import kotlinx.android.synthetic.main.activity_locations.*
import kotlinx.android.synthetic.main.app_bar_locations.*
import kotlinx.android.synthetic.main.content_locations.*
import utils.*
import utils.location.GetAddress

class LocationsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var isEditHome = true

    private var workTitle = ""
    private var homeTitle = ""
    private var uid = ""

    private lateinit var workLocation: Location
    private lateinit var homeLocation: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locations)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { _ ->
            editLocations()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        uid = mAuth.currentUser!!.uid
        val txtName: TextView = nav_view.getHeaderView(0).findViewById(R.id.username)
        val txtNumber: TextView = nav_view.getHeaderView(0).findViewById(R.id.phone_number)

        val phoneNumber = mAuth.currentUser!!.phoneNumber!!
        val num = "${phoneNumber.subSequence(0, 4)} ${phoneNumber.subSequence(4, 7)}" +
                " ${phoneNumber.subSequence(7, phoneNumber.length)}"
        txtNumber.text = num

        val userDBRef = mDatabase.getReference(FirebaseUtil.USERS_REF)
        userDBRef.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(snapshot: DataSnapshot?) {
                if (snapshot != null) {
                    val user = snapshot.getValue(MekoUser::class.java)
                    if (user != null) {
                        val name = user.full_name

                        txtName.text = name
                    }
                }
            }
        })

        btn_home_done.setOnClickListener {
            val homeDescription = location_home_description.text.toString()
            if (homeDescription.isEmpty()) {
                fab.snackBar("Enter a description of your home location")
            } else {
                saveHomeLocation(homeDescription)
            }
        }
        btn_work_done.setOnClickListener {
            val workDescription = location_work_description.text.toString()
            if (workDescription.isEmpty()) {
                fab.snackBar("Enter a description of your work location")
            } else {
                saveWorkLocation(workDescription)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        nav_view.menu.getItem(2).isChecked = true
        Preferences.loadPreferences(this)
    }

    private fun editLocations() {
        val dialog = MaterialDialog.Builder(this)
                .title("Edit locations")
                .items(arrayListOf("Home", "Work"))
                .itemsCallback { dialog, _, position, _ ->
                    isEditHome = when (position) {
                        0 -> true
                        else -> false
                    }
                    dialog.dismiss()

                    selectLocation()
                }
                .build()

        dialog.show()
    }

    private fun selectLocation() {
        val locationExtra = if (isEditHome) {
            "Pick your home location"
        } else {
            "Pick your work location"
        }

        startActivityForResult(Intent(this, MapActivity::class.java)
                .putExtra("subtitle", locationExtra),
                RC_EDIT_LOCATIONS)
    }


    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            R.id.nav_orders -> {
                startActivity(Intent(this, OrdersActivity::class.java))
                finishAffinity()
            }
            R.id.nav_locations -> {

            }
            R.id.nav_payments -> {
                startActivity(Intent(this, PaymentsActivity::class.java))
                finishAffinity()
            }
            R.id.nav_settings -> {

            }
            R.id.nav_help -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RC_EDIT_LOCATIONS -> {
                    if (data != null) {
                        val locationName = data.getStringExtra("location name")
                        val location = data.getParcelableExtra<Location>("location")
                        if (isEditHome) {
                            homeLocation = location
                            handleHomeData(locationName, location)
                        } else {
                            workLocation = location
                            handleWorkData(locationName, location)
                        }
                    }
                }
            }
        }
    }

    private fun handleWorkData(name: String, location: Location) {
        workTitle = if (name == getString(R.string.no_location_selected)) {
            GetAddress(this).fetchCurrentAddress(location)
        } else {
            name
        }

        txt_location_work.text = workTitle
        location_work_description.visibility = View.VISIBLE
        btn_work_done.visibility = View.VISIBLE
    }

    private fun handleHomeData(name: String, location: Location) {
        homeTitle = if (name == getString(R.string.no_location_selected)) {
            GetAddress(this).fetchCurrentAddress(location)
        } else {
            name
        }

        txt_location_home.text = homeTitle
        location_home_description.visibility = View.VISIBLE

        btn_home_done.visibility = View.VISIBLE

    }


    private fun saveHomeLocation(des: String) {
        val home = Home(homeLocation.latitude, homeLocation.longitude, homeTitle, des)
        val workRef = mDatabase.getReference(FirebaseUtil.SAVED_LOCATIONS)
                .child(uid)
                .child("SavedLocations")
                .child("Home")
        workRef.setValue(home)

        btn_home_done.visibility = View.INVISIBLE
        location_home_description.visibility = View.GONE

        fab.snackBar("Home location added")
        Preferences.setPreferenceHome(true)

    }

    private fun saveWorkLocation(des: String) {
        val work = Work(workLocation.latitude, workLocation.longitude, workTitle, des)
        val workRef = mDatabase.getReference(FirebaseUtil.SAVED_LOCATIONS)
                .child(uid)
                .child("SavedLocations")
                .child("Work")
        workRef.setValue(work)

        btn_work_done.visibility = View.INVISIBLE
        location_work_description.visibility = View.GONE

        fab.snackBar("Work location added")
        Preferences.setPreferenceWork(true)

    }

    companion object {
        const val RC_EDIT_LOCATIONS = 100
    }
}
