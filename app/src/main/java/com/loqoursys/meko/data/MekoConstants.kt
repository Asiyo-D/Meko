package com.loqoursys.meko.data

/**
 * Created by root on 4/11/18 for LoqourSys
 */
enum class OrderStatus {
    PROCESSING, PROCESSED, DELIVERED, CANCELLED
}

enum class PaymentStatus {
    PENDING, PAID
}

enum class ReviewStatus {
    PENDING, REVIEWED
}

enum class AreasServed {
    //    Active locations
    NYERI
//    Future locations
//    , NAIROBI, NAKURU
//    , KISUMU, ELDORET, MOMBASA
}