package com.loqoursys.meko.ui


import android.content.Context
import android.support.v7.widget.AppCompatRatingBar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.loqoursys.meko.R
import com.loqoursys.meko.data.FoodItem
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.meko_item.view.*
import utils.loadURL
import utils.showToast
import utils.snackBar

/**
 * Created by root on 4/25/18 for LoqourSys
 */

class MainAdapter(val context: Context, private var foodItems: ArrayList<FoodItem>) : RecyclerView.Adapter<MainAdapter.MainViewHolder>() {

    var data: List<FoodItem> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.meko_item, parent, false)
        )
    }

    override fun getItemCount() = foodItems.size

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) = holder.bind(data[position])

    fun swapData(data: ArrayList<FoodItem>) {
        this.data = data
        notifyDataSetChanged()
    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            MekoImageView.LoadingRequestListener.Callback {
        private val foodName: TextView = itemView.food_name
        private val foodPhoto: MekoImageView = itemView.food_photo
        private val foodDescription: TextView = itemView.food_description
        private val cookName: TextView = itemView.cook_name
        private val cookPhoto: CircleImageView = itemView.cook_photo
        private val cookRating: AppCompatRatingBar = itemView.cook_rating
        private val cookView: LinearLayout = itemView.linear_cook

        fun bind(item: FoodItem) = with(itemView) {
            foodName.text = item.item_name
            foodDescription.text = item.description

            foodPhoto.loadURL(item.photo_url, this@MainViewHolder)

            if (item.cook != null) {
                cookView.visibility = View.VISIBLE

                cookRating.rating = item.cook!!.rating
                cookName.text = item.cook!!.full_name
                cookPhoto.loadURL(itemView.context, item.cook!!.photoUrl)
            } else {
                cookView.visibility = View.GONE
            }
            setOnClickListener {
                it.snackBar("Clicked")
            }
        }

        override fun onFailure(message: String) {
            showToast(itemView.context, message)
        }
    }
}