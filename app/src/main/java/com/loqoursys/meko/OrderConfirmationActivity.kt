package com.loqoursys.meko

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.loqoursys.meko.data.MekoOrder
import com.loqoursys.meko.data.MekoUser
import com.loqoursys.meko.data.OrderLocation
import kotlinx.android.synthetic.main.activity_order_confirmation.*
import kotlinx.android.synthetic.main.content_order_confirmation.*
import utils.*
import utils.network.CheckConnectivity
import utils.network.ConnectionStatus

class OrderConfirmationActivity : AppCompatActivity() {
    private lateinit var dialog: MaterialDialog
    private lateinit var adapter: CartAdapter

    var locationTitle = ""
    var locationDes = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_confirmation)
        setSupportActionBar(toolbar)

        btn_order.setOnClickListener { view ->
            checkConnection(view)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        locationTitle = intent.getStringExtra("location_title")
        locationDes = intent.getStringExtra("location_des")

        adapter = CartAdapter(this, mekoCart)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL

        recycler_confirmation.layoutManager = layoutManager
        recycler_confirmation.adapter = adapter

        dialog = MaterialDialog.Builder(this)
                .content("Placing order...")
                .contentColor(Color.BLACK)
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .autoDismiss(false)
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .build()
    }


    private fun checkConnection(view: View) {
        dialog.show()
        CheckConnectivity(completeListener = object : CheckConnectivity.ConnectivityCompleteListener {
            override fun onConnectivityCheckComplete(connectionStatus: ConnectionStatus): Boolean {
                if (connectionStatus == ConnectionStatus.CONNECTED) {
                    getProfileInfo()
                } else {
                    dialog.dismiss()
                    Snackbar.make(view, "Check your connection then try again",
                            Snackbar.LENGTH_SHORT).show()
                }
                return true
            }
        }).execute()
    }

    private fun getProfileInfo() {
        val userRef = mDatabase.getReference(FirebaseUtil.USERS_REF)

        val uid = mAuth.currentUser!!.uid
        userRef.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(snap: DataSnapshot?) {
                if (snap != null) {
                    val user = snap.getValue(MekoUser::class.java)
                    if (user != null) {
                        placeOrder(user)
                    }
                }
            }
        })

    }

    private fun placeOrder(user: MekoUser) {

        val orderDBRef = mDatabase.getReference(FirebaseUtil.ORDERS_REF)
        val receiptId = orderDBRef.push().key


        if (mekoCart.isEmpty()) {
            dialog.dismiss()
            Snackbar.make(btn_order, "Add at least 1 item to cart to order",
                    Snackbar.LENGTH_SHORT).show()
            close()
        }

        val subtotal = getOrderTotal(mekoCart)
        val total = subtotal + 10F percentOf subtotal
        if (selectedLocation != null) {
            val order = MekoOrder(receiptId, System.currentTimeMillis(), mekoCart,
                    OrderLocation(selectedLocation!!.latitude, selectedLocation!!.longitude,
                            locationTitle, locationDes),
                    total, order_person = user)

            orderDBRef.child(receiptId).setValue(order)

            finalizeOrder(receiptId)
        } else {
            dialog.dismiss()
            Snackbar.make(btn_order, "Select your preferred delivery location",
                    Snackbar.LENGTH_SHORT).show()
            finish()
        }

    }

    private fun finalizeOrder(receiptId: String) {
        mekoCart.clear()
        dialog.dismiss()

        val intent = Intent(this, ReceiptActivity::class.java)
        intent.putExtra("orderId", receiptId)
        startActivity(intent)
    }

    private fun close() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

        startActivity(intent)

    }

    private infix fun Float.percentOf(amount: Float): Float = (amount * this) / 100
}
