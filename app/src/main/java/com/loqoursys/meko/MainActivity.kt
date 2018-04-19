package com.loqoursys.meko

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.NavigationView
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import utils.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
//            saveRec()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        btn_qr.setOnClickListener {
            val text = txt_qr.text.toString()
            if (text.isEmpty()) {
                incr()
            } else {
                generateCode(text)
            }
        }

        val uid = mAuth.currentUser!!.uid
        val txtName: TextView = nav_view.getHeaderView(0).findViewById(R.id.username)
        val txtNumber: TextView = nav_view.getHeaderView(0).findViewById(R.id.phone_number)

        val phoneNumber = mAuth.currentUser!!.phoneNumber!!
        val num = "${phoneNumber.subSequence(0, 4)} ${phoneNumber.subSequence(4, phoneNumber.length)}"
        txtNumber.text = num

        val userDBRef = mDatabase.getReference(FirebaseUtil.USERS_REF)
        userDBRef.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {}
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

        showToast(this, "Fee $deliveryFee")
    }

    override fun onResume() {
        super.onResume()
        nav_view.menu.getItem(0).isChecked = true
        fab.count = mekoCart.size
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
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_orders -> {
                // Handle the camera action
                startActivity(Intent(this, OrdersActivity::class.java))
            }
            R.id.nav_payments -> {
                startActivity(Intent(this, PaymentsActivity::class.java))
            }
            R.id.nav_settings -> {

            }
            R.id.nav_help -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    var bitmap: Bitmap? = null
    private fun generateCode(text: String) {
        bitmap = Receipts.generateReceipt(this, text)
        img_code.setImageBitmap(bitmap)
    }

    private fun saveRec() {
        if (bitmap != null) {
            Receipts.saveReceipt(bitmap!!, "Sample")
        }
    }

    private fun incr() {
        fab.increase()
    }
}
