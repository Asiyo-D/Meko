package com.loqoursys.meko

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.location.Location
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.loqoursys.meko.data.MekoOrder
import kotlinx.android.synthetic.main.activity_receipt.*
import kotlinx.android.synthetic.main.content_receipt.*
import utils.*
import utils.location.GetAddress
import java.util.*

class ReceiptActivity : AppCompatActivity() {
    lateinit var context: Context

    private var receipt: Bitmap? = null
    private lateinit var getAddress: GetAddress
    var iseReceipt = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)
        setSupportActionBar(toolbar)
        context = this
        fab.setOnClickListener { _ ->
            startActivity(Intent(this, RatingActivity::class.java))
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val closeButton = ContextCompat.getDrawable(this, R.drawable.ic_clear_black_24dp)!!
        closeButton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)

        supportActionBar?.setHomeAsUpIndicator(closeButton)

        val receiptId = intent.getStringExtra("orderId")
        iseReceipt = intent.getBooleanExtra("eReceipt", false)

        getAddress = GetAddress(this)
        generateReceipt(receiptId)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_order_items.layoutManager = layoutManager
        recycler_order_items.isNestedScrollingEnabled = false

        getOrder(receiptId)
    }

    private fun generateReceipt(orderId: String) {
        val eReceipt = "Order # $orderId"
        txt_order_id.text = eReceipt
        receipt = Receipts.generateReceipt(context, orderId)
        order_receipt.setImageBitmap(receipt)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        return when (itemId) {
            android.R.id.home -> {
                closeReceipt()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        closeReceipt()
    }

    private fun closeReceipt() {
        if (iseReceipt) {
            finish()
        } else {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

            context.startActivity(intent)
        }
    }

    private fun getOrder(oid: String) {
        val orderDBRef = mDatabase.getReference(FirebaseUtil.ORDERS_REF)
        orderDBRef.child(oid).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(snap: DataSnapshot?) {
                if (snap != null) {
                    val order = snap.getValue(MekoOrder::class.java)
                    if (order != null) {
                        initReceipt(order)
                    }
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun initReceipt(order: MekoOrder) {
        delivery_status.text = order.order_status.toString()

        val cal = Calendar.getInstance()
        cal.time = Date(order.order_date)
        val orderDate = DateTimeTemplate.format(cal, "%DD%, %MMM% %dd%")
        txt_date.text = orderDate

        val location = Location("")
        location.latitude = order.order_location.latitude
        location.longitude = order.order_location.longitutde

        val place = getAddress.fetchCurrentAddress(location)
        txt_place.text = place


        val total = "${order.order_amount}".toKshs()

        txt_total.text = "$total *"

        txt_payment_status.text = order.payment_status.toString()
        if (order.orderItems != null) {
            recycler_order_items.adapter = CartAdapter(this, order.orderItems!!)
        }
    }
}
