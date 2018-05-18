package com.loqoursys.meko.ui


import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.loqoursys.meko.R
import com.loqoursys.meko.data.FoodItem
import com.loqoursys.meko.listener.MainClickListener
import kotlinx.android.synthetic.main.item_drink.view.*
import utils.showToast
import utils.toKshs

/**
 * Created by root on 5/14/18 for LoqourSys
 */

class DrinksAdapter(val context: Context, private var foodItems: ArrayList<FoodItem>,
                    clickListener: MainClickListener)
    : RecyclerView.Adapter<DrinksAdapter.DrinksViewHolder>() {

    private val listener: MainClickListener = clickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrinksViewHolder {
        return DrinksViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_drink, parent, false)
        )
    }

    override fun getItemCount() = foodItems.size

    override fun onBindViewHolder(holder: DrinksViewHolder, position: Int) = holder
            .bind(foodItems[position], position)

    fun swapData(data: ArrayList<FoodItem>) {
        this.foodItems = data
        notifyDataSetChanged()
    }

    inner class DrinksViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            MekoImageView.LoadingRequestListener.Callback {
        private val drinkName: TextView = itemView.drink_title
        private val drinkPhoto: MekoImageView = itemView.drink_photo
        private val drinkDescription: TextView = itemView.drink_description
        private val drinkPrice: TextView = itemView.drink_price

        fun bind(item: FoodItem, pos: Int) = with(itemView) {
            drinkName.text = item.item_name
            drinkDescription.text = item.description
            val price = "${item.price}".toKshs()
            drinkPrice.text = price

            drinkPhoto.loadURL(item.photo_url, this@DrinksViewHolder)

            setOnClickListener { _ ->
                listener.onItemClick(pos)
            }
        }

        override fun onFailure(message: String) {
            showToast(itemView.context, message)
        }

        override fun onComplete() {

        }
    }
}