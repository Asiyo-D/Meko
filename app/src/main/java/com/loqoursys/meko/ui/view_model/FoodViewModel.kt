package com.loqoursys.meko.ui.view_model

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.loqoursys.meko.data.FoodItem
import utils.FirebaseUtil
import utils.mDatabase
import utils.mekoFoods

/**
 * Created by root on 5/14/18 for LoqourSys
 */
class FoodViewModel : ViewModel() {

    private val foodItems = MutableLiveData<ArrayList<FoodItem>>()

    fun getFood(isFood: Boolean): LiveData<ArrayList<FoodItem>> {
        if (isFood) {
            getDayFoods()
        } else {
            getDrinks()
        }
        return foodItems
    }

    private fun getDayFoods() {
        val foodDBRef = mDatabase.getReference(FirebaseUtil.FOOD_REF)
        val searchIndex = FirebaseUtil().getFoodSearchIndex()

        foodDBRef.orderByChild("searchIndex")
                .equalTo(searchIndex)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot?) {
                        mekoFoods.clear()
                        snapshot?.children?.forEach {
                            val foodItem = it.getValue(FoodItem::class.java)
                            if (foodItem != null) {
                                mekoFoods.add(foodItem)
                            }
                        }
                        foodItems.value = mekoFoods
                    }
                })
    }

    private fun getDrinks() {
        val foodDBRef = mDatabase.getReference(FirebaseUtil.FOOD_REF)
        val searchIndex = FirebaseUtil().getFoodSearchIndex()

        foodDBRef.orderByChild("searchIndex")
                .equalTo(searchIndex)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot?) {
                        mekoFoods.clear()
                        snapshot?.children?.forEach {
                            val foodItem = it.getValue(FoodItem::class.java)
                            if (foodItem != null) {
                                mekoFoods.add(foodItem)
                            }
                        }
                        foodItems.value = mekoFoods
                    }
                })
    }
}
