package com.loqoursys.meko.data

/**
 * Created by root on 4/11/18 for LoqourSys
 */
@Suppress("unused")
data class MekoReview(var reviewId: String = "", var rating: Double = 0.0, var date: Long = 0L,
                      var review_text: String = "", var cook: MekoCook = MekoCook(),
                      var review_item: FoodItem = FoodItem(), var user: MekoUser = MekoUser()) {
    constructor() : this("")
}