package com.loqoursys.meko.data

/**
 * Created by root on 4/11/18 for LoqourSys
 */
@Suppress("unused")
data class FoodItem(var item_id: String = "", var item_name: String = "", var description: String = "",
                    var ingredients: String = "", var servings: Int = 0, var photo_url: String = "",
                    var price: Float = 0F, var cook: MekoCook? = null, var searchIndex: String = "") {
    constructor() : this("")
}