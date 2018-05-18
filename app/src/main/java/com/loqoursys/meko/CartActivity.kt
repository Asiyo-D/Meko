package com.loqoursys.meko

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import com.loqoursys.meko.data.FoodItem
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.content_cart.*
import kotlinx.android.synthetic.main.content_empty_cart.*
import utils.*
import java.util.*

class CartActivity : AppCompatActivity() {

    private lateinit var adapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        setSupportActionBar(toolbar)

        btn_proceed.setOnClickListener { view ->
            if (mekoCart.isNotEmpty()) {
                startActivity(Intent(this, PaymentDeliveryActivity::class.java))
            } else {
                Snackbar.make(view, "Add at least 1 item to cart", Snackbar.LENGTH_LONG).show()
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val cal = Calendar.getInstance()
        val date = DateTimeTemplate.format(cal, "%DD%, %MMM% %dd%")
        txt_date.text = date

        adapter = CartAdapter(this, mekoCart)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL


        recycler_cart.layoutManager = layoutManager
        recycler_cart.adapter = adapter

        recycler_cart.isNestedScrollingEnabled = false

        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                saveAndDelete(viewHolder.adapterPosition)
            }

        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recycler_cart)

        scrollView.post {
            scrollView.smoothScrollTo(0, card_cart.top)
        }

        calculateTotals()
        btn_back.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        toggleCartView()
    }

    private fun calculateTotals() {
        toggleCartView()

        var subtotal = 0F
        mekoCart.forEach { subtotal += it.price }

        val fee = deliveryFee percentOf subtotal
        val total = subtotal + fee

        txt_subtotal.text = "$subtotal".toKshs()
        txt_delivery_fee.text = "$fee".toKshs()
        txt_total.text = "$total".toKshs()

        pop(this, txt_total)
    }

    private var tempItem: Pair<Int, FoodItem>? = null

    private fun saveAndDelete(pos: Int) {
        tempItem = Pair(pos, mekoCart[pos])

        Snackbar.make(parent_view, "${mekoCart[pos].item_name} removed from cart", Snackbar.LENGTH_LONG)
                .setAction("UNDO", { undoDelete() }).show()
        adapter.removeItem(pos)

        calculateTotals()
    }

    private fun undoDelete() {
        if (tempItem != null) {
            adapter.addItem(tempItem!!.first, tempItem!!.second)

            calculateTotals()
        }
    }

    private fun toggleCartView() {
        if (mekoCart.isEmpty()) {
            view_empty_cart.visibility = View.VISIBLE
            card_proceed.visibility = View.INVISIBLE
        } else {
            view_empty_cart.visibility = View.INVISIBLE
            card_proceed.visibility = View.VISIBLE
        }
    }

    private infix fun Float.percentOf(amount: Float): Float = (amount * this) / 100

}
