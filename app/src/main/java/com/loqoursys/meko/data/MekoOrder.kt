package com.loqoursys.meko.data

/**
 * Created by root on 4/11/18 for LoqourSys
 */
@Suppress("unused")
data class MekoOrder(var order_id: String = "", var order_date: Long = 0L, var orderItems: ArrayList<FoodItem>? = null,
                     var order_location: OrderLocation = OrderLocation(),
                     var order_amount: Float = 0F, var order_status: OrderStatus = OrderStatus.PROCESSING,
                     var payment_status: PaymentStatus = PaymentStatus.PENDING,
                     var review_status: ReviewStatus = ReviewStatus.PENDING,
                     var order_person: MekoUser = MekoUser()) {
    constructor() : this("")
}