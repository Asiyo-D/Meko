package com.loqoursys.meko

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.loqoursys.meko.data.MekoUser

import kotlinx.android.synthetic.main.activity_locations.*
import kotlinx.android.synthetic.main.app_bar_locations.*

import utils.FirebaseUtil
import utils.mAuth
import utils.mDatabase

class LocationsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locations)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        val uid = mAuth.currentUser!!.uid
        val txtName: TextView = nav_view.getHeaderView(0).findViewById(R.id.username)
        val txtNumber: TextView = nav_view.getHeaderView(0).findViewById(R.id.phone_number)

        val phoneNumber = mAuth.currentUser!!.phoneNumber!!
        val num = "${phoneNumber.subSequence(0, 4)} ${phoneNumber.subSequence(4, phoneNumber.length)}"
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

    }

    override fun onResume() {
        super.onResume()
        nav_view.menu.getItem(2).isChecked = true
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


}
