package com.loqoursys.meko

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.loqoursys.meko.data.MekoOrder
import com.loqoursys.meko.data.MekoUser
import com.loqoursys.meko.listener.ClickListener
import kotlinx.android.synthetic.main.activity_orders.*
import kotlinx.android.synthetic.main.app_bar_orders.*
import kotlinx.android.synthetic.main.content_orders.*
import utils.*

class OrdersActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var ordersAdapter: OrdersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        val uid = mAuth.currentUser!!.uid
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

        ordersAdapter = OrdersAdapter(this, mekoOrders)
        recycler_orders.layoutManager = LinearLayoutManager(this)
        recycler_orders.adapter = ordersAdapter

        val itemTouch = RecyclerTouchListener(this, object : ClickListener {
            override fun onClick(view: View, position: Int) {
                handleOrderClick(position)
            }
        })
        recycler_orders.addOnItemTouchListener(itemTouch)
        retrieveOrders()
    }

    override fun onResume() {
        super.onResume()
        nav_view.menu.getItem(1).isChecked = true
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
            R.id.nav_home -> {
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            R.id.nav_orders -> {

            }
            R.id.nav_locations -> {
                startActivity(Intent(this, LocationsActivity::class.java))
                finishAffinity()
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

    private fun retrieveOrders() {
        if (mekoOrders.isNotEmpty()) {
            mekoOrders.clear()
        }
        val orderDBREf = mDatabase.getReference(FirebaseUtil.ORDERS_REF)

        val uid = mAuth.currentUser!!.uid
        orderDBREf.orderByChild("order_person/user_id")
                .equalTo(uid)
                .addChildEventListener(object : ChildEventListener {
                    override fun onCancelled(p0: DatabaseError?) {
                    }

                    override fun onChildMoved(p0: DataSnapshot?, p1: String?) {

                    }

                    override fun onChildChanged(p0: DataSnapshot?, p1: String?) {

                    }

                    override fun onChildAdded(snapshot: DataSnapshot?, p1: String?) {
                        if (snapshot != null) {
                            val order = snapshot.getValue(MekoOrder::class.java)
                            if (order != null) {
                                mekoOrders.add(order)
                                ordersAdapter.notifyDataSetChanged()
                            }
                        }
                    }

                    override fun onChildRemoved(p0: DataSnapshot?) {

                    }
                })
    }

    private fun handleOrderClick(pos: Int) {
        val intent = Intent(this, ReceiptActivity::class.java)
                .putExtra("orderId", mekoOrders[pos].order_id)
                .putExtra("eReceipt", true)
        startActivity(intent)
    }
}
